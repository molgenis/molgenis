package org.molgenis.data.importer.emx.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.util.exception.ExceptionMessageTest;

class InvalidValueExceptionTest extends ExceptionMessageTest {
  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("data-import");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    assertExceptionMessageEquals(
        new InvalidValueException("value", "attr", "true,false", "attributes", 2), lang, message);
  }

  static Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {
        "en",
        "Illegal 'attr' value 'value'. Allowed values are true,false. (sheet: 'attributes', row 2)"
      },
      {
        "nl",
        "Illegale 'attr' waarde 'value'. Toegestane waardes zijn true,false. (werkblad: 'attributes', rij 2)"
      }
    };
  }
}
