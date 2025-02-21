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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.error.list.ErrorList;

import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.AllowanceChargeType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.CommodityClassificationType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.CustomerPartyType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.InvoiceLineType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.ItemPropertyType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.ItemType;
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
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._100.*;
import un.unece.uncefact.data.standard.unqualifieddatatype._100.CodeType;
import un.unece.uncefact.data.standard.unqualifieddatatype._100.QuantityType;

/**
 * UBL 2.1 to CII D16B converter.
 *
 * @author Vartika Rastogi
 * @author Philip Helger
 */
public final class UBL21InvoiceToCIID16BConverter extends AbstractToCIID16BConverter
{
  private UBL21InvoiceToCIID16BConverter ()
  {}

  @Nonnull
  private static SupplyChainTradeLineItemType _convertInvoiceLine (@Nonnull final InvoiceLineType aUBLLine)
  {
    final SupplyChainTradeLineItemType ret = new SupplyChainTradeLineItemType ();
    final DocumentLineDocumentType aDLDT = new DocumentLineDocumentType ();

    aDLDT.setLineID (aUBLLine.getIDValue ());

    for (final var aUBLNote : aUBLLine.getNote ())
      aDLDT.addIncludedNote (convertNote (aUBLNote));

    ret.setAssociatedDocumentLineDocument (aDLDT);

    // SpecifiedTradeProduct
    final TradeProductType aTPT = new TradeProductType ();
    final ItemType aUBLItem = aUBLLine.getItem ();
    if (aUBLItem.getStandardItemIdentification () != null)
      aTPT.setGlobalID (convertID (aUBLItem.getStandardItemIdentification ().getID ()));

    if (aUBLItem.getSellersItemIdentification () != null)
      aTPT.setSellerAssignedID (aUBLItem.getSellersItemIdentification ().getIDValue ());

    aTPT.addName (convertText (aUBLItem.getNameValue ()));

    if (aUBLItem.hasDescriptionEntries ())
      aTPT.setDescription (aUBLItem.getDescriptionAtIndex (0).getValue ());

    // ApplicableProductCharacteristic
    for (final ItemPropertyType aUBLAddItemProp : aUBLLine.getItem ().getAdditionalItemProperty ())
    {
      final ProductCharacteristicType aPCT = new ProductCharacteristicType ();
      ifNotNull (aPCT::addDescription, convertText (aUBLAddItemProp.getNameValue ()));
      ifNotNull (aPCT::addValue, convertText (aUBLAddItemProp.getValueValue ()));
      aTPT.addApplicableProductCharacteristic (aPCT);
    }

    // DesignatedProductClassification
    for (final CommodityClassificationType aUBLCC : aUBLLine.getItem ().getCommodityClassification ())
    {
      final ProductClassificationType aPCT = new ProductClassificationType ();
      final CodeType aCT = new CodeType ();
      ifNotEmpty (aCT::setListID, aUBLCC.getItemClassificationCode ().getListID ());
      ifNotEmpty (aCT::setValue, aUBLCC.getItemClassificationCode ().getValue ());
      aPCT.setClassCode (aCT);
      aTPT.addDesignatedProductClassification (aPCT);
    }
    ret.setSpecifiedTradeProduct (aTPT);

    // BuyerOrderReferencedDocument
    final ReferencedDocumentType aRDT = new ReferencedDocumentType ();
    if (aUBLLine.hasOrderLineReferenceEntries ())
    {
      aRDT.setLineID (aUBLLine.getOrderLineReferenceAtIndex (0).getLineIDValue ());
    }

    // NetPriceProductTradePrice
    final TradePriceType aLTPT = new TradePriceType ();
    if (aUBLLine.getPrice () != null && aUBLLine.getPrice ().getPriceAmount () != null)
    {
      aLTPT.addChargeAmount (convertAmount (aUBLLine.getPrice ().getPriceAmount ()));
    }

    // SpecifiedLineTradeAgreement
    final LineTradeAgreementType aLTAT = new LineTradeAgreementType ();
    aLTAT.setBuyerOrderReferencedDocument (aRDT);
    aLTAT.setNetPriceProductTradePrice (aLTPT);
    ret.setSpecifiedLineTradeAgreement (aLTAT);

    // SpecifiedLineTradeDelivery
    final LineTradeDeliveryType aLTDT = new LineTradeDeliveryType ();
    final QuantityType aQuantity = new QuantityType ();
    aQuantity.setUnitCode (aUBLLine.getInvoicedQuantity ().getUnitCode ());
    aQuantity.setValue (aUBLLine.getInvoicedQuantity ().getValue ());
    aLTDT.setBilledQuantity (aQuantity);
    ret.setSpecifiedLineTradeDelivery (aLTDT);

    // SpecifiedLineTradeSettlement
    final LineTradeSettlementType aSLTS = new LineTradeSettlementType ();
    for (final TaxCategoryType aUBLTaxCategory : aUBLLine.getItem ().getClassifiedTaxCategory ())
    {
      final TaxSchemeType aUBLTaxScheme = aUBLTaxCategory.getTaxScheme ();

      final TradeTaxType aTradeTax = new TradeTaxType ();
      if (aUBLTaxScheme != null)
        ifNotEmpty (aTradeTax::setTypeCode, aUBLTaxCategory.getTaxScheme ().getIDValue ());
      ifNotEmpty (aTradeTax::setCategoryCode, aUBLTaxCategory.getIDValue ());
      ifNotNull (aTradeTax::setRateApplicablePercent, aUBLTaxCategory.getPercentValue ());
      aSLTS.addApplicableTradeTax (aTradeTax);
    }

    final TradeSettlementLineMonetarySummationType aTSLMST = new TradeSettlementLineMonetarySummationType ();
    ifNotNull (aTSLMST::addLineTotalAmount, convertAmount (aUBLLine.getLineExtensionAmount ()));

    if (aUBLLine.getAccountingCostValue () != null)
    {
      final TradeAccountingAccountType aTAATL = new TradeAccountingAccountType ();
      aTAATL.setID (aUBLLine.getAccountingCostValue ());
      aSLTS.addReceivableSpecifiedTradeAccountingAccount (aTAATL);
    }

    aSLTS.setSpecifiedTradeSettlementLineMonetarySummation (aTSLMST);
    ret.setSpecifiedLineTradeSettlement (aSLTS);

    return ret;
  }

