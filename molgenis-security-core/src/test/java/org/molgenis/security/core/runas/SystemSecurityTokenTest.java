package org.molgenis.security.core.runas;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import org.molgenis.security.core.runas.SystemSecurityToken.SystemPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.testng.annotations.Test;

public class SystemSecurityTokenTest {
  @Test
  public void testGetAuthorities() {
    ImmutableList<SimpleGrantedAuthority> expectedAuthorities =
        ImmutableList.of(
            new SimpleGrantedAuthority("ROLE_SYSTEM"),
            new SimpleGrantedAuthority("ROLE_ACL_TAKE_OWNERSHIP"),
            new SimpleGrantedAuthority("ROLE_ACL_MODIFY_AUDITING"),
            new SimpleGrantedAuthority("ROLE_ACL_GENERAL_CHANGES"));
    assertEquals(SystemSecurityToken.getInstance().getAuthorities(), expectedAuthorities);
  }

  @Test
  public void testGetCredentials() {
    assertNull(SystemSecurityToken.getInstance().getCredentials());
  }

  @Test
  public void testgetPrincipal() {
    assertTrue(SystemSecurityToken.getInstance().getPrincipal() instanceof SystemPrincipal);
  }
}
