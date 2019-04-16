package org.molgenis.api.permissions.exceptions;

import org.molgenis.i18n.test.exception.ExceptionMessageTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class MissingUserOrRoleExceptionTest extends ExceptionMessageTest {
  @BeforeMethod
  public void setUp() {
    messageSource.addMolgenisNamespaces("api-permissions");
  }

  @Test(dataProvider = "languageMessageProvider")
  @Override
  public void testGetLocalizedMessage(String lang, String message) {
    ExceptionMessageTest.assertExceptionMessageEquals(
        new MissingUserOrRoleException(), lang, message);
  }

  @DataProvider(name = "languageMessageProvider")
  @Override
  public Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {"en", "Please provide a 'user' or 'role' for this operation."}
    };
  }
}
