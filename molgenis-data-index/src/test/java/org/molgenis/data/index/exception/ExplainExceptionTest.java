package org.molgenis.data.index.exception;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.util.exception.ExceptionMessageTest;

class ExplainExceptionTest extends ExceptionMessageTest {
  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("index");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    ExceptionMessageTest.assertExceptionMessageEquals(
        new ExplainException("index", "id", "query"), lang, message);
  }

  @Test
  void testGetMessage() {
    var ex = new ExplainException(new IOException("IO"), "index", "id", "query");
    assertEquals("indices:index, id:id, query:query", ex.getMessage());
    assertEquals("query", ex.getQuery());
  }

  @Test
  void testGetQuery() {
    var ex = new ExplainException("index", "id", "query");
    assertEquals("query", ex.getQuery());
  }

  public static Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {"en", "Error explaining doc with id 'id' in index 'index' for query 'query'."},
      new Object[] {
        "nl", "Fout bij het uitleggen van resultaat met id 'id' in index 'index' bij query 'query'."
      }
    };
  }
}
