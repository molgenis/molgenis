package org.molgenis.data.security.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.util.exception.CodedRuntimeException;
import org.molgenis.util.exception.ExceptionMessageTest;

class AclClassAlreadyExistsExceptionTest extends ExceptionMessageTest {
  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("data-security");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    ExceptionMessageTest.assertExceptionMessageEquals(
        new AclClassAlreadyExistsException("type"), lang, message);
  }

  @Test
  void testGetMessage() {
    CodedRuntimeException ex = new AclNotFoundException("type");
    assertEquals("typeId:type", ex.getMessage());
  }

  public static Object[][] languageMessageProvider() {
    return new Object[][] {new Object[] {"en", "A type with id 'type' already exists."}};
  }
}
