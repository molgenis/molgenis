package org.molgenis.data;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;

import org.molgenis.util.exception.ExceptionMessageTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class UnknownValueReferenceExceptionTest extends ExceptionMessageTest {
  @BeforeMethod
  public void setUp() {
    messageSource.addMolgenisNamespaces("data");
  }

  @Test(dataProvider = "languageMessageProvider")
  @Override
  public void testGetLocalizedMessage(String lang, String message) {
    ExceptionMessageTest.assertExceptionMessageEquals(
        new UnknownValueReferenceException(
            "MyEntityType", "myAttributeName", "myValue", mock(Throwable.class)),
        lang,
        message);
  }

  @Test
  public void testGetMessage() {
    UnknownValueReferenceException ex =
        new UnknownValueReferenceException(
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
        "Unknown xref value 'myValue' for attribute 'myAttributeName' of entity 'MyEntityType'."
      },
      new Object[] {
        "nl",
        "Onbekende xref waarde 'myValue' voor attribuut 'myAttributeName' van entiteit 'MyEntityType'."
      }
    };
  }
}
