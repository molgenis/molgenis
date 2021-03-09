package org.molgenis.data.security.permission;

import static com.google.common.collect.ImmutableList.of;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.data.security.auth.RoleMembershipMetadata.ROLE_MEMBERSHIP;
import static org.molgenis.data.security.auth.UserMetadata.USERNAME;

import com.google.common.collect.ImmutableList;
import java.time.Duration;
import java.time.Instant;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.Fetch;
import org.molgenis.data.Query;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.security.auth.Role;
import org.molgenis.data.security.auth.RoleMembership;
import org.molgenis.data.security.auth.RoleMembershipFactory;
import org.molgenis.data.security.auth.RoleMembershipMetadata;
import org.molgenis.data.security.auth.RoleMetadata;
import org.molgenis.data.security.auth.User;
import org.molgenis.data.security.auth.UserMetadata;
import org.molgenis.data.security.user.UserService;
import org.molgenis.test.AbstractMockitoTest;

class RoleMembershipServiceImplTest extends AbstractMockitoTest {
  @Mock private UserService userService;
  @Mock private RoleMembershipFactory roleMembershipFactory;
  @Mock private DataService dataService;
  @Mock private UserMetadata userMetadata;
  @Mock private RoleMetadata roleMetadata;
  @Mock private RoleMembership roleMembership;
  @Mock private RoleMembership oldRoleMembership;
  @Mock private Role viewer;
  @Mock private Role editor;

  @Captor private ArgumentCaptor<Instant> instantCaptor;

  private RoleMembershipServiceImpl roleMembershipService;

  @BeforeEach
  void beforeMethod() {
    roleMembershipService =
        new RoleMembershipServiceImpl(
            userService, roleMembershipFactory, dataService, userMetadata, roleMetadata);
  }

  @Test
  void addUserToRole() {
    String username = "henk";
    User user = mock(User.class);
    when(userService.getUser(username)).thenReturn(user);

    Role role = mock(Role.class);
    String rolename = "GCC_MANAGER";
    @SuppressWarnings("unchecked")
    Query<Role> query = mock(Query.class, RETURNS_SELF);
    when(dataService.query(RoleMetadata.ROLE, Role.class)).thenReturn(query);
    when(query.eq(RoleMetadata.NAME, rolename).findOne()).thenReturn(role);

    RoleMembership roleMembership = mock(RoleMembership.class);
    when(roleMembershipFactory.create()).thenReturn(roleMembership);

    roleMembershipService.addUserToRole(username, rolename);

    verify(dataService, times(1)).add(eq(ROLE_MEMBERSHIP), any(RoleMembership.class));
    verify(roleMembership).setRole(role);
    verify(roleMembership).setUser(user);
    verify(roleMembership).setFrom(instantCaptor.capture());

    assertTrue(Duration.between(Instant.now(), instantCaptor.getValue()).getSeconds() < 1);
  }

  @Test
  void addUserToNonExistingRole() {
    Attribute roleNameAttr = mock(Attribute.class);
    when(roleNameAttr.getName()).thenReturn("name");
    when(roleMetadata.getAttribute(RoleMetadata.NAME)).thenReturn(roleNameAttr);
    when(roleMetadata.getId()).thenReturn("sys_sec_Role");

    String username = "henk";
    User user = mock(User.class);
    when(userService.getUser(username)).thenReturn(user);

    String rolename = "GCC_DELETER";
    @SuppressWarnings("unchecked")
    Query<Role> query = mock(Query.class, RETURNS_SELF);
    when(dataService.query(RoleMetadata.ROLE, Role.class)).thenReturn(query);
    when(query.eq(RoleMetadata.NAME, rolename).findOne()).thenReturn(null);

    Exception exception =
        assertThrows(
            UnknownEntityException.class,
            () -> roleMembershipService.addUserToRole(username, rolename));
    assertThat(exception.getMessage())
        .containsPattern("type:sys_sec_Role id:GCC_DELETER attribute:name");
  }

