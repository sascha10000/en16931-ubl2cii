package com.helger.en16931.ubl2cii;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import com.helger.commons.error.list.ErrorList;
import com.helger.commons.io.file.FileHelper;
import com.helger.commons.io.stream.NonBlockingByteArrayOutputStream;
import com.helger.commons.state.ESuccess;

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
        try (NonBlockingByteArrayOutputStream aBAOS = new NonBlockingByteArrayOutputStream ())
        {
          final ErrorList aErrorList = new ErrorList ();
          final ESuccess eSuccess = UBLCIIConversionHelper.convertUBL21AutoDetectToCIID16B (aIS, aBAOS, aErrorList);
          assertTrue (eSuccess.isSuccess ());
        }
      }

    for (final File aFile : MockSettings.getAllTestFilesUBL21CreditNote ())
      try (InputStream aIS = FileHelper.getInputStream (aFile))
      {
        try (NonBlockingByteArrayOutputStream aBAOS = new NonBlockingByteArrayOutputStream ())
        {
          final ErrorList aErrorList = new ErrorList ();
          final ESuccess eSuccess = UBLCIIConversionHelper.convertUBL21AutoDetectToCIID16B (aIS, aBAOS, aErrorList);
          assertTrue (eSuccess.isSuccess ());
        }
      }
  }
}
