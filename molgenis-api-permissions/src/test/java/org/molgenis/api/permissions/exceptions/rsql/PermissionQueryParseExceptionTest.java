package org.molgenis.api.permissions.exceptions.rsql;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cz.jirutka.rsql.parser.RSQLParserException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.util.exception.ExceptionMessageTest;

class PermissionQueryParseExceptionTest extends ExceptionMessageTest {
  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("api-permissions");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    RSQLParserException cause = mock(RSQLParserException.class);
    when(cause.getLocalizedMessage()).thenReturn("bacause");
    ExceptionMessageTest.assertExceptionMessageEquals(
        new PermissionQueryParseException(cause), lang, message);
  }

  static Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {"en", "An error occured while parsing the RSQL query: 'bacause'."}
    };
  }
}
