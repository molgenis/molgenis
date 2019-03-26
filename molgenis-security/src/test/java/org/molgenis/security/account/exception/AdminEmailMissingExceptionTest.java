package org.molgenis.security.account.exception;

import static org.testng.Assert.*;

import org.molgenis.i18n.test.exception.ExceptionMessageTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class AdminEmailMissingExceptionTest extends ExceptionMessageTest {

  @BeforeMethod
  public void setUp() {
    messageSource.addMolgenisNamespaces("security");
  }

  @Test(dataProvider = "languageMessageProvider")
  @Override
  public void testGetLocalizedMessage(String lang, String message) {

    ExceptionMessageTest.assertExceptionMessageEquals(
        new AdminEmailMissingException(), lang, message);
  }

  @DataProvider(name = "languageMessageProvider")
  @Override
  public Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {"en", "Administrator account is missing required email address."},
      {"nl", "Administrator account mist het verplichte email adres."}
    };
  }
}
