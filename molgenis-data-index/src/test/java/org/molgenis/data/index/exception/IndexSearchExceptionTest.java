package org.molgenis.data.index.exception;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.util.exception.CodedRuntimeException;
import org.molgenis.util.exception.ExceptionMessageTest;

class IndexSearchExceptionTest extends ExceptionMessageTest {
  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("index");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    ExceptionMessageTest.assertExceptionMessageEquals(
        new IndexSearchException(List.of("index"), "query"), lang, message);
  }

  @Test
  void testGetMessage() {
    CodedRuntimeException ex = new IndexSearchException(List.of("index"), "query");
    assertEquals("indices:index, query:query", ex.getMessage());
  }

  public static Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {"en", "Error searching docs in index 'index' with query 'query'."},
      new Object[] {"nl", "Fout bij het zoeken in index 'index' met zoekvraag 'query'."}
    };
  }
}
