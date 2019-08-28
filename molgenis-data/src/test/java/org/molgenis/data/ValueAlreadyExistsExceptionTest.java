package org.molgenis.data;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;

import org.molgenis.util.exception.ExceptionMessageTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ValueAlreadyExistsExceptionTest extends ExceptionMessageTest {
  @BeforeMethod
  public void setUp() {
    messageSource.addMolgenisNamespaces("data");
  }

  @Test(dataProvider = "languageMessageProvider")
  @Override
  public void testGetLocalizedMessage(String lang, String message) {
    ExceptionMessageTest.assertExceptionMessageEquals(
        new ValueAlreadyExistsException(
            "MyEntityType", "myAttributeName", "myValue", mock(Throwable.class)),
        lang,
        message);
  }

  @Test
  public void testGetMessage() {
    ValueAlreadyExistsException ex =
        new ValueAlreadyExistsException(
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
        "Value 'myValue' for unique attribute 'myAttributeName' from entity 'MyEntityType' already exists."
      },
      new Object[] {
        "nl",
        "Waarde 'myValue' voor uniek attribuut 'myAttributeName' van entiteit 'MyEntityType' bestaat al."
      }
    };
  }
}
