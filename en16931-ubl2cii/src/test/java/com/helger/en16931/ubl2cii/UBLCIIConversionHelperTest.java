package com.helger.en16931.ubl2cii;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import com.helger.commons.error.list.ErrorList;
import com.helger.commons.io.file.FileHelper;

import un.unece.uncefact.data.standard.crossindustryinvoice._100.CrossIndustryInvoiceType;

/**
 * Test class for class {@link UBLCIIConversionHelper}.
 *
 * @author Philip Helger
 */
public final class UBLCIIConversionHelperTest
{
  @Test
  public void testConvertAndValidateAllAutoDetect () throws IOException
  {
    for (final File aFile : MockSettings.getAllTestFilesUBL21Invoice ())
      try (InputStream aIS = FileHelper.getInputStream (aFile))
      {
        final ErrorList aErrorList = new ErrorList ();
        final CrossIndustryInvoiceType aCII = UBLCIIConversionHelper.convertUBL21AutoDetectToCIID16B (aIS, aErrorList);
        assertNotNull (aCII);
      }

    for (final File aFile : MockSettings.getAllTestFilesUBL21CreditNote ())
      try (InputStream aIS = FileHelper.getInputStream (aFile))
      {
        final ErrorList aErrorList = new ErrorList ();
        final CrossIndustryInvoiceType aCII = UBLCIIConversionHelper.convertUBL21AutoDetectToCIID16B (aIS, aErrorList);
        assertNotNull (aCII);
      }
  }
}
