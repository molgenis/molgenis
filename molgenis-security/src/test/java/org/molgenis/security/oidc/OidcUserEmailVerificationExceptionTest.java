package org.molgenis.security.oidc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

class OidcUserEmailVerificationExceptionTest extends AbstractMockitoTest {
  @Mock private OidcUser oidcUser;
  private OidcUserEmailVerificationException oidcUserEmailVerificationException;

  @BeforeEach
  void setUpBeforeMethod() {
    oidcUserEmailVerificationException = new OidcUserEmailVerificationException(oidcUser);
  }

  @Test
  void testGetMessage() {
    when(oidcUser.getSubject()).thenReturn("userId");
    assertEquals(
        oidcUserEmailVerificationException.getMessage(),
        "email verification claim exists but evaluates to false for subject 'userId'");
  }
}
