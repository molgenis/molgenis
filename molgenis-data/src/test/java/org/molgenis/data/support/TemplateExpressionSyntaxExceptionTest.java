package org.molgenis.data.support;

import static org.mockito.Mockito.mock;

import org.molgenis.i18n.test.exception.ExceptionMessageTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TemplateExpressionSyntaxExceptionTest extends ExceptionMessageTest {
  @BeforeMethod
  public void setUp() {
    messageSource.addMolgenisNamespaces("data");
  }

  @Test(dataProvider = "languageMessageProvider")
  @Override
  public void testGetLocalizedMessage(String lang, String message) {
    String expression = "{\"platetem\":\"value\"}";
    Exception exception = mock(Exception.class);
    assertExceptionMessageEquals(
        new TemplateExpressionSyntaxException(expression, exception), lang, message);
  }

  @DataProvider(name = "languageMessageProvider")
  @Override
  public Object[][] languageMessageProvider() {
    Object[] enParams = {"en", "Expression '{\"platetem\":\"value\"}' syntax invalid."};
    Object[] nlParams = {"nl", "Expressie '{\"platetem\":\"value\"}' syntax ongeldig."};
    return new Object[][] {enParams, nlParams};
  }
}
