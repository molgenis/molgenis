package org.molgenis.data.index.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.util.exception.CodedRuntimeException;
import org.molgenis.util.exception.ExceptionMessageTest;

class UnknownIndexExceptionTest extends ExceptionMessageTest {
  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("index");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    ExceptionMessageTest.assertExceptionMessageEquals(
        new UnknownIndexException("index"), lang, message);
  }

  @Test
  void testGetMessage() {
    CodedRuntimeException ex = new UnknownIndexException("index");
    assertEquals("indices:index", ex.getMessage());
  }

  public static Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {"en", "Unknown index 'index'."},
      new Object[] {"nl", "Onbekende index 'index'."}
    };
  }
}
