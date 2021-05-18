package org.molgenis.web.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.util.exception.CodedRuntimeException;
import org.molgenis.util.exception.ExceptionMessageTest;

class UnsupportedRsqlOperatorExceptionTest extends ExceptionMessageTest {

  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("web");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    ExceptionMessageTest.assertExceptionMessageEquals(
        new UnsupportedRsqlOperatorException("=dismax="), lang, message);
  }

  @Test
  void testGetMessage() {
    CodedRuntimeException ex = new UnsupportedRsqlOperatorException("=dismax=");
    assertEquals("operator:=dismax=", ex.getMessage());
  }

  public static Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {"en", "Unsupported RSQL query operator [=dismax=]"},
      new Object[] {"nl", "Niet-ondersteunde RSQL query operator [=dismax=]"}
    };
  }
}
