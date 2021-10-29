package org.molgenis.data.index.exception;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.util.exception.CodedRuntimeException;
import org.molgenis.util.exception.ExceptionMessageTest;

class IndexDeleteExceptionTest extends ExceptionMessageTest {
  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("index");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    ExceptionMessageTest.assertExceptionMessageEquals(
        new IndexDeleteException(List.of("index")), lang, message);
  }

  @Test
  void testGetMessage() {
    CodedRuntimeException ex = new IndexDeleteException(List.of("index"));
    assertEquals("indices:index", ex.getMessage());
  }

  public static Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {"en", "Error deleting index 'index'."},
      new Object[] {"nl", "Fout bij het verwijderen van index 'index'."}
    };
  }
}
