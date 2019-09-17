package org.molgenis.data.security.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.data.UnknownDataException;
import org.molgenis.util.exception.ExceptionMessageTest;

class UnknownTypeExceptionTest extends ExceptionMessageTest {
  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("data-security");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    ExceptionMessageTest.assertExceptionMessageEquals(
        new UnknownTypeException("type"), lang, message);
  }

  @Test
  void testGetMessage() {
    UnknownDataException ex = new UnknownTypeException("type");
    assertEquals("typeId:type", ex.getMessage());
  }

  static Object[][] languageMessageProvider() {
    return new Object[][] {new Object[] {"en", "No type with id 'type' could be found."}};
  }
}
