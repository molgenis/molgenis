package org.molgenis.data.importer.emx.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.util.exception.ExceptionMessageTest;

class AttributeNameCaseMismatchExceptionTest extends ExceptionMessageTest {
  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("data-import");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    assertExceptionMessageEquals(
        new AttributeNameCaseMismatchException("Test", "test", "attributes"), lang, message);
  }

  static Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {
        "en", "Unsupported attribute metadata: 'Test', did you mean 'test'? (sheet: 'attributes')"
      },
      {
        "nl",
        "Niet ondersteunde attribuut metadata: 'Test', bedoelde u misschien 'test'? (werkblad: 'attributes')"
      }
    };
  }
}
