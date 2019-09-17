package org.molgenis.data.importer.emx.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.util.exception.ExceptionMessageTest;

class SelfReferencingCompoundExceptionTest extends ExceptionMessageTest {
  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("data-import");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    assertExceptionMessageEquals(
        new SelfReferencingCompoundException(
            "partOfAttribute", "attr", "entityTypeId", "attributes", 4),
        lang,
        message);
  }

  static Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {
        "en",
        "partOfAttribute 'partOfAttribute' of attribute 'attr' of entity 'entityTypeId' cannot refer to itself. (sheet: 'attributes', row 4)"
      },
      {
        "nl",
        "partOfAttribute 'partOfAttribute' van attribuut 'attr' of entiteit 'entityTypeId' kan niet naar zichzelf verwijzen. (werkblad: 'attributes', rij 4)"
      }
    };
  }
}
