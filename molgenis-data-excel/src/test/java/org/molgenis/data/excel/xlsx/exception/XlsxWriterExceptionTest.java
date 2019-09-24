package org.molgenis.data.excel.xlsx.exception;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.util.exception.ExceptionMessageTest;

class XlsxWriterExceptionTest extends ExceptionMessageTest {

  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("excel");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    Exception cause = mock(Exception.class);
    when(cause.getLocalizedMessage()).thenReturn("panic: stuff went wrong here!");

    ExceptionMessageTest.assertExceptionMessageEquals(
        new XlsxWriterException(cause), lang, message);
  }

  static Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {"en", "An error occured while writing XLSX:  panic: stuff went wrong here!"},
      {"nl", "Er is een fout opgetreden bij het schrijven van XLSX:  panic: stuff went wrong here!"}
    };
  }
}
