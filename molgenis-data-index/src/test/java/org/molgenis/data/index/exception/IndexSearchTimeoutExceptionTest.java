package org.molgenis.data.index.exception;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.util.exception.CodedRuntimeException;
import org.molgenis.util.exception.ExceptionMessageTest;

class IndexSearchTimeoutExceptionTest extends ExceptionMessageTest {
  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("index");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    ExceptionMessageTest.assertExceptionMessageEquals(
        new IndexSearchTimeoutException(List.of("index"), "query", 20000), lang, message);
  }

  @Test
  void testGetMessage() {
    CodedRuntimeException ex = new IndexSearchTimeoutException(List.of("index"), "query", 20000);
    assertEquals("indices:index, query:query, millis:20000", ex.getMessage());
  }

  public static Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {
        "en", "Timeout searching docs in index 'index' with query 'query' after 20,000ms."
      },
      new Object[] {
        "nl", "Timeout bij het zoeken in index 'index' met zoekvraag 'query' na 20.000ms."
      }
    };
  }
}
