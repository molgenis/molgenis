package org.molgenis.api.permissions.exceptions;

import static org.testng.Assert.assertEquals;

import org.molgenis.data.security.exception.InvalidTypeIdException;
import org.molgenis.i18n.CodedRuntimeException;
import org.molgenis.i18n.test.exception.ExceptionMessageTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class InvalidTypeIdExceptionTest extends ExceptionMessageTest {
  @BeforeMethod
  public void setUp() {
    messageSource.addMolgenisNamespaces("api-permissions");
  }

  @Test(dataProvider = "languageMessageProvider")
  @Override
  public void testGetLocalizedMessage(String lang, String message) {
    ExceptionMessageTest.assertExceptionMessageEquals(
        new InvalidTypeIdException("type"), lang, message);
  }

  @Test
  public void testGetMessage() {
    CodedRuntimeException ex = new InvalidTypeIdException("type");
    assertEquals(ex.getMessage(), "typeId:type");
  }

  @DataProvider(name = "languageMessageProvider")
  @Override
  public Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {
        "en",
        "Unknown type identifier 'type', if you attempt to use a entitytype identifier please prefix with 'entity-'."
      }
    };
  }
}
