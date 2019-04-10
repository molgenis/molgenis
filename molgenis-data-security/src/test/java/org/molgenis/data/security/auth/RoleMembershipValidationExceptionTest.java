package org.molgenis.data.security.auth;

import static org.mockito.Mockito.mock;

import org.molgenis.i18n.test.exception.ExceptionMessageTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class RoleMembershipValidationExceptionTest extends ExceptionMessageTest {
  @BeforeMethod
  public void setUp() {
    messageSource.addMolgenisNamespaces("data-security");
  }

  @Test(dataProvider = "languageMessageProvider")
  @Override
  public void testGetLocalizedMessage(String lang, String message) {
    assertExceptionMessageEquals(
        new RoleMembershipValidationException(mock(RoleMembership.class)), lang, message);
  }

  @DataProvider(name = "languageMessageProvider")
  @Override
  public Object[][] languageMessageProvider() {
    Object[] enParams = {"en", "User cannot have multiple roles within the same group."};
    Object[] nlParams = {"nl", "Gebruiker kan niet meerdere rollen binnen dezelfde groep hebben."};
    return new Object[][] {enParams, nlParams};
  }
}
