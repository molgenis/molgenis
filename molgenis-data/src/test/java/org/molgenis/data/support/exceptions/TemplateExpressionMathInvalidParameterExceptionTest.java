package org.molgenis.data.support.exceptions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.util.exception.ExceptionMessageTest;

class TemplateExpressionMathInvalidParametersExceptionTest extends ExceptionMessageTest {
  static Object[][] languageMessageProvider() {
    Object[] enParams = {
      "en", "molgenis-math Cannot perform operations on null or non-numerical values."
    };
    Object[] nlParams = {
      "nl", "molgenis-math kan geen operaties doen op null of non-numerieke waarden."
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
    assertExceptionMessageEquals(
        new TemplateExpressionMathInvalidParameterException(), lang, message);
  }
}
