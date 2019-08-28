package org.molgenis.data;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertNull;

import org.molgenis.util.exception.ExceptionMessageTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ValueLengthExceededExceptionTest extends ExceptionMessageTest {
  @BeforeMethod
  public void setUp() {
    messageSource.addMolgenisNamespaces("data");
  }

  @Test(dataProvider = "languageMessageProvider")
  @Override
  public void testGetLocalizedMessage(String lang, String message) {
    ExceptionMessageTest.assertExceptionMessageEquals(
        new ValueLengthExceededException(mock(Throwable.class)), lang, message);
  }

  @Test
  public void testGetMessage() {
    ValueLengthExceededException ex = new ValueLengthExceededException(mock(Throwable.class));
    assertNull(ex.getMessage());
  }

  @DataProvider(name = "languageMessageProvider")
  @Override
  public Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {"en", "One of the values being added is too long."},
      new Object[] {"nl", "Een van de toegevoegde waarden is te lang."}
    };
  }
}
