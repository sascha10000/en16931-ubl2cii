/**
 *
 */
package com.myproj.ublcii.utility;

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

import oasis.names.specification.ubl.schema.xsd.invoice_21.InvoiceType;
import un.unece.uncefact.data.standard.crossindustryinvoice._100.CrossIndustryInvoiceType;

/**
 * @author Vartika Gupta
 * @author Philip Helger
 */
@Immutable
public final class UBLCIIConversionUtility
{
  private UBLCIIConversionUtility ()
  {}

  @Nonnull
  public static ESuccess convertUBLtoCII (@Nonnull @WillNotClose final InputStream aIS,
                                          @Nonnull @WillClose final OutputStream aOS,
                                          @Nonnull final ErrorList aErrorList)
  {
    // Read UBL 2.1
    final InvoiceType aUBLInvoice = UBL21Marshaller.invoice ().setCollectErrors (aErrorList).read (aIS);
    if (aUBLInvoice == null)
      return ESuccess.FAILURE;

    // Main conversion
    final CrossIndustryInvoiceType aCrossIndustryInvoice = new UBLToCII16BConverter ().convertUBLToCII (aUBLInvoice, new ErrorList ());

    // Write CII D16B XML
    return new CIID16BCrossIndustryInvoiceTypeMarshaller ().setFormattedOutput (true)
                                                           .setCollectErrors (aErrorList)
                                                           .write (aCrossIndustryInvoice, aOS);
  }
}
