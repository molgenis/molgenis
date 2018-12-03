package org.molgenis.navigator.copy.exception;

import static org.mockito.Mockito.mock;

import org.molgenis.i18n.test.exception.ExceptionMessageTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class UnknownCopyExceptionTest extends ExceptionMessageTest {

  @BeforeMethod
  public void setUp() {
    messageSource.addMolgenisNamespaces("navigator");
  }

  @Test(dataProvider = "languageMessageProvider")
  @Override
  public void testGetLocalizedMessage(String lang, String message) {
    assertExceptionMessageEquals(
        new UnknownCopyFailedException(mock(Throwable.class)), lang, message);
  }

  @DataProvider(name = "languageMessageProvider")
  @Override
  public Object[][] languageMessageProvider() {
    Object[] enParams = {"en", "An error occurred during copying."};
    Object[] nlParams = {"nl", "Er is een fout opgetreden tijdens het kopiëren."};
    return new Object[][] {enParams, nlParams};
  }
}
