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

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.datetime.PDTFactory;
import com.helger.commons.math.MathHelper;
import com.helger.commons.string.StringHelper;

import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.*;
import un.unece.uncefact.data.standard.qualifieddatatype._100.FormattedDateTimeType;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._100.*;
import un.unece.uncefact.data.standard.unqualifieddatatype._100.AmountType;
import un.unece.uncefact.data.standard.unqualifieddatatype._100.BinaryObjectType;
import un.unece.uncefact.data.standard.unqualifieddatatype._100.IDType;
import un.unece.uncefact.data.standard.unqualifieddatatype._100.IndicatorType;
import un.unece.uncefact.data.standard.unqualifieddatatype._100.TextType;

/**
 * Abstract base class to convert UBL to CII D16B
 *
 * @author Philip Helger
 */
public abstract class AbstractToCIID16BConverter {
  private static final String CII_DATE_FORMAT = "102";

  protected static <T> boolean ifNotNull(@Nonnull final Consumer<? super T> aConsumer, @Nullable final T aObj) {
    if (aObj == null)
      return false;
    aConsumer.accept(aObj);
    return true;
  }

  protected static boolean ifNotEmpty(@Nonnull final Consumer<? super String> aConsumer, @Nullable final String s) {
    if (StringHelper.hasNoText(s))
      return false;
    aConsumer.accept(s);
    return true;
  }

  protected static String valueOrEmpty (@Nullable final String s) {
    return s == null ? "" : s;
  }

  protected static boolean isOriginatorDocumentReferenceTypeCode(@Nullable final String s) {
    // BT-17
    return "50".equals(s);
  }

  protected static boolean isValidDocumentReferenceTypeCode(@Nullable final String s) {
    // BT-17 or BT-18
    // Value 916 from BT-122 should not lead to a DocumentTypeCode
    return isOriginatorDocumentReferenceTypeCode(s) || "130".equals(s);
  }

  @Nullable
  private static String _getAsVAIfNecessary(@Nullable final String s) {
    if ("VAT".equals(s))
      return "VA";
    return s;
  }

  @Nullable
  protected static String createFormattedDateValue(@Nullable final LocalDate aLocalDate) {
    if (aLocalDate == null)
      return null;

    final SimpleDateFormat aFormatter = new SimpleDateFormat("yyyyMMdd");
    final Date aDate = PDTFactory.createDate(aLocalDate);
    return aFormatter.format(aDate);
  }

  @Nullable
  private static un.unece.uncefact.data.standard.unqualifieddatatype._100.DateTimeType.DateTimeString createDateTimeString(@Nullable final LocalDate aLocalDate) {
    if (aLocalDate == null)
      return null;

    final un.unece.uncefact.data.standard.unqualifieddatatype._100.DateTimeType.DateTimeString aret = new un.unece.uncefact.data.standard.unqualifieddatatype._100.DateTimeType.DateTimeString();
    aret.setFormat(CII_DATE_FORMAT);
    aret.setValue(createFormattedDateValue(aLocalDate));
    return aret;
  }

  @Nullable
  protected static un.unece.uncefact.data.standard.unqualifieddatatype._100.DateTimeType convertDate(@Nullable final LocalDate aLocalDate) {
    if (aLocalDate == null)
      return null;

    final un.unece.uncefact.data.standard.unqualifieddatatype._100.DateTimeType ret = new un.unece.uncefact.data.standard.unqualifieddatatype._100.DateTimeType();
    ret.setDateTimeString(createDateTimeString(aLocalDate));
    return ret;
  }

  @Nullable
  protected static TextType convertText(@Nullable final String sValue) {
    if (sValue == null)
      return null;

    final TextType ret = new TextType();
    ret.setValue(sValue);
    return ret;
  }

  @Nullable
  protected static IDType convertID(@Nullable final com.helger.xsds.ccts.cct.schemamodule.IdentifierType aUBLID) {
    if (aUBLID == null)
      return null;

    final IDType ret = new IDType();
    ifNotNull(ret::setSchemeID, aUBLID.getSchemeID());
    ifNotNull(ret::setValue, aUBLID.getValue());
    return ret;
  }

  @Nullable
  protected static AmountType convertAmount(@Nullable final com.helger.xsds.ccts.cct.schemamodule.AmountType aUBLAmount) {
    return convertAmount(aUBLAmount, false);
  }

