package org.molgenis.data.importer.emx.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.util.exception.ExceptionMessageTest;

class MissingCompoundExceptionTest extends ExceptionMessageTest {
  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("data-import");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    assertExceptionMessageEquals(
        new MissingCompoundException(
            "partOfAttribute", "attributeName", "entityTypeId", "attributes", 3),
        lang,
        message);
  }

  static Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {
        "en",
        "partOfAttribute 'partOfAttribute' of attribute 'attributeName' of entity 'entityTypeId' must refer to an existing compound attribute. (sheet: 'attributes', row 3)"
      },
      {
        "nl",
        "partOfAttribute 'partOfAttribute' van attribuut 'attributeName' of entiteit 'entityTypeId' moet verwijzen naar een bestaand compound attribuut. (werkblad: 'attributes', rij 3)"
      }
    };
  }
}
