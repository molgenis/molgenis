package org.molgenis.data.importer.emx.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.util.exception.ExceptionMessageTest;

class UnknownEntityValueExceptionTest extends ExceptionMessageTest {
  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("data-import");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    assertExceptionMessageEquals(
        new UnknownEntityValueException("value", "refEntity", "attr", "attributes", 9),
        lang,
        message);
  }

  static Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {
        "en", "Unknown 'refEntity' value 'value' for entity 'attr'. (sheet: 'attributes', row 9)"
      },
      {
        "nl",
        "Onbekende 'refEntity' waarde 'value' for entiteit 'attr'. (werkblad: 'attributes', rij 9)"
      }
    };
  }
}
