/*
 * Copyright (C) 2024 Philip Helger
 * http://www.helger.com
 * philip[at]helger[dot]com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.helger.en16931.ubl2cii;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.datetime.PDTFactory;
import com.helger.commons.error.list.ErrorList;
import com.helger.commons.math.MathHelper;

import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.*;
import oasis.names.specification.ubl.schema.xsd.invoice_21.InvoiceType;
import un.unece.uncefact.data.standard.crossindustryinvoice._100.CrossIndustryInvoiceType;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._100.*;
import un.unece.uncefact.data.standard.unqualifieddatatype._100.AmountType;
import un.unece.uncefact.data.standard.unqualifieddatatype._100.BinaryObjectType;
import un.unece.uncefact.data.standard.unqualifieddatatype._100.CodeType;
import un.unece.uncefact.data.standard.unqualifieddatatype._100.IDType;
import un.unece.uncefact.data.standard.unqualifieddatatype._100.IndicatorType;
import un.unece.uncefact.data.standard.unqualifieddatatype._100.QuantityType;
import un.unece.uncefact.data.standard.unqualifieddatatype._100.TextType;

/**
 * UBL 2.1 to CII D16B converter.
 *
 * @author Vartika Rastogi
 * @author Philip Helger
 */
public final class UBL21ToCII16BConverter
{
  private static final String CII_DATE_FORMAT = "102";

  private UBL21ToCII16BConverter ()
  {}

  @Nonnull
  private static String _createFormattedDateValue (@Nonnull final LocalDate aLocalDate)
  {
    final SimpleDateFormat aFormatter = new SimpleDateFormat ("yyyyMMdd");
    final Date aDate = PDTFactory.createDate (aLocalDate);
    return aFormatter.format (aDate);
  }

  @Nonnull
  private static un.unece.uncefact.data.standard.unqualifieddatatype._100.DateTimeType _createDate (@Nonnull final LocalDate aLocalDate)
  {
    final un.unece.uncefact.data.standard.unqualifieddatatype._100.DateTimeType aDTT = new un.unece.uncefact.data.standard.unqualifieddatatype._100.DateTimeType ();
    {
      final un.unece.uncefact.data.standard.unqualifieddatatype._100.DateTimeType.DateTimeString aDTS = new un.unece.uncefact.data.standard.unqualifieddatatype._100.DateTimeType.DateTimeString ();
      aDTS.setFormat (CII_DATE_FORMAT);
      aDTS.setValue (_createFormattedDateValue (aLocalDate));
      aDTT.setDateTimeString (aDTS);
    }
    return aDTT;
  }

  @Nonnull
  private static TextType _convertTextType (@Nonnull final String sValue)
  {
    final TextType aTT = new TextType ();
    aTT.setValue (sValue);
    return aTT;
  }

  @Nonnull
  private static IDType _convertIDType (@Nonnull final com.helger.xsds.ccts.cct.schemamodule.IdentifierType aID)
  {
    final IDType aITG = new IDType ();
    aITG.setSchemeID (aID.getSchemeID ());
    aITG.setValue (aID.getValue ());
    return aITG;
  }

  @Nonnull
  private static AmountType _convertAmountType (final com.helger.xsds.ccts.cct.schemamodule.AmountType aUBLAmount)
  {
    final AmountType aATTPT = new AmountType ();
    if (false)
      aATTPT.setCurrencyID (aUBLAmount.getCurrencyID ());
    aATTPT.setValue (MathHelper.getWithoutTrailingZeroes (aUBLAmount.getValue ()));
    return aATTPT;
  }

  @Nonnull
  private static un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._100.NoteType _convertNote (final oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.NoteType aNote)
  {
    final un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._100.NoteType aNTSC = new un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._100.NoteType ();
    final TextType aTT = new TextType ();
    aTT.setValue (aNote.getValue ());
    aNTSC.addContent (aTT);
    return aNTSC;
  }

