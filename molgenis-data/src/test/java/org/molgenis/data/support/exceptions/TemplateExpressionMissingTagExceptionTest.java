package org.molgenis.data.support.exceptions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.util.exception.ExceptionMessageTest;

class TemplateExpressionMissingTagExceptionTest extends ExceptionMessageTest {
  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("data");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    String expression = "hello {{xref}}";
    String tag = "xref";
    assertExceptionMessageEquals(
        new TemplateExpressionMissingTagException(expression, tag), lang, message);
  }

  static Object[][] languageMessageProvider() {
    Object[] enParams = {
      "en", "Expression 'hello {{xref}}' with tag 'xref' is missing a reference tag."
    };
    Object[] nlParams = {
      "nl", "Expressie 'hello {{xref}}' met tag 'xref' mist een referentie tag."
    };
    return new Object[][] {enParams, nlParams};
  }
}
