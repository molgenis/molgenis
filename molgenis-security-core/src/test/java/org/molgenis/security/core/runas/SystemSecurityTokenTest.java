package org.molgenis.security.core.runas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.molgenis.security.core.runas.SystemSecurityToken.getInstance;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import org.molgenis.security.core.runas.SystemSecurityToken.SystemPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

class SystemSecurityTokenTest {
  @Test
  void testGetAuthorities() {
    ImmutableList<SimpleGrantedAuthority> expectedAuthorities =
        ImmutableList.of(
            new SimpleGrantedAuthority("ROLE_SYSTEM"),
            new SimpleGrantedAuthority("ROLE_ACL_TAKE_OWNERSHIP"),
            new SimpleGrantedAuthority("ROLE_ACL_MODIFY_AUDITING"),
            new SimpleGrantedAuthority("ROLE_ACL_GENERAL_CHANGES"));
    assertEquals(expectedAuthorities, getInstance().getAuthorities());
  }

  @Test
  void testGetCredentials() {
    assertNull(SystemSecurityToken.getInstance().getCredentials());
  }

  @Test
  void testGetPrincipal() {
    assertTrue(SystemSecurityToken.getInstance().getPrincipal() instanceof SystemPrincipal);
  }

  @Test
  void testIsAuthenticated() {
    assertTrue(SystemSecurityToken.getInstance().isAuthenticated());
  }
}