  @Nonnull
  private static List <SupplyChainTradeLineItemType> _convertInvoiceLines (final List <oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.InvoiceLineType> aLstIL)
  {
    final List <SupplyChainTradeLineItemType> aLstSCTLIT = new ArrayList <> ();
    for (final InvoiceLineType aILT : aLstIL)
    {
      final SupplyChainTradeLineItemType aISCTLI = new SupplyChainTradeLineItemType ();
      final DocumentLineDocumentType aDLDT = new DocumentLineDocumentType ();

      aDLDT.setLineID (aILT.getIDValue ());

      if (aILT.hasNoteEntries ())
      {
        aDLDT.addIncludedNote (_convertNote (aILT.getNote ().get (0)));
      }

      aISCTLI.setAssociatedDocumentLineDocument (aDLDT);

      // SpecifiedTradeProduct
      final TradeProductType aTPT = new TradeProductType ();
      final ItemType aIT = aILT.getItem ();
      if (aIT.getStandardItemIdentification () != null)
      {
        aTPT.setGlobalID (_convertIDType (aIT.getStandardItemIdentification ().getID ()));
      }

      if (aIT.getSellersItemIdentification () != null)
      {
        aTPT.setSellerAssignedID (aIT.getSellersItemIdentification ().getIDValue ());
      }

      aTPT.addName (_convertTextType (aIT.getNameValue ()));

      if (aIT.hasDescriptionEntries ())
      {
        aTPT.setDescription (aIT.getDescription ().get (0).getValue ());
      }

      // ApplicableProductCharacteristic
      final List <ProductCharacteristicType> aLstPCT = new ArrayList <> ();
      for (final ItemPropertyType aIPT : aILT.getItem ().getAdditionalItemProperty ())
      {
        final ProductCharacteristicType aPCT = new ProductCharacteristicType ();
        aPCT.addDescription (_convertTextType (aIPT.getNameValue ()));
        aPCT.addValue (_convertTextType (aIPT.getValueValue ()));
        aLstPCT.add (aPCT);
      }
      aTPT.setApplicableProductCharacteristic (aLstPCT);

      // DesignatedProductClassification
      for (final CommodityClassificationType aCCT : aILT.getItem ().getCommodityClassification ())
      {
        final ProductClassificationType aPCT = new ProductClassificationType ();
        final CodeType aCT = new CodeType ();
        aCT.setListID (aCCT.getItemClassificationCode ().getListID ());
        aCT.setValue (aCCT.getItemClassificationCode ().getValue ());
        aPCT.setClassCode (aCT);
        aTPT.addDesignatedProductClassification (aPCT);
      }
      aISCTLI.setSpecifiedTradeProduct (aTPT);

      // SpecifiedLineTradeAgreement
      final LineTradeAgreementType aLTAT = new LineTradeAgreementType ();
      // BuyerOrderReferencedDocument
      final ReferencedDocumentType aRDT = new ReferencedDocumentType ();
      if (aILT.hasOrderLineReferenceEntries ())
      {
        aRDT.setLineID (aILT.getOrderLineReference ().get (0).getLineIDValue ());
      }

      // NetPriceProductTradePrice
      final TradePriceType aLTPT = new TradePriceType ();
      if (aILT.getPrice () != null && aILT.getPrice ().getPriceAmount () != null)
      {
        aLTPT.addChargeAmount (_convertAmountType (aILT.getPrice ().getPriceAmount ()));
      }

      aLTAT.setBuyerOrderReferencedDocument (aRDT);
      aLTAT.setNetPriceProductTradePrice (aLTPT);
      aISCTLI.setSpecifiedLineTradeAgreement (aLTAT);

      // SpecifiedLineTradeDelivery
      final LineTradeDeliveryType aLTDT = new LineTradeDeliveryType ();
      final QuantityType aQT = new QuantityType ();
      aQT.setUnitCode (aILT.getInvoicedQuantity ().getUnitCode ());
      aQT.setValue (aILT.getInvoicedQuantity ().getValue ());
      aLTDT.setBilledQuantity (aQT);
      aISCTLI.setSpecifiedLineTradeDelivery (aLTDT);

      // SpecifiedLineTradeSettlement
      final LineTradeSettlementType aLTST = new LineTradeSettlementType ();
      {
        for (final TaxCategoryType aTCT : aILT.getItem ().getClassifiedTaxCategory ())
        {
          final TradeTaxType aTTT = new TradeTaxType ();
          aTTT.setTypeCode (aTCT.getTaxScheme ().getIDValue ());
          aTTT.setCategoryCode (aTCT.getIDValue ());
          if (aTCT.getPercent () != null)
            aTTT.setRateApplicablePercent (aTCT.getPercentValue ());
          aLTST.addApplicableTradeTax (aTTT);
        }
      }
      final TradeSettlementLineMonetarySummationType aTSLMST = new TradeSettlementLineMonetarySummationType ();
      if (aILT.getLineExtensionAmount () != null)
      {
        aTSLMST.addLineTotalAmount (_convertAmountType (aILT.getLineExtensionAmount ()));
      }

      if (aILT.getAccountingCostValue () != null)
      {
        final TradeAccountingAccountType aTAATL = new TradeAccountingAccountType ();
        aTAATL.setID (aILT.getAccountingCostValue ());
        aLTST.addReceivableSpecifiedTradeAccountingAccount (aTAATL);
      }

      aLTST.setSpecifiedTradeSettlementLineMonetarySummation (aTSLMST);
      aISCTLI.setSpecifiedLineTradeSettlement (aLTST);

      aLstSCTLIT.add (aISCTLI);
    }
    return aLstSCTLIT;
  }

