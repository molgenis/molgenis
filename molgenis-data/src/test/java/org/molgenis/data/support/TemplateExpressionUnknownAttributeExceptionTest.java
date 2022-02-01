package org.molgenis.data.support;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.data.support.exceptions.TemplateExpressionUnknownAttributeException;
import org.molgenis.util.exception.ExceptionMessageTest;

public class TemplateExpressionUnknownAttributeExceptionTest extends ExceptionMessageTest {
  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("data");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    String expression = "hello {{name}}";
    String tag = "name";
    assertExceptionMessageEquals(
        new TemplateExpressionUnknownAttributeException(expression, tag), lang, message);
  }

  static Object[][] languageMessageProvider() {
    Object[] enParams = {
      "en", "Expression 'hello {{name}}' with tag 'name' refers to unknown attribute."
    };
    Object[] nlParams = {
      "nl", "Expressie 'hello {{name}}' met tag 'name' verwijst naar onbekend attibuut."
    };
    return new Object[][] {enParams, nlParams};
  }
}
