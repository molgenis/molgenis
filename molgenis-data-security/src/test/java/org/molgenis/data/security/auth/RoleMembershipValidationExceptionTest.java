package org.molgenis.data.security.auth;

import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.util.exception.ExceptionMessageTest;

public class RoleMembershipValidationExceptionTest extends ExceptionMessageTest {
  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("data-security");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    assertExceptionMessageEquals(
        new RoleMembershipValidationException(mock(RoleMembership.class)), lang, message);
  }

  public static Object[][] languageMessageProvider() {
    Object[] enParams = {"en", "User cannot have multiple roles within the same group."};
    Object[] nlParams = {"nl", "Gebruiker kan niet meerdere rollen binnen dezelfde groep hebben."};
    return new Object[][] {enParams, nlParams};
  }
}