  @Nullable
  private static TradeAddressType _convertAddress (@Nullable final AddressType aAddress)
  {
    if (aAddress == null)
      return null;

    final TradeAddressType ret = new TradeAddressType ();

    if (aAddress.getStreetName () != null)
      ret.setLineOne (aAddress.getStreetName ().getValue ());

    if (aAddress.getAdditionalStreetName () != null)
      ret.setLineTwo (aAddress.getAdditionalStreetName ().getValue ());

    if (aAddress.hasAddressLineEntries ())
      ret.setLineThree (aAddress.getAddressLineAtIndex (0).getLineValue ());

    if (aAddress.getCityName () != null)
      ret.setCityName (aAddress.getCityName ().getValue ());

    if (aAddress.getPostalZone () != null)
      ret.setPostcodeCode (aAddress.getPostalZone ().getValue ());

    if (aAddress.getCountrySubentity () != null)
      ret.addCountrySubDivisionName (_convertTextType (aAddress.getCountrySubentity ().getValue ()));

    if (aAddress.getCountry () != null && aAddress.getCountry ().getIdentificationCode () != null)
      ret.setCountryID (aAddress.getCountry ().getIdentificationCode ().getValue ());

    return ret;
  }

  @Nullable
  private static TradePartyType _convertParty (@Nullable final PartyType aParty)
  {
    if (aParty == null)
      return null;

    final TradePartyType aTPT = new TradePartyType ();
    for (final var aPartyID : aParty.getPartyIdentification ())
      if (aPartyID.getID () != null)
        aTPT.addID (_convertIDType (aPartyID.getID ()));

    if (aParty.hasPartyNameEntries ())
      aTPT.setName (aParty.getPartyName ().get (0).getNameValue ());

    if (aParty.hasPartyLegalEntityEntries ())
    {
      final PartyLegalEntityType aLE = aParty.getPartyLegalEntity ().get (0);

      final LegalOrganizationType aLOT = new LegalOrganizationType ();
      aLOT.setTradingBusinessName (aLE.getRegistrationNameValue ());
      if (aLE.getCompanyID () != null)
        aLOT.setID (_convertIDType (aLE.getCompanyID ()));
      aLOT.setPostalTradeAddress (_convertAddress (aLE.getRegistrationAddress ()));

      aTPT.setSpecifiedLegalOrganization (aLOT);
    }

    aTPT.setPostalTradeAddress (_convertAddress (aParty.getPostalAddress ()));

    if (aParty.getEndpointID () != null)
    {
      final UniversalCommunicationType aUCT = new UniversalCommunicationType ();
      aUCT.setURIID (_convertIDType (aParty.getEndpointID ()));
      aTPT.addURIUniversalCommunication (aUCT);
    }

    if (aParty.hasPartyTaxSchemeEntries ())
    {
      final TaxRegistrationType aTRT = new TaxRegistrationType ();
      aTRT.setID (_convertIDType (aParty.getPartyTaxScheme ().get (0).getTaxScheme ().getID ()));
      aTPT.addSpecifiedTaxRegistration (aTRT);
    }
    return aTPT;
  }

