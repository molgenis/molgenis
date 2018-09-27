package org.molgenis.data.support;

import org.molgenis.i18n.test.exception.ExceptionMessageTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TemplateExpressionUnknownAttributeExceptionTest extends ExceptionMessageTest {
  @BeforeMethod
  public void setUp() {
    messageSource.addMolgenisNamespaces("data");
  }

  @Test(dataProvider = "languageMessageProvider")
  @Override
  public void testGetLocalizedMessage(String lang, String message) {
    String expression = "hello {{name}}";
    String tag = "name";
    assertExceptionMessageEquals(
        new TemplateExpressionUnknownAttributeException(expression, tag), lang, message);
  }

  @DataProvider(name = "languageMessageProvider")
  @Override
  public Object[][] languageMessageProvider() {
    Object[] enParams = {
      "en", "Expression 'hello {{name}}' with tag 'name' refers to unknown attribute."
    };
    Object[] nlParams = {
      "nl", "Expressie 'hello {{name}}' met tag 'name' verwijst naar onbekend attibuut."
    };
    return new Object[][] {enParams, nlParams};
  }
}
