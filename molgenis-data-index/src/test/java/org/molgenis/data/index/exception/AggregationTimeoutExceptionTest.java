package org.molgenis.data.index.exception;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.util.exception.CodedRuntimeException;
import org.molgenis.util.exception.ExceptionMessageTest;

class AggregationTimeoutExceptionTest extends ExceptionMessageTest {
  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("index");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    ExceptionMessageTest.assertExceptionMessageEquals(
        new AggregationTimeoutException(List.of("index1", "index2"), 20000L), lang, message);
  }

  @Test
  void testGetMessage() {
    CodedRuntimeException ex = new AggregationTimeoutException(List.of("index1", "index2"), 20000L);
    assertEquals("indices:index1, index2, millis:20000", ex.getMessage());
  }

  public static Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {"en", "Timeout aggregating docs in indices 'index1, index2' after 20,000ms."},
      new Object[] {
        "nl", "Timeout bij het aggregeren van documenten in indices 'index1, index2' na 20.000ms."
      }
    };
  }
}
