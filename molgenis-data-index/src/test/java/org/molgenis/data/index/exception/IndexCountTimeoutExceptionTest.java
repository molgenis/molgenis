package org.molgenis.data.index.exception;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.util.exception.CodedRuntimeException;
import org.molgenis.util.exception.ExceptionMessageTest;

class IndexCountTimeoutExceptionTest extends ExceptionMessageTest {
  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("index");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    ExceptionMessageTest.assertExceptionMessageEquals(
        new IndexCountTimeoutException(List.of("index"), 20000L), lang, message);
  }

  @Test
  void testGetMessage() {
    CodedRuntimeException ex = new IndexCountTimeoutException(List.of("index"), 20000L);
    assertEquals("indices:index, millis:20000", ex.getMessage());
  }

  public static Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {"en", "Timeout counting docs in index 'index' after 20,000ms."},
      new Object[] {"nl", "Timeout bij het tellen van documenten in index 'index' na 20.000ms."}
    };
  }
}
