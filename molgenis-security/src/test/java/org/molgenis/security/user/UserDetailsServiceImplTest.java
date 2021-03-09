package org.molgenis.security.user;

import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.data.security.auth.Role;
import org.molgenis.data.security.auth.RoleMembership;
import org.molgenis.data.security.auth.User;
import org.molgenis.data.security.permission.RoleMembershipService;
import org.molgenis.data.security.user.UserService;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

class UserDetailsServiceImplTest extends AbstractMockitoTest {
  @Mock private GrantedAuthoritiesMapper grantedAuthoritiesMapper;
  @Mock private User user;
  @Mock private RoleMembership membership;
  @Mock private Role role;
  @Mock private UserService userService;
  @Mock private RoleMembershipService roleMembershipService;

  private UserDetailsServiceImpl userDetailsServiceImpl;

  @BeforeEach
  void setUp() {
    userDetailsServiceImpl =
        new UserDetailsServiceImpl(grantedAuthoritiesMapper, userService, roleMembershipService);
  }

  @Test
  void testUserDetailsService() {
    assertThrows(NullPointerException.class, () -> new UserDetailsServiceImpl(null, null, null));
  }

  @SuppressWarnings("unchecked")
  @Test
  void testLoadUserByUsernameSuperuser() {
    String username = "user";
    when(user.getUsername()).thenReturn(username);
    when(user.getPassword()).thenReturn("pw");
    when(user.isActive()).thenReturn(true);
    when(user.isSuperuser()).thenReturn(true);
    when(userService.getUser(username)).thenReturn(user);

    when(membership.getRole()).thenReturn(role);
    when(role.getName()).thenReturn("MY_ROLE");
    when(roleMembershipService.getCurrentMemberships(user)).thenReturn(List.of(membership));

    Set<GrantedAuthority> userAuthorities = new LinkedHashSet<>();
    userAuthorities.add(new SimpleGrantedAuthority("ROLE_SU"));
    userAuthorities.add(new SimpleGrantedAuthority("ROLE_USER"));
    userAuthorities.add(new SimpleGrantedAuthority("ROLE_MY_ROLE"));
    Collection<GrantedAuthority> mappedAuthorities =
        singleton(new SimpleGrantedAuthority("ROLE_MAPPED"));
    when(grantedAuthoritiesMapper.mapAuthorities(userAuthorities))
        .thenReturn((Collection) mappedAuthorities);

    UserDetails userDetails = userDetailsServiceImpl.loadUserByUsername(username);
    assertEquals(username, userDetails.getUsername());
    assertEquals(mappedAuthorities, userDetails.getAuthorities());
  }

  @SuppressWarnings("unchecked")
  @Test
  void testLoadUserByUsernameNonSuperuser() {
    String username = "user";
    User user = mock(User.class);
    when(user.getUsername()).thenReturn(username);
    when(user.getPassword()).thenReturn("pw");
    when(user.isActive()).thenReturn(true);
    when(userService.getUser(username)).thenReturn(user);

    when(membership.getRole()).thenReturn(role);
    when(role.getName()).thenReturn("MY_ROLE");
    when(roleMembershipService.getCurrentMemberships(user)).thenReturn(List.of(membership));

    Set<GrantedAuthority> userAuthorities = new LinkedHashSet<>();
    userAuthorities.add(new SimpleGrantedAuthority("ROLE_USER"));
    userAuthorities.add(new SimpleGrantedAuthority("ROLE_MY_ROLE"));
    Collection<GrantedAuthority> mappedAuthorities =
        singleton(new SimpleGrantedAuthority("ROLE_MAPPED"));
    when(grantedAuthoritiesMapper.mapAuthorities(userAuthorities))
        .thenReturn((Collection) mappedAuthorities);

    UserDetails userDetails = userDetailsServiceImpl.loadUserByUsername(username);
    assertEquals(username, userDetails.getUsername());
    assertEquals(mappedAuthorities, userDetails.getAuthorities());
  }

  @SuppressWarnings("unchecked")
  @Test
  void testLoadUserByUsernameAnonymous() {
    String username = "anonymous";
    when(user.getUsername()).thenReturn(username);
    when(user.getPassword()).thenReturn("pw");
    when(user.isActive()).thenReturn(true);
    when(user.isSuperuser()).thenReturn(false);
    when(userService.getUser(username)).thenReturn(user);

    when(membership.getRole()).thenReturn(role);
    when(role.getName()).thenReturn("MY_ROLE");
    when(roleMembershipService.getCurrentMemberships(user)).thenReturn(List.of(membership));

    Set<GrantedAuthority> userAuthorities = new LinkedHashSet<>();
    userAuthorities.add(new SimpleGrantedAuthority("ROLE_ANONYMOUS"));
    userAuthorities.add(new SimpleGrantedAuthority("ROLE_MY_ROLE"));
    Collection<GrantedAuthority> mappedAuthorities =
        singleton(new SimpleGrantedAuthority("ROLE_MAPPED"));
    when(grantedAuthoritiesMapper.mapAuthorities(userAuthorities))
        .thenReturn((Collection) mappedAuthorities);

    UserDetails userDetails = userDetailsServiceImpl.loadUserByUsername(username);
    assertEquals(username, userDetails.getUsername());
    assertEquals(mappedAuthorities, userDetails.getAuthorities());
  }

  @Test
  void testLoadUserByUsernameUnknownUser() {
    Exception exception =
        assertThrows(
            UsernameNotFoundException.class,
            () -> userDetailsServiceImpl.loadUserByUsername("unknownUser"));
    assertThat(exception.getMessage()).containsPattern("unknown user 'unknownUser'");
  }
}
