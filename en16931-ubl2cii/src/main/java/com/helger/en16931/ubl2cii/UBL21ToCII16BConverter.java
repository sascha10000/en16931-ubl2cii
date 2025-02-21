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
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.datetime.PDTFactory;
import com.helger.commons.error.list.ErrorList;
import com.helger.commons.math.MathHelper;
import com.helger.commons.string.StringHelper;

import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.*;
import oasis.names.specification.ubl.schema.xsd.invoice_21.InvoiceType;
import un.unece.uncefact.data.standard.crossindustryinvoice._100.CrossIndustryInvoiceType;
import un.unece.uncefact.data.standard.qualifieddatatype._100.FormattedDateTimeType;
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

  protected static <T> boolean ifNotNull (@Nonnull final Consumer <? super T> aConsumer, @Nullable final T aObj)
  {
    if (aObj == null)
      return false;
    aConsumer.accept (aObj);
    return true;
  }

  protected static boolean ifNotEmpty (@Nonnull final Consumer <? super String> aConsumer, @Nullable final String s)
  {
    if (StringHelper.hasNoText (s))
      return false;
    aConsumer.accept (s);
    return true;
  }

  protected static boolean isOriginatorDocumentReferenceTypeCode (@Nullable final String s)
  {
    // BT-17
    return "50".equals (s);
  }

  protected static boolean isValidDocumentReferenceTypeCode (@Nullable final String s)
  {
    // BT-17 or BT-18
    // Value 916 from BT-122 should not lead to a DocumentTypeCode
    return isOriginatorDocumentReferenceTypeCode (s) || "130".equals (s);
  }

  @Nullable
  private static String _getAsVAIfNecessary (@Nullable final String s)
  {
    if ("VAT".equals (s))
      return "VA";
    return s;
  }

  @Nonnull
  private static String _createFormattedDateValue (@Nonnull final LocalDate aLocalDate)
  {
    final SimpleDateFormat aFormatter = new SimpleDateFormat ("yyyyMMdd");
    final Date aDate = PDTFactory.createDate (aLocalDate);
    return aFormatter.format (aDate);
  }

  @Nonnull
  private static un.unece.uncefact.data.standard.unqualifieddatatype._100.DateTimeType.DateTimeString _createDateTimeString (@Nonnull final LocalDate aLocalDate)
  {
    final un.unece.uncefact.data.standard.unqualifieddatatype._100.DateTimeType.DateTimeString aret = new un.unece.uncefact.data.standard.unqualifieddatatype._100.DateTimeType.DateTimeString ();
    aret.setFormat (CII_DATE_FORMAT);
    aret.setValue (_createFormattedDateValue (aLocalDate));
    return aret;
  }

  @Nonnull
  private static un.unece.uncefact.data.standard.unqualifieddatatype._100.DateTimeType _createDate (@Nonnull final LocalDate aLocalDate)
  {
    final un.unece.uncefact.data.standard.unqualifieddatatype._100.DateTimeType ret = new un.unece.uncefact.data.standard.unqualifieddatatype._100.DateTimeType ();
    ret.setDateTimeString (_createDateTimeString (aLocalDate));
    return ret;
  }

  @Nonnull
  private static TextType _convertTextType (@Nonnull final String sValue)
  {
    final TextType ret = new TextType ();
    ret.setValue (sValue);
    return ret;
  }

  @Nullable
  private static IDType _convertIDType (@Nullable final com.helger.xsds.ccts.cct.schemamodule.IdentifierType aID)
  {
    if (aID == null)
      return null;

    final IDType ret = new IDType ();
    ret.setSchemeID (aID.getSchemeID ());
    ret.setValue (aID.getValue ());
    return ret;
  }

  @Nullable
  private static AmountType _convertAmountType (@Nullable final com.helger.xsds.ccts.cct.schemamodule.AmountType aUBLAmount)
  {
    return _convertAmountType (aUBLAmount, false);
  }

  @Nullable
  private static AmountType _convertAmountType (@Nullable final com.helger.xsds.ccts.cct.schemamodule.AmountType aUBLAmount,
                                                final boolean bWithCurrency)
  {
    if (aUBLAmount == null)
      return null;

    final AmountType ret = new AmountType ();
    if (bWithCurrency)
      ret.setCurrencyID (aUBLAmount.getCurrencyID ());
    ret.setValue (MathHelper.getWithoutTrailingZeroes (aUBLAmount.getValue ()));
    return ret;
  }

  @Nonnull
  private static un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._100.NoteType _convertNote (final oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.NoteType aNote)
  {
    final un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._100.NoteType ret = new un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._100.NoteType ();
    final TextType aTT = new TextType ();
    aTT.setValue (aNote.getValue ());
    ret.addContent (aTT);
    return ret;
  }

  @Nonnull
  private static List <SupplyChainTradeLineItemType> _convertInvoiceLines (final List <oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.InvoiceLineType> aLstIL)
  {
    final List <SupplyChainTradeLineItemType> ret = new ArrayList <> ();
    for (final InvoiceLineType aILT : aLstIL)
    {
      final SupplyChainTradeLineItemType aISCTLI = new SupplyChainTradeLineItemType ();
      final DocumentLineDocumentType aDLDT = new DocumentLineDocumentType ();

      aDLDT.setLineID (aILT.getIDValue ());

      for (final var aNote : aILT.getNote ())
        aDLDT.addIncludedNote (_convertNote (aNote));

      aISCTLI.setAssociatedDocumentLineDocument (aDLDT);

      // SpecifiedTradeProduct
      final TradeProductType aTPT = new TradeProductType ();
      final ItemType aIT = aILT.getItem ();
      if (aIT.getStandardItemIdentification () != null)
      {
        aTPT.setGlobalID (_convertIDType (aIT.getStandardItemIdentification ().getID ()));
      }

      if (aIT.getSellersItemIdentification () != null)
        aTPT.setSellerAssignedID (aIT.getSellersItemIdentification ().getIDValue ());

      aTPT.addName (_convertTextType (aIT.getNameValue ()));

      if (aIT.hasDescriptionEntries ())
      {
        aTPT.setDescription (aIT.getDescriptionAtIndex (0).getValue ());
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
        for (final TaxCategoryType aTaxCategory : aILT.getItem ().getClassifiedTaxCategory ())
        {
          final TaxSchemeType aTaxScheme = aTaxCategory.getTaxScheme ();

          final TradeTaxType aTradeTax = new TradeTaxType ();
          if (aTaxScheme != null)
            ifNotEmpty (aTradeTax::setTypeCode, aTaxCategory.getTaxScheme ().getIDValue ());
          ifNotEmpty (aTradeTax::setCategoryCode, aTaxCategory.getIDValue ());
          ifNotNull (aTradeTax::setRateApplicablePercent, aTaxCategory.getPercentValue ());
          aLTST.addApplicableTradeTax (aTradeTax);
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

      ret.add (aISCTLI);
    }
    return ret;
  }

  @Nullable
  private static TradeAddressType _convertAddress (@Nullable final AddressType aAddress)
  {
    if (aAddress == null)
      return null;

    final TradeAddressType ret = new TradeAddressType ();

    ifNotEmpty (ret::setLineOne, aAddress.getStreetNameValue ());
    ifNotEmpty (ret::setLineTwo, aAddress.getAdditionalStreetNameValue ());
    if (aAddress.hasAddressLineEntries ())
      ifNotEmpty (ret::setLineThree, aAddress.getAddressLineAtIndex (0).getLineValue ());
    ifNotEmpty (ret::setCityName, aAddress.getCityNameValue ());
    ifNotEmpty (ret::setPostcodeCode, aAddress.getPostalZoneValue ());
    if (aAddress.getCountrySubentity () != null)
      ret.addCountrySubDivisionName (_convertTextType (aAddress.getCountrySubentity ().getValue ()));
    if (aAddress.getCountry () != null)
      ifNotEmpty (ret::setCountryID, aAddress.getCountry ().getIdentificationCodeValue ());

    return ret;
  }

  @Nullable
  private static TradePartyType _convertParty (@Nullable final PartyType aParty)
  {
    if (aParty == null)
      return null;

    final TradePartyType aTPT = new TradePartyType ();
    for (final var aPartyID : aParty.getPartyIdentification ())
      ifNotNull (aTPT::addID, _convertIDType (aPartyID.getID ()));

    if (aParty.hasPartyNameEntries ())
      ifNotEmpty (aTPT::setName, aParty.getPartyNameAtIndex (0).getNameValue ());

    if (aParty.hasPartyLegalEntityEntries ())
    {
      final PartyLegalEntityType aLE = aParty.getPartyLegalEntity ().get (0);

      final LegalOrganizationType aLOT = new LegalOrganizationType ();
      ifNotEmpty (aLOT::setTradingBusinessName, aLE.getRegistrationNameValue ());
      ifNotNull (aLOT::setID, _convertIDType (aLE.getCompanyID ()));
      ifNotNull (aLOT::setPostalTradeAddress, _convertAddress (aLE.getRegistrationAddress ()));

      if (StringHelper.hasNoText (aTPT.getNameValue ()))
      {
        // Fill mandatory field
        ifNotEmpty (aTPT::setName, aLE.getRegistrationNameValue ());
      }

      aTPT.setSpecifiedLegalOrganization (aLOT);
    }

    ifNotNull (aTPT::setPostalTradeAddress, _convertAddress (aParty.getPostalAddress ()));

    if (aParty.getEndpointID () != null)
    {
      final UniversalCommunicationType aUCT = new UniversalCommunicationType ();
      ifNotNull (aUCT::setURIID, _convertIDType (aParty.getEndpointID ()));
      aTPT.addURIUniversalCommunication (aUCT);
    }

    if (aParty.hasPartyTaxSchemeEntries ())
    {
      final PartyTaxSchemeType aPTS = aParty.getPartyTaxSchemeAtIndex (0);
      if (aPTS.getCompanyIDValue () != null)
      {
        final TaxRegistrationType aTaxReg = new TaxRegistrationType ();
        final IDType aID = _convertIDType (aPTS.getCompanyID ());
        if (aPTS.getTaxScheme () != null)
        {
          // MUST use "VA" scheme
          ifNotEmpty (aID::setSchemeID, _getAsVAIfNecessary (aPTS.getTaxScheme ().getIDValue ()));
        }
        aTaxReg.setID (aID);
        aTPT.addSpecifiedTaxRegistration (aTaxReg);
      }
    }
    return aTPT;
  }

  @Nullable
  private static HeaderTradeDeliveryType _convertApplicableHeaderTradeDelivery (@Nullable final DeliveryType aDelivery)
  {
    // Object is mandatory
    final HeaderTradeDeliveryType ret = new HeaderTradeDeliveryType ();

    if (aDelivery != null)
    {
      final LocationType aDL = aDelivery.getDeliveryLocation ();
      if (aDL != null)
      {
        final TradePartyType aTPTHT = new TradePartyType ();
        ifNotNull (aTPTHT::addID, _convertIDType (aDL.getID ()));

        aTPTHT.setPostalTradeAddress (_convertAddress (aDL.getAddress ()));
        ret.setShipToTradeParty (aTPTHT);
      }

      if (aDelivery.getActualDeliveryDate () != null)
      {
        final SupplyChainEventType aSCET = new SupplyChainEventType ();
        aSCET.setOccurrenceDateTime (_createDate (aDelivery.getActualDeliveryDate ().getValueLocal ()));
        ret.setActualDeliverySupplyChainEvent (aSCET);
      }
    }
    return ret;
  }

  @Nonnull
  private static HeaderTradeSettlementType _convertApplicableHeaderTradeSettlement (@Nonnull final InvoiceType aUBLInvoice)
  {
    final HeaderTradeSettlementType ret = new HeaderTradeSettlementType ();

    final PaymentMeansType aPM = aUBLInvoice.hasPaymentMeansEntries () ? aUBLInvoice.getPaymentMeansAtIndex (0) : null;

    if (aPM != null && aPM.hasPaymentIDEntries ())
      ret.addPaymentReference (_convertTextType (aPM.getPaymentID ().get (0).getValue ()));

    ret.setInvoiceCurrencyCode (aUBLInvoice.getDocumentCurrencyCode ().getValue ());

    if (aUBLInvoice.getPayeeParty () != null)
      ret.setPayeeTradeParty (_convertParty (aUBLInvoice.getPayeeParty ()));

    if (aPM != null)
    {
      final TradeSettlementPaymentMeansType aTSPMT = new TradeSettlementPaymentMeansType ();
      aTSPMT.setTypeCode (aPM.getPaymentMeansCodeValue ());

      final CreditorFinancialAccountType aCFAT = new CreditorFinancialAccountType ();
      if (aPM.getPayeeFinancialAccount () != null)
        aCFAT.setIBANID (aPM.getPayeeFinancialAccount ().getIDValue ());

      aTSPMT.setPayeePartyCreditorFinancialAccount (aCFAT);
      ret.addSpecifiedTradeSettlementPaymentMeans (aTSPMT);
    }

    ret.setApplicableTradeTax (_convertApplicableTradeTax (aUBLInvoice));

    if (aUBLInvoice.hasInvoicePeriodEntries ())
    {
      final PeriodType aIP = aUBLInvoice.getInvoicePeriodAtIndex (0);

      final SpecifiedPeriodType aSPT = new SpecifiedPeriodType ();
      if (aIP.getStartDate () != null)
        aSPT.setStartDateTime (_createDate (aIP.getStartDate ().getValueLocal ()));

      if (aIP.getEndDate () != null)
        aSPT.setEndDateTime (_createDate (aIP.getEndDate ().getValueLocal ()));

      ret.setBillingSpecifiedPeriod (aSPT);
    }

    ret.setSpecifiedTradeAllowanceCharge (_convertSpecifiedTradeAllowanceCharge (aUBLInvoice));
    ret.setSpecifiedTradePaymentTerms (_convertSpecifiedTradePaymentTerms (aUBLInvoice));
    ret.setSpecifiedTradeSettlementHeaderMonetarySummation (_convertSpecifiedTradeSettlementHeaderMonetarySummation (aUBLInvoice));

    if (aUBLInvoice.getAccountingCost () != null)
    {
      final TradeAccountingAccountType aTAAT = new TradeAccountingAccountType ();
      aTAAT.setID (aUBLInvoice.getAccountingCost ().getValue ());
      ret.addReceivableSpecifiedTradeAccountingAccount (aTAAT);
    }

    return ret;
  }

  @Nonnull
  private static TradeSettlementHeaderMonetarySummationType _convertSpecifiedTradeSettlementHeaderMonetarySummation (@Nonnull final InvoiceType aUBLInvoice)
  {
    final TradeSettlementHeaderMonetarySummationType aTSHMST = new TradeSettlementHeaderMonetarySummationType ();
    final MonetaryTotalType aUBLLMT = aUBLInvoice.getLegalMonetaryTotal ();
    if (aUBLLMT != null)
    {
      ifNotNull (aTSHMST::addLineTotalAmount, _convertAmountType (aUBLLMT.getLineExtensionAmount ()));
      ifNotNull (aTSHMST::addChargeTotalAmount, _convertAmountType (aUBLLMT.getChargeTotalAmount ()));
      ifNotNull (aTSHMST::addAllowanceTotalAmount, _convertAmountType (aUBLLMT.getAllowanceTotalAmount ()));
      ifNotNull (aTSHMST::addTaxBasisTotalAmount, _convertAmountType (aUBLLMT.getTaxExclusiveAmount ()));
    }

    if (aUBLInvoice.hasTaxTotalEntries ())
    {
      // Currency ID is required here
      ifNotNull (aTSHMST::addTaxTotalAmount, _convertAmountType (aUBLInvoice.getTaxTotalAtIndex (0).getTaxAmount (), true));
    }

    if (aUBLLMT != null)
    {
      ifNotNull (aTSHMST::addRoundingAmount, _convertAmountType (aUBLLMT.getPayableRoundingAmount ()));
      ifNotNull (aTSHMST::addGrandTotalAmount, _convertAmountType (aUBLLMT.getTaxInclusiveAmount ()));
      ifNotNull (aTSHMST::addTotalPrepaidAmount, _convertAmountType (aUBLLMT.getPrepaidAmount ()));
      ifNotNull (aTSHMST::addDuePayableAmount, _convertAmountType (aUBLLMT.getPayableAmount ()));
    }

    return aTSHMST;
  }

  @Nonnull
  private static List <TradeTaxType> _convertApplicableTradeTax (@Nonnull final InvoiceType aUBLInvoice)
  {
    final List <TradeTaxType> ret = new ArrayList <> ();
    for (final TaxTotalType aTaxTotal : aUBLInvoice.getTaxTotal ())
      for (final TaxSubtotalType aTaxSubtotal : aTaxTotal.getTaxSubtotal ())
      {
        final TaxCategoryType aTaxCategory = aTaxSubtotal.getTaxCategory ();
        final TaxSchemeType aTaxScheme = aTaxCategory.getTaxScheme ();

        final TradeTaxType aTradeTax = new TradeTaxType ();

        if (aTaxScheme != null)
          ifNotEmpty (aTradeTax::setTypeCode, aTaxScheme.getIDValue ());
        ifNotEmpty (aTradeTax::setCategoryCode, aTaxCategory.getIDValue ());

        ifNotNull (aTradeTax::addCalculatedAmount, _convertAmountType (aTaxSubtotal.getTaxAmount ()));

        if (aTaxSubtotal.getTaxCategory () != null)
          ifNotEmpty (aTradeTax::setCategoryCode, aTaxSubtotal.getTaxCategory ().getIDValue ());

        ifNotNull (aTradeTax::addBasisAmount, _convertAmountType (aTaxSubtotal.getTaxableAmount ()));
        ifNotNull (aTradeTax::setRateApplicablePercent, aTaxCategory.getPercentValue ());

        if (aTaxCategory.hasTaxExemptionReasonEntries ())
          ifNotEmpty (aTradeTax::setExemptionReason, aTaxCategory.getTaxExemptionReasonAtIndex (0).getValue ());

        ifNotEmpty (aTradeTax::setExemptionReasonCode, aTaxCategory.getTaxExemptionReasonCodeValue ());
        ret.add (aTradeTax);
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
    for (final AllowanceChargeType aItem : aUBLInvoice.getAllowanceCharge ())
    {
      final TradeAllowanceChargeType aTDCT = new TradeAllowanceChargeType ();

      final IndicatorType aITDC = new IndicatorType ();
      aITDC.setIndicator (Boolean.valueOf (aItem.getChargeIndicator ().isValue ()));
      aTDCT.setChargeIndicator (aITDC);

      aTDCT.addActualAmount (_convertAmountType (aItem.getAmount ()));

      if (aItem.getAllowanceChargeReasonCode () != null)
        aTDCT.setReasonCode (aItem.getAllowanceChargeReasonCodeValue ());

      if (aItem.hasAllowanceChargeReasonEntries ())
        aTDCT.setReason (aItem.getAllowanceChargeReason ().get (0).getValue ());

      if (aItem.getMultiplierFactorNumeric () != null)
        aTDCT.setCalculationPercent (aItem.getMultiplierFactorNumericValue ());

      if (aItem.getBaseAmount () != null)
        aTDCT.setBasisAmount (aItem.getBaseAmountValue ());

      if (aItem.hasTaxCategoryEntries ())
      {
        final TaxCategoryType aTaxCategory = aItem.getTaxCategoryAtIndex (0);
        final TaxSchemeType aTaxSchene = aTaxCategory.getTaxScheme ();

        final TradeTaxType aTradeTax = new TradeTaxType ();
        if (aTaxSchene != null)
          ifNotEmpty (aTradeTax::setTypeCode, aTaxSchene.getIDValue ());
        ifNotEmpty (aTradeTax::setCategoryCode, aTaxCategory.getIDValue ());
        ifNotNull (aTradeTax::setRateApplicablePercent, aTaxCategory.getPercentValue ());

        aTDCT.addCategoryTradeTax (aTradeTax);
      }

      ret.add (aTDCT);
    }
    return ret;
  }

  @Nonnull
  private static List <ReferencedDocumentType> _convertAdditionalReferencedDocument (@Nonnull final InvoiceType aUBLInvoice)
  {
    final List <ReferencedDocumentType> ret = new ArrayList <> ();
    for (final DocumentReferenceType aDocDesc : aUBLInvoice.getAdditionalDocumentReference ())
    {
      final ReferencedDocumentType aURDT = new ReferencedDocumentType ();

      if (aDocDesc.getID () != null)
        aURDT.setIssuerAssignedID (aDocDesc.getIDValue ());

      // Add DocumentTypeCode where possible
      if (aDocDesc.getDocumentTypeCode () != null && isValidDocumentReferenceTypeCode (aDocDesc.getDocumentTypeCodeValue ()))
        aURDT.setTypeCode (aDocDesc.getDocumentTypeCodeValue ());
      else
        aURDT.setTypeCode ("916");

      if (aDocDesc.getIssueDate () != null)
      {
        final FormattedDateTimeType aFIDT = new FormattedDateTimeType ();
        aFIDT.setDateTimeString (_createFormattedDateValue (aDocDesc.getIssueDateValueLocal ()));
        aURDT.setFormattedIssueDateTime (aFIDT);
      }

      for (final var aDesc : aDocDesc.getDocumentDescription ())
      {
        final TextType aText = new TextType ();
        aText.setValue (aDesc.getValue ());
        aText.setLanguageID (aDesc.getLanguageID ());
        aText.setLanguageLocaleID (aDesc.getLanguageLocaleID ());
        aURDT.addName (aText);
      }

      final AttachmentType aAttachment = aDocDesc.getAttachment ();
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
      aSCTT.setApplicableHeaderTradeDelivery (_convertApplicableHeaderTradeDelivery (aUBLInvoice.hasDeliveryEntries () ? aUBLInvoice.getDeliveryAtIndex (0)
                                                                                                                       : null));

      // ApplicableHeaderTradeSettlement
      aSCTT.setApplicableHeaderTradeSettlement (_convertApplicableHeaderTradeSettlement (aUBLInvoice));

      aCIIInvoice.setSupplyChainTradeTransaction (aSCTT);
    }

    return aCIIInvoice;
  }
}
