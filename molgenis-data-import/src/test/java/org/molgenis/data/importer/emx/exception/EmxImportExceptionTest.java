package org.molgenis.data.importer.emx.exception;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.util.exception.ExceptionMessageTest;

class EmxImportExceptionTest extends ExceptionMessageTest {
  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("data-import");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    Exception cause = mock(Exception.class);
    when(cause.getLocalizedMessage()).thenReturn("panic: stuff went wrong here!");
    assertExceptionMessageEquals(new EmxImportException(cause, "entities", 29), lang, message);
  }

  static Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {
        "en", "Import failed: panic: stuff went wrong here! (sheet: 'entities', row 29)"
      },
      {"nl", "Inladen gefaald: panic: stuff went wrong here! (werkblad: 'entities', rij 29)"}
    };
  }
}
