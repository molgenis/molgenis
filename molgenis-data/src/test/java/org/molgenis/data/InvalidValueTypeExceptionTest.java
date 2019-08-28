package org.molgenis.data;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;

import org.molgenis.util.exception.ExceptionMessageTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class InvalidValueTypeExceptionTest extends ExceptionMessageTest {
  @BeforeMethod
  public void setUp() {
    messageSource.addMolgenisNamespaces("data");
  }

  @Test(dataProvider = "languageMessageProvider")
  @Override
  public void testGetLocalizedMessage(String lang, String message) {
    ExceptionMessageTest.assertExceptionMessageEquals(
        new InvalidValueTypeException("value", "type", mock(Throwable.class)), lang, message);
  }

  @Test
  public void testGetMessage() {
    InvalidValueTypeException ex =
        new InvalidValueTypeException("value", "type", mock(Throwable.class));
    assertEquals(ex.getMessage(), "value:value type:type");
  }

  @DataProvider(name = "languageMessageProvider")
  @Override
  public Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {"en", "Value 'value' of this entity attribute is not of type 'type'."},
      new Object[] {"nl", "Waarde 'value' van dit entiteit attribuut is niet van type 'type'."}
    };
  }
}
