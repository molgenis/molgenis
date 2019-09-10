package org.molgenis.api.convert;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cz.jirutka.rsql.parser.RSQLParserException;
import org.molgenis.util.exception.ExceptionMessageTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class QueryParseExceptionTest extends ExceptionMessageTest {
  @BeforeMethod
  public void setUp() {
    messageSource.addMolgenisNamespaces("api");
  }

  @Test(dataProvider = "languageMessageProvider")
  @Override
  public void testGetLocalizedMessage(String lang, String message) {
    RSQLParserException ex = mock(RSQLParserException.class);
    when(ex.getLocalizedMessage()).thenReturn("RSQL MESSAGE");
    Throwable cause = new IllegalAccessException("Query cannot be null");
    when(ex.getCause()).thenReturn(cause);
    assertExceptionMessageEquals(new QueryParseException(ex), lang, message);
  }

  @DataProvider(name = "languageMessageProvider")
  @Override
  public Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {"en", "An error occurred while parsing the RSQL: 'RSQL MESSAGE'."}
    };
  }
}