  @Nullable
  protected static AmountType convertAmount(@Nullable final com.helger.xsds.ccts.cct.schemamodule.AmountType aUBLAmount,
                                            final boolean bWithCurrency) {
    if (aUBLAmount == null)
      return null;

    final AmountType ret = new AmountType();
    if (bWithCurrency)
      ret.setCurrencyID(aUBLAmount.getCurrencyID());
    ifNotNull(ret::setValue, MathHelper.getWithoutTrailingZeroes(aUBLAmount.getValue()));
    return ret;
  }

  @Nullable
  protected static un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._100.NoteType convertNote(@Nullable final oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.NoteType aUBLNote) {
    if (aUBLNote == null || aUBLNote.getValue() == null)
      return null;

    final un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._100.NoteType ret = new un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._100.NoteType();
    final TextType aTT = new TextType();
    aTT.setValue(aUBLNote.getValue());
    ret.addContent(aTT);
    return ret;
  }

  @Nullable
  protected static TradeAddressType convertAddress(@Nullable final AddressType aUBLAddress) {
    if (aUBLAddress == null)
      return null;

    final TradeAddressType ret = new TradeAddressType();
    ifNotEmpty(ret::setLineOne, aUBLAddress.getStreetNameValue());
    ifNotEmpty(ret::setLineTwo, aUBLAddress.getAdditionalStreetNameValue());
    if (aUBLAddress.hasAddressLineEntries())
      ifNotEmpty(ret::setLineThree, aUBLAddress.getAddressLineAtIndex(0).getLineValue());
    ifNotEmpty(ret::setCityName, aUBLAddress.getCityNameValue());
    ifNotEmpty(ret::setPostcodeCode, aUBLAddress.getPostalZoneValue());
    if (aUBLAddress.getCountrySubentity() != null)
      ret.addCountrySubDivisionName(convertText(aUBLAddress.getCountrySubentity().getValue()));
    if (aUBLAddress.getCountry() != null)
      ifNotEmpty(ret::setCountryID, aUBLAddress.getCountry().getIdentificationCodeValue());
    return ret;
  }

  @Nonnull
  protected static AmountType sumAmountType(@Nullable final AmountType aA, @Nullable final AmountType aB) {
    if(aA == null && aB == null) return new AmountType (new BigDecimal(0));
    if (aA == null) return aB;
    if (aB == null) return aA;

    return new AmountType(aA.getValue().add(aB.getValue()));
  }

  @Nonnull
  protected static List<AmountType> sumAmountTypeList(@Nullable final List<AmountType> aA) {
    if (aA == null) return null;
    var sum = aA.stream().reduce(new AmountType(new BigDecimal(0)), AbstractToCIID16BConverter::sumAmountType);
    return List.of(sum);
  }