  @Nonnull
  private static HeaderTradeSettlementType _createApplicableHeaderTradeSettlement (@Nonnull final InvoiceType aUBLInvoice)
  {
    final HeaderTradeSettlementType ret = new HeaderTradeSettlementType ();

    final PaymentMeansType aUBLPaymentMeans = aUBLInvoice.hasPaymentMeansEntries () ? aUBLInvoice.getPaymentMeansAtIndex (0)
                                                                                    : null;

    if (aUBLPaymentMeans != null && aUBLPaymentMeans.hasPaymentIDEntries ())
      ret.addPaymentReference (convertText (aUBLPaymentMeans.getPaymentIDAtIndex (0).getValue ()));

    ifNotEmpty (ret::setInvoiceCurrencyCode, aUBLInvoice.getDocumentCurrencyCodeValue ());
    ifNotNull (ret::setPayeeTradeParty, convertParty (aUBLInvoice.getPayeeParty ()));

    if (aUBLPaymentMeans != null)
    {
      final TradeSettlementPaymentMeansType aTSPMT = new TradeSettlementPaymentMeansType ();
      ifNotEmpty (aTSPMT::setTypeCode, aUBLPaymentMeans.getPaymentMeansCodeValue ());

      final CreditorFinancialAccountType aCFAT = new CreditorFinancialAccountType ();
      if (aUBLPaymentMeans.getPayeeFinancialAccount () != null)
        ifNotEmpty (aCFAT::setIBANID, aUBLPaymentMeans.getPayeeFinancialAccount ().getIDValue ());
      aTSPMT.setPayeePartyCreditorFinancialAccount (aCFAT);
      ret.addSpecifiedTradeSettlementPaymentMeans (aTSPMT);
    }

    for (final TaxTotalType aUBLTaxTotal : aUBLInvoice.getTaxTotal ())
      for (final TaxSubtotalType aUBLTaxSubtotal : aUBLTaxTotal.getTaxSubtotal ())
        ret.addApplicableTradeTax (convertApplicableTradeTax (aUBLTaxSubtotal));

    if (aUBLInvoice.hasInvoicePeriodEntries ())
    {
      final PeriodType aUBLPeriod = aUBLInvoice.getInvoicePeriodAtIndex (0);

      final SpecifiedPeriodType aSPT = new SpecifiedPeriodType ();
      if (aUBLPeriod.getStartDate () != null)
        aSPT.setStartDateTime (convertDate (aUBLPeriod.getStartDate ().getValueLocal ()));
      if (aUBLPeriod.getEndDate () != null)
        aSPT.setEndDateTime (convertDate (aUBLPeriod.getEndDate ().getValueLocal ()));
      ret.setBillingSpecifiedPeriod (aSPT);
    }

    for (final AllowanceChargeType aUBLAllowanceCharge : aUBLInvoice.getAllowanceCharge ())
      ret.addSpecifiedTradeAllowanceCharge (convertSpecifiedTradeAllowanceCharge (aUBLAllowanceCharge));

    for (final PaymentTermsType aUBLPaymentTerms : aUBLInvoice.getPaymentTerms ())
      ret.addSpecifiedTradePaymentTerms (convertSpecifiedTradePaymentTerms (aUBLPaymentTerms, aUBLPaymentMeans));

    final TaxTotalType aUBLTaxTotal = aUBLInvoice.hasTaxTotalEntries () ? aUBLInvoice.getTaxTotalAtIndex (0) : null;
    ret.setSpecifiedTradeSettlementHeaderMonetarySummation (createSpecifiedTradeSettlementHeaderMonetarySummation (aUBLInvoice.getLegalMonetaryTotal (),
                                                                                                                   aUBLTaxTotal));

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
      ifNotEmpty (aEDT::setID, aUBLInvoice.getIDValue ());
      ifNotEmpty (aEDT::setTypeCode, aUBLInvoice.getInvoiceTypeCodeValue ());

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
      for (final var aLine : aUBLInvoice.getInvoiceLine ())
        aSCTT.addIncludedSupplyChainTradeLineItem (_convertInvoiceLine (aLine));

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
          aRDT.setIssuerAssignedID (aUBLInvoice.getOrderReference ().getIDValue ());
          aHTAT.setBuyerOrderReferencedDocument (aRDT);
        }

