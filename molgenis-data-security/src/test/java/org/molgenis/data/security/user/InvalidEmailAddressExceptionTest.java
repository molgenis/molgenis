package org.molgenis.data.security.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.util.exception.ExceptionMessageTest;

class InvalidEmailAddressExceptionTest extends ExceptionMessageTest {
  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("data-security");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    assertExceptionMessageEquals(new InvalidEmailAddressException(), lang, message);
  }

  static Object[][] languageMessageProvider() {
    Object[] enParams = {"en", "Invalid email address."};
    Object[] nlParams = {"nl", "Ongeldig e-mailadres."};
    return new Object[][] {enParams, nlParams};
  }
}
