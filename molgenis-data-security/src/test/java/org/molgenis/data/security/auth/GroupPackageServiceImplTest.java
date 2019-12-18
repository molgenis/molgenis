package org.molgenis.data.security.auth;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Answers.RETURNS_SELF;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.molgenis.data.security.auth.GroupMetadata.GROUP;
import static org.molgenis.data.security.auth.GroupService.EDITOR;
import static org.molgenis.data.security.auth.GroupService.MANAGER;
import static org.molgenis.data.security.auth.RoleMetadata.ROLE;

import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.security.GroupIdentity;
import org.molgenis.data.security.permission.RoleMembershipService;
import org.molgenis.security.core.GroupValueFactory;
import org.molgenis.security.core.model.GroupValue;
import org.molgenis.security.core.model.RoleValue;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.security.acls.model.MutableAclService;

class GroupPackageServiceImplTest extends AbstractMockitoTest {

  @Mock private GroupValueFactory groupValueFactory;
  @Mock private RoleMembershipService roleMembershipService;
  @Mock private GroupPermissionService groupPermissionService;
  @Mock private RoleFactory roleFactory;
  @Mock private DataService dataService;
  @Mock private GroupFactory groupFactory;
  @Mock private RoleMetadata roleMetadata;
  @Mock private MutableAclService mutableAclService;

  @Captor private ArgumentCaptor<Stream<Role>> roleCaptor;
  @Captor private ArgumentCaptor<Stream<RoleMembership>> memberCaptor;

  private GroupPackageService groupPackageService;

  @BeforeEach
  void setUp() {
    groupPackageService =
        new GroupPackageServiceImpl(
            groupValueFactory,
            roleMembershipService,
            groupPermissionService,
            roleFactory,
            dataService,
            groupFactory,
            roleMetadata,
            mutableAclService);
  }

  @Test
  void testCreateGroup() {
    String id = "test";
    Package aPackage = mock(Package.class);
    when(aPackage.getId()).thenReturn(id);
    when(aPackage.getLabel()).thenReturn(id);
    GroupValue groupValue = mock(GroupValue.class);
    when(groupValue.getName()).thenReturn("group");
    Group group = mock(Group.class);
    Role editorRole = mock(Role.class);
    Role managerRole = mock(Role.class);

    RoleValue editorRoleValue =
        RoleValue.builder().setName("GROUP_EDITOR").setLabel(EDITOR).build();
    RoleValue managerRoleValue =
        RoleValue.builder().setName("GROUP_MANAGER").setLabel(MANAGER).build();

    when(groupValueFactory.createGroup(id, id, GroupService.DEFAULT_ROLES)).thenReturn(groupValue);
    when(groupFactory.create(groupValue)).thenReturn(group);

    when(editorRole.getName()).thenReturn("GROUP_EDITOR");
    when(editorRole.getLabel()).thenReturn(EDITOR);
    doReturn(editorRole).when(roleFactory).create(editorRoleValue);

    when(managerRole.getName()).thenReturn("GROUP_MANAGER");
    when(managerRole.getLabel()).thenReturn(MANAGER);
    doReturn(managerRole).when(roleFactory).create(managerRoleValue);

    Role defaultEditorRole = mock(Role.class);
    when(defaultEditorRole.getLabel()).thenReturn(EDITOR);

    Role defaultManagerRole = mock(Role.class);
    when(defaultManagerRole.getIncludes()).thenReturn(singletonList(defaultEditorRole));

    when(groupValue.getRoles()).thenReturn(ImmutableList.of(editorRoleValue, managerRoleValue));

    @SuppressWarnings("unchecked")
    Query<Role> query = mock(Query.class, RETURNS_SELF);
    when(dataService.query(ROLE, Role.class)).thenReturn(query);
    when(query.eq(eq(RoleMetadata.NAME), any(String.class)).findOne())
        .thenReturn(defaultManagerRole, defaultEditorRole);

    groupPackageService.createGroup(aPackage);

    verify(dataService).add(GROUP, group);
    verify(dataService).add(eq(ROLE), roleCaptor.capture());
    assertEquals(asList(managerRole, editorRole), roleCaptor.getValue().collect(toList()));

    verify(managerRole).setGroup(group);
    verify(managerRole).setIncludes(asList(defaultManagerRole, editorRole));

    verify(editorRole).setGroup(group);
    verify(editorRole).setIncludes(singletonList(defaultEditorRole));
  }

