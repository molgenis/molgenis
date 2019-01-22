package org.molgenis.security.oidc;

import org.mockito.Mock;
import org.molgenis.security.user.UserDetailsServiceImpl;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class MappedOidcUserServiceTest extends AbstractMockitoTest {
  @Mock private OidcUserMapper oidcUserMapper;
  @Mock private UserDetailsServiceImpl userDetailsService;
  private MappedOidcUserService mappedOidcUserService;

  @BeforeMethod
  public void setUpBeforeMethod() {
    mappedOidcUserService = new MappedOidcUserService(oidcUserMapper, userDetailsService);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void testMappedOidcUserService() {
    new MappedOidcUserService(null, null);
  }

  // due to the fact that MappedOidcUserService extends OidcUserService and ClientRegistration is a
  // final class, unit testing loadUser is not feasible
}
