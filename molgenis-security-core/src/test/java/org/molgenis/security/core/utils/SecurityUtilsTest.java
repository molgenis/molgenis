package org.molgenis.security.core.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.molgenis.security.core.utils.SecurityUtils.AUTHORITY_SU;
import static org.molgenis.security.core.utils.SecurityUtils.AUTHORITY_USER;
import static org.molgenis.security.core.utils.SecurityUtils.ROLE_SYSTEM;
import static org.molgenis.security.core.utils.SecurityUtils.getCurrentUsername;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.molgenis.security.core.runas.SystemSecurityToken;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

class SecurityUtilsTest {
  private static Authentication authentication;
  private UserDetails userDetails;
  private static SecurityContext previousContext;

  @BeforeAll
  static void setUpBeforeClass() {
    previousContext = SecurityContextHolder.getContext();
    SecurityContext testContext = SecurityContextHolder.createEmptyContext();
    authentication = mock(Authentication.class);
    testContext.setAuthentication(authentication);
    SecurityContextHolder.setContext(testContext);
  }

  @SuppressWarnings("unchecked")
  @BeforeEach
  void setUpBeforeMethod() {
    reset(authentication);

    GrantedAuthority authority1 = mock(GrantedAuthority.class);
    when(authority1.getAuthority()).thenReturn("authority1");
    GrantedAuthority authority2 = mock(GrantedAuthority.class);
    when(authority2.getAuthority()).thenReturn("authority2");
    userDetails = mock(UserDetails.class);
    when(userDetails.getUsername()).thenReturn("username");
    when(userDetails.getPassword()).thenReturn("encoded-password");
    when((Collection<GrantedAuthority>) userDetails.getAuthorities())
        .thenReturn(Arrays.asList(authority1, authority2));
    when(authentication.getPrincipal()).thenReturn(userDetails);
    when((Collection<GrantedAuthority>) authentication.getAuthorities())
        .thenReturn(Arrays.asList(authority1, authority2));
  }

  @AfterAll
  static void tearDownAfterClass() {
    SecurityContextHolder.setContext(previousContext);
  }

  @SuppressWarnings("unchecked")
  @Test
  void currentUserIsAuthenticated_true() {
    when(authentication.isAuthenticated()).thenReturn(true);
    GrantedAuthority authorityUser = mock(GrantedAuthority.class);
    when(authorityUser.getAuthority()).thenReturn(AUTHORITY_USER);
    when((Collection<GrantedAuthority>) authentication.getAuthorities())
        .thenReturn(Collections.singletonList(authorityUser));
    assertTrue(SecurityUtils.currentUserIsAuthenticated());
  }

  @Test
  void currentUserIsAuthenticated_false() {
    when(authentication.isAuthenticated()).thenReturn(false);
    assertFalse(SecurityUtils.currentUserIsAuthenticated());
  }

  @SuppressWarnings("unchecked")
  @Test
  void currentUserIsAuthenticated_falseAnonymous() {
    Authentication anonymousAuthentication = mock(AnonymousAuthenticationToken.class);
    when(anonymousAuthentication.isAuthenticated()).thenReturn(true);
    GrantedAuthority authoritySu = mock(GrantedAuthority.class);
    when(authoritySu.getAuthority()).thenReturn("ROLE_ANONYMOUS");
    when((Collection<GrantedAuthority>) anonymousAuthentication.getAuthorities())
        .thenReturn(Collections.singletonList(authoritySu));
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
    GrantedAuthority authoritySystem = mock(GrantedAuthority.class);
    when(authoritySystem.getAuthority()).thenReturn(ROLE_SYSTEM);
    when((Collection<GrantedAuthority>) authentication.getAuthorities())
        .thenReturn(Collections.singletonList(authoritySystem));
    assertTrue(SecurityUtils.currentUserIsSystem());
    assertTrue(SecurityUtils.currentUserIsSuOrSystem());
  }

  @Test
  void currentUserIsSystemFalse() {
    when(userDetails.getUsername()).thenReturn("user");
    assertFalse(SecurityUtils.currentUserIsSystem());
    assertFalse(SecurityUtils.currentUserIsSuOrSystem());
  }

  @Test
  void getCurrentUsernameUserDetails() {
    assertEquals(userDetails.getUsername(), getCurrentUsername());
  }

  @Test
  void getCurrentUsernameSystemPrincipal() {
    try {
      SecurityContextHolder.getContext().setAuthentication(SystemSecurityToken.create());
      assertNull(SecurityUtils.getCurrentUsername());
    } finally {
      SecurityContextHolder.getContext().setAuthentication(authentication);
    }
  }

  @Test
  void isUserInRole() {
    assertTrue(SecurityUtils.currentUserHasRole("authority1"));
    assertTrue(SecurityUtils.currentUserHasRole("authority2"));
    assertTrue(SecurityUtils.currentUserHasRole("authority1", "authority2"));
    assertTrue(SecurityUtils.currentUserHasRole("authority2", "authority1"));
    assertTrue(SecurityUtils.currentUserHasRole("authority1", "authority3"));
  }
}
