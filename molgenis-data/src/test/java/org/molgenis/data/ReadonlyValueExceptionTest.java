package org.molgenis.data;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;

import org.molgenis.util.exception.ExceptionMessageTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ReadonlyValueExceptionTest extends ExceptionMessageTest {
  @BeforeMethod
  public void setUp() {
    messageSource.addMolgenisNamespaces("data");
  }

  @Test(dataProvider = "languageMessageProvider")
  @Override
  public void testGetLocalizedMessage(String lang, String message) {
    ExceptionMessageTest.assertExceptionMessageEquals(
        new ReadonlyValueException(
            "MyEntityType", "myAttributeName", "myEntityId", mock(Throwable.class)),
        lang,
        message);
  }

  @Test
  public void testGetMessage() {
    ReadonlyValueException ex =
        new ReadonlyValueException(
            "MyEntityType", "myAttributeName", "myEntityId", mock(Throwable.class));
    assertEquals(
        ex.getMessage(),
        "entityTypeId:MyEntityType attributeName:myAttributeName entityId:myEntityId");
  }

  @DataProvider(name = "languageMessageProvider")
  @Override
  public Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {
        "en",
        "Updating read-only attribute 'myAttributeName' of type 'MyEntityType' with id 'myEntityId' is not allowed."
      },
      new Object[] {
        "nl",
        "Updaten van alleen-lezen attribuut 'myAttributeName' van type 'MyEntityType' met id 'myEntityId' is niet toegestaan."
      }
    };
  }
}
