package org.molgenis.api.convert;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cz.jirutka.rsql.parser.RSQLParserException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.util.exception.ExceptionMessageTest;

class QueryParseExceptionTest extends ExceptionMessageTest {
  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("api");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    RSQLParserException ex = mock(RSQLParserException.class);
    when(ex.getLocalizedMessage()).thenReturn("RSQL MESSAGE");
    Throwable cause = new IllegalAccessException("Query cannot be null");
    when(ex.getCause()).thenReturn(cause);
    assertExceptionMessageEquals(new QueryParseException(ex), lang, message);
  }

  static Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {"en", "An error occurred while parsing the RSQL: 'RSQL MESSAGE'."}
    };
  }
}
