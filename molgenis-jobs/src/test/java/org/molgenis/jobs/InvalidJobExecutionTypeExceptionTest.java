package org.molgenis.jobs;

import org.molgenis.i18n.test.exception.ExceptionMessageTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class InvalidJobExecutionTypeExceptionTest extends ExceptionMessageTest {
  @BeforeMethod
  public void setUp() {
    messageSource.addMolgenisNamespaces("jobs");
  }

  @Test(dataProvider = "languageMessageProvider")
  @Override
  public void testGetLocalizedMessage(String lang, String message) {
    assertExceptionMessageEquals(
        new InvalidJobExecutionTypeException("MyEntityType"), lang, message);
  }

  @DataProvider(name = "languageMessageProvider")
  @Override
  public Object[][] languageMessageProvider() {
    Object[] enParams = {"en", "Invalid job execution type 'MyEntityType'."};
    Object[] nlParams = {"nl", "Ongeldig job execution type 'MyEntityType'."};
    return new Object[][] {enParams, nlParams};
  }
}
