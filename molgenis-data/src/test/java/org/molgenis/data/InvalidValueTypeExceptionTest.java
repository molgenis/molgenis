package org.molgenis.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.util.exception.ExceptionMessageTest;

class InvalidValueTypeExceptionTest extends ExceptionMessageTest {
  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("data");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    ExceptionMessageTest.assertExceptionMessageEquals(
        new InvalidValueTypeException("value", "type", mock(Throwable.class)), lang, message);
  }

  @Test
  void testGetMessage() {
    InvalidValueTypeException ex =
        new InvalidValueTypeException("value", "type", mock(Throwable.class));
    assertEquals(ex.getMessage(), "value:value type:type");
  }

  static Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {"en", "Value 'value' of this entity attribute is not of type 'type'."},
      new Object[] {"nl", "Waarde 'value' van dit entiteit attribuut is niet van type 'type'."}
    };
  }
}
