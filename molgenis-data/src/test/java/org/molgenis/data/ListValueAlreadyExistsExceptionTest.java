package org.molgenis.data;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;

import org.molgenis.util.exception.ExceptionMessageTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ListValueAlreadyExistsExceptionTest extends ExceptionMessageTest {
  @BeforeMethod
  public void setUp() {
    messageSource.addMolgenisNamespaces("data");
  }

  @Test(dataProvider = "languageMessageProvider")
  @Override
  public void testGetLocalizedMessage(String lang, String message) {
    ExceptionMessageTest.assertExceptionMessageEquals(
        new ListValueAlreadyExistsException(
            "MyEntityTypeId", "myAttributeName", "myEntityId", "myValue", mock(Throwable.class)),
        lang,
        message);
  }

  @Test
  public void testGetMessage() {
    ListValueAlreadyExistsException ex =
        new ListValueAlreadyExistsException(
            "MyEntityTypeId", "myAttributeName", "myEntityId", "myValue", mock(Throwable.class));
    assertEquals(
        ex.getMessage(),
        "entityTypeId:MyEntityTypeId attributeName:myAttributeName entityId:myEntityId value:myValue");
  }

  @DataProvider(name = "languageMessageProvider")
  @Override
  public Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {
        "en",
        "Duplicate list value 'myValue' for attribute 'myAttributeName' from entity 'MyEntityTypeId' with id 'myEntityId'."
      },
      new Object[] {
        "nl",
        "Gedupliceerde lijst waarde 'myValue' voor attribuut 'myAttributeName' van entities 'MyEntityTypeId' met id 'myEntityId'."
      }
    };
  }
}
