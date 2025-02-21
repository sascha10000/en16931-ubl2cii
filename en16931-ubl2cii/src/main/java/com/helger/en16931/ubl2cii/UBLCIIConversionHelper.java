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

import java.io.InputStream;
import java.io.OutputStream;

import javax.annotation.Nonnull;
import javax.annotation.WillClose;
import javax.annotation.WillNotClose;
import javax.annotation.concurrent.Immutable;

import com.helger.cii.d16b.CIID16BCrossIndustryInvoiceTypeMarshaller;
import com.helger.commons.error.list.ErrorList;
import com.helger.commons.state.ESuccess;
import com.helger.ubl21.UBL21Marshaller;

import oasis.names.specification.ubl.schema.xsd.creditnote_21.CreditNoteType;
import oasis.names.specification.ubl.schema.xsd.invoice_21.InvoiceType;
import un.unece.uncefact.data.standard.crossindustryinvoice._100.CrossIndustryInvoiceType;

/**
 * @author Vartika Gupta
 * @author Philip Helger
 */
@Immutable
public final class UBLCIIConversionHelper
{
  private UBLCIIConversionHelper ()
  {}

  @Nonnull
  public static ESuccess convertUBL21InvoicetoCIID16B (@Nonnull @WillNotClose final InputStream aIS,
                                                       @Nonnull @WillClose final OutputStream aOS,
                                                       @Nonnull final ErrorList aErrorList)
  {
    // Read UBL 2.1
    final InvoiceType aUBLInvoice = UBL21Marshaller.invoice ().setCollectErrors (aErrorList).read (aIS);
    if (aUBLInvoice == null)
      return ESuccess.FAILURE;

    // Main conversion
    final CrossIndustryInvoiceType aCrossIndustryInvoice = UBL21InvoiceToCII16BConverter.convertToCrossIndustryInvoice (aUBLInvoice,
                                                                                                                        aErrorList);
    if (aCrossIndustryInvoice == null)
      return ESuccess.FAILURE;

    // Write CII D16B XML
    return new CIID16BCrossIndustryInvoiceTypeMarshaller ().setFormattedOutput (true)
                                                           .setCollectErrors (aErrorList)
                                                           .write (aCrossIndustryInvoice, aOS);
  }

  @Nonnull
  public static ESuccess convertUBL21CreditNotetoCIID16B (@Nonnull @WillNotClose final InputStream aIS,
                                                          @Nonnull @WillClose final OutputStream aOS,
                                                          @Nonnull final ErrorList aErrorList)
  {
    // Read UBL 2.1
    final CreditNoteType aUBLCreditNote = UBL21Marshaller.creditNote ().setCollectErrors (aErrorList).read (aIS);
    if (aUBLCreditNote == null)
      return ESuccess.FAILURE;

    // Main conversion
    final CrossIndustryInvoiceType aCrossIndustryInvoice = UBL21CreditNoteToCII16BConverter.convertToCrossIndustryInvoice (aUBLCreditNote,
                                                                                                                           aErrorList);
    if (aCrossIndustryInvoice == null)
      return ESuccess.FAILURE;

    // Write CII D16B XML
    return new CIID16BCrossIndustryInvoiceTypeMarshaller ().setFormattedOutput (true)
                                                           .setCollectErrors (aErrorList)
                                                           .write (aCrossIndustryInvoice, aOS);
  }
}
