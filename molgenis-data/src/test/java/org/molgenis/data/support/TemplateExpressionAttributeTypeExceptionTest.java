package org.molgenis.data.support;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.AttributeType.COMPOUND;

import org.molgenis.data.meta.model.Attribute;
import org.molgenis.i18n.test.exception.ExceptionMessageTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TemplateExpressionAttributeTypeExceptionTest extends ExceptionMessageTest {
  @BeforeMethod
  public void setUp() {
    messageSource.addMolgenisNamespaces("data");
  }

  @Test(dataProvider = "languageMessageProvider")
  @Override
  public void testGetLocalizedMessage(String lang, String message) {
    String expression = "hello {{name}}";
    String tag = "name";
    Attribute attribute = when(mock(Attribute.class).getDataType()).thenReturn(COMPOUND).getMock();
    assertExceptionMessageEquals(
        new TemplateExpressionAttributeTypeException(expression, tag, attribute), lang, message);
  }

  @DataProvider(name = "languageMessageProvider")
  @Override
  public Object[][] languageMessageProvider() {
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
