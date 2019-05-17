package org.molgenis.data.security.exception;

import static org.testng.Assert.assertEquals;

import org.molgenis.data.DataAlreadyExistsException;
import org.molgenis.i18n.test.exception.ExceptionMessageTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class AclAlreadyExistsExceptionTest extends ExceptionMessageTest {
  @BeforeMethod
  public void setUp() {
    messageSource.addMolgenisNamespaces("data-security");
  }

  @Test(dataProvider = "languageMessageProvider")
  @Override
  public void testGetLocalizedMessage(String lang, String message) {
    ExceptionMessageTest.assertExceptionMessageEquals(
        new AclAlreadyExistsException("type", "identifier"), lang, message);
  }

  @Test
  public void testGetMessage() {
    DataAlreadyExistsException ex = new AclAlreadyExistsException("type", "identifier");
    assertEquals(ex.getMessage(), "typeId:type, id:identifier");
  }

  @DataProvider(name = "languageMessageProvider")
  @Override
  public Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {"en", "An acl with id 'identifier' for type with id 'type' already exists."}
    };
  }
}
