package org.molgenis.data;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;

import org.molgenis.util.exception.ExceptionMessageTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ExistingNullValueExceptionTest extends ExceptionMessageTest {
  @BeforeMethod
  public void setUp() {
    messageSource.addMolgenisNamespaces("data");
  }

  @Test(dataProvider = "languageMessageProvider")
  @Override
  public void testGetLocalizedMessage(String lang, String message) {
    ExceptionMessageTest.assertExceptionMessageEquals(
        new ExistingNullValueException("MyEntityType", "myAttributeName", mock(Throwable.class)),
        lang,
        message);
  }

  @Test
  public void testGetMessage() {
    ExistingNullValueException ex =
        new ExistingNullValueException("MyEntityType", "myAttributeName", mock(Throwable.class));
    assertEquals(ex.getMessage(), "entityTypeId:MyEntityType attributeName:myAttributeName");
  }

  @DataProvider(name = "languageMessageProvider")
  @Override
  public Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {
        "en", "The attribute 'myAttributeName' of entity 'MyEntityType' contains null values."
      },
      new Object[] {
        "nl", "Het attribuut 'myAttributeName' van entiteit 'MyEntityType' bevat null waarden."
      }
    };
  }
}
