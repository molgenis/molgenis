package org.molgenis.data.support;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.util.exception.ExceptionMessageTest;

class TemplateExpressionMathExceptionTest extends ExceptionMessageTest {
  static Object[][] languageMessageProvider() {
    Object[] enParams = {
      "en",
      "molgenis-math requires three parameters"
    };
    Object[] nlParams = {
      "nl",
      "molgenis-math heeft drie parameters nodig"
    };
    return new Object[][] {enParams, nlParams};
  }

  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("data");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    String errorMessage = "error: "+ message;
    assertExceptionMessageEquals(
        new TemplateExpressionMathException(message), lang, errorMessage);
  }
}
