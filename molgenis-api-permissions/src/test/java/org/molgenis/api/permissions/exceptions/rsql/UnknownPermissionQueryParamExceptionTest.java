package org.molgenis.api.permissions.exceptions.rsql;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.util.exception.CodedRuntimeException;
import org.molgenis.util.exception.ExceptionMessageTest;

class UnknownPermissionQueryParamExceptionTest extends ExceptionMessageTest {
  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("api-permissions");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    ExceptionMessageTest.assertExceptionMessageEquals(
        new UnknownPermissionQueryParamException("type"), lang, message);
  }

  @Test
  void testGetMessage() {
    CodedRuntimeException ex = new UnknownPermissionQueryParamException("type");
    assertEquals(ex.getMessage(), "key:type");
  }

  static Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {"en", "Unknown field 'type' in query, only 'user' and 'role' are supported."}
    };
  }
}
