package org.molgenis.data.security.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.util.exception.CodedRuntimeException;
import org.molgenis.util.exception.ExceptionMessageTest;

class PermissionNotSuitableExceptionTest extends ExceptionMessageTest {
  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("data-security");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    ExceptionMessageTest.assertExceptionMessageEquals(
        new PermissionNotSuitableException("permission", "type"), lang, message);
  }

  @Test
  void testGetMessage() {
    CodedRuntimeException ex = new PermissionNotSuitableException("permission", "type");
    assertEquals("permission:permission, typeId:type", ex.getMessage());
  }

  static Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {"en", "The permission 'permission' cannot be used for type 'type'."}
    };
  }
}