  @Nullable
  private static HeaderTradeDeliveryType _convertApplicableHeaderTradeDelivery (@Nullable final DeliveryType aDelivery)
  {
    final HeaderTradeDeliveryType ret = new HeaderTradeDeliveryType ();

    final LocationType aDL = aDelivery.getDeliveryLocation ();
    if (aDL != null)
    {
      final TradePartyType aTPTHT = new TradePartyType ();
      if (aDL.getID () != null)
        aTPTHT.addID (_convertIDType (aDL.getID ()));

      aTPTHT.setPostalTradeAddress (_convertAddress (aDL.getAddress ()));
      ret.setShipToTradeParty (aTPTHT);
    }

    if (aDelivery.getActualDeliveryDate () != null)
    {
      final SupplyChainEventType aSCET = new SupplyChainEventType ();
      aSCET.setOccurrenceDateTime (_createDate (aDelivery.getActualDeliveryDate ().getValueLocal ()));
      ret.setActualDeliverySupplyChainEvent (aSCET);
    }
    return ret;
  }

  @Nonnull
  private static HeaderTradeSettlementType _convertApplicableHeaderTradeSettlement (@Nonnull final InvoiceType aUBLInvoice)
  {
    final HeaderTradeSettlementType aHTST = new HeaderTradeSettlementType ();

    final PaymentMeansType aPM = aUBLInvoice.hasPaymentMeansEntries () ? aUBLInvoice.getPaymentMeansAtIndex (0) : null;

    if (aPM != null && aPM.hasPaymentIDEntries ())
      aHTST.addPaymentReference (_convertTextType (aPM.getPaymentID ().get (0).getValue ()));

    aHTST.setInvoiceCurrencyCode (aUBLInvoice.getDocumentCurrencyCode ().getValue ());

    if (aUBLInvoice.getPayeeParty () != null)
      aHTST.setPayeeTradeParty (_convertParty (aUBLInvoice.getPayeeParty ()));

    if (aPM != null)
    {
      final TradeSettlementPaymentMeansType aTSPMT = new TradeSettlementPaymentMeansType ();
      aTSPMT.setTypeCode (aPM.getPaymentMeansCodeValue ());

      final CreditorFinancialAccountType aCFAT = new CreditorFinancialAccountType ();
      if (aPM.getPayeeFinancialAccount () != null)
        aCFAT.setIBANID (aPM.getPayeeFinancialAccount ().getIDValue ());

      aTSPMT.setPayeePartyCreditorFinancialAccount (aCFAT);
      aHTST.addSpecifiedTradeSettlementPaymentMeans (aTSPMT);
    }

    aHTST.setApplicableTradeTax (_convertApplicableTradeTax (aUBLInvoice));

    if (aUBLInvoice.hasInvoicePeriodEntries ())
    {
      final PeriodType aIP = aUBLInvoice.getInvoicePeriodAtIndex (0);

      final SpecifiedPeriodType aSPT = new SpecifiedPeriodType ();
      if (aIP.getStartDate () != null)
        aSPT.setStartDateTime (_createDate (aIP.getStartDate ().getValueLocal ()));

      if (aIP.getEndDate () != null)
        aSPT.setEndDateTime (_createDate (aIP.getEndDate ().getValueLocal ()));

      aHTST.setBillingSpecifiedPeriod (aSPT);
    }

    aHTST.setSpecifiedTradeAllowanceCharge (_convertSpecifiedTradeAllowanceCharge (aUBLInvoice));
    aHTST.setSpecifiedTradePaymentTerms (_convertSpecifiedTradePaymentTerms (aUBLInvoice));
    aHTST.setSpecifiedTradeSettlementHeaderMonetarySummation (_convertSpecifiedTradeSettlementHeaderMonetarySummation (aUBLInvoice));

    if (aUBLInvoice.getAccountingCost () != null)
    {
      final TradeAccountingAccountType aTAAT = new TradeAccountingAccountType ();
      aTAAT.setID (aUBLInvoice.getAccountingCost ().getValue ());
      aHTST.addReceivableSpecifiedTradeAccountingAccount (aTAAT);
    }

    return aHTST;
  }

