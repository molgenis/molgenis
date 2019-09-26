package org.molgenis.data.security.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.data.DataAlreadyExistsException;
import org.molgenis.util.exception.ExceptionMessageTest;

class AclAlreadyExistsExceptionTest extends ExceptionMessageTest {
  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("data-security");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    ExceptionMessageTest.assertExceptionMessageEquals(
        new AclAlreadyExistsException("type", "identifier"), lang, message);
  }

  @Test
  void testGetMessage() {
    DataAlreadyExistsException ex = new AclAlreadyExistsException("type", "identifier");
    assertEquals("typeId:type, id:identifier", ex.getMessage());
  }

  public static Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {"en", "An acl with id 'identifier' for type with id 'type' already exists."}
    };
  }
}
