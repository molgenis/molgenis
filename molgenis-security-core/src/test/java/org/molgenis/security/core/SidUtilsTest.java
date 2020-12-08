package org.molgenis.security.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.molgenis.security.core.SidUtils.createRoleAuthority;
import static org.molgenis.security.core.SidUtils.createSecurityContextSid;

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
    assertEquals(new PrincipalSid(principal), createSecurityContextSid());
  }

  @Test
  void createSecurityContextSidSystem() {
    SecurityContextHolder.getContext().setAuthentication(SystemSecurityToken.getInstance());
    assertEquals(new GrantedAuthoritySid("ROLE_SYSTEM"), createSecurityContextSid());
  }

  @Test
  void createSecurityContextSidAnonymous() {
    SecurityContextHolder.getContext().setAuthentication(null);
    assertEquals(new GrantedAuthoritySid("ROLE_ANONYMOUS"), createSecurityContextSid());
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
    assertEquals(new PrincipalSid("username"), sid);
  }

  @Test
  void testCreateSidUsernameAnonymous() {
    Sid sid = SidUtils.createUserSid("anonymous");
    assertEquals(new GrantedAuthoritySid("ROLE_ANONYMOUS"), sid);
  }

  @Test
  void testCreateSidRole() {
    Sid sid = SidUtils.createRoleSid("NAME");
    assertEquals(new GrantedAuthoritySid(new SimpleGrantedAuthority("ROLE_NAME")), sid);
  }

  @Test
  void testCreateRoleAuthority() {
    assertEquals("ROLE_NAME", createRoleAuthority("NAME"));
  }

  @Test
  void testGetStringValuePrincipalSid() {
    Sid sid = new PrincipalSid("test");
    assertEquals("test", SidUtils.getStringValue(sid));
  }

  @Test
  void testGetStringValueGrantedAuthoritySid() {
    Sid sid = new GrantedAuthoritySid("test");
    assertEquals("test", SidUtils.getStringValue(sid));
  }

  @Test
  void testGetStringValueUnknownSidType() {
    class UnknownSid implements Sid {}
    Sid sid = new UnknownSid();
    assertThrows(IllegalStateException.class, () -> SidUtils.getStringValue(sid));
  }
}
