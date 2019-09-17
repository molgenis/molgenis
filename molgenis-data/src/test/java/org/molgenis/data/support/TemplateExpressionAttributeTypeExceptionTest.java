package org.molgenis.data.support;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.AttributeType.COMPOUND;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.util.exception.ExceptionMessageTest;

class TemplateExpressionAttributeTypeExceptionTest extends ExceptionMessageTest {
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
    Attribute attribute = when(mock(Attribute.class).getDataType()).thenReturn(COMPOUND).getMock();
    assertExceptionMessageEquals(
        new TemplateExpressionAttributeTypeException(expression, tag, attribute), lang, message);
  }

  static Object[][] languageMessageProvider() {
    Object[] enParams = {
      "en",
      "Expression 'hello {{name}}' with tag 'name' refers to attribute with invalid type 'COMPOUND'."
    };
    Object[] nlParams = {
      "nl",
      "Expressie 'hello {{name}}' met tag 'name' verwijst naar attibuut met ongeldig type 'COMPOUND'."
    };
    return new Object[][] {enParams, nlParams};
  }
}
