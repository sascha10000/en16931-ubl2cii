/*
 * Copyright (C) 2024-2025 Philip Helger
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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.error.list.ErrorList;

import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.AllowanceChargeType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.AttachmentType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.CommodityClassificationType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.CustomerPartyType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.DeliveryType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.DocumentReferenceType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.InvoiceLineType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.ItemPropertyType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.ItemType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.LocationType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.MonetaryTotalType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.PaymentMeansType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.PaymentTermsType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.PeriodType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.SupplierPartyType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.TaxCategoryType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.TaxSchemeType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.TaxSubtotalType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.TaxTotalType;
import oasis.names.specification.ubl.schema.xsd.invoice_21.InvoiceType;
import un.unece.uncefact.data.standard.crossindustryinvoice._100.CrossIndustryInvoiceType;
import un.unece.uncefact.data.standard.qualifieddatatype._100.FormattedDateTimeType;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._100.*;
import un.unece.uncefact.data.standard.unqualifieddatatype._100.BinaryObjectType;
import un.unece.uncefact.data.standard.unqualifieddatatype._100.CodeType;
import un.unece.uncefact.data.standard.unqualifieddatatype._100.IndicatorType;
import un.unece.uncefact.data.standard.unqualifieddatatype._100.QuantityType;
import un.unece.uncefact.data.standard.unqualifieddatatype._100.TextType;

/**
 * UBL 2.1 to CII D16B converter.
 *
 * @author Vartika Rastogi
 * @author Philip Helger
 */
public final class UBL21InvoiceToCII16BConverter extends AbstractToCII16BConverter
{
  private UBL21InvoiceToCII16BConverter ()
  {}

