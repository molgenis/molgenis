package org.molgenis.security.account;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.util.exception.ExceptionMessageTest;

class PasswordResetTokenCreationExceptionTest extends ExceptionMessageTest {
  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("security");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    assertExceptionMessageEquals(new PasswordResetTokenCreationException(), lang, message);
  }

  static Object[][] languageMessageProvider() {
    Object[] enParams = {"en", "Password reset failed."};
    Object[] nlParams = {"nl", "Wachtwoord reset niet geslaagd."};
    return new Object[][] {enParams, nlParams};
  }
}
