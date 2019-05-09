package org.molgenis.security.core.utils;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.molgenis.security.core.utils.SecurityUtils.AUTHORITY_SU;
import static org.molgenis.security.core.utils.SecurityUtils.AUTHORITY_USER;
import static org.molgenis.security.core.utils.SecurityUtils.ROLE_SYSTEM;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import org.molgenis.security.core.runas.SystemSecurityToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SecurityUtilsTest {
  private Authentication authentication;
  private UserDetails userDetails;
  private SecurityContext previousContext;

  @BeforeClass
  public void setUpBeforeClass() {
    previousContext = SecurityContextHolder.getContext();
    SecurityContext testContext = SecurityContextHolder.createEmptyContext();
    authentication = mock(Authentication.class);
    testContext.setAuthentication(authentication);
    SecurityContextHolder.setContext(testContext);
  }

  @SuppressWarnings("unchecked")
  @BeforeMethod
  public void setUpBeforeMethod() {
    reset(authentication);

    GrantedAuthority authority1 =
        when(mock(GrantedAuthority.class).getAuthority()).thenReturn("authority1").getMock();
    GrantedAuthority authority2 =
        when(mock(GrantedAuthority.class).getAuthority()).thenReturn("authority2").getMock();
    userDetails = mock(UserDetails.class);
    when(userDetails.getUsername()).thenReturn("username");
    when(userDetails.getPassword()).thenReturn("encoded-password");
    when((Collection<GrantedAuthority>) userDetails.getAuthorities())
        .thenReturn(Arrays.asList(authority1, authority2));
    when(authentication.getPrincipal()).thenReturn(userDetails);
    when((Collection<GrantedAuthority>) authentication.getAuthorities())
        .thenReturn(Arrays.asList(authority1, authority2));
  }

  @AfterClass
  public void tearDownAfterClass() {
    SecurityContextHolder.setContext(previousContext);
  }

  @Test
  public void currentUserIsAuthenticated_true() {
    when(authentication.isAuthenticated()).thenReturn(true);
    GrantedAuthority authorityUser =
        when(mock(GrantedAuthority.class).getAuthority()).thenReturn(AUTHORITY_USER).getMock();
    when((Collection<GrantedAuthority>) authentication.getAuthorities())
        .thenReturn(Collections.singletonList(authorityUser));
    assertTrue(SecurityUtils.currentUserIsAuthenticated());
  }

  @Test
  public void currentUserIsAuthenticated_false() {
    when(authentication.isAuthenticated()).thenReturn(false);
    assertFalse(SecurityUtils.currentUserIsAuthenticated());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void currentUserIsAuthenticated_falseAnonymous() {
    when(authentication.isAuthenticated()).thenReturn(true);
    GrantedAuthority authoritySu =
        when(mock(GrantedAuthority.class).getAuthority()).thenReturn("ROLE_ANONYMOUS").getMock();
    when((Collection<GrantedAuthority>) authentication.getAuthorities())
        .thenReturn(Collections.singletonList(authoritySu));
    assertFalse(SecurityUtils.currentUserIsAuthenticated());
  }

  @Test
  public void currentUserIsSu_false() {
    assertFalse(SecurityUtils.currentUserIsSu());
    assertFalse(SecurityUtils.currentUserIsSuOrSystem());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void currentUserIsSu_true() {
    GrantedAuthority authoritySu =
        when(mock(GrantedAuthority.class).getAuthority()).thenReturn(AUTHORITY_SU).getMock();
    when((Collection<GrantedAuthority>) authentication.getAuthorities())
        .thenReturn(Collections.singletonList(authoritySu));
    assertTrue(SecurityUtils.currentUserIsSu());
    assertTrue(SecurityUtils.currentUserIsSuOrSystem());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void currentUserIsSystemTrue() {
    GrantedAuthority authoritySystem =
        when(mock(GrantedAuthority.class).getAuthority()).thenReturn(ROLE_SYSTEM).getMock();
    when((Collection<GrantedAuthority>) authentication.getAuthorities())
        .thenReturn(Collections.singletonList(authoritySystem));
    assertTrue(SecurityUtils.currentUserIsSystem());
    assertTrue(SecurityUtils.currentUserIsSuOrSystem());
  }

  @Test
  public void currentUserIsSystemFalse() {
    when(userDetails.getUsername()).thenReturn("user");
    assertFalse(SecurityUtils.currentUserIsSystem());
    assertFalse(SecurityUtils.currentUserIsSuOrSystem());
  }

  @Test
  public void getCurrentUsernameUserDetails() {
    assertEquals(SecurityUtils.getCurrentUsername(), userDetails.getUsername());
  }

  @Test
  public void getCurrentUsernameSystemPrincipal() {
    try {
      SecurityContextHolder.getContext().setAuthentication(SystemSecurityToken.getInstance());
      assertNull(SecurityUtils.getCurrentUsername());
    } finally {
      SecurityContextHolder.getContext().setAuthentication(authentication);
    }
  }

  @Test
  public void isUserInRole() {
    assertTrue(SecurityUtils.currentUserHasRole("authority1"));
    assertTrue(SecurityUtils.currentUserHasRole("authority2"));
    assertTrue(SecurityUtils.currentUserHasRole("authority1", "authority2"));
    assertTrue(SecurityUtils.currentUserHasRole("authority2", "authority1"));
    assertTrue(SecurityUtils.currentUserHasRole("authority1", "authority3"));
  }
}