  @Nullable
  protected static TradePartyType convertParty(@Nullable final PartyType aUBLParty) {
    if (aUBLParty == null)
      return null;

    final TradePartyType aTPT = new TradePartyType();
    for (final var aUBLPartyID : aUBLParty.getPartyIdentification())
      ifNotNull(aTPT::addID, convertID(aUBLPartyID.getID()));

    if (aUBLParty.hasPartyNameEntries())
      ifNotEmpty(aTPT::setName, aUBLParty.getPartyNameAtIndex(0).getNameValue());

    if (aUBLParty.hasPartyLegalEntityEntries()) {
      final PartyLegalEntityType aUBLLegalEntity = aUBLParty.getPartyLegalEntity().get(0);

      final LegalOrganizationType aLOT = new LegalOrganizationType();
      ifNotEmpty(aLOT::setTradingBusinessName, aUBLLegalEntity.getRegistrationNameValue());
      ifNotNull(aLOT::setID, convertID(aUBLLegalEntity.getCompanyID()));
      ifNotNull(aLOT::setPostalTradeAddress, convertAddress(aUBLLegalEntity.getRegistrationAddress()));

      if (StringHelper.hasNoText(aTPT.getNameValue())) {
        // Fill mandatory field
        ifNotEmpty(aTPT::setName, aUBLLegalEntity.getRegistrationNameValue());
      }

      aTPT.setSpecifiedLegalOrganization(aLOT);
    }

    ifNotNull(aTPT::setPostalTradeAddress, convertAddress(aUBLParty.getPostalAddress()));

    if (aUBLParty.getEndpointID() != null) {
      final UniversalCommunicationType aUCT = new UniversalCommunicationType();
      ifNotNull(aUCT::setURIID, convertID(aUBLParty.getEndpointID()));
      aTPT.addURIUniversalCommunication(aUCT);
    }

    if (aUBLParty.hasPartyTaxSchemeEntries()) {
      final PartyTaxSchemeType aUBLPartyTaxScheme = aUBLParty.getPartyTaxSchemeAtIndex(0);
      if (aUBLPartyTaxScheme.getCompanyIDValue() != null) {
        final TaxRegistrationType aTaxReg = new TaxRegistrationType();
        final IDType aID = convertID(aUBLPartyTaxScheme.getCompanyID());
        if (aUBLPartyTaxScheme.getTaxScheme() != null) {
          // MUST use "VA" scheme
          ifNotEmpty(aID::setSchemeID, _getAsVAIfNecessary(aUBLPartyTaxScheme.getTaxScheme().getIDValue()));
        }
        aTaxReg.setID(aID);
        aTPT.addSpecifiedTaxRegistration(aTaxReg);
      }
    }

    if (!aUBLParty.hasNoPersonEntries())
      for (var aPerson : aUBLParty.getPerson())
        aTPT.addDefinedTradeContact(convertPersonType(aPerson));
    if(aUBLParty.getContact() != null) {
      aTPT.addDefinedTradeContact(convertPersonType(aUBLParty.getContact()));
    }

    return aTPT;
  }

  @Nonnull
  protected static TradeContactType convertPersonType(final PersonType aPerson) {
    TradeContactType aTCT;
    if(aPerson.getContact() != null)
      aTCT = convertPersonType(aPerson.getContact());
    else aTCT = new TradeContactType();

    aTCT.setDepartmentName(aPerson.getOrganizationDepartmentValue());
    var personName = valueOrEmpty(aPerson.getFirstNameValue()) + valueOrEmpty(aPerson.getMiddleNameValue()) + valueOrEmpty(aPerson.getFamilyNameValue()) + valueOrEmpty(aPerson.getNameSuffixValue());
    aTCT.setPersonName(personName);
    return aTCT;
  }

  @Nonnull
  protected static TradeContactType convertPersonType(final ContactType aContact) {
    var aTCT = new TradeContactType();
    aTCT.setPersonName(aContact.getNameValue());
    aTCT.setTelephoneUniversalCommunication(convertUniversalCommunication(aContact.getTelephoneValue(), CommunicationType.Telephone));
    aTCT.setEmailURIUniversalCommunication(convertUniversalCommunication(aContact.getElectronicMailValue(), CommunicationType.Email));
    return aTCT;
  }

  protected enum CommunicationType {
    Telephone,
    Email
  }

  @Nullable
  protected static UniversalCommunicationType convertUniversalCommunication(final String value, CommunicationType eType) {
    if (value == null){
      return null;
    }

    var aUCT = new UniversalCommunicationType();
    switch ( eType) {
      case Email:
        aUCT.setURIID(value);
        break;
      case Telephone:
        aUCT.setCompleteNumber(value);
        break;
    }
    return aUCT;
  }

