package org.molgenis.data.importer.emx.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.util.exception.ExceptionMessageTest;

class MissingEmxAttributeAttributeValueExceptionTest extends ExceptionMessageTest {
  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("data-import");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    assertExceptionMessageEquals(
        new MissingEmxAttributeAttributeValueException(
            "enum_options", "attr", "entityName", "attributes", 2),
        lang,
        message);
  }

  static Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {
        "en",
        "Missing 'enum_options' for attribute 'attr' of entity 'entityName'. (sheet: 'attributes', row 2)"
      },
      {
        "nl",
        "Onbrekende kolom 'enum_options' voor attribuut 'attr' of entiteit 'entityName'. (werkblad: 'attributes', rij 2)"
      }
    };
  }
}
