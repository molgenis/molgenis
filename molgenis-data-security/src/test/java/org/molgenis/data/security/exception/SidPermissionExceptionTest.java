package org.molgenis.data.security.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.util.exception.CodedRuntimeException;
import org.molgenis.util.exception.ExceptionMessageTest;

class SidPermissionExceptionTest extends ExceptionMessageTest {
  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("data-security");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    ExceptionMessageTest.assertExceptionMessageEquals(
        new SidPermissionException("user1,user2,roleA,roleB"), lang, message);
  }

  @Test
  void testGetMessage() {
    CodedRuntimeException ex = new SidPermissionException("user1,user2,roleA,roleB");
    assertEquals(ex.getMessage(), "sids:user1,user2,roleA,roleB");
  }

  static Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {
        "en",
        "No permission to read permissions for user(s) and/or role(s) 'user1,user2,roleA,roleB'."
      }
    };
  }
}