  @Nonnull
  private static TradeSettlementHeaderMonetarySummationType _convertSpecifiedTradeSettlementHeaderMonetarySummation (@Nonnull final InvoiceType aUBLInvoice)
  {
    final TradeSettlementHeaderMonetarySummationType aTSHMST = new TradeSettlementHeaderMonetarySummationType ();
    final MonetaryTotalType aUBLLMT = aUBLInvoice.getLegalMonetaryTotal ();
    if (aUBLLMT != null)
    {
      if (aUBLLMT.getLineExtensionAmount () != null)
        aTSHMST.addLineTotalAmount (_convertAmountType (aUBLLMT.getLineExtensionAmount ()));

      if (aUBLLMT.getChargeTotalAmount () != null)
        aTSHMST.addChargeTotalAmount (_convertAmountType (aUBLLMT.getChargeTotalAmount ()));

      if (aUBLLMT.getAllowanceTotalAmount () != null)
        aTSHMST.addAllowanceTotalAmount (_convertAmountType (aUBLLMT.getAllowanceTotalAmount ()));

      if (aUBLLMT.getTaxExclusiveAmount () != null)
        aTSHMST.addTaxBasisTotalAmount (_convertAmountType (aUBLLMT.getTaxExclusiveAmount ()));
    }

    if (!aUBLInvoice.getTaxTotal ().isEmpty () && aUBLInvoice.getTaxTotal ().get (0).getTaxAmount () != null)
      aTSHMST.addTaxTotalAmount (_convertAmountType (aUBLInvoice.getTaxTotal ().get (0).getTaxAmount ()));

    if (aUBLLMT != null)
    {
      if (aUBLLMT.getPayableRoundingAmount () != null)
        aTSHMST.addRoundingAmount (_convertAmountType (aUBLLMT.getPayableRoundingAmount ()));

      if (aUBLLMT.getTaxInclusiveAmount () != null)
        aTSHMST.addGrandTotalAmount (_convertAmountType (aUBLLMT.getTaxInclusiveAmount ()));

      if (aUBLLMT.getPrepaidAmount () != null)
        aTSHMST.addTotalPrepaidAmount (_convertAmountType (aUBLLMT.getPrepaidAmount ()));

      if (aUBLLMT.getPayableAmount () != null)
        aTSHMST.addDuePayableAmount (_convertAmountType (aUBLLMT.getPayableAmount ()));
    }

    return aTSHMST;
  }

  @Nonnull
  private static List <TradeTaxType> _convertApplicableTradeTax (@Nonnull final InvoiceType aUBLInvoice)
  {
    final List <TradeTaxType> ret = new ArrayList <> ();
    for (final TaxTotalType aTTT : aUBLInvoice.getTaxTotal ())
      if (aTTT.hasTaxSubtotalEntries ())
      {
        final TaxSubtotalType aFirst = aTTT.getTaxSubtotal ().get (0);

        final TradeTaxType aTTTST = new TradeTaxType ();
        if (aFirst.getTaxAmount () != null)
          aTTTST.addCalculatedAmount (_convertAmountType (aFirst.getTaxAmount ()));

        if (aFirst.getTaxCategory () != null)
          aTTTST.setTypeCode (aFirst.getTaxCategory ().getIDValue ());

        if (aFirst.getTaxableAmount () != null)
          aTTTST.addBasisAmount (_convertAmountType (aFirst.getTaxableAmount ()));

        final TaxCategoryType aTC = aFirst.getTaxCategory ();
        if (aTC != null)
        {
          if (aTC.hasTaxExemptionReasonEntries ())
            aTTTST.setExemptionReason (aTC.getTaxExemptionReason ().get (0).getValue ());

          if (aTC.getTaxExemptionReasonCode () != null)
            aTTTST.setExemptionReasonCode (aTC.getTaxExemptionReasonCode ().getValue ());
        }
        ret.add (aTTTST);
      }
    return ret;
  }

