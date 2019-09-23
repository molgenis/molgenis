package org.molgenis.data.importer.emx.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.util.exception.ExceptionMessageTest;

class NillableReferenceAggregatableExceptionTest extends ExceptionMessageTest {
  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("data-import");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    assertExceptionMessageEquals(
        new NillableReferenceAggregatableException(
            "entity", "attr", AttributeType.CATEGORICAL, "attributes", 3),
        lang,
        message);
  }

  static Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {
        "en",
        "Nullable aggregatable attribute 'attr' of entity 'entity' cannot be of type 'CATEGORICAL'. (sheet: 'attributes', row 3)"
      },
      {
        "nl",
        "Optioneel aggregeerbaar attribuut 'attr' van entiteit 'entity' kan niet van type 'CATEGORICAL' zijn. (werkblad: 'attributes', rij 3)"
      }
    };
  }
}
