package org.molgenis.data;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.util.exception.ExceptionMessageTest;

class ValueLengthExceededExceptionTest extends ExceptionMessageTest {
  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("data");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    ExceptionMessageTest.assertExceptionMessageEquals(
        new ValueLengthExceededException(mock(Throwable.class)), lang, message);
  }

  @Test
  void testGetMessage() {
    ValueLengthExceededException ex = new ValueLengthExceededException(mock(Throwable.class));
    assertNull(ex.getMessage());
  }

  static Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {"en", "One of the values being added is too long."},
      new Object[] {"nl", "Een van de toegevoegde waarden is te lang."}
    };
  }
}
