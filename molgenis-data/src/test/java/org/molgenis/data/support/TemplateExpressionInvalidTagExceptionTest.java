package org.molgenis.data.support;

import org.molgenis.i18n.test.exception.ExceptionMessageTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TemplateExpressionInvalidTagExceptionTest extends ExceptionMessageTest {
  @BeforeMethod
  public void setUp() {
    messageSource.addMolgenisNamespaces("data");
  }

  @Test(dataProvider = "languageMessageProvider")
  @Override
  public void testGetLocalizedMessage(String lang, String message) {
    String expression = "hello {{text.id}}";
    String tag = "id";
    assertExceptionMessageEquals(
        new TemplateExpressionInvalidTagException(expression, tag), lang, message);
  }

  @DataProvider(name = "languageMessageProvider")
  @Override
  public Object[][] languageMessageProvider() {
    Object[] enParams = {"en", "Expression 'hello {{text.id}}' with tag 'id' is invalid."};
    Object[] nlParams = {"nl", "Expressie 'hello {{text.id}}' met tag 'id' is ongeldig."};
    return new Object[][] {enParams, nlParams};
  }
}
