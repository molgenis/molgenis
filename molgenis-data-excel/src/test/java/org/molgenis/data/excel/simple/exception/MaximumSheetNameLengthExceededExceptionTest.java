package org.molgenis.data.excel.simple.exception;

import org.molgenis.i18n.test.exception.ExceptionMessageTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class MaximumSheetNameLengthExceededExceptionTest extends ExceptionMessageTest {

  @BeforeMethod
  public void setUp() {
    messageSource.addMolgenisNamespaces("excel");
  }

  @Test(dataProvider = "languageMessageProvider")
  @Override
  public void testGetLocalizedMessage(String lang, String message) {
    ExceptionMessageTest.assertExceptionMessageEquals(
        new MaximumSheetNameLengthExceededException("Thisisasheennamethatexceedsthelimit"),
        lang,
        message);
  }

  @DataProvider(name = "languageMessageProvider")
  @Override
  public Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {
        "en",
        "The entitytype name 'Thisisasheennamethatexceedsthelimit' is too long to be used as a sheetname in xslx."
      },
      {
        "nl",
        "De entiteitsoort naam  'Thisisasheennamethatexceedsthelimit' is te lang om als tabblad naam in xslx gebruikt te worden."
      }
    };
  }
}