  @Test
  void addNonExistingUserToRole() {
    Attribute userNameAttr = mock(Attribute.class);
    when(userNameAttr.getName()).thenReturn("username");
    when(userMetadata.getAttribute(USERNAME)).thenReturn(userNameAttr);
    when(userMetadata.getId()).thenReturn("sys_sec_User");

    String username = "henk";
    when(userService.getUser(username)).thenReturn(null);

    Exception exception =
        assertThrows(
            UnknownEntityException.class,
            () -> roleMembershipService.addUserToRole(username, "GCC_MANAGER"));
    assertThat(exception.getMessage())
        .containsPattern("type:sys_sec_User id:henk attribute:username");
  }

  @Test
  void testRemoveMembership() {
    roleMembershipService.removeMembership(roleMembership);

    verify(dataService).delete(ROLE_MEMBERSHIP, roleMembership);
  }

  @Test
  void testUpdateMembership() {
    when(roleMembership.getId()).thenReturn("membershipId");
    when(dataService.findOneById(ROLE_MEMBERSHIP, "membershipId", RoleMembership.class))
        .thenReturn(roleMembership);

    roleMembershipService.updateMembership(roleMembership, editor);

    verify(roleMembership).setRole(editor);
    verify(dataService).update(ROLE_MEMBERSHIP, roleMembership);
  }

  @Test
  void testUpdateMembershipNotAMember() {
    when(roleMembership.getId()).thenReturn("membershipId");
    when(dataService.findOneById(ROLE_MEMBERSHIP, "membershipId", RoleMembership.class))
        .thenReturn(null);

    assertThrows(
        UnknownEntityException.class,
        () -> roleMembershipService.updateMembership(roleMembership, editor));
  }

  @Test
  void testGetMemberships() {
    Fetch roleFetch = new Fetch().field(RoleMetadata.NAME).field(RoleMetadata.LABEL);
    Fetch userFetch = new Fetch().field(UserMetadata.USERNAME).field(UserMetadata.ID);
    Fetch fetch =
        new Fetch()
            .field(RoleMembershipMetadata.ROLE, roleFetch)
            .field(RoleMembershipMetadata.USER, userFetch)
            .field(RoleMembershipMetadata.FROM)
            .field(RoleMembershipMetadata.TO)
            .field(RoleMembershipMetadata.ID);

    @SuppressWarnings("unchecked")
    Query<RoleMembership> query = mock(Query.class, RETURNS_SELF);
    when(dataService.query(RoleMembershipMetadata.ROLE_MEMBERSHIP, RoleMembership.class))
        .thenReturn(query);
    when(query
            .in(RoleMembershipMetadata.ROLE, ImmutableList.of(editor, viewer))
            .fetch(fetch)
            .findAll())
        .thenReturn(Stream.of(oldRoleMembership, roleMembership));

    when(roleMembership.isCurrent()).thenReturn(true);

    assertEquals(
        singletonList(roleMembership), roleMembershipService.getMemberships(of(editor, viewer)));
  }

  @Test
  void testGetCurrentMemberships() {
    User user = mock(User.class);

    Fetch roleFetch = new Fetch().field(RoleMetadata.NAME).field(RoleMetadata.LABEL);
    Fetch userFetch = new Fetch().field(UserMetadata.USERNAME).field(UserMetadata.ID);
    Fetch fetch =
        new Fetch()
            .field(RoleMembershipMetadata.ROLE, roleFetch)
            .field(RoleMembershipMetadata.USER, userFetch)
            .field(RoleMembershipMetadata.FROM)
            .field(RoleMembershipMetadata.TO)
            .field(RoleMembershipMetadata.ID);

    @SuppressWarnings("unchecked")
    Query<RoleMembership> query = mock(Query.class, RETURNS_SELF);
    when(dataService.query(RoleMembershipMetadata.ROLE_MEMBERSHIP, RoleMembership.class))
        .thenReturn(query);
    when(query.eq(RoleMembershipMetadata.USER, user).fetch(fetch).findAll())
        .thenReturn(Stream.of(oldRoleMembership, roleMembership));

    when(roleMembership.isCurrent()).thenReturn(true);

    assertEquals(singletonList(roleMembership), roleMembershipService.getCurrentMemberships(user));
  }
}
