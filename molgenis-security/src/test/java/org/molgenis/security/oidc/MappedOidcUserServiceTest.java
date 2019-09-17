package org.molgenis.security.oidc;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.security.user.UserDetailsServiceImpl;
import org.molgenis.test.AbstractMockitoTest;

class MappedOidcUserServiceTest extends AbstractMockitoTest {
  @Mock private OidcUserMapper oidcUserMapper;
  @Mock private UserDetailsServiceImpl userDetailsService;
  private MappedOidcUserService mappedOidcUserService;

  @BeforeEach
  void setUpBeforeMethod() {
    mappedOidcUserService = new MappedOidcUserService(oidcUserMapper, userDetailsService);
  }

  @Test
  void testMappedOidcUserService() {
    assertThrows(NullPointerException.class, () -> new MappedOidcUserService(null, null));
  }

  // due to the fact that MappedOidcUserService extends OidcUserService and ClientRegistration is a
  // final class, unit testing loadUser is not feasible
}
