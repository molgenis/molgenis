package org.molgenis.security.core.runas;

import static java.util.Collections.emptySet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import org.molgenis.security.core.runas.SystemSecurityToken.SystemPrincipal;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

class SystemSecurityTokenTest {

  @Test
  void testGetAuthorities() {
    ImmutableList<SimpleGrantedAuthority> expectedAuthorities =
        ImmutableList.of(
            new SimpleGrantedAuthority("ROLE_SYSTEM"),
            new SimpleGrantedAuthority("ROLE_ACL_TAKE_OWNERSHIP"),
            new SimpleGrantedAuthority("ROLE_ACL_MODIFY_AUDITING"),
            new SimpleGrantedAuthority("ROLE_ACL_GENERAL_CHANGES"));
    assertEquals(expectedAuthorities, SystemSecurityToken.create().getAuthorities());
  }

  @Test
  void testGetCredentials() {
    assertNull(SystemSecurityToken.create().getCredentials());
  }

  @Test
  void testGetPrincipal() {
    assertTrue(SystemSecurityToken.create().getPrincipal() instanceof SystemPrincipal);
  }

  @Test
  void testIsAuthenticated() {
    assertTrue(SystemSecurityToken.create().isAuthenticated());
  }

  @Test
  void testGetOriginalAuthenticationWithElevatedUser() {
    var user = new User("name", "password", emptySet());
    var auth = new UsernamePasswordAuthenticationToken(user, "password", emptySet());

    var originalAuth = SystemSecurityToken.createElevated(auth).getOriginalAuthentication();

    assertTrue(originalAuth.isPresent());
    assertEquals(auth, originalAuth.get());
  }

  @Test
  void testGetOriginalAuthenticationWithoutElevatedUser() {
    var originalAuth = SystemSecurityToken.create().getOriginalAuthentication();
    assertFalse(originalAuth.isPresent());
  }
}
