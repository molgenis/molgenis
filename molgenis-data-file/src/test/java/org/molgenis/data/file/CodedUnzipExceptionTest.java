package org.molgenis.data.file;

import org.molgenis.i18n.test.exception.ExceptionMessageTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class CodedUnzipExceptionTest extends ExceptionMessageTest {
  @BeforeMethod
  public void setUp() {
    messageSource.addMolgenisNamespaces("file");
  }

  @Test(dataProvider = "languageMessageProvider")
  @Override
  public void testGetLocalizedMessage(String lang, String message) {
    assertExceptionMessageEquals(new CodedUnzipException("name"), lang, message);
  }

  @DataProvider(name = "languageMessageProvider")
  @Override
  public Object[][] languageMessageProvider() {
    Object[] enParams = {"en", "Error unzipping file 'name'."};
    return new Object[][] {enParams};
  }
}