        // ContractReferencedDocument
        if (aUBLInvoice.hasContractDocumentReferenceEntries ())
        {
          final ReferencedDocumentType aCRDT = new ReferencedDocumentType ();
          aCRDT.setIssuerAssignedID (aUBLInvoice.getContractDocumentReferenceAtIndex (0).getIDValue ());
          aHTAT.setContractReferencedDocument (aCRDT);
        }

        // AdditionalReferencedDocument
        for (final var aUBLDocDesc : aUBLInvoice.getAdditionalDocumentReference ())
          aHTAT.addAdditionalReferencedDocument (convertAdditionalReferencedDocument (aUBLDocDesc));
        aSCTT.setApplicableHeaderTradeAgreement (aHTAT);
      }

      // ApplicableHeaderTradeDelivery
      aSCTT.setApplicableHeaderTradeDelivery (createApplicableHeaderTradeDelivery (aUBLInvoice.hasDeliveryEntries () ? aUBLInvoice.getDeliveryAtIndex (0)
                                                                                                                     : null));

      // ApplicableHeaderTradeSettlement
      aSCTT.setApplicableHeaderTradeSettlement (_createApplicableHeaderTradeSettlement (aUBLInvoice));

      aCIIInvoice.setSupplyChainTradeTransaction (aSCTT);
    }

    return aCIIInvoice;
  }
}
