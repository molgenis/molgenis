package org.molgenis.data.index.exception;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.util.exception.CodedRuntimeException;
import org.molgenis.util.exception.ExceptionMessageTest;

class AggregationExceptionTest extends ExceptionMessageTest {
  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("index");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    ExceptionMessageTest.assertExceptionMessageEquals(
        new AggregationException(List.of("index1"), "Lots of details"), lang, message);
  }

  @ParameterizedTest
  @MethodSource("languageMessageProviderPlural")
  protected void testGetLocalizedMessagePlural(String lang, String message) {
    ExceptionMessageTest.assertExceptionMessageEquals(
        new AggregationException(List.of("index1", "index2"), "Lots of details"), lang, message);
  }

  @Test
  void testGetMessage() {
    CodedRuntimeException ex =
        new AggregationException(List.of("index1", "index2"), "Lots of details");
    assertEquals("indices:index1, index2, detailMessage:Lots of details", ex.getMessage());
    assertEquals("IX01A", ex.getErrorCode());
  }

  @Test
  void testGetMessageNoDetails() {
    CodedRuntimeException ex =
        new AggregationException(List.of("index1", "index2"), new IOException("IO"));
    assertEquals("indices:index1, index2, detailMessage:null", ex.getMessage());
    assertEquals("IX01", ex.getErrorCode());
  }

  public static Object[][] languageMessageProviderPlural() {
    return new Object[][] {
      new Object[] {
        "en", "Error aggregating docs in indices 'index1, index2'. Details: Lots of details."
      },
      new Object[] {
        "nl",
        "Fout bij het aggregeren van documenten in indices 'index1, index2'. Details: Lots of details."
      }
    };
  }

  public static Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {"en", "Error aggregating docs in index 'index1'. Details: Lots of details."},
      new Object[] {
        "nl", "Fout bij het aggregeren van documenten in index 'index1'. Details: Lots of details."
      }
    };
  }
}
