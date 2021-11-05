package org.molgenis.data.index.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.util.exception.CodedRuntimeException;
import org.molgenis.util.exception.ExceptionMessageTest;

class IndexAlreadyExistsExceptionTest extends ExceptionMessageTest {
  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("index");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    ExceptionMessageTest.assertExceptionMessageEquals(
        new IndexAlreadyExistsException("index"), lang, message);
  }

  @Test
  void testGetMessage() {
    CodedRuntimeException ex = new IndexAlreadyExistsException("index");
    assertEquals("indices:index", ex.getMessage());
  }

  public static Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {"en", "Index 'index' already exists."},
      new Object[] {"nl", "Index 'index' bestaat al."}
    };
  }
}
