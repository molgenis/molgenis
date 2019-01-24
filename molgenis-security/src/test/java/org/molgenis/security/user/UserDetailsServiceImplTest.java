package org.molgenis.security.user;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.security.auth.RoleMembershipMetadata.ROLE_MEMBERSHIP;
import static org.molgenis.data.security.auth.UserMetadata.USER;
import static org.molgenis.data.security.auth.UserMetadata.USERNAME;
import static org.testng.Assert.assertEquals;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Stream;
import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.data.security.auth.Role;
import org.molgenis.data.security.auth.RoleMembership;
import org.molgenis.data.security.auth.RoleMembershipMetadata;
import org.molgenis.data.security.auth.User;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class UserDetailsServiceImplTest extends AbstractMockitoTest {
  @Mock private GrantedAuthoritiesMapper grantedAuthoritiesMapper;
  @Mock private DataService dataService;
  @Mock private User user;
  @Mock private RoleMembership currentMembership;
  @Mock private RoleMembership pastMembership;
  @Mock private Role role;

  private UserDetailsServiceImpl userDetailsServiceImpl;

  @BeforeMethod
  public void setUp() {
    userDetailsServiceImpl = new UserDetailsServiceImpl(dataService, grantedAuthoritiesMapper);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void testUserDetailsService() {
    new UserDetailsServiceImpl(null, null);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testLoadUserByUsernameSuperuser() {
    String username = "user";
    when(user.getUsername()).thenReturn(username);
    when(user.getPassword()).thenReturn("pw");
    when(user.isActive()).thenReturn(true);
    when(user.isSuperuser()).thenReturn(true);
    Query<User> userQuery = mock(Query.class, RETURNS_SELF);
    doReturn(userQuery).when(dataService).query(USER, User.class);
    when(userQuery.eq(USERNAME, username).findOne()).thenReturn(user);

    when(currentMembership.isCurrent()).thenReturn(true);
    when(currentMembership.getRole()).thenReturn(role);
    when(role.getName()).thenReturn("MY_ROLE");
    Query<RoleMembership> roleMembershipQuery = mock(Query.class, RETURNS_SELF);
    doReturn(roleMembershipQuery).when(dataService).query(ROLE_MEMBERSHIP, RoleMembership.class);
    when(roleMembershipQuery.eq(RoleMembershipMetadata.USER, user).findAll())
        .thenReturn(Stream.of(currentMembership, pastMembership));

    Set<GrantedAuthority> userAuthorities = new LinkedHashSet<>();
    userAuthorities.add(new SimpleGrantedAuthority("ROLE_SU"));
    userAuthorities.add(new SimpleGrantedAuthority("ROLE_USER"));
    userAuthorities.add(new SimpleGrantedAuthority("ROLE_MY_ROLE"));
    Collection<GrantedAuthority> mappedAuthorities =
        singletonList(new SimpleGrantedAuthority("ROLE_MAPPED"));
    when(grantedAuthoritiesMapper.mapAuthorities(userAuthorities))
        .thenReturn((Collection) mappedAuthorities);

    UserDetails userDetails = userDetailsServiceImpl.loadUserByUsername(username);
    assertEquals(userDetails.getUsername(), username);
    assertEquals(userDetails.getAuthorities(), mappedAuthorities);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testLoadUserByUsernameNonSuperuser() {
    String username = "user";
    User user = mock(User.class);
    when(user.getUsername()).thenReturn(username);
    when(user.getPassword()).thenReturn("pw");
    when(user.isActive()).thenReturn(true);
    Query<User> userQuery = mock(Query.class, RETURNS_SELF);
    doReturn(userQuery).when(dataService).query(USER, User.class);
    when(userQuery.eq(USERNAME, username).findOne()).thenReturn(user);

    when(currentMembership.isCurrent()).thenReturn(true);
    when(currentMembership.getRole()).thenReturn(role);
    when(role.getName()).thenReturn("MY_ROLE");
    Query<RoleMembership> roleMembershipQuery = mock(Query.class, RETURNS_SELF);
    doReturn(roleMembershipQuery).when(dataService).query(ROLE_MEMBERSHIP, RoleMembership.class);
    when(roleMembershipQuery.eq(RoleMembershipMetadata.USER, user).findAll())
        .thenReturn(Stream.of(currentMembership, pastMembership));

    Set<GrantedAuthority> userAuthorities = new LinkedHashSet<>();
    userAuthorities.add(new SimpleGrantedAuthority("ROLE_USER"));
    userAuthorities.add(new SimpleGrantedAuthority("ROLE_MY_ROLE"));
    Collection<GrantedAuthority> mappedAuthorities =
        singletonList(new SimpleGrantedAuthority("ROLE_MAPPED"));
    when(grantedAuthoritiesMapper.mapAuthorities(userAuthorities))
        .thenReturn((Collection) mappedAuthorities);

    UserDetails userDetails = userDetailsServiceImpl.loadUserByUsername(username);
    assertEquals(userDetails.getUsername(), username);
    assertEquals(userDetails.getAuthorities(), mappedAuthorities);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testLoadUserByUsernameAnonymous() {
    String username = "anonymous";
    when(user.getUsername()).thenReturn(username);
    when(user.getPassword()).thenReturn("pw");
    when(user.isActive()).thenReturn(true);
    when(user.isSuperuser()).thenReturn(false);
    Query<User> userQuery = mock(Query.class, RETURNS_SELF);
    doReturn(userQuery).when(dataService).query(USER, User.class);
    when(userQuery.eq(USERNAME, username).findOne()).thenReturn(user);

    when(currentMembership.isCurrent()).thenReturn(true);
    when(currentMembership.getRole()).thenReturn(role);
    when(role.getName()).thenReturn("MY_ROLE");
    Query<RoleMembership> roleMembershipQuery = mock(Query.class, RETURNS_SELF);
    doReturn(roleMembershipQuery).when(dataService).query(ROLE_MEMBERSHIP, RoleMembership.class);
    when(roleMembershipQuery.eq(RoleMembershipMetadata.USER, user).findAll())
        .thenReturn(Stream.of(currentMembership, pastMembership));

    Set<GrantedAuthority> userAuthorities = new LinkedHashSet<>();
    userAuthorities.add(new SimpleGrantedAuthority("ROLE_ANONYMOUS"));
    userAuthorities.add(new SimpleGrantedAuthority("ROLE_MY_ROLE"));
    Collection<GrantedAuthority> mappedAuthorities =
        singletonList(new SimpleGrantedAuthority("ROLE_MAPPED"));
    when(grantedAuthoritiesMapper.mapAuthorities(userAuthorities))
        .thenReturn((Collection) mappedAuthorities);

    UserDetails userDetails = userDetailsServiceImpl.loadUserByUsername(username);
    assertEquals(userDetails.getUsername(), username);
    assertEquals(userDetails.getAuthorities(), mappedAuthorities);
  }

  @Test(
      expectedExceptions = UsernameNotFoundException.class,
      expectedExceptionsMessageRegExp = "unknown user 'unknownUser'")
  public void testLoadUserByUsernameUnknownUser() {
    String username = "unknownUser";
    @SuppressWarnings("unchecked")
    Query<User> userQuery = mock(Query.class, RETURNS_SELF);
    doReturn(userQuery).when(dataService).query(USER, User.class);
    when(userQuery.eq(USERNAME, username).findOne()).thenReturn(null);

    userDetailsServiceImpl.loadUserByUsername(username);
  }
}
