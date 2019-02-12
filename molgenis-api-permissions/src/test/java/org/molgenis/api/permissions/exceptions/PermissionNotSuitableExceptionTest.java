package org.molgenis.api.permissions.exceptions;

import static org.testng.Assert.assertEquals;

import org.molgenis.i18n.CodedRuntimeException;
import org.molgenis.i18n.test.exception.ExceptionMessageTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class PermissionNotSuitableExceptionTest extends ExceptionMessageTest {
  @BeforeMethod
  public void setUp() {
    messageSource.addMolgenisNamespaces("api-permissions");
  }

  @Test(dataProvider = "languageMessageProvider")
  @Override
  public void testGetLocalizedMessage(String lang, String message) {
    ExceptionMessageTest.assertExceptionMessageEquals(
        new PermissionNotSuitableException("permission", "type"), lang, message);
  }

  @Test
  public void testGetMessage() {
    CodedRuntimeException ex = new PermissionNotSuitableException("permission", "type");
    assertEquals(ex.getMessage(), "permission:permission, typeId:type");
  }

  @DataProvider(name = "languageMessageProvider")
  @Override
  public Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {"en", "The permission 'permission' cannot be used for type 'type'."},
      {"nl", "De permissie 'permission' kan niet toegepast worden op type 'type'."}
    };
  }
}
