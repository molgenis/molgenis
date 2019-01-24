package org.molgenis.security.oidc;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import org.mockito.Mock;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class OidcUserEmailVerificationExceptionTest extends AbstractMockitoTest {
  @Mock private OidcUser oidcUser;
  private OidcUserEmailVerificationException oidcUserEmailVerificationException;

  @BeforeMethod
  public void setUpBeforeMethod() {
    oidcUserEmailVerificationException = new OidcUserEmailVerificationException(oidcUser);
  }

  @Test
  public void testGetMessage() {
    when(oidcUser.getSubject()).thenReturn("userId");
    assertEquals(
        oidcUserEmailVerificationException.getMessage(),
        "email verification claim exists but evaluates to false for subject 'userId'");
  }
}
