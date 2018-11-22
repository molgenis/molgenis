package org.molgenis.data.excel.xlsx.exception;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

import org.molgenis.i18n.test.exception.ExceptionMessageTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class XlsxWriterExceptionTest  extends ExceptionMessageTest {

  @BeforeMethod
  public void setUp() {
    messageSource.addMolgenisNamespaces("excel");
  }

  @Test(dataProvider = "languageMessageProvider")
  @Override
  public void testGetLocalizedMessage(String lang, String message) {
    Exception cause = mock(Exception.class);
    when(cause.getLocalizedMessage()).thenReturn("panic: stuff went wrong here!");

    ExceptionMessageTest.assertExceptionMessageEquals(
        new XlsxWriterException(cause), lang, message);
  }

  @DataProvider(name = "languageMessageProvider")
  @Override
  public Object[][] languageMessageProvider() {
    return new Object[][] {
        new Object[] {"en", "An error occured while writing XLSX:  panic: stuff went wrong here!"},
        {"nl", "Er is een fout opgetreden bij het schrijven van XLSX:  panic: stuff went wrong here!"}
    };
  }
}
