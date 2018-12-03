package org.molgenis.navigator.copy.exception;

import org.molgenis.i18n.test.exception.ExceptionMessageTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class RecursiveCopyExceptionTest extends ExceptionMessageTest {

  @BeforeMethod
  public void setUp() {
    messageSource.addMolgenisNamespaces("navigator");
  }

  @Test(dataProvider = "languageMessageProvider")
  @Override
  public void testGetLocalizedMessage(String lang, String message) {
    assertExceptionMessageEquals(new RecursiveCopyException(), lang, message);
  }

  @DataProvider(name = "languageMessageProvider")
  @Override
  public Object[][] languageMessageProvider() {
    Object[] enParams = {"en", "You can't copy a package into itself."};
    Object[] nlParams = {"nl", "Je kunt een map niet naar zichzelf kopiëren."};
    return new Object[][] {enParams, nlParams};
  }
}
