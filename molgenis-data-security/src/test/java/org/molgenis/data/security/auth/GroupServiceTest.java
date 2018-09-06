package org.molgenis.data.security.auth;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.molgenis.data.meta.model.PackageMetadata.PACKAGE;
import static org.molgenis.data.security.auth.GroupMetadata.GROUP;
import static org.molgenis.data.security.auth.GroupService.EDITOR;
import static org.molgenis.data.security.auth.GroupService.MANAGER;
import static org.molgenis.data.security.auth.RoleMetadata.NAME;
import static org.molgenis.data.security.auth.RoleMetadata.ROLE;
import static org.testng.Assert.*;

import com.google.common.collect.ImmutableList;
import java.util.stream.Stream;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.Fetch;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.model.PackageFactory;
import org.molgenis.data.meta.model.PackageMetadata;
import org.molgenis.data.security.exception.IsAlreadyMemberException;
import org.molgenis.data.security.exception.NotAValidGroupRoleException;
import org.molgenis.data.security.permission.RoleMembershipService;
import org.molgenis.security.core.model.GroupValue;
import org.molgenis.security.core.model.PackageValue;
import org.molgenis.security.core.model.RoleValue;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class GroupServiceTest extends AbstractMockitoTest {
  @Mock private GroupFactory groupFactory;
  @Mock private RoleFactory roleFactory;
  @Mock private PackageFactory packageFactory;

  @Mock(answer = RETURNS_DEEP_STUBS)
  private DataService dataService;

  @Mock private GroupMetadata groupMetadata;
  @Mock private RoleMembershipService roleMembershipService;
  @Mock private RoleMetadata roleMetadata;
  @Mock private Attribute attribute;

  @Mock private Group group;
  @Mock private Role managerRole;
  @Mock private Role editorRole;
  @Mock private User user;
  @Mock private Package aPackage;
  @Mock private RoleMembershipMetadata roleMembershipMetadata;
  @Mock private RoleMembership membership;

  @Captor private ArgumentCaptor<Stream<Role>> roleCaptor;

  private GroupService groupService;

  private GroupValue groupValue;
  private RoleValue managerRoleValue;
  private RoleValue editorRoleValue;
  private PackageValue packageValue;

  private static final String MANAGER_NAME = "GROUP_MANAGER";
  private static final String EDITOR_NAME = "GROUP_EDITOR";

  @BeforeMethod
  public void beforeMethod() {
    packageValue = PackageValue.builder().setName("package").setLabel("Package").build();
    managerRoleValue = RoleValue.builder().setName(MANAGER_NAME).setLabel(MANAGER).build();
    editorRoleValue = RoleValue.builder().setName(EDITOR_NAME).setLabel(EDITOR).build();
    GroupValue.Builder builder =
        GroupValue.builder()
            .setRootPackage(packageValue)
            .setName("group")
            .setLabel("Group")
            .setPublic(true)
            .setDescription("description");

    builder.rolesBuilder().add(managerRoleValue);
    builder.rolesBuilder().add(editorRoleValue);
    groupValue = builder.build();

    groupService =
        new GroupService(
            groupFactory,
            roleFactory,
            packageFactory,
            dataService,
            groupMetadata,
            roleMembershipService,
            roleMetadata,
            roleMembershipMetadata);
  }

  @Test
  public void testPersist() {
    when(groupFactory.create(groupValue)).thenReturn(group);
    when(packageFactory.create(packageValue)).thenReturn(aPackage);

    when(editorRole.getName()).thenReturn(EDITOR_NAME);
    when(editorRole.getLabel()).thenReturn(EDITOR);
    doReturn(editorRole).when(roleFactory).create(editorRoleValue);

    when(managerRole.getName()).thenReturn(MANAGER_NAME);
    when(managerRole.getLabel()).thenReturn(MANAGER);
    doReturn(managerRole).when(roleFactory).create(managerRoleValue);

    Role defaultEditorRole = mock(Role.class);
    when(defaultEditorRole.getLabel()).thenReturn(EDITOR);

    Role defaultManagerRole = mock(Role.class);
    when(defaultManagerRole.getIncludes()).thenReturn(singletonList(defaultEditorRole));

    when(dataService.query(ROLE, Role.class).eq(eq(RoleMetadata.NAME), any(String.class)).findOne())
        .thenReturn(defaultManagerRole, defaultEditorRole);

    groupService.persist(groupValue);

    verify(dataService).add(GROUP, group);
    verify(dataService).add(eq(ROLE), roleCaptor.capture());
    verify(dataService).add(PackageMetadata.PACKAGE, aPackage);
    assertEquals(roleCaptor.getValue().collect(toList()), asList(managerRole, editorRole));

    verify(managerRole).setGroup(group);
    verify(managerRole).setIncludes(asList(defaultManagerRole, editorRole));

    verify(editorRole).setGroup(group);
    verify(editorRole).setIncludes(singletonList(defaultEditorRole));
  }

  @Test(
      expectedExceptions = UnknownEntityException.class,
      expectedExceptionsMessageRegExp = "type:sys_sec_Role id:MANAGER attribute:name")
  public void testPersistIncludedRoleNotFound() {
    doReturn(editorRole).when(roleFactory).create(editorRoleValue);
    doReturn(managerRole).when(roleFactory).create(managerRoleValue);
    when(managerRole.getName()).thenReturn(MANAGER_NAME);
    when(managerRole.getLabel()).thenReturn(MANAGER);
    when(editorRole.getName()).thenReturn(EDITOR_NAME);
    when(packageFactory.create(packageValue)).thenReturn(aPackage);

    when(roleMetadata.getId()).thenReturn(ROLE);
    when(roleMetadata.getAttribute(NAME)).thenReturn(attribute);
    when(attribute.getName()).thenReturn(NAME);

    when(dataService.query(ROLE, Role.class).eq(RoleMetadata.NAME, MANAGER.toUpperCase()).findOne())
        .thenReturn(null);

    groupService.persist(groupValue);
  }

  @Test
  public void testGetGroups() {
    when(dataService.findAll(GROUP, Group.class)).thenReturn(Stream.of(group));
    assertEquals(groupService.getGroups(), singletonList(group));
  }

  @Test
  public void testGetGroup() {
    Fetch fetch = buildGroupFetch();
    when(dataService
            .query(GroupMetadata.GROUP, Group.class)
            .eq(GroupMetadata.NAME, "devs")
            .fetch(fetch)
            .findOne())
        .thenReturn(group);
    assertEquals(groupService.getGroup("devs"), group);
  }

  @Test(expectedExceptions = UnknownEntityException.class)
  public void testGetGroupNotFound() {
    Fetch fetch = buildGroupFetch();
    when(groupMetadata.getAttribute(GroupMetadata.NAME)).thenReturn(attribute);
    when(dataService
            .query(GroupMetadata.GROUP, Group.class)
            .eq(GroupMetadata.NAME, "devs")
            .fetch(fetch)
            .findOne())
        .thenReturn(null);
    groupService.getGroup("devs");
  }

  private Fetch buildGroupFetch() {
    Fetch roleFetch = new Fetch().field(RoleMetadata.NAME).field(RoleMetadata.LABEL);
    return new Fetch()
        .field(GroupMetadata.ROLES, roleFetch)
        .field(GroupMetadata.NAME)
        .field(GroupMetadata.LABEL)
        .field(GroupMetadata.DESCRIPTION)
        .field(GroupMetadata.ID)
        .field(GroupMetadata.PUBLIC)
        .field(GroupMetadata.ROOT_PACKAGE);
  }

  @Test
  public void testAddMemberHappyPath() {
    when(group.getRoles()).thenReturn(singletonList(managerRole));
    when(managerRole.getName()).thenReturn("dev");

    when(roleMembershipService.getMemberships(singletonList(managerRole))).thenReturn(emptyList());

    groupService.addMember(group, user, managerRole);

    verify(roleMembershipService).addUserToRole(user, managerRole);
  }

  @Test(expectedExceptions = IsAlreadyMemberException.class)
  public void testAddMemberAlreadyAMember() {
    when(group.getRoles()).thenReturn(singletonList(managerRole));
    when(managerRole.getName()).thenReturn("dev");

    when(roleMembershipService.getMemberships(singletonList(managerRole)))
        .thenReturn(singletonList(membership));
    when(membership.getUser()).thenReturn(user);

    groupService.addMember(group, user, managerRole);
  }

  @Test(expectedExceptions = NotAValidGroupRoleException.class)
  public void testAddMemberNotAValidGroupRole() {
    when(group.getRoles()).thenReturn(emptyList());

    groupService.addMember(group, user, managerRole);
  }

  @Test
  public void testRemoveMember() {
    when(group.getRoles()).thenReturn(singletonList(managerRole));
    when(roleMembershipService.getMemberships(singletonList(managerRole)))
        .thenReturn(singletonList(membership));
    when(membership.getUser()).thenReturn(user);
    when(user.getId()).thenReturn("userID");

    groupService.removeMember(group, user);

    verify(roleMembershipService).removeMembership(membership);
  }

  @Test(expectedExceptions = UnknownEntityException.class)
  public void testRemoveMemberNotAMember() {
    when(group.getRoles()).thenReturn(singletonList(managerRole));
    when(roleMembershipService.getMemberships(singletonList(managerRole))).thenReturn(emptyList());
    when(roleMembershipMetadata.getAttribute(RoleMembershipMetadata.USER)).thenReturn(attribute);
    when(user.getUsername()).thenReturn("henkie");

    groupService.removeMember(group, user);
  }

  @Test
  public void testUpdateMemberRole() {
    when(managerRole.getName()).thenReturn(MANAGER_NAME);
    when(editorRole.getName()).thenReturn(EDITOR_NAME);
    when(group.getRoles()).thenReturn(ImmutableList.of(managerRole, editorRole));
    when(roleMembershipService.getMemberships(ImmutableList.of(managerRole, editorRole)))
        .thenReturn(singletonList(membership));
    when(membership.getUser()).thenReturn(user);
    when(user.getId()).thenReturn("userID");

    groupService.updateMemberRole(group, user, editorRole);

    verify(roleMembershipService).updateMembership(membership, editorRole);
  }

  @Test(expectedExceptions = NotAValidGroupRoleException.class)
  public void testUpdateMemberRoleNotAGroupRole() {
    when(group.getRoles()).thenReturn(ImmutableList.of(managerRole));
    when(managerRole.getName()).thenReturn(MANAGER_NAME);
    when(editorRole.getName()).thenReturn(EDITOR_NAME);

    groupService.updateMemberRole(group, user, editorRole);
  }

  @Test(expectedExceptions = UnknownEntityException.class)
  public void testUpdateMemberRoleUserNotAMember() {
    when(group.getRoles()).thenReturn(ImmutableList.of(managerRole, editorRole));
    when(roleMembershipService.getMemberships(ImmutableList.of(managerRole, editorRole)))
        .thenReturn(emptyList());
    when(roleMembershipMetadata.getAttribute(RoleMembershipMetadata.USER)).thenReturn(attribute);
    when(user.getUsername()).thenReturn("henkie");
    when(managerRole.getName()).thenReturn(MANAGER_NAME);
    when(editorRole.getName()).thenReturn(EDITOR_NAME);

    groupService.updateMemberRole(group, user, editorRole);
  }

  @Test
  public void testIsGroupNameAvailableReturnsTrueIfNameIsAvailable() {
    when(dataService.query(PACKAGE, Package.class).eq(PackageMetadata.ID, "foo_bar").findOne())
        .thenReturn(null);

    PackageValue rootPackage = PackageValue.builder().setName("foo_bar").setLabel("label").build();
    final GroupValue groupValue =
        GroupValue.builder()
            .setRootPackage(rootPackage)
            .setPublic(true)
            .setName("foo-bar")
            .setLabel("label")
            .build();

    assertTrue(groupService.isGroupNameAvailable(groupValue));
  }

  @Test
  public void testIsGroupNameAvailableReturnsFalseIfNameIsNotAvailable() {
    Package mock = mock(Package.class);
    when(dataService.query(PACKAGE, Package.class).eq(PackageMetadata.ID, "foo_bar").findOne())
        .thenReturn(mock);

    PackageValue rootPackage = PackageValue.builder().setName("foo_bar").setLabel("label").build();
    final GroupValue groupValue =
        GroupValue.builder()
            .setRootPackage(rootPackage)
            .setPublic(true)
            .setName("foo-bar")
            .setLabel("label")
            .build();

    assertFalse(groupService.isGroupNameAvailable(groupValue));
  }
}
