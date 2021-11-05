package org.molgenis.data.index.exception;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.util.exception.ExceptionMessageTest;

class DocumentIndexExceptionTest extends ExceptionMessageTest {
  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("index");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    ExceptionMessageTest.assertExceptionMessageEquals(
        new DocumentIndexException("index", "id"), lang, message);
  }

  @Test
  void testGetMessage() {
    var ex = new DocumentIndexException("index", "id", new IOException("error"));
    assertEquals("indices:index, id:id", ex.getMessage());
    assertEquals("id", ex.getDocumentId());
  }

  public static Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {"en", "Error indexing document with id 'id' in index 'index'."},
      new Object[] {"nl", "Fout bij het indexeren van document met id 'id' in index 'index'."}
    };
  }
}
