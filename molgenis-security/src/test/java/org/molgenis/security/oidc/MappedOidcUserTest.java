package org.molgenis.security.oidc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.springframework.security.oauth2.core.oidc.StandardClaimNames.SUB;

import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

class MappedOidcUserTest extends AbstractMockitoTest {
  @Mock private OidcUser oidcUser;
  @Mock private OidcIdToken oidcIdToken;
  @Mock private OidcUserInfo userInfo;
  private MappedOidcUser withNameOverride;
  private MappedOidcUser keyOverrides;

  @BeforeEach
  void beforeEach() {
    Set<? extends GrantedAuthority> molgenisRoles =
        Set.of(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority("ABCDE_EDITOR"));
    Set<GrantedAuthority> userAuthorities = Set.of(new SimpleGrantedAuthority("USER"));
    doReturn(userAuthorities).when(oidcUser).getAuthorities();

    Map<String, Object> userInfoClaims =
        Map.of(
            SUB,
            "d8995976-e8d8-4390-839b-007a382fc12b",
            "emailAddress",
            "user@example.org",
            "preferredUsername",
            "henk");
    Map<String, Object> idTokenClaims = Map.of(SUB, "d8995976-e8d8-4390-839b-007a382fc12b");
    when(oidcIdToken.getClaims()).thenReturn(idTokenClaims);
    when(userInfo.getClaims()).thenReturn(userInfoClaims);

    when(oidcUser.getIdToken()).thenReturn(oidcIdToken);
    when(oidcUser.getUserInfo()).thenReturn(userInfo);
    when(oidcUser.getAttributes()).thenReturn(userInfoClaims);

    withNameOverride = new MappedOidcUser(oidcUser, molgenisRoles, "emailAddress", "molgenis");
    keyOverrides = new MappedOidcUser(oidcUser, "emailAddress", "preferredUsername");
  }

  @Test
  void getNameWithNameOverride() {
    assertEquals("molgenis", withNameOverride.getName());
  }

  @Test
  void getNameWithKeyOverride() {
    assertEquals("henk", keyOverrides.getName());
  }

  @Test
  void getEmailWithKeyOverride() {
    assertEquals("user@example.org", withNameOverride.getEmail());
    assertEquals("user@example.org", keyOverrides.getEmail());
  }
}