  @Test
  void testCreateGroups() {
    String id = "test";
    Package aPackage = mock(Package.class);
    when(aPackage.getId()).thenReturn(id);
    when(aPackage.getLabel()).thenReturn(id);
    GroupValue groupValue = mock(GroupValue.class);
    when(groupValue.getName()).thenReturn("group");
    Group group = mock(Group.class);
    Role editorRole = mock(Role.class);
    Role managerRole = mock(Role.class);

    RoleValue editorRoleValue =
        RoleValue.builder().setName("GROUP_EDITOR").setLabel(EDITOR).build();
    RoleValue managerRoleValue =
        RoleValue.builder().setName("GROUP_MANAGER").setLabel(MANAGER).build();

    when(groupValueFactory.createGroup(id, id, GroupService.DEFAULT_ROLES)).thenReturn(groupValue);
    when(groupFactory.create(groupValue)).thenReturn(group);

    when(editorRole.getName()).thenReturn("GROUP_EDITOR");
    when(editorRole.getLabel()).thenReturn(EDITOR);
    doReturn(editorRole).when(roleFactory).create(editorRoleValue);

    when(managerRole.getName()).thenReturn("GROUP_MANAGER");
    when(managerRole.getLabel()).thenReturn(MANAGER);
    doReturn(managerRole).when(roleFactory).create(managerRoleValue);

    Role defaultEditorRole = mock(Role.class);
    when(defaultEditorRole.getLabel()).thenReturn(EDITOR);

    Role defaultManagerRole = mock(Role.class);
    when(defaultManagerRole.getIncludes()).thenReturn(singletonList(defaultEditorRole));

    when(groupValue.getRoles()).thenReturn(ImmutableList.of(editorRoleValue, managerRoleValue));

    @SuppressWarnings("unchecked")
    Query<Role> query = mock(Query.class, RETURNS_SELF);
    when(dataService.query(ROLE, Role.class)).thenReturn(query);
    when(query.eq(eq(RoleMetadata.NAME), any(String.class)).findOne())
        .thenReturn(defaultManagerRole, defaultEditorRole);

    groupPackageService.createGroups(singletonList(aPackage));

    verify(dataService).add(GROUP, group);
    verify(dataService).add(eq(ROLE), roleCaptor.capture());
    assertEquals(asList(managerRole, editorRole), roleCaptor.getValue().collect(toList()));

    verify(managerRole).setGroup(group);
    verify(managerRole).setIncludes(asList(defaultManagerRole, editorRole));

    verify(editorRole).setGroup(group);
    verify(editorRole).setIncludes(singletonList(defaultEditorRole));
  }

  @SuppressWarnings("unchecked")
  @Test
  void testDelete() {
    String groupName = "group";
    Package pack = mock(Package.class);
    when(pack.getId()).thenReturn(groupName);

    Group group = mock(Group.class);
    when(group.getName()).thenReturn(groupName);

    Role role1 = mock(Role.class);
    when(role1.getId()).thenReturn("role1");
    Role role2 = mock(Role.class);
    when(role2.getId()).thenReturn("role2");
    List<Role> roles = Arrays.asList(role1, role2);
    when(group.getRoles()).thenReturn(roles);

    RoleMembership member1 = mock(RoleMembership.class);
    RoleMembership member2 = mock(RoleMembership.class);
    List<RoleMembership> members = Arrays.asList(member1, member2);

    Query<Group> groupQuery = mock(Query.class, RETURNS_SELF);
    when(dataService.query(GROUP, Group.class)).thenReturn(groupQuery);
    when(groupQuery.eq(GroupMetadata.ROOT_PACKAGE, groupName).findOne()).thenReturn(group);

    Query<RoleMembership> membershipQuery = mock(Query.class, Mockito.RETURNS_SELF);
    when(dataService.query(RoleMembershipMetadata.ROLE_MEMBERSHIP, RoleMembership.class))
        .thenReturn(membershipQuery);
    Query<RoleMembership> role1Query = mock(Query.class);
    when(role1Query.findAll()).thenReturn(members.stream());
    doReturn(role1Query).when(membershipQuery).eq(RoleMembershipMetadata.ROLE, "role1");
    Query<RoleMembership> role2Query = mock(Query.class);
    when(role2Query.findAll()).thenReturn(Stream.empty());
    doReturn(role2Query).when(membershipQuery).eq(RoleMembershipMetadata.ROLE, "role2");

    groupPackageService.deleteGroup(pack);

    verify(dataService, times(2))
        .delete(eq(RoleMembershipMetadata.ROLE_MEMBERSHIP), memberCaptor.capture());
    List<Stream<RoleMembership>> values = memberCaptor.getAllValues();
    assertEquals(emptyList(), values.get(1).collect(toList()));
    assertEquals(asList(member1, member2), values.get(0).collect(toList()));
    verify(dataService).delete(eq(ROLE), roleCaptor.capture());
    assertEquals(asList(role1, role2), roleCaptor.getValue().collect(toList()));
    verify(dataService).delete(GROUP, group);
    verify(mutableAclService).deleteAcl(new GroupIdentity(groupName), true);
  }

  @SuppressWarnings("unchecked")
  @Test
  void testDeleteUnknownGroup() {
    String groupName = "group";
    Package pack = mock(Package.class);
    when(pack.getId()).thenReturn(groupName);

    Query<Group> groupQuery = mock(Query.class, RETURNS_SELF);
    when(dataService.query(GROUP, Group.class)).thenReturn(groupQuery);
    when(groupQuery.eq(GroupMetadata.ROOT_PACKAGE, groupName).findOne()).thenReturn(null);

    groupPackageService.deleteGroup(pack);

    assertAll(
        () -> verifyNoMoreInteractions(dataService), () -> verifyNoInteractions(mutableAclService));
  }
}
