package org.molgenis.api.convert;

import org.molgenis.util.exception.ExceptionMessageTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class SelectionParseExceptionTest extends ExceptionMessageTest {
  @BeforeMethod
  public void setUp() {
    messageSource.addMolgenisNamespaces("api");
  }

  @Test(dataProvider = "languageMessageProvider")
  @Override
  public void testGetLocalizedMessage(String lang, String message) {
    Token token = new Token();
    token.beginLine = 1;
    token.beginColumn = 10;
    token.image = "IMAGE";
    Token next = new Token();
    next.beginLine = 1;
    next.beginColumn = 15;
    next.image = "NEXT";
    token.next = next;
    ParseException parseException = new ParseException(token, new int[][] {}, new String[] {});
    assertExceptionMessageEquals(new SelectionParseException(parseException), lang, message);
  }

  @DataProvider(name = "languageMessageProvider")
  @Override
  public Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {
        "en", "Unexpected token 'IMAGE' while parsing the selection, line '1' position '10'."
      }
    };
  }
}
