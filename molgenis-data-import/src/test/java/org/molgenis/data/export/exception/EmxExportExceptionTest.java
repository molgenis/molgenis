package org.molgenis.data.export.exception;

import static org.mockito.Mockito.mock;

import org.molgenis.i18n.test.exception.ExceptionMessageTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class EmxExportExceptionTest extends ExceptionMessageTest {
  @BeforeMethod
  public void setUp() {
    messageSource.addMolgenisNamespaces("data-import");
  }

  @Test(dataProvider = "languageMessageProvider")
  @Override
  public void testGetLocalizedMessage(String lang, String message) {
    Exception e = mock(Exception.class);
    ExceptionMessageTest.assertExceptionMessageEquals(new EmxExportException(e), lang, message);
  }

  @DataProvider(name = "languageMessageProvider")
  @Override
  public Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {"en", "An error occured while downloading your selection."},
      {"nl", "Er is een fout opgetreden bij het downloaden van uw selectie."}
    };
  }
}