  @Nonnull
  private static List <SupplyChainTradeLineItemType> _convertInvoiceLines (@Nonnull final List <InvoiceLineType> aLstIL)
  {
    final List <SupplyChainTradeLineItemType> ret = new ArrayList <> ();
    for (final InvoiceLineType aILT : aLstIL)
    {
      final SupplyChainTradeLineItemType aISCTLI = new SupplyChainTradeLineItemType ();
      final DocumentLineDocumentType aDLDT = new DocumentLineDocumentType ();

      aDLDT.setLineID (aILT.getIDValue ());

      for (final var aNote : aILT.getNote ())
        aDLDT.addIncludedNote (convertNote (aNote));

      aISCTLI.setAssociatedDocumentLineDocument (aDLDT);

      // SpecifiedTradeProduct
      final TradeProductType aTPT = new TradeProductType ();
      final ItemType aIT = aILT.getItem ();
      if (aIT.getStandardItemIdentification () != null)
      {
        aTPT.setGlobalID (convertID (aIT.getStandardItemIdentification ().getID ()));
      }

      if (aIT.getSellersItemIdentification () != null)
        aTPT.setSellerAssignedID (aIT.getSellersItemIdentification ().getIDValue ());

      aTPT.addName (convertText (aIT.getNameValue ()));

      if (aIT.hasDescriptionEntries ())
      {
        aTPT.setDescription (aIT.getDescriptionAtIndex (0).getValue ());
      }

      // ApplicableProductCharacteristic
      for (final ItemPropertyType aIPT : aILT.getItem ().getAdditionalItemProperty ())
      {
        final ProductCharacteristicType aPCT = new ProductCharacteristicType ();
        aPCT.addDescription (convertText (aIPT.getNameValue ()));
        aPCT.addValue (convertText (aIPT.getValueValue ()));
        aTPT.addApplicableProductCharacteristic (aPCT);
      }

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
        aLTPT.addChargeAmount (convertAmount (aILT.getPrice ().getPriceAmount ()));
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
        aTSLMST.addLineTotalAmount (convertAmount (aILT.getLineExtensionAmount ()));

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
        aFIDT.setDateTimeString (createFormattedDateValue (aDocDesc.getIssueDateValueLocal ()));
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
  private static HeaderTradeDeliveryType _createApplicableHeaderTradeDelivery (@Nullable final DeliveryType aDelivery)
  {
    // Object is mandatory
    final HeaderTradeDeliveryType ret = new HeaderTradeDeliveryType ();

    if (aDelivery != null)
    {
      final LocationType aDL = aDelivery.getDeliveryLocation ();
      if (aDL != null)
      {
        final TradePartyType aTPTHT = new TradePartyType ();
        ifNotNull (aTPTHT::addID, convertID (aDL.getID ()));

        aTPTHT.setPostalTradeAddress (convertAddress (aDL.getAddress ()));
        ret.setShipToTradeParty (aTPTHT);
      }

      if (aDelivery.getActualDeliveryDate () != null)
      {
        final SupplyChainEventType aSCET = new SupplyChainEventType ();
        aSCET.setOccurrenceDateTime (convertDate (aDelivery.getActualDeliveryDate ().getValueLocal ()));
        ret.setActualDeliverySupplyChainEvent (aSCET);
      }
    }
    return ret;
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

        ifNotNull (aTradeTax::addCalculatedAmount, convertAmount (aTaxSubtotal.getTaxAmount ()));

        if (aTaxSubtotal.getTaxCategory () != null)
          ifNotEmpty (aTradeTax::setCategoryCode, aTaxSubtotal.getTaxCategory ().getIDValue ());

        ifNotNull (aTradeTax::addBasisAmount, convertAmount (aTaxSubtotal.getTaxableAmount ()));
        ifNotNull (aTradeTax::setRateApplicablePercent, aTaxCategory.getPercentValue ());

        if (aTaxCategory.hasTaxExemptionReasonEntries ())
          ifNotEmpty (aTradeTax::setExemptionReason, aTaxCategory.getTaxExemptionReasonAtIndex (0).getValue ());

        ifNotEmpty (aTradeTax::setExemptionReasonCode, aTaxCategory.getTaxExemptionReasonCodeValue ());
        ret.add (aTradeTax);
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

      aTDCT.addActualAmount (convertAmount (aItem.getAmount ()));

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
  private static List <TradePaymentTermsType> _convertSpecifiedTradePaymentTerms (@Nonnull final InvoiceType aUBLInvoice)
  {
    final List <TradePaymentTermsType> ret = new ArrayList <> ();
    for (final PaymentTermsType aPTT : aUBLInvoice.getPaymentTerms ())
    {
      final TradePaymentTermsType aTPTT = new TradePaymentTermsType ();

      for (final var aNote : aPTT.getNote ())
        aTPTT.addDescription (convertText (aNote.getValue ()));

      if (aUBLInvoice.hasPaymentMeansEntries ())
      {
        final PaymentMeansType aPM = aUBLInvoice.getPaymentMeans ().get (0);
        if (aPM.getPaymentDueDate () != null)
          aTPTT.setDueDateDateTime (convertDate (aPM.getPaymentDueDate ().getValueLocal ()));
      }
      ret.add (aTPTT);
    }
    return ret;
  }

  @Nonnull
  private static TradeSettlementHeaderMonetarySummationType _createSpecifiedTradeSettlementHeaderMonetarySummation (@Nonnull final InvoiceType aUBLInvoice)
  {
    final TradeSettlementHeaderMonetarySummationType aTSHMST = new TradeSettlementHeaderMonetarySummationType ();
    final MonetaryTotalType aUBLLMT = aUBLInvoice.getLegalMonetaryTotal ();
    if (aUBLLMT != null)
    {
      ifNotNull (aTSHMST::addLineTotalAmount, convertAmount (aUBLLMT.getLineExtensionAmount ()));
      ifNotNull (aTSHMST::addChargeTotalAmount, convertAmount (aUBLLMT.getChargeTotalAmount ()));
      ifNotNull (aTSHMST::addAllowanceTotalAmount, convertAmount (aUBLLMT.getAllowanceTotalAmount ()));
      ifNotNull (aTSHMST::addTaxBasisTotalAmount, convertAmount (aUBLLMT.getTaxExclusiveAmount ()));
    }

    if (aUBLInvoice.hasTaxTotalEntries ())
    {
      // Currency ID is required here
      ifNotNull (aTSHMST::addTaxTotalAmount, convertAmount (aUBLInvoice.getTaxTotalAtIndex (0).getTaxAmount (), true));
    }

    if (aUBLLMT != null)
    {
      ifNotNull (aTSHMST::addRoundingAmount, convertAmount (aUBLLMT.getPayableRoundingAmount ()));
      ifNotNull (aTSHMST::addGrandTotalAmount, convertAmount (aUBLLMT.getTaxInclusiveAmount ()));
      ifNotNull (aTSHMST::addTotalPrepaidAmount, convertAmount (aUBLLMT.getPrepaidAmount ()));
      ifNotNull (aTSHMST::addDuePayableAmount, convertAmount (aUBLLMT.getPayableAmount ()));
    }

    return aTSHMST;
  }

  @Nonnull
  private static HeaderTradeSettlementType _createApplicableHeaderTradeSettlement (@Nonnull final InvoiceType aUBLInvoice)
  {
    final HeaderTradeSettlementType ret = new HeaderTradeSettlementType ();

    final PaymentMeansType aPM = aUBLInvoice.hasPaymentMeansEntries () ? aUBLInvoice.getPaymentMeansAtIndex (0) : null;

    if (aPM != null && aPM.hasPaymentIDEntries ())
      ret.addPaymentReference (convertText (aPM.getPaymentIDAtIndex (0).getValue ()));

    ret.setInvoiceCurrencyCode (aUBLInvoice.getDocumentCurrencyCode ().getValue ());

    if (aUBLInvoice.getPayeeParty () != null)
      ret.setPayeeTradeParty (convertParty (aUBLInvoice.getPayeeParty ()));

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
        aSPT.setStartDateTime (convertDate (aIP.getStartDate ().getValueLocal ()));

      if (aIP.getEndDate () != null)
        aSPT.setEndDateTime (convertDate (aIP.getEndDate ().getValueLocal ()));

      ret.setBillingSpecifiedPeriod (aSPT);
    }

    ret.setSpecifiedTradeAllowanceCharge (_convertSpecifiedTradeAllowanceCharge (aUBLInvoice));
    ret.setSpecifiedTradePaymentTerms (_convertSpecifiedTradePaymentTerms (aUBLInvoice));
    ret.setSpecifiedTradeSettlementHeaderMonetarySummation (_createSpecifiedTradeSettlementHeaderMonetarySummation (aUBLInvoice));

    if (aUBLInvoice.getAccountingCost () != null)
    {
      final TradeAccountingAccountType aTAAT = new TradeAccountingAccountType ();
      aTAAT.setID (aUBLInvoice.getAccountingCost ().getValue ());
      ret.addReceivableSpecifiedTradeAccountingAccount (aTAAT);
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
        aEDT.setIssueDateTime (convertDate (aUBLInvoice.getIssueDate ().getValueLocal ()));

      // Add add IncludedNote
      for (final var aNote : aUBLInvoice.getNote ())
        aEDT.addIncludedNote (convertNote (aNote));

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
          aHTAT.setSellerTradeParty (convertParty (aSupplierParty.getParty ()));

        // BuyerTradeParty
        final CustomerPartyType aCustomerParty = aUBLInvoice.getAccountingCustomerParty ();
        if (aCustomerParty != null)
          aHTAT.setBuyerTradeParty (convertParty (aCustomerParty.getParty ()));

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
      aSCTT.setApplicableHeaderTradeDelivery (_createApplicableHeaderTradeDelivery (aUBLInvoice.hasDeliveryEntries () ? aUBLInvoice.getDeliveryAtIndex (0)
                                                                                                                      : null));

      // ApplicableHeaderTradeSettlement
      aSCTT.setApplicableHeaderTradeSettlement (_createApplicableHeaderTradeSettlement (aUBLInvoice));

      aCIIInvoice.setSupplyChainTradeTransaction (aSCTT);
    }

    return aCIIInvoice;
  }
}
