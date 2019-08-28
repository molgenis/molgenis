package org.molgenis.data;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.*;

import org.molgenis.util.exception.ExceptionMessageTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ValueReferencedExceptionTest extends ExceptionMessageTest {
  @BeforeMethod
  public void setUp() {
    messageSource.addMolgenisNamespaces("data");
  }

  @Test(dataProvider = "languageMessageProvider")
  @Override
  public void testGetLocalizedMessage(String lang, String message) {
    ExceptionMessageTest.assertExceptionMessageEquals(
        new ValueReferencedException(
            "MyEntityType", "myAttributeName", "myValue", mock(Throwable.class)),
        lang,
        message);
  }

  @Test
  public void testGetMessage() {
    ValueReferencedException ex =
        new ValueReferencedException(
            "MyEntityType", "myAttributeName", "myValue", mock(Throwable.class));
    assertEquals(
        ex.getMessage(), "entityTypeId:MyEntityType attributeName:myAttributeName value:myValue");
  }

  @DataProvider(name = "languageMessageProvider")
  @Override
  public Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {
        "en",
        "Value 'myValue' for attribute 'myAttributeName' is referenced by entity 'MyEntityType'."
      },
      new Object[] {
        "nl",
        "Waarde 'myValue' voor attribuut 'myAttributeName' wordt gerefereerd door entiteit 'MyEntityType'."
      }
    };
  }
}
