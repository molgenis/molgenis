package org.molgenis.api.permissions.exceptions;

import org.molgenis.data.security.exception.InsufficientInheritancePermissionsException;
import org.molgenis.i18n.test.exception.ExceptionMessageTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class InsufficientInheritancePermissionsExceptionTest extends ExceptionMessageTest {
  @BeforeMethod
  public void setUp() {
    messageSource.addMolgenisNamespaces("api-permissions");
  }

  @Test(dataProvider = "languageMessageProvider")
  @Override
  public void testGetLocalizedMessage(String lang, String message) {
    ExceptionMessageTest.assertExceptionMessageEquals(
        new InsufficientInheritancePermissionsException(), lang, message);
  }

  @DataProvider(name = "languageMessageProvider")
  @Override
  public Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {
        "en",
        "Read permissions on 'sys_sec_Role' and 'sys_sec_RoleMembership' are required to be able to show inherited permissions."
      }
    };
  }
}
