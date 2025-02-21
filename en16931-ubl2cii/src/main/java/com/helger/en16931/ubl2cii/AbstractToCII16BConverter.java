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

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.datetime.PDTFactory;
import com.helger.commons.math.MathHelper;
import com.helger.commons.string.StringHelper;

import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.AddressType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.PartyLegalEntityType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.PartyTaxSchemeType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.PartyType;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._100.LegalOrganizationType;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._100.TaxRegistrationType;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._100.TradeAddressType;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._100.TradePartyType;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._100.UniversalCommunicationType;
import un.unece.uncefact.data.standard.unqualifieddatatype._100.AmountType;
import un.unece.uncefact.data.standard.unqualifieddatatype._100.IDType;
import un.unece.uncefact.data.standard.unqualifieddatatype._100.TextType;

public abstract class AbstractToCII16BConverter
{
  private static final String CII_DATE_FORMAT = "102";

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

  @Nullable
  protected static String createFormattedDateValue (@Nullable final LocalDate aLocalDate)
  {
    if (aLocalDate == null)
      return null;

    final SimpleDateFormat aFormatter = new SimpleDateFormat ("yyyyMMdd");
    final Date aDate = PDTFactory.createDate (aLocalDate);
    return aFormatter.format (aDate);
  }

  @Nullable
  private static un.unece.uncefact.data.standard.unqualifieddatatype._100.DateTimeType.DateTimeString createDateTimeString (@Nullable final LocalDate aLocalDate)
  {
    if (aLocalDate == null)
      return null;

    final un.unece.uncefact.data.standard.unqualifieddatatype._100.DateTimeType.DateTimeString aret = new un.unece.uncefact.data.standard.unqualifieddatatype._100.DateTimeType.DateTimeString ();
    aret.setFormat (CII_DATE_FORMAT);
    aret.setValue (createFormattedDateValue (aLocalDate));
    return aret;
  }

  @Nullable
  protected static un.unece.uncefact.data.standard.unqualifieddatatype._100.DateTimeType convertDate (@Nullable final LocalDate aLocalDate)
  {
    if (aLocalDate == null)
      return null;

    final un.unece.uncefact.data.standard.unqualifieddatatype._100.DateTimeType ret = new un.unece.uncefact.data.standard.unqualifieddatatype._100.DateTimeType ();
    ret.setDateTimeString (createDateTimeString (aLocalDate));
    return ret;
  }

  @Nullable
  protected static TextType convertText (@Nullable final String sValue)
  {
    if (sValue == null)
      return null;

    final TextType ret = new TextType ();
    ret.setValue (sValue);
    return ret;
  }

  @Nullable
  protected static IDType convertID (@Nullable final com.helger.xsds.ccts.cct.schemamodule.IdentifierType aID)
  {
    if (aID == null)
      return null;

    final IDType ret = new IDType ();
    ifNotNull (ret::setSchemeID, aID.getSchemeID ());
    ifNotNull (ret::setValue, aID.getValue ());
    return ret;
  }

  @Nullable
  protected static AmountType convertAmount (@Nullable final com.helger.xsds.ccts.cct.schemamodule.AmountType aUBLAmount)
  {
    return convertAmount (aUBLAmount, false);
  }

  @Nullable
  protected static AmountType convertAmount (@Nullable final com.helger.xsds.ccts.cct.schemamodule.AmountType aUBLAmount,
                                             final boolean bWithCurrency)
  {
    if (aUBLAmount == null)
      return null;

    final AmountType ret = new AmountType ();
    if (bWithCurrency)
      ret.setCurrencyID (aUBLAmount.getCurrencyID ());
    ifNotNull (ret::setValue, MathHelper.getWithoutTrailingZeroes (aUBLAmount.getValue ()));
    return ret;
  }

  @Nullable
  protected static un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._100.NoteType convertNote (@Nullable final oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.NoteType aNote)
  {
    if (aNote == null || aNote.getValue () == null)
      return null;

    final un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._100.NoteType ret = new un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._100.NoteType ();
    final TextType aTT = new TextType ();
    aTT.setValue (aNote.getValue ());
    ret.addContent (aTT);
    return ret;
  }

  @Nullable
  protected static TradeAddressType convertAddress (@Nullable final AddressType aAddress)
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
      ret.addCountrySubDivisionName (convertText (aAddress.getCountrySubentity ().getValue ()));
    if (aAddress.getCountry () != null)
      ifNotEmpty (ret::setCountryID, aAddress.getCountry ().getIdentificationCodeValue ());

    return ret;
  }

  @Nullable
  protected static TradePartyType convertParty (@Nullable final PartyType aParty)
  {
    if (aParty == null)
      return null;

    final TradePartyType aTPT = new TradePartyType ();
    for (final var aPartyID : aParty.getPartyIdentification ())
      ifNotNull (aTPT::addID, convertID (aPartyID.getID ()));

    if (aParty.hasPartyNameEntries ())
      ifNotEmpty (aTPT::setName, aParty.getPartyNameAtIndex (0).getNameValue ());

    if (aParty.hasPartyLegalEntityEntries ())
    {
      final PartyLegalEntityType aLE = aParty.getPartyLegalEntity ().get (0);

      final LegalOrganizationType aLOT = new LegalOrganizationType ();
      ifNotEmpty (aLOT::setTradingBusinessName, aLE.getRegistrationNameValue ());
      ifNotNull (aLOT::setID, convertID (aLE.getCompanyID ()));
      ifNotNull (aLOT::setPostalTradeAddress, convertAddress (aLE.getRegistrationAddress ()));

      if (StringHelper.hasNoText (aTPT.getNameValue ()))
      {
        // Fill mandatory field
        ifNotEmpty (aTPT::setName, aLE.getRegistrationNameValue ());
      }

      aTPT.setSpecifiedLegalOrganization (aLOT);
    }

    ifNotNull (aTPT::setPostalTradeAddress, convertAddress (aParty.getPostalAddress ()));

    if (aParty.getEndpointID () != null)
    {
      final UniversalCommunicationType aUCT = new UniversalCommunicationType ();
      ifNotNull (aUCT::setURIID, convertID (aParty.getEndpointID ()));
      aTPT.addURIUniversalCommunication (aUCT);
    }

    if (aParty.hasPartyTaxSchemeEntries ())
    {
      final PartyTaxSchemeType aPTS = aParty.getPartyTaxSchemeAtIndex (0);
      if (aPTS.getCompanyIDValue () != null)
      {
        final TaxRegistrationType aTaxReg = new TaxRegistrationType ();
        final IDType aID = convertID (aPTS.getCompanyID ());
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
}
