package org.molgenis.data.importer.emx.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.util.exception.ExceptionMessageTest;

class InvalidRangeExceptionTest extends ExceptionMessageTest {
  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("data-import");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    assertExceptionMessageEquals(
        new InvalidRangeException("one", "entityId", "rangeMax", "attributeName", "entities", 8),
        lang,
        message);
  }

  static Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {
        "en",
        "Invalid value 'one' for column 'rangeMax' for attribute 'attributeName' of entity 'entityId', should be a long(sheet: 'entities', row 8)"
      },
      {
        "nl",
        "Incorrecte waarde 'one' voor kolom 'rangeMax' voor attribuut 'attributeName' van entiteit 'entityId', waarde moet van type long zijn. (werkblad: 'entities', rij 8)"
      }
    };
  }
}
