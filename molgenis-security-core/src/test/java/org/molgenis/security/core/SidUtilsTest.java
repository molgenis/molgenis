package org.molgenis.security.core;

import static org.testng.Assert.assertEquals;

import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.Sid;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.testng.annotations.Test;

public class SidUtilsTest {
  @Test
  public void testCreateSidUser() {
    Sid sid = SidUtils.createUserSid("username");
    assertEquals(sid, new PrincipalSid("username"));
  }

  @Test
  public void testCreateSidUsernameAnonymous() {
    Sid sid = SidUtils.createUserSid("anonymous");
    assertEquals(sid, new GrantedAuthoritySid("ROLE_ANONYMOUS"));
  }

  @Test
  public void testCreateSidRole() {
    Sid sid = SidUtils.createRoleSid("NAME");
    assertEquals(sid, new GrantedAuthoritySid(new SimpleGrantedAuthority("ROLE_NAME")));
  }

  @Test
  public void testCreateRoleAuthority() {
    assertEquals("ROLE_NAME", SidUtils.createRoleAuthority("NAME"));
  }
}
