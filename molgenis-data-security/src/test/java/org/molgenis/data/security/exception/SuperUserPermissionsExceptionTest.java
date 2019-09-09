package org.molgenis.data.security.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.util.exception.CodedRuntimeException;
import org.molgenis.util.exception.ExceptionMessageTest;

class SuperUserPermissionsExceptionTest extends ExceptionMessageTest {

  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("data-security");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    ExceptionMessageTest.assertExceptionMessageEquals(
        new SuperUserPermissionsException("admin"), lang, message);
  }

  @Test
  void testGetMessage() {
    CodedRuntimeException ex = new SuperUserPermissionsException("admin");
    assertEquals(ex.getMessage(), "name:admin");
  }

  static Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {
        "en",
        "Getting or setting permissions for 'admin' is not allowed since it has role 'superuser' which has permissions on everything."
      }
    };
  }
}
