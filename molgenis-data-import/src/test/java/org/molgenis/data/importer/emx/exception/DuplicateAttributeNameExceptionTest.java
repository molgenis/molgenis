package org.molgenis.data.importer.emx.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.util.exception.ExceptionMessageTest;

class DuplicateAttributeNameExceptionTest extends ExceptionMessageTest {
  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("data-import");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    assertExceptionMessageEquals(
        new DuplicateAttributeNameException("attr", "entity", "attributes", 20), lang, message);
  }

  static Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {
        "en",
        "Duplicate attribute name 'attr' for entity type 'entity'.(sheet: 'attributes', row 20)"
      },
      {
        "nl",
        "Dubbele attribuut name 'attr' for entiteit type 'entity'. (werkblad: 'attributes', rij 20)"
      }
    };
  }
}
