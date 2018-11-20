package org.molgenis.security.core;

import static org.testng.Assert.assertEquals;

import org.molgenis.security.core.runas.SystemSecurityToken;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.Sid;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SidUtilsTest {
  private Authentication authenticationPrevious;

  @BeforeMethod
  public void setUpBeforeMethod() {
    authenticationPrevious = SecurityContextHolder.getContext().getAuthentication();
  }

  @AfterMethod
  public void tearDownAfterMethod() {
    SecurityContextHolder.getContext().setAuthentication(authenticationPrevious);
  }

  @Test
  public void createSecurityContextSidUser() {
    String principal = "username";
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(principal, null));
    assertEquals(SidUtils.createSecurityContextSid(), new PrincipalSid(principal));
  }

  @Test
  public void createSecurityContextSidSystem() {
    SecurityContextHolder.getContext().setAuthentication(SystemSecurityToken.getInstance());
    assertEquals(SidUtils.createSecurityContextSid(), new GrantedAuthoritySid("ROLE_SYSTEM"));
  }

  @Test
  public void createSecurityContextSidAnonymous() {
    SecurityContextHolder.getContext().setAuthentication(null);
    assertEquals(SidUtils.createSecurityContextSid(), new GrantedAuthoritySid("ROLE_ANONYMOUS"));
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void createSecurityContextNoPrincipal() {
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(null, null));
    SidUtils.createSecurityContextSid();
  }

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
