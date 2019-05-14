package org.molgenis.data.security.exception;

import static org.testng.Assert.*;

import org.molgenis.i18n.CodedRuntimeException;
import org.molgenis.i18n.test.exception.ExceptionMessageTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class SuperUserPermissionsExceptionTest extends ExceptionMessageTest {

  @BeforeMethod
  public void setUp() {
    messageSource.addMolgenisNamespaces("data-security");
  }

  @Test(dataProvider = "languageMessageProvider")
  @Override
  public void testGetLocalizedMessage(String lang, String message) {
    ExceptionMessageTest.assertExceptionMessageEquals(
        new SuperUserPermissionsException("admin"), lang, message);
  }

  @Test
  public void testGetMessage() {
    CodedRuntimeException ex = new SuperUserPermissionsException("admin");
    assertEquals(ex.getMessage(), "name:admin");
  }

  @DataProvider(name = "languageMessageProvider")
  @Override
  public Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {
        "en",
        "Getting or setting permissions for 'admin' is not allowed since it has role 'superuser' which has permissions on everything."
      }
    };
  }
}