  @Nonnull
  private static List <TradePaymentTermsType> _convertSpecifiedTradePaymentTerms (@Nonnull final InvoiceType aUBLInvoice)
  {
    final List <TradePaymentTermsType> ret = new ArrayList <> ();
    for (final PaymentTermsType aPTT : aUBLInvoice.getPaymentTerms ())
    {
      final TradePaymentTermsType aTPTT = new TradePaymentTermsType ();

      for (final var aNote : aPTT.getNote ())
        aTPTT.addDescription (_convertTextType (aNote.getValue ()));

      if (aUBLInvoice.hasPaymentMeansEntries ())
      {
        final PaymentMeansType aPM = aUBLInvoice.getPaymentMeans ().get (0);
        if (aPM.getPaymentDueDate () != null)
          aTPTT.setDueDateDateTime (_createDate (aPM.getPaymentDueDate ().getValueLocal ()));
      }
      ret.add (aTPTT);
    }
    return ret;
  }

  @Nonnull
  private static List <TradeAllowanceChargeType> _convertSpecifiedTradeAllowanceCharge (@Nonnull final InvoiceType aUBLInvoice)
  {
    final List <TradeAllowanceChargeType> ret = new ArrayList <> ();
    for (final AllowanceChargeType aACT : aUBLInvoice.getAllowanceCharge ())
    {
      final TradeAllowanceChargeType aTDCT = new TradeAllowanceChargeType ();

      final IndicatorType aITDC = new IndicatorType ();
      aITDC.setIndicator (Boolean.valueOf (aACT.getChargeIndicator ().isValue ()));
      aTDCT.setChargeIndicator (aITDC);

      final AmountType aAT = new AmountType ();
      aAT.setValue (aACT.getAmount ().getValue ());
      aTDCT.addActualAmount (aAT);

      if (aACT.hasAllowanceChargeReasonEntries ())
        aTDCT.setReason (aACT.getAllowanceChargeReason ().get (0).getValue ());

      ret.add (aTDCT);
    }
    return ret;
  }

  @Nonnull
  private static List <ReferencedDocumentType> _convertAdditionalReferencedDocument (@Nonnull final InvoiceType aUBLInvoice)
  {
    final List <ReferencedDocumentType> ret = new ArrayList <> ();
    for (final DocumentReferenceType aDRT : aUBLInvoice.getAdditionalDocumentReference ())
    {
      final AttachmentType aAttachment = aDRT.getAttachment ();

      final ReferencedDocumentType aURDT = new ReferencedDocumentType ();
      aURDT.setIssuerAssignedID (aDRT.getID ().getValue ());

      if (aAttachment != null)
      {
        // External Reference and Embedded Document Binary Object should be
        // mutually exclusive
        if (aAttachment.getExternalReference () != null && aAttachment.getExternalReference ().getURI () != null)
        {
          aURDT.setURIID (aAttachment.getExternalReference ().getURI ().getValue ());
        }
        if (aAttachment.getEmbeddedDocumentBinaryObject () != null)
        {
          final BinaryObjectType aBOT = new BinaryObjectType ();
          aBOT.setMimeCode (aAttachment.getEmbeddedDocumentBinaryObject ().getMimeCode ());
          aBOT.setValue (aAttachment.getEmbeddedDocumentBinaryObject ().getValue ());
          aURDT.addAttachmentBinaryObject (aBOT);
        }
      }
      ret.add (aURDT);
    }
    return ret;
  }

