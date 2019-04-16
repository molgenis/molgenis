package org.molgenis.api.permissions;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.mockito.Mock;
import org.molgenis.api.permissions.rsql.PermissionsQuery;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.DataService;
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
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.Sid;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class UserRoleToolsTest extends AbstractMolgenisSpringTest {

  @Mock UserPermissionEvaluator userPermissionEvaluator;
  @Mock DataService dataService;
  @Mock UserService userService;

  private UserRoleTools userRoleTools;

  @BeforeMethod
  private void setUp() {

    this.userRoleTools = new UserRoleTools(userService, dataService, userPermissionEvaluator);
  }

  @Test
  public void testGetSids() {
    @SuppressWarnings("unchecked")
    Query<Role> query = mock(Query.class);
    when(dataService.query(RoleMetadata.ROLE, Role.class)).thenReturn(query);
    when(query.eq(RoleMetadata.NAME, "ROLE")).thenReturn(query);
    when(query.findOne()).thenReturn(mock(Role.class));

    when(userService.getUser("user")).thenReturn(mock(User.class));
    PermissionsQuery permissionsQuery =
        new PermissionsQuery(Collections.singletonList("user"), Collections.singletonList("role"));
    assertTrue(
        userRoleTools
            .getSids(permissionsQuery)
            .containsAll(
                Arrays.asList(new PrincipalSid("user"), new GrantedAuthoritySid("ROLE_role"))));
  }

  @Test
  public void testGetSid() {
    when(userService.getUser("user")).thenReturn(mock(User.class));
    assertEquals(userRoleTools.getSid("user", null), new PrincipalSid("user"));
  }

  @Test
  public void testGetSidRole() {
    @SuppressWarnings("unchecked")
    Query<Role> query = mock(Query.class);
    when(dataService.query(RoleMetadata.ROLE, Role.class)).thenReturn(query);
    when(query.eq(RoleMetadata.NAME, "ROLE")).thenReturn(query);
    when(query.findOne()).thenReturn(mock(Role.class));

    assertEquals(userRoleTools.getSid(null, "role"), new GrantedAuthoritySid("ROLE_role"));
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

    Repository roleRepository = mock(Repository.class);
    Query roleQuery = mock(Query.class);
    Query role1Query = mock(Query.class);
    Query role2Query = mock(Query.class);
    Query role3Query = mock(Query.class);

    doReturn(role1Query).when(roleQuery).eq(RoleMetadata.NAME, "role1");
    when(role1Query.findOne()).thenReturn(role1);

    doReturn(role2Query).when(roleQuery).eq(RoleMetadata.NAME, "role2");
    when(role2Query.findOne()).thenReturn(role2);

    doReturn(role3Query).when(roleQuery).eq(RoleMetadata.NAME, "role3");
    when(role3Query.findOne()).thenReturn(role3);

    when(roleRepository.query()).thenReturn(roleQuery);
    doReturn(roleRepository).when(dataService).getRepository(RoleMetadata.ROLE, Role.class);

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

    Set<Sid> expected = Sets.newHashSet(role1Sid, role2Sid, role3Sid);
    assertEquals(userRoleTools.getRoles(user), expected);
  }
}
