package org.molgenis.api.permissions.exceptions.rsql;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

import cz.jirutka.rsql.parser.RSQLParserException;
import org.molgenis.i18n.test.exception.ExceptionMessageTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class PermissionQueryParseExceptionTest extends ExceptionMessageTest {
  @BeforeMethod
  public void setUp() {
    messageSource.addMolgenisNamespaces("api-permissions");
  }

  @Test(dataProvider = "languageMessageProvider")
  @Override
  public void testGetLocalizedMessage(String lang, String message) {
    RSQLParserException cause = mock(RSQLParserException.class);
    when(cause.getLocalizedMessage()).thenReturn("bacause");
    ExceptionMessageTest.assertExceptionMessageEquals(
        new PermissionQueryParseException(cause), lang, message);
  }

  @DataProvider(name = "languageMessageProvider")
  @Override
  public Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {"en", "An error occured while parsing the RSQL query: 'bacause'."}
    };
  }
}