  @Nullable
  public static CrossIndustryInvoiceType convertToCrossIndustryInvoice (@Nonnull final InvoiceType aUBLInvoice,
                                                                        @Nonnull final ErrorList aErrorList)
  {
    ValueEnforcer.notNull (aUBLInvoice, "UBLInvoice");
    ValueEnforcer.notNull (aErrorList, "ErrorList");

    final CrossIndustryInvoiceType aCIIInvoice = new CrossIndustryInvoiceType ();

    {
      final ExchangedDocumentContextType aEDCT = new ExchangedDocumentContextType ();
      if (aUBLInvoice.getCustomizationID () != null)
      {
        final DocumentContextParameterType aDCP = new DocumentContextParameterType ();
        aDCP.setID (aUBLInvoice.getCustomizationIDValue ());
        aEDCT.addGuidelineSpecifiedDocumentContextParameter (aDCP);
      }
      aCIIInvoice.setExchangedDocumentContext (aEDCT);
    }

    {
      final ExchangedDocumentType aEDT = new ExchangedDocumentType ();
      aEDT.setID (aUBLInvoice.getIDValue ());
      aEDT.setTypeCode (aUBLInvoice.getInvoiceTypeCodeValue ());

      // IssueDate
      if (aUBLInvoice.getIssueDate () != null)
        aEDT.setIssueDateTime (_createDate (aUBLInvoice.getIssueDate ().getValueLocal ()));

      // Add add IncludedNote
      for (final var aNote : aUBLInvoice.getNote ())
        aEDT.addIncludedNote (_convertNote (aNote));

      aCIIInvoice.setExchangedDocument (aEDT);
    }

    {
      final SupplyChainTradeTransactionType aSCTT = new SupplyChainTradeTransactionType ();

      // IncludedSupplyChainTradeLineItem
      aSCTT.setIncludedSupplyChainTradeLineItem (_convertInvoiceLines (aUBLInvoice.getInvoiceLine ()));

      // ApplicableHeaderTradeAgreement
      {
        final HeaderTradeAgreementType aHTAT = new HeaderTradeAgreementType ();

        // SellerTradeParty
        final SupplierPartyType aSupplierParty = aUBLInvoice.getAccountingSupplierParty ();
        if (aSupplierParty != null)
          aHTAT.setSellerTradeParty (_convertParty (aSupplierParty.getParty ()));

        // BuyerTradeParty
        final CustomerPartyType aCustomerParty = aUBLInvoice.getAccountingCustomerParty ();
        if (aCustomerParty != null)
          aHTAT.setBuyerTradeParty (_convertParty (aCustomerParty.getParty ()));

        // BuyerOrderReferencedDocument
        if (aUBLInvoice.getOrderReference () != null && aUBLInvoice.getOrderReference ().getID () != null)
        {
          final ReferencedDocumentType aRDT = new ReferencedDocumentType ();
          aRDT.setIssuerAssignedID (aUBLInvoice.getOrderReference ().getID ().getValue ());
          aHTAT.setBuyerOrderReferencedDocument (aRDT);
        }

        // ContractReferencedDocument
        if (aUBLInvoice.hasContractDocumentReferenceEntries ())
        {
          final ReferencedDocumentType aCRDT = new ReferencedDocumentType ();
          aCRDT.setIssuerAssignedID (aUBLInvoice.getContractDocumentReferenceAtIndex (0).getID ().getValue ());
          aHTAT.setContractReferencedDocument (aCRDT);
        }

        // AdditionalReferencedDocument
        aHTAT.setAdditionalReferencedDocument (_convertAdditionalReferencedDocument (aUBLInvoice));
        aSCTT.setApplicableHeaderTradeAgreement (aHTAT);
      }

      // ApplicableHeaderTradeDelivery
      if (aUBLInvoice.hasDeliveryEntries ())
        aSCTT.setApplicableHeaderTradeDelivery (_convertApplicableHeaderTradeDelivery (aUBLInvoice.getDeliveryAtIndex (0)));

      // ApplicableHeaderTradeSettlement
      aSCTT.setApplicableHeaderTradeSettlement (_convertApplicableHeaderTradeSettlement (aUBLInvoice));

      aCIIInvoice.setSupplyChainTradeTransaction (aSCTT);
    }

    return aCIIInvoice;
  }
}
