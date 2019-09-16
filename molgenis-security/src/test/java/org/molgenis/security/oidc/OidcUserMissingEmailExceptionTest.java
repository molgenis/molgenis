package org.molgenis.security.oidc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

class OidcUserMissingEmailExceptionTest extends AbstractMockitoTest {
  @Mock private OidcUser oidcUser;
  private OidcUserMissingEmailException oidcUserMissingEmailException;

  @BeforeEach
  void setUpBeforeMethod() {
    oidcUserMissingEmailException = new OidcUserMissingEmailException(oidcUser);
  }

  @Test
  void testGetMessage() {
    when(oidcUser.getSubject()).thenReturn("userId");
    assertEquals(
        "email claim missing for subject 'userId'", oidcUserMissingEmailException.getMessage());
  }
}
