package org.molgenis.data.support;

import org.molgenis.i18n.test.exception.ExceptionMessageTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TemplateExpressionMissingTagExceptionTest extends ExceptionMessageTest {
  @BeforeMethod
  public void setUp() {
    messageSource.addMolgenisNamespaces("data");
  }

  @Test(dataProvider = "languageMessageProvider")
  @Override
  public void testGetLocalizedMessage(String lang, String message) {
    String expression = "hello {{xref}}";
    String tag = "xref";
    assertExceptionMessageEquals(
        new TemplateExpressionMissingTagException(expression, tag), lang, message);
  }

  @DataProvider(name = "languageMessageProvider")
  @Override
  public Object[][] languageMessageProvider() {
    Object[] enParams = {
      "en", "Expression 'hello {{xref}}' with tag 'xref' is missing a reference tag."
    };
    Object[] nlParams = {
      "nl", "Expressie 'hello {{xref}}' met tag 'xref' mist een referentie tag."
    };
    return new Object[][] {enParams, nlParams};
  }
}
