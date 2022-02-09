package org.molgenis.data.support.exceptions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.util.exception.ExceptionMessageTest;

class TemplateExpressionMathNotEnoughParametersExceptionTest extends ExceptionMessageTest {
  static Object[][] languageMessageProvider() {
    Object[] enParams = {"en", "molgenis-math Requires three parameters."};
    Object[] nlParams = {"nl", "molgenis-math Heeft drie parameters nodig."};
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
    assertExceptionMessageEquals(
        new TemplateExpressionMathNotEnoughParametersException(), lang, message);
  }
}
