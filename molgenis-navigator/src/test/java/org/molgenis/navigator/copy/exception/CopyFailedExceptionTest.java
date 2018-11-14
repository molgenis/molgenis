package org.molgenis.navigator.copy.exception;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.molgenis.i18n.test.exception.ExceptionMessageTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class CopyFailedExceptionTest extends ExceptionMessageTest {
  @BeforeMethod
  public void setUp() {
    messageSource.addMolgenisNamespaces("navigator");
  }

  @Test(dataProvider = "languageMessageProvider")
  @Override
  public void testGetLocalizedMessage(String lang, String message) {
    Exception cause = mock(Exception.class);
    when(cause.getLocalizedMessage()).thenReturn("ERROR!");
    ExceptionMessageTest.assertExceptionMessageEquals(
        new CopyFailedException(cause), lang, message);
  }

  @DataProvider(name = "languageMessageProvider")
  @Override
  public Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {"en", "Copy failed: ERROR!"},
      {"nl", "Kopiëren gefaald: ERROR!"}
    };
  }
}
