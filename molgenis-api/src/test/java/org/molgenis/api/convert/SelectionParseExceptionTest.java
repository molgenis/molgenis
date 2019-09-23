package org.molgenis.api.convert;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.util.exception.ExceptionMessageTest;

class SelectionParseExceptionTest extends ExceptionMessageTest {
  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("api");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
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

  static Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {
        "en", "Unexpected token 'IMAGE' while parsing the selection, line '1' position '10'."
      }
    };
  }
}
