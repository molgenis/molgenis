package org.molgenis.security.account.exception;

import static org.testng.Assert.*;

import org.molgenis.i18n.test.exception.ExceptionMessageTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class EmailAlreadyExistsExceptionTest extends ExceptionMessageTest {

  @BeforeMethod
  public void setUp() {
    messageSource.addMolgenisNamespaces("security");
  }

  @Test(dataProvider = "languageMessageProvider")
  @Override
  public void testGetLocalizedMessage(String lang, String message) {

    ExceptionMessageTest.assertExceptionMessageEquals(
        new EmailAlreadyExistsException("e.mail.com"), lang, message);
  }

  @DataProvider(name = "languageMessageProvider")
  @Override
  public Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {"en", "A user with email adress 'e.mail.com' is already registered."},
      {"nl", "Er bestaat al een gebruiker met email adres 'e.mail.com'."}
    };
  }
}
