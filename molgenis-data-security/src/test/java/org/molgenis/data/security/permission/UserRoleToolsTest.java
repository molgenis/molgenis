package org.molgenis.data.security.permission;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.data.security.EntityTypePermission;
import org.molgenis.data.security.auth.Role;
import org.molgenis.data.security.auth.RoleMembership;
import org.molgenis.data.security.auth.RoleMembershipMetadata;
import org.molgenis.data.security.auth.RoleMetadata;
import org.molgenis.data.security.auth.User;
import org.molgenis.data.security.user.UserService;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.Sid;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class UserRoleToolsTest extends AbstractMockitoTest {
  @Mock UserPermissionEvaluator userPermissionEvaluator;
  @Mock DataService dataService;
  @Mock UserService userService;

  private UserRoleTools userRoleTools;

  @BeforeMethod
  private void setUpBeforeMethod() {
    this.userRoleTools = new UserRoleTools(userService, dataService, userPermissionEvaluator);
  }

  @Test
  public void testGetUser() {
    assertEquals(UserRoleTools.getUser(new PrincipalSid("test")), "test");
  }

  @Test
  public void testGetRole() {
    assertEquals(UserRoleTools.getRole(new GrantedAuthoritySid("ROLE_test")), "test");
  }

  @Test
  public void testGetNameRole() {
    assertEquals(UserRoleTools.getName(new GrantedAuthoritySid("ROLE_test")), "test");
  }

  @Test
  public void testGetName() {
    assertEquals(UserRoleTools.getName(new PrincipalSid("test")), "test");
  }

  @Test
  public void testGetRolesForSid() {
    User molgenisUser = mock(User.class);
    when(molgenisUser.getId()).thenReturn("user");
    when(userService.getUser("user")).thenReturn(molgenisUser);

    PrincipalSid user = mock(PrincipalSid.class);
    when(user.getPrincipal()).thenReturn("user");
    Role role1 = mock(Role.class);
    when(role1.getName()).thenReturn("role1");
    Role role2 = mock(Role.class);
    when(role2.getName()).thenReturn("role2");

    RoleMembership roleMembership1 = mock(RoleMembership.class);
    RoleMembership roleMembership2 = mock(RoleMembership.class);

    when(roleMembership1.getRole()).thenReturn(role1);
    when(roleMembership2.getRole()).thenReturn(role2);

    Repository userRepository = mock(Repository.class);
    Query userQuery = mock(Query.class);
    when(userQuery.eq(RoleMembershipMetadata.USER, "user")).thenReturn(userQuery);
    when(userQuery.findAll()).thenReturn(Stream.of(roleMembership1, roleMembership2));
    when(userRepository.query()).thenReturn(userQuery);
    doReturn(userRepository)
        .when(dataService)
        .getRepository(RoleMembershipMetadata.ROLE_MEMBERSHIP, RoleMembership.class);
    doReturn(true)
        .when(userPermissionEvaluator)
        .hasPermission(
            new EntityTypeIdentity(RoleMembershipMetadata.ROLE_MEMBERSHIP),
            EntityTypePermission.READ_DATA);

    List<Sid> expected =
        Arrays.asList(new GrantedAuthoritySid("ROLE_role1"), new GrantedAuthoritySid("ROLE_role2"));
    UserRoleTools userRoleTools =
        new UserRoleTools(userService, dataService, userPermissionEvaluator);
    assertEquals(userRoleTools.getRolesForSid(user), expected);
  }

  @Test
  public void testGetInheritedPermissions() {
    User molgenisUser = mock(User.class);
    when(molgenisUser.getId()).thenReturn("user");
    when(userService.getUser("user")).thenReturn(molgenisUser);
    PrincipalSid user = mock(PrincipalSid.class);
    when(user.getPrincipal()).thenReturn("user");
    Role role3 = mock(Role.class);
    when(role3.getName()).thenReturn("role3");
    when(role3.getIncludes()).thenReturn(Collections.emptyList());
    Sid role1Sid = new GrantedAuthoritySid("ROLE_role1");
    Role role2 = mock(Role.class);
    when(role2.getName()).thenReturn("role2");
    when(role2.getIncludes()).thenReturn(Collections.emptyList());
    Sid role2Sid = new GrantedAuthoritySid("ROLE_role2");
    Role role1 = mock(Role.class);
    when(role1.getName()).thenReturn("role1");
    when(role1.getIncludes()).thenReturn(Collections.singletonList(role3));
    Sid role3Sid = new GrantedAuthoritySid("ROLE_role3");
    RoleMembership roleMembership1 = mock(RoleMembership.class);
    RoleMembership roleMembership2 = mock(RoleMembership.class);

    when(roleMembership1.getRole()).thenReturn(role1);
    when(roleMembership2.getRole()).thenReturn(role2);

    Repository userRepository = mock(Repository.class);
    Query userQuery = mock(Query.class);
    when(userQuery.eq(RoleMembershipMetadata.USER, "user")).thenReturn(userQuery);
    when(userQuery.findAll()).thenAnswer(invocation -> Stream.of(roleMembership1, roleMembership2));
    when(userRepository.query()).thenReturn(userQuery);
    doReturn(userRepository)
        .when(dataService)
        .getRepository(RoleMembershipMetadata.ROLE_MEMBERSHIP, RoleMembership.class);

    doReturn(true)
        .when(userPermissionEvaluator)
        .hasPermission(
            new EntityTypeIdentity(RoleMembershipMetadata.ROLE_MEMBERSHIP),
            EntityTypePermission.READ_DATA);
    doReturn(true)
        .when(userPermissionEvaluator)
        .hasPermission(new EntityTypeIdentity(RoleMetadata.ROLE), EntityTypePermission.READ_DATA);

    Query query = mock(Query.class);
    Query query1 = mock(Query.class);
    Query query2 = mock(Query.class);
    Query query3 = mock(Query.class);
    when(dataService.query(RoleMetadata.ROLE, Role.class)).thenReturn(query);
    doReturn(query1).when(query).eq(RoleMetadata.NAME, "role1");
    doReturn(query2).when(query).eq(RoleMetadata.NAME, "role2");
    doReturn(query3).when(query).eq(RoleMetadata.NAME, "role3");
    when(query1.findOne()).thenReturn(role1);
    when(query2.findOne()).thenReturn(role2);
    when(query3.findOne()).thenReturn(role3);

    Set<Sid> expected = Sets.newHashSet(role1Sid, role2Sid, role3Sid);
    assertEquals(userRoleTools.getRoles(user), expected);
  }

  @Test
  public void testGetAllAvailableSids() {
    User user = mock(User.class);
    when(user.getUsername()).thenReturn("username");
    when(userService.getUsers()).thenReturn(Collections.singletonList(user));

    Role role = mock(Role.class);
    when(role.getString(RoleMetadata.NAME)).thenReturn("role1");
    List<Entity> roles = Collections.singletonList(role);
    when(dataService.findAll(RoleMetadata.ROLE)).thenReturn(roles.stream());
    assertEquals(
        userRoleTools.getAllAvailableSids(),
        Sets.newHashSet(new GrantedAuthoritySid("ROLE_role1"), new PrincipalSid("username")));
  }

  @Test
  public void testGetSids() {
    List<Sid> expected =
        Arrays.asList(
            new PrincipalSid("user1"),
            new PrincipalSid("user2"),
            new GrantedAuthoritySid("ROLE_role1"),
            new GrantedAuthoritySid("ROLE_role2"));

    Query query = mock(Query.class);

    doReturn(query).when(query).eq(RoleMetadata.NAME, "ROLE1");
    doReturn(query).when(query).eq(RoleMetadata.NAME, "ROLE2");
    doReturn(mock(Role.class)).when(query).findOne();

    doReturn(mock(User.class)).when(userService).getUser("user1");
    doReturn(mock(User.class)).when(userService).getUser("user2");

    when(dataService.query(RoleMetadata.ROLE, Role.class)).thenReturn(query);
    when(query.findOne()).thenReturn(mock(Role.class));

    assertTrue(
        userRoleTools
            .getSids(Arrays.asList("user1", "user2"), Arrays.asList("role1", "role2"))
            .containsAll(expected));
  }

  @Test
  public void testSortSids() {
    Sid sid1 = new PrincipalSid("b");
    Sid sid2 = new PrincipalSid("a");
    Sid sid3 = new GrantedAuthoritySid("ROLE_b");
    Sid sid4 = new GrantedAuthoritySid("ROLE_a");
    LinkedList expected = new LinkedList<>();
    expected.addAll(Arrays.asList(sid2, sid4, sid1, sid3));
    assertEquals(userRoleTools.sortSids(Sets.newHashSet(sid1, sid2, sid3, sid4)), expected);
  }

  @Test
  public void testGetRoles() {
    Sid sid1 = new GrantedAuthoritySid("ROLE_a");
    Sid sid2 = new GrantedAuthoritySid("ROLE_b");
    LinkedList expected = new LinkedList<>();
    expected.addAll(Arrays.asList(sid2, sid1));

    Role role = mock(Role.class);
    Role roleA = mock(Role.class);
    when(roleA.getName()).thenReturn("a");
    Role roleB = mock(Role.class);
    when(roleB.getName()).thenReturn("b");
    when(role.getIncludes()).thenReturn(Arrays.asList(roleA, roleB));

    Query query = mock(Query.class);
    Query query1 = mock(Query.class);
    Query query2 = mock(Query.class);

    when(dataService.query(RoleMetadata.ROLE, Role.class)).thenReturn(query);
    doReturn(query).when(query).eq(RoleMetadata.NAME, "role1");
    doReturn(query1).when(query).eq(RoleMetadata.NAME, "a");
    doReturn(query2).when(query).eq(RoleMetadata.NAME, "b");
    when(query.findOne()).thenReturn(role);
    when(query1.findOne()).thenReturn(mock(Role.class));
    when(query2.findOne()).thenReturn(mock(Role.class));

    when(userPermissionEvaluator.hasPermission(
            new EntityTypeIdentity(RoleMetadata.ROLE), EntityTypePermission.READ_DATA))
        .thenReturn(true);

    assertTrue(
        userRoleTools
            .getRoles(
                Sets.newHashSet(
                    new GrantedAuthoritySid("ROLE_role1"),
                    new GrantedAuthoritySid("ROLE_a"),
                    new GrantedAuthoritySid("ROLE_b")))
            .containsAll(expected));
  }
}