  @Nonnull
  protected static ReferencedDocumentType convertAdditionalReferencedDocument(@Nonnull final DocumentReferenceType aUBLDocRef) {
    final ReferencedDocumentType aURDT = new ReferencedDocumentType();

    ifNotEmpty(aURDT::setIssuerAssignedID, aUBLDocRef.getIDValue());

    // Add DocumentTypeCode where possible
    if (isValidDocumentReferenceTypeCode(aUBLDocRef.getDocumentTypeCodeValue()))
      aURDT.setTypeCode(aUBLDocRef.getDocumentTypeCodeValue());
    else
      aURDT.setTypeCode("916");

    if (aUBLDocRef.getIssueDate() != null) {
      final FormattedDateTimeType aFIDT = new FormattedDateTimeType();
      aFIDT.setDateTimeString(createFormattedDateValue(aUBLDocRef.getIssueDateValueLocal()));
      aURDT.setFormattedIssueDateTime(aFIDT);
    }

    for (final var aUBLDocDesc : aUBLDocRef.getDocumentDescription()) {
      final TextType aText = new TextType();
      ifNotEmpty(aText::setValue, aUBLDocDesc.getValue());
      ifNotEmpty(aText::setLanguageID, aUBLDocDesc.getLanguageID());
      ifNotEmpty(aText::setLanguageLocaleID, aUBLDocDesc.getLanguageLocaleID());
      aURDT.addName(aText);
    }

    final AttachmentType aUBLAttachment = aUBLDocRef.getAttachment();
    if (aUBLAttachment != null) {
      // External Reference and Embedded Document Binary Object should be
      // mutually exclusive
      if (aUBLAttachment.getExternalReference() != null && aUBLAttachment.getExternalReference().getURI() != null) {
        ifNotEmpty(aURDT::setURIID, aUBLAttachment.getExternalReference().getURI().getValue());
      }

      if (aUBLAttachment.getEmbeddedDocumentBinaryObject() != null) {
        final BinaryObjectType aBOT = new BinaryObjectType();
        ifNotEmpty(aBOT::setMimeCode, aUBLAttachment.getEmbeddedDocumentBinaryObject().getMimeCode());
        ifNotNull(aBOT::setValue, aUBLAttachment.getEmbeddedDocumentBinaryObject().getValue());
        aURDT.addAttachmentBinaryObject(aBOT);
      }
    }
    return aURDT;
  }

  @Nullable
  protected static HeaderTradeDeliveryType createApplicableHeaderTradeDelivery(@Nullable final DeliveryType aUBLDelivery) {
    // Object is mandatory
    final HeaderTradeDeliveryType ret = new HeaderTradeDeliveryType();

    if (aUBLDelivery != null) {
      final LocationType aUBLLocation = aUBLDelivery.getDeliveryLocation();
      if (aUBLLocation != null) {
        final TradePartyType aTPTHT = new TradePartyType();
        ifNotNull(aTPTHT::addID, convertID(aUBLLocation.getID()));
        ifNotNull(aTPTHT::setPostalTradeAddress, convertAddress(aUBLLocation.getAddress()));
        ret.setShipToTradeParty(aTPTHT);
      }

      if (aUBLDelivery.getActualDeliveryDate() != null) {
        final SupplyChainEventType aSCET = new SupplyChainEventType();
        aSCET.setOccurrenceDateTime(convertDate(aUBLDelivery.getActualDeliveryDate().getValueLocal()));
        ret.setActualDeliverySupplyChainEvent(aSCET);
      }
    }
    return ret;
  }

  @Nonnull
  protected static TradeTaxType convertApplicableTradeTax(@Nonnull final TaxSubtotalType aUBLTaxSubtotal) {
    final TaxCategoryType aUBLTaxCategory = aUBLTaxSubtotal.getTaxCategory();
    final TaxSchemeType aUBLTaxScheme = aUBLTaxCategory.getTaxScheme();

    final TradeTaxType ret = new TradeTaxType();
    if (aUBLTaxScheme != null)
      ifNotEmpty(ret::setTypeCode, aUBLTaxScheme.getIDValue());
    ifNotEmpty(ret::setCategoryCode, aUBLTaxCategory.getIDValue());
    ifNotNull(ret::addCalculatedAmount, convertAmount(aUBLTaxSubtotal.getTaxAmount()));
    ifNotEmpty(ret::setCategoryCode, aUBLTaxCategory.getIDValue());
    ifNotNull(ret::addBasisAmount, convertAmount(aUBLTaxSubtotal.getTaxableAmount()));
    ifNotNull(ret::setRateApplicablePercent, aUBLTaxCategory.getPercentValue());
    if (aUBLTaxCategory.hasTaxExemptionReasonEntries())
      ifNotEmpty(ret::setExemptionReason, aUBLTaxCategory.getTaxExemptionReasonAtIndex(0).getValue());
    ifNotEmpty(ret::setExemptionReasonCode, aUBLTaxCategory.getTaxExemptionReasonCodeValue());
    return ret;
  }

