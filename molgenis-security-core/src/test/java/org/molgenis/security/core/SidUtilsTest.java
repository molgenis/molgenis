package org.molgenis.security.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.molgenis.security.core.runas.SystemSecurityToken;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.Sid;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

class SidUtilsTest {

  private SecurityContext previousContext;

  @BeforeEach
  void setUpBeforeMethod() {
    previousContext = SecurityContextHolder.getContext();
    SecurityContext testContext = SecurityContextHolder.createEmptyContext();
    SecurityContextHolder.setContext(testContext);
  }

  @AfterEach
  void tearDownAfterMethod() {
    SecurityContextHolder.setContext(previousContext);
  }

  @Test
  void createSecurityContextSidUser() {
    String principal = "username";
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(principal, null));
    assertEquals(SidUtils.createSecurityContextSid(), new PrincipalSid(principal));
  }

  @Test
  void createSecurityContextSidSystem() {
    SecurityContextHolder.getContext().setAuthentication(SystemSecurityToken.getInstance());
    assertEquals(SidUtils.createSecurityContextSid(), new GrantedAuthoritySid("ROLE_SYSTEM"));
  }

  @Test
  void createSecurityContextSidAnonymous() {
    SecurityContextHolder.getContext().setAuthentication(null);
    assertEquals(SidUtils.createSecurityContextSid(), new GrantedAuthoritySid("ROLE_ANONYMOUS"));
  }

  @Test
  void createSecurityContextNoPrincipal() {
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(null, null));
    assertThrows(NullPointerException.class, SidUtils::createSecurityContextSid);
  }

  @Test
  void testCreateSidUser() {
    Sid sid = SidUtils.createUserSid("username");
    assertEquals(sid, new PrincipalSid("username"));
  }

  @Test
  void testCreateSidUsernameAnonymous() {
    Sid sid = SidUtils.createUserSid("anonymous");
    assertEquals(sid, new GrantedAuthoritySid("ROLE_ANONYMOUS"));
  }

  @Test
  void testCreateSidRole() {
    Sid sid = SidUtils.createRoleSid("NAME");
    assertEquals(sid, new GrantedAuthoritySid(new SimpleGrantedAuthority("ROLE_NAME")));
  }

  @Test
  void testCreateRoleAuthority() {
    assertEquals("ROLE_NAME", SidUtils.createRoleAuthority("NAME"));
  }
}
