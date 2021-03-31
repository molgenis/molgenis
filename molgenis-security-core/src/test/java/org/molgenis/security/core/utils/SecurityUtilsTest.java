package org.molgenis.security.core.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.security.core.utils.SecurityUtils.AUTHORITY_SU;
import static org.molgenis.security.core.utils.SecurityUtils.ROLE_SYSTEM;
import static org.molgenis.security.core.utils.SecurityUtils.currentUserIsRunningAsSystem;
import static org.molgenis.security.core.utils.SecurityUtils.currentUserIsUser;
import static org.molgenis.security.core.utils.SecurityUtils.getActualUsername;
import static org.molgenis.security.core.utils.SecurityUtils.getCurrentUsername;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.molgenis.security.core.WithMockSystemUser;
import org.molgenis.security.core.runas.SystemSecurityToken;
import org.molgenis.test.AbstractMockitoSpringContextTests;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = SecurityUtilsTest.Config.class)
class SecurityUtilsTest extends AbstractMockitoSpringContextTests {

  @Configuration
  static class Config {}

  private static SecurityContext previousContext;

  @BeforeAll
  static void setUpBeforeClass() {
    previousContext = SecurityContextHolder.getContext();
  }

  private Authentication mockAuthentication() {
    var authentication = mock(Authentication.class);

    SecurityContext testContext = SecurityContextHolder.createEmptyContext();
    testContext.setAuthentication(authentication);
    SecurityContextHolder.setContext(testContext);

    return authentication;
  }

  @AfterAll
  static void tearDownAfterClass() {
    SecurityContextHolder.setContext(previousContext);
  }

  @Test
  void currentUserIsAuthenticated_true() {
    var authentication = mockAuthentication();
    when(authentication.isAuthenticated()).thenReturn(true);
    assertTrue(SecurityUtils.currentUserIsAuthenticated());
  }

  @Test
  void currentUserIsAuthenticated_false() {
    var authentication = mockAuthentication();
    when(authentication.isAuthenticated()).thenReturn(false);
    assertFalse(SecurityUtils.currentUserIsAuthenticated());
  }

  @Test
  void currentUserIsAuthenticated_falseAnonymous() {
    assertFalse(SecurityUtils.currentUserIsAuthenticated());
  }

  @Test
  void currentUserIsSu_false() {
    assertFalse(SecurityUtils.currentUserIsSu());
    assertFalse(SecurityUtils.currentUserIsSuOrSystem());
  }

  @SuppressWarnings("unchecked")
  @Test
  void currentUserIsSu_true() {
    var authentication = mockAuthentication();
    GrantedAuthority authoritySu = mock(GrantedAuthority.class);
    when(authoritySu.getAuthority()).thenReturn(AUTHORITY_SU);
    when((Collection<GrantedAuthority>) authentication.getAuthorities())
        .thenReturn(Collections.singletonList(authoritySu));
    assertTrue(SecurityUtils.currentUserIsSu());
    assertTrue(SecurityUtils.currentUserIsSuOrSystem());
  }

  @SuppressWarnings("unchecked")
  @Test
  void currentUserIsSystemTrue() {
    var authentication = mockAuthentication();
    GrantedAuthority authoritySystem = mock(GrantedAuthority.class);
    when(authoritySystem.getAuthority()).thenReturn(ROLE_SYSTEM);
    when((Collection<GrantedAuthority>) authentication.getAuthorities())
        .thenReturn(Collections.singletonList(authoritySystem));
    assertTrue(SecurityUtils.currentUserIsSystem());
    assertTrue(SecurityUtils.currentUserIsSuOrSystem());
  }

  @WithMockUser()
  @Test
  void currentUserIsSystemFalse() {
    assertFalse(SecurityUtils.currentUserIsSystem());
    assertFalse(SecurityUtils.currentUserIsSuOrSystem());
  }

  @WithMockUser()
  @Test
  void getCurrentUsernameUserDetails() {
    assertEquals("user", getCurrentUsername());
  }

  @Test
  void getCurrentUsernameSystemPrincipal() {
    var authentication = mockAuthentication();
    try {
      SecurityContextHolder.getContext().setAuthentication(SystemSecurityToken.create());
      assertEquals("SYSTEM", getCurrentUsername());
    } finally {
      SecurityContextHolder.getContext().setAuthentication(authentication);
    }
  }

  @SuppressWarnings("unchecked")
  @Test
  void isUserInRole() {
    var authentication = mockAuthentication();
    GrantedAuthority authority1 = mock(GrantedAuthority.class);
    when(authority1.getAuthority()).thenReturn("authority1");
    GrantedAuthority authority2 = mock(GrantedAuthority.class);
    when(authority2.getAuthority()).thenReturn("authority2");
    when((Collection<GrantedAuthority>) authentication.getAuthorities())
        .thenReturn(Arrays.asList(authority1, authority2));

    assertTrue(SecurityUtils.currentUserHasRole("authority1"));
    assertTrue(SecurityUtils.currentUserHasRole("authority2"));
    assertTrue(SecurityUtils.currentUserHasRole("authority1", "authority2"));
    assertTrue(SecurityUtils.currentUserHasRole("authority2", "authority1"));
    assertTrue(SecurityUtils.currentUserHasRole("authority1", "authority3"));
  }

  @Test
  @WithMockUser("henk")
  void testGetActualUsername() {
    assertEquals("henk", getActualUsername());
  }

  @Test
  @WithMockSystemUser(originalUsername = "henk")
  void testGetActualUsernameElevated() {
    assertEquals("henk", getActualUsername());
  }

  @Test
  @WithMockSystemUser
  void testGetActualUsernameSystem() {
    assertEquals("SYSTEM", getActualUsername());
  }

  @Test
  @WithMockUser("bofke")
  void testIsRunByUser() {
    assertTrue(currentUserIsUser());
  }

  @Test
  @WithMockSystemUser(originalUsername = "henk")
  void testIsRunByUserElevated() {
    assertTrue(currentUserIsUser());
  }

  @Test
  @WithMockSystemUser
  void testIsRunByUserSystem() {
    assertFalse(currentUserIsUser());
  }

  @Test
  @WithMockUser("bofke")
  void testIsRunAsSystemUser() {
    assertFalse(currentUserIsRunningAsSystem());
  }

  @Test
  @WithMockSystemUser(originalUsername = "henk")
  void testIsRunAsSystemElevated() {
    assertTrue(currentUserIsRunningAsSystem());
  }

  @Test
  @WithMockSystemUser
  void testIsRunAsSystemSystem() {
    assertFalse(currentUserIsRunningAsSystem());
  }
}
