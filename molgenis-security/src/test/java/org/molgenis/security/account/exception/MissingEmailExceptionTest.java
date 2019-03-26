package org.molgenis.security.account.exception;

import org.molgenis.i18n.test.exception.ExceptionMessageTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class MissingEmailExceptionTest extends ExceptionMessageTest {

  @BeforeMethod
  public void setUp() {
    messageSource.addMolgenisNamespaces("security");
  }

  @Test(dataProvider = "languageMessageProvider")
  @Override
  public void testGetLocalizedMessage(String lang, String message) {
    ExceptionMessageTest.assertExceptionMessageEquals(
        new MissingEmailException("username"), lang, message);
  }

  @DataProvider(name = "languageMessageProvider")
  @Override
  public Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {"en", "User 'username' is missing required email address."},
      {"nl", "Gebruiker 'username' mist het verplichte email adres."}
    };
  }
}
