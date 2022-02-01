package org.molgenis.data.support.exceptions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.util.exception.ExceptionMessageTest;

class TemplateExpressionMathUnknownOperatorExceptionTest extends ExceptionMessageTest {
  static Object[][] languageMessageProvider() {
    Object[] enParams = {"en", "Unknown operator '@' for molgenis-math."};
    Object[] nlParams = {"nl", "Onbekende operator '@' voor molgenis-math."};
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
    String operator = "@";
    assertExceptionMessageEquals(
        new TemplateExpressionMathUnknownOperatorException(operator), lang, message);
  }
}
