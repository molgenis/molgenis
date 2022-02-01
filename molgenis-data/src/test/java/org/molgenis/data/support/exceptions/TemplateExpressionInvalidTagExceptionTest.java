package org.molgenis.data.support.exceptions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.util.exception.ExceptionMessageTest;

class TemplateExpressionInvalidTagExceptionTest extends ExceptionMessageTest {
  static Object[][] languageMessageProvider() {
    Object[] enParams = {"en", "Expression 'hello {{text.id}}' with tag 'id' is invalid."};
    Object[] nlParams = {"nl", "Expressie 'hello {{text.id}}' met tag 'id' is ongeldig."};
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
    String expression = "hello {{text.id}}";
    String tag = "id";
    assertExceptionMessageEquals(
        new TemplateExpressionInvalidTagException(expression, tag), lang, message);
  }
}
