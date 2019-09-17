package org.molgenis.data.support;

import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.util.exception.ExceptionMessageTest;

public class TemplateExpressionSyntaxExceptionTest extends ExceptionMessageTest {
  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("data");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    String expression = "{\"platetem\":\"value\"}";
    Exception exception = mock(Exception.class);
    assertExceptionMessageEquals(
        new TemplateExpressionSyntaxException(expression, exception), lang, message);
  }

  static Object[][] languageMessageProvider() {
    Object[] enParams = {"en", "Expression '{\"platetem\":\"value\"}' syntax invalid."};
    Object[] nlParams = {"nl", "Expressie '{\"platetem\":\"value\"}' syntax ongeldig."};
    return new Object[][] {enParams, nlParams};
  }
}
