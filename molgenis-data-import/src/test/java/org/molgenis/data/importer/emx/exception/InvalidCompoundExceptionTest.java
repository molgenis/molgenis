package org.molgenis.data.importer.emx.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.util.exception.ExceptionMessageTest;

class InvalidCompoundExceptionTest extends ExceptionMessageTest {
  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("data-import");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    assertExceptionMessageEquals(
        new InvalidCompoundException("attr", "attrName", "entityTypeId", "attributes", 3),
        lang,
        message);
  }

  static Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {
        "en",
        "partOfAttribute 'attr' of attribute attrName of entity entityTypeId must refer to a attribute of type 'compound'. (sheet: 'attributes', row 3)"
      },
      {
        "nl",
        "partOfattribuut 'attr' van attribuut 'attrName' van entiteit 'entityTypeId' moet refereren naar een attribuut van type 'compound'. (werkblad: 'attributes', rij 3)"
      }
    };
  }
}
