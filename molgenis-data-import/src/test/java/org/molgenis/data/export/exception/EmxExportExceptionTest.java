package org.molgenis.data.export.exception;

import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.util.exception.ExceptionMessageTest;

class EmxExportExceptionTest extends ExceptionMessageTest {
  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("data-import");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    Exception e = mock(Exception.class);
    ExceptionMessageTest.assertExceptionMessageEquals(new EmxExportException(e), lang, message);
  }

  static Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {"en", "An error occured while downloading your selection."},
      {"nl", "Er is een fout opgetreden bij het downloaden van uw selectie."}
    };
  }
}