  @Nonnull
  protected static TradeAllowanceChargeType convertSpecifiedTradeAllowanceCharge(@Nonnull final AllowanceChargeType aUBLAllowanceCharge) {
    final TradeAllowanceChargeType ret = new TradeAllowanceChargeType();

    final IndicatorType aITDC = new IndicatorType();
    aITDC.setIndicator(Boolean.valueOf(aUBLAllowanceCharge.getChargeIndicator().isValue()));
    ret.setChargeIndicator(aITDC);

    ret.addActualAmount(convertAmount(aUBLAllowanceCharge.getAmount()));
    ifNotEmpty(ret::setReasonCode, aUBLAllowanceCharge.getAllowanceChargeReasonCodeValue());
    if (aUBLAllowanceCharge.hasAllowanceChargeReasonEntries())
      ret.setReason(aUBLAllowanceCharge.getAllowanceChargeReason().get(0).getValue());
    ifNotNull(ret::setCalculationPercent, aUBLAllowanceCharge.getMultiplierFactorNumericValue());
    ifNotNull(ret::setBasisAmount, aUBLAllowanceCharge.getBaseAmountValue());

    if (aUBLAllowanceCharge.hasTaxCategoryEntries()) {
      final TaxCategoryType aUBLTaxCategory = aUBLAllowanceCharge.getTaxCategoryAtIndex(0);
      final TaxSchemeType aUBLTaxSchene = aUBLTaxCategory.getTaxScheme();

      final TradeTaxType aTradeTax = new TradeTaxType();
      if (aUBLTaxSchene != null)
        ifNotEmpty(aTradeTax::setTypeCode, aUBLTaxSchene.getIDValue());
      ifNotEmpty(aTradeTax::setCategoryCode, aUBLTaxCategory.getIDValue());
      ifNotNull(aTradeTax::setRateApplicablePercent, aUBLTaxCategory.getPercentValue());
      ret.addCategoryTradeTax(aTradeTax);
    }

    return ret;
  }

  @Nonnull
  protected static TradePaymentTermsType convertSpecifiedTradePaymentTerms(@Nonnull final PaymentTermsType aUBLPaymenTerms,
                                                                           @Nullable final PaymentMeansType aUBLPaymentMeans) {
    final TradePaymentTermsType ret = new TradePaymentTermsType();
    for (final var aNote : aUBLPaymenTerms.getNote())
      ret.addDescription(convertText(aNote.getValue()));

    if (aUBLPaymentMeans != null && aUBLPaymentMeans.getPaymentDueDate() != null)
      ret.setDueDateDateTime(convertDate(aUBLPaymentMeans.getPaymentDueDate().getValueLocal()));
    return ret;
  }

  @Nonnull
  protected static TradeSettlementHeaderMonetarySummationType createSpecifiedTradeSettlementHeaderMonetarySummation(@Nullable final MonetaryTotalType aUBLMonetaryTotal,
                                                                                                                    @Nullable final TaxTotalType aUBLTaxTotal) {
    final TradeSettlementHeaderMonetarySummationType ret = new TradeSettlementHeaderMonetarySummationType();
    if (aUBLMonetaryTotal != null) {
      ifNotNull(ret::addLineTotalAmount, convertAmount(aUBLMonetaryTotal.getLineExtensionAmount()));
      ifNotNull(ret::addChargeTotalAmount, convertAmount(aUBLMonetaryTotal.getChargeTotalAmount()));
      ifNotNull(ret::addAllowanceTotalAmount, convertAmount(aUBLMonetaryTotal.getAllowanceTotalAmount()));
      ifNotNull(ret::addTaxBasisTotalAmount, convertAmount(aUBLMonetaryTotal.getTaxExclusiveAmount()));
    }

    if (aUBLTaxTotal != null) {
      // Currency ID is required here
      ifNotNull(ret::addTaxTotalAmount, convertAmount(aUBLTaxTotal.getTaxAmount(), true));
    }

    if (aUBLMonetaryTotal != null) {
      ifNotNull(ret::addRoundingAmount, convertAmount(aUBLMonetaryTotal.getPayableRoundingAmount()));
      ifNotNull(ret::addGrandTotalAmount, convertAmount(aUBLMonetaryTotal.getTaxInclusiveAmount()));
      ifNotNull(ret::addTotalPrepaidAmount, convertAmount(aUBLMonetaryTotal.getPrepaidAmount()));
      ifNotNull(ret::addDuePayableAmount, convertAmount(aUBLMonetaryTotal.getPayableAmount()));
    }

    return ret;
  }
}
