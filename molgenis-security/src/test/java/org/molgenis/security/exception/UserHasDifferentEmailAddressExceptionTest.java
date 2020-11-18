package org.molgenis.security.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.util.exception.ExceptionMessageTest;

class UserHasDifferentEmailAddressExceptionTest extends ExceptionMessageTest {
  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("security");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    assertExceptionMessageEquals(
        new UserHasDifferentEmailAddressException("henk", "henk@example.org"), lang, message);
  }

  static Object[][] languageMessageProvider() {
    Object[] enParams = {
      "en",
      "Failed to register new user. "
          + "A user with username 'henk' already exists, "
          + "but does not have email address 'henk@example.org'."
    };
    return new Object[][] {enParams};
  }
}
