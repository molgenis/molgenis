package org.molgenis.data.index.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.util.exception.CodedRuntimeException;
import org.molgenis.util.exception.ExceptionMessageTest;

class LargeBatchExceptionTest extends ExceptionMessageTest {
  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("index");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    ExceptionMessageTest.assertExceptionMessageEquals(
        new LargeBatchException(2000, 1000), lang, message);
  }

  @Test
  void testGetMessage() {
    CodedRuntimeException ex = new LargeBatchException(2000, 1000);
    assertEquals("size:2000, maxSize:1000", ex.getMessage());
  }

  public static Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {
        "en", "Batch size of 2,000 exceeds the maximum batch size of 1,000 for search queries."
      },
      new Object[] {
        "nl", "Batch size 2.000 is groter dan de maximum batch size 1.000 voor zoekvragen."
      }
    };
  }
}
