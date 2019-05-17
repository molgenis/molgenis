package org.molgenis.data.security.exception;

import static org.testng.Assert.assertEquals;

import org.molgenis.data.UnknownDataException;
import org.molgenis.i18n.test.exception.ExceptionMessageTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class UnknownTypeExceptionTest extends ExceptionMessageTest {
  @BeforeMethod
  public void setUp() {
    messageSource.addMolgenisNamespaces("data-security");
  }

  @Test(dataProvider = "languageMessageProvider")
  @Override
  public void testGetLocalizedMessage(String lang, String message) {
    ExceptionMessageTest.assertExceptionMessageEquals(
        new UnknownTypeException("type"), lang, message);
  }

  @Test
  public void testGetMessage() {
    UnknownDataException ex = new UnknownTypeException("type");
    assertEquals(ex.getMessage(), "typeId:type");
  }

  @DataProvider(name = "languageMessageProvider")
  @Override
  public Object[][] languageMessageProvider() {
    return new Object[][] {new Object[] {"en", "No type with id 'type' could be found."}};
  }
}
