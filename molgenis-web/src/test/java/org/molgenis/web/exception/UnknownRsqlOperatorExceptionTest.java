package org.molgenis.web.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.util.exception.CodedRuntimeException;
import org.molgenis.util.exception.ExceptionMessageTest;

class UnknownRsqlOperatorExceptionTest extends ExceptionMessageTest {

  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("web");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    ExceptionMessageTest.assertExceptionMessageEquals(
        new UnknownRsqlOperatorException("=foo="), lang, message);
  }

  @Test
  void testGetMessage() {
    CodedRuntimeException ex = new UnknownRsqlOperatorException("=foo=");
    assertEquals("operator:=foo=", ex.getMessage());
  }

  public static Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {"en", "Unknown RSQL query operator [=foo=]"},
      new Object[] {"nl", "Onbekende RSQL query operator [=foo=]"}
    };
  }
}
