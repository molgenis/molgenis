package org.molgenis.data.importer.emx.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.util.exception.ExceptionMessageTest;

class NoFilenameExtensionExceptionTest extends ExceptionMessageTest {
  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("data-import");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  @SuppressWarnings("java:S5786") // Method has protected visibility in super class
  protected void testGetLocalizedMessage(String lang, String message) {
    assertExceptionMessageEquals(new NoFilenameExtensionException("data"), lang, message);
  }

  static Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {"en", "File name 'data' has no extension."},
      {"nl", "Filenaam 'data' heeft geen extensie."}
    };
  }
}
