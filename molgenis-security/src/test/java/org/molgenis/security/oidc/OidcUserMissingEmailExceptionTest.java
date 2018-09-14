package org.molgenis.security.oidc;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import org.mockito.Mock;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class OidcUserMissingEmailExceptionTest extends AbstractMockitoTest {
  @Mock private OidcUser oidcUser;
  private OidcUserMissingEmailException oidcUserMissingEmailException;

  @BeforeMethod
  public void setUpBeforeMethod() {
    oidcUserMissingEmailException = new OidcUserMissingEmailException(oidcUser);
  }

  @Test
  public void testGetMessage() {
    when(oidcUser.getSubject()).thenReturn("userId");
    assertEquals(
        oidcUserMissingEmailException.getMessage(), "email claim missing for subject 'userId'");
  }
}
