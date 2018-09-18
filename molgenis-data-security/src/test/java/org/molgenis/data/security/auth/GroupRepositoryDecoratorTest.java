package org.molgenis.data.security.auth;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.Repository;
import org.molgenis.data.security.GroupIdentity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.security.acls.model.MutableAclService;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class GroupRepositoryDecoratorTest extends AbstractMockitoTest {

  @Mock private DataService dataService;
  @Mock private MutableAclService aclService;
  @Mock private Repository repository;

  @Captor private ArgumentCaptor<Stream<Role>> roleCaptor;
  @Captor private ArgumentCaptor<Stream<RoleMembership>> memberCaptor;
  @Captor private ArgumentCaptor<String> groupCaptor;
  @Captor private ArgumentCaptor<GroupIdentity> identityCaptor;

  private GroupRepositoryDecorator groupRepositoryDecorator;

  @BeforeMethod
  public void setUp() {
    groupRepositoryDecorator = new GroupRepositoryDecorator(repository, dataService, aclService);
  }

  @Test
  public void testDelete() {
    Group group = mock(Group.class);
    when(group.getId()).thenReturn("test");
    when(group.getName()).thenReturn("name");

    Role role1 = mock(Role.class);
    when(role1.getId()).thenReturn("role1");
    Role role2 = mock(Role.class);
    when(role2.getId()).thenReturn("role2");
    List<Role> roles = Arrays.asList(role1, role2);

    RoleMembership member1 = mock(RoleMembership.class);
    RoleMembership member2 = mock(RoleMembership.class);
    List<RoleMembership> members = Arrays.asList(member1, member2);

    when(dataService.findOneById(GroupMetadata.GROUP, "test", Group.class)).thenReturn(group);
    when(dataService.findAll(
            RoleMetadata.ROLE, new QueryImpl<Role>().eq(RoleMetadata.GROUP, "test"), Role.class))
        .thenReturn(roles.stream());
    doReturn(members.stream())
        .when(dataService)
        .findAll(
            RoleMembershipMetadata.ROLE_MEMBERSHIP,
            new QueryImpl<RoleMembership>().eq(RoleMembershipMetadata.ROLE, "role1"),
            RoleMembership.class);
    doReturn(Collections.<RoleMembership>emptyList().stream())
        .when(dataService)
        .findAll(
            RoleMembershipMetadata.ROLE_MEMBERSHIP,
            new QueryImpl<RoleMembership>().eq(RoleMembershipMetadata.ROLE, "role2"),
            RoleMembership.class);

    groupRepositoryDecorator.delete(group);

    verify(dataService, times(2))
        .delete(eq(RoleMembershipMetadata.ROLE_MEMBERSHIP), memberCaptor.capture());
    List<Stream<RoleMembership>> values = memberCaptor.getAllValues();
    assertEquals(values.get(1).collect(toList()), emptyList());
    assertEquals(values.get(0).collect(toList()), asList(member1, member2));
    verify(dataService).delete(eq(RoleMetadata.ROLE), roleCaptor.capture());
    assertEquals(roleCaptor.getValue().collect(toList()), asList(role1, role2));
    verify(repository).deleteById("test");
    verify(aclService).deleteAcl(new GroupIdentity("name"), true);
  }

  @Test
  public void testDeleteById() {
    Group group = mock(Group.class);
    when(group.getName()).thenReturn("name");

    Role role1 = mock(Role.class);
    when(role1.getId()).thenReturn("role1");
    Role role2 = mock(Role.class);
    when(role2.getId()).thenReturn("role2");
    List<Role> roles = Arrays.asList(role1, role2);

    RoleMembership member1 = mock(RoleMembership.class);
    RoleMembership member2 = mock(RoleMembership.class);
    List<RoleMembership> members = Arrays.asList(member1, member2);

    when(dataService.findOneById(GroupMetadata.GROUP, "test", Group.class)).thenReturn(group);
    when(dataService.findAll(
            RoleMetadata.ROLE, new QueryImpl<Role>().eq(RoleMetadata.GROUP, "test"), Role.class))
        .thenReturn(roles.stream());
    doReturn(members.stream())
        .when(dataService)
        .findAll(
            RoleMembershipMetadata.ROLE_MEMBERSHIP,
            new QueryImpl<RoleMembership>().eq(RoleMembershipMetadata.ROLE, "role1"),
            RoleMembership.class);
    doReturn(Collections.<RoleMembership>emptyList().stream())
        .when(dataService)
        .findAll(
            RoleMembershipMetadata.ROLE_MEMBERSHIP,
            new QueryImpl<RoleMembership>().eq(RoleMembershipMetadata.ROLE, "role2"),
            RoleMembership.class);

    groupRepositoryDecorator.deleteById("test");

    verify(dataService, times(2))
        .delete(eq(RoleMembershipMetadata.ROLE_MEMBERSHIP), memberCaptor.capture());
    List<Stream<RoleMembership>> values = memberCaptor.getAllValues();
    assertEquals(values.get(1).collect(toList()), emptyList());
    assertEquals(values.get(0).collect(toList()), asList(member1, member2));
    verify(dataService).delete(eq(RoleMetadata.ROLE), roleCaptor.capture());
    assertEquals(roleCaptor.getValue().collect(toList()), asList(role1, role2));
    verify(repository).deleteById("test");
    verify(aclService).deleteAcl(new GroupIdentity("name"), true);
  }

  @Test
  public void testDeleteAll() {
    Group group = mock(Group.class);
    when(group.getId()).thenReturn("test");
    when(group.getName()).thenReturn("name");
    Group group2 = mock(Group.class);
    when(group2.getId()).thenReturn("test2");
    when(group2.getName()).thenReturn("name2");

    Role role1 = mock(Role.class);
    when(role1.getId()).thenReturn("role1");
    Role role2 = mock(Role.class);
    when(role2.getId()).thenReturn("role2");
    List<Role> roles = Arrays.asList(role1, role2);
    Role role3 = mock(Role.class);
    when(role3.getId()).thenReturn("role3");
    List<Role> roles2 = Arrays.asList(role3);

    RoleMembership member1 = mock(RoleMembership.class);
    RoleMembership member2 = mock(RoleMembership.class);
    List<RoleMembership> members = Arrays.asList(member1, member2);
    RoleMembership member3 = mock(RoleMembership.class);
    List<RoleMembership> members2 = Arrays.asList(member3);

    doReturn(Stream.of(group, group2)).when(dataService).findAll(GroupMetadata.GROUP, Group.class);
    doReturn(group).when(dataService).findOneById(GroupMetadata.GROUP, "test", Group.class);
    doReturn(group2).when(dataService).findOneById(GroupMetadata.GROUP, "test2", Group.class);
    doReturn(roles.stream())
        .when(dataService)
        .findAll(
            RoleMetadata.ROLE, new QueryImpl<Role>().eq(RoleMetadata.GROUP, "test"), Role.class);
    doReturn(roles2.stream())
        .when(dataService)
        .findAll(
            RoleMetadata.ROLE, new QueryImpl<Role>().eq(RoleMetadata.GROUP, "test2"), Role.class);
    doReturn(members.stream())
        .when(dataService)
        .findAll(
            RoleMembershipMetadata.ROLE_MEMBERSHIP,
            new QueryImpl<RoleMembership>().eq(RoleMembershipMetadata.ROLE, "role1"),
            RoleMembership.class);
    doReturn(Collections.<RoleMembership>emptyList().stream())
        .when(dataService)
        .findAll(
            RoleMembershipMetadata.ROLE_MEMBERSHIP,
            new QueryImpl<RoleMembership>().eq(RoleMembershipMetadata.ROLE, "role2"),
            RoleMembership.class);
    doReturn(members2.stream())
        .when(dataService)
        .findAll(
            RoleMembershipMetadata.ROLE_MEMBERSHIP,
            new QueryImpl<RoleMembership>().eq(RoleMembershipMetadata.ROLE, "role3"),
            RoleMembership.class);

    groupRepositoryDecorator.deleteAll();

    verify(dataService, times(3))
        .delete(eq(RoleMembershipMetadata.ROLE_MEMBERSHIP), memberCaptor.capture());
    List<Stream<RoleMembership>> values = memberCaptor.getAllValues();
    assertEquals(values.get(0).collect(toList()), asList(member1, member2));
    assertEquals(values.get(1).collect(toList()), emptyList());
    assertEquals(values.get(2).collect(toList()), asList(member3));
    verify(dataService, times(2)).delete(eq(RoleMetadata.ROLE), roleCaptor.capture());
    List<Stream<Role>> roleValues = roleCaptor.getAllValues();
    assertEquals(roleValues.get(0).collect(toList()), asList(role1, role2));
    assertEquals(roleValues.get(1).collect(toList()), asList(role3));
    verify(repository, times(2)).deleteById(groupCaptor.capture());
    List<String> groupValues = groupCaptor.getAllValues();
    assertEquals(groupValues.get(0), "test");
    assertEquals(groupValues.get(1), "test2");
    verify(aclService, times(2)).deleteAcl(identityCaptor.capture(), eq(true));
    List<GroupIdentity> identityValues = identityCaptor.getAllValues();
    assertEquals(identityValues.get(0), new GroupIdentity("name"));
    assertEquals(identityValues.get(1), new GroupIdentity("name2"));
  }

  @Test
  public void testDeleteStream() {
    Group group = mock(Group.class);
    when(group.getId()).thenReturn("test");
    when(group.getName()).thenReturn("name");
    Group group2 = mock(Group.class);
    when(group2.getId()).thenReturn("test2");
    when(group2.getName()).thenReturn("name2");

    Role role1 = mock(Role.class);
    when(role1.getId()).thenReturn("role1");
    Role role2 = mock(Role.class);
    when(role2.getId()).thenReturn("role2");
    List<Role> roles = Arrays.asList(role1, role2);
    Role role3 = mock(Role.class);
    when(role3.getId()).thenReturn("role3");
    List<Role> roles2 = Arrays.asList(role3);

    RoleMembership member1 = mock(RoleMembership.class);
    RoleMembership member2 = mock(RoleMembership.class);
    List<RoleMembership> members = Arrays.asList(member1, member2);
    RoleMembership member3 = mock(RoleMembership.class);
    List<RoleMembership> members2 = Arrays.asList(member3);

    doReturn(group).when(dataService).findOneById(GroupMetadata.GROUP, "test", Group.class);
    doReturn(group2).when(dataService).findOneById(GroupMetadata.GROUP, "test2", Group.class);
    doReturn(roles.stream())
        .when(dataService)
        .findAll(
            RoleMetadata.ROLE, new QueryImpl<Role>().eq(RoleMetadata.GROUP, "test"), Role.class);
    doReturn(roles2.stream())
        .when(dataService)
        .findAll(
            RoleMetadata.ROLE, new QueryImpl<Role>().eq(RoleMetadata.GROUP, "test2"), Role.class);
    doReturn(members.stream())
        .when(dataService)
        .findAll(
            RoleMembershipMetadata.ROLE_MEMBERSHIP,
            new QueryImpl<RoleMembership>().eq(RoleMembershipMetadata.ROLE, "role1"),
            RoleMembership.class);
    doReturn(Collections.<RoleMembership>emptyList().stream())
        .when(dataService)
        .findAll(
            RoleMembershipMetadata.ROLE_MEMBERSHIP,
            new QueryImpl<RoleMembership>().eq(RoleMembershipMetadata.ROLE, "role2"),
            RoleMembership.class);
    doReturn(members2.stream())
        .when(dataService)
        .findAll(
            RoleMembershipMetadata.ROLE_MEMBERSHIP,
            new QueryImpl<RoleMembership>().eq(RoleMembershipMetadata.ROLE, "role3"),
            RoleMembership.class);

    groupRepositoryDecorator.delete(Stream.of(group, group2));

    verify(dataService, times(3))
        .delete(eq(RoleMembershipMetadata.ROLE_MEMBERSHIP), memberCaptor.capture());
    List<Stream<RoleMembership>> values = memberCaptor.getAllValues();
    assertEquals(values.get(0).collect(toList()), asList(member1, member2));
    assertEquals(values.get(1).collect(toList()), emptyList());
    assertEquals(values.get(2).collect(toList()), asList(member3));
    verify(dataService, times(2)).delete(eq(RoleMetadata.ROLE), roleCaptor.capture());
    List<Stream<Role>> roleValues = roleCaptor.getAllValues();
    assertEquals(roleValues.get(0).collect(toList()), asList(role1, role2));
    assertEquals(roleValues.get(1).collect(toList()), asList(role3));
    verify(repository, times(2)).deleteById(groupCaptor.capture());
    List<String> groupValues = groupCaptor.getAllValues();
    assertEquals(groupValues.get(0), "test");
    assertEquals(groupValues.get(1), "test2");
    verify(aclService, times(2)).deleteAcl(identityCaptor.capture(), eq(true));
    List<GroupIdentity> identityValues = identityCaptor.getAllValues();
    assertEquals(identityValues.get(0), new GroupIdentity("name"));
    assertEquals(identityValues.get(1), new GroupIdentity("name2"));
  }

  @Test
  public void testDeleteAllStream() {
    Group group = mock(Group.class);
    when(group.getName()).thenReturn("name");
    Group group2 = mock(Group.class);
    when(group2.getName()).thenReturn("name2");

    Role role1 = mock(Role.class);
    when(role1.getId()).thenReturn("role1");
    Role role2 = mock(Role.class);
    when(role2.getId()).thenReturn("role2");
    List<Role> roles = Arrays.asList(role1, role2);
    Role role3 = mock(Role.class);
    when(role3.getId()).thenReturn("role3");
    List<Role> roles2 = Arrays.asList(role3);

    RoleMembership member1 = mock(RoleMembership.class);
    RoleMembership member2 = mock(RoleMembership.class);
    List<RoleMembership> members = Arrays.asList(member1, member2);
    RoleMembership member3 = mock(RoleMembership.class);
    List<RoleMembership> members2 = Arrays.asList(member3);

    doReturn(group).when(dataService).findOneById(GroupMetadata.GROUP, "test", Group.class);
    doReturn(group2).when(dataService).findOneById(GroupMetadata.GROUP, "test2", Group.class);
    doReturn(roles.stream())
        .when(dataService)
        .findAll(
            RoleMetadata.ROLE, new QueryImpl<Role>().eq(RoleMetadata.GROUP, "test"), Role.class);
    doReturn(roles2.stream())
        .when(dataService)
        .findAll(
            RoleMetadata.ROLE, new QueryImpl<Role>().eq(RoleMetadata.GROUP, "test2"), Role.class);
    doReturn(members.stream())
        .when(dataService)
        .findAll(
            RoleMembershipMetadata.ROLE_MEMBERSHIP,
            new QueryImpl<RoleMembership>().eq(RoleMembershipMetadata.ROLE, "role1"),
            RoleMembership.class);
    doReturn(Collections.<RoleMembership>emptyList().stream())
        .when(dataService)
        .findAll(
            RoleMembershipMetadata.ROLE_MEMBERSHIP,
            new QueryImpl<RoleMembership>().eq(RoleMembershipMetadata.ROLE, "role2"),
            RoleMembership.class);
    doReturn(members2.stream())
        .when(dataService)
        .findAll(
            RoleMembershipMetadata.ROLE_MEMBERSHIP,
            new QueryImpl<RoleMembership>().eq(RoleMembershipMetadata.ROLE, "role3"),
            RoleMembership.class);

    groupRepositoryDecorator.deleteAll(Stream.of("test", "test2"));

    verify(dataService, times(3))
        .delete(eq(RoleMembershipMetadata.ROLE_MEMBERSHIP), memberCaptor.capture());
    List<Stream<RoleMembership>> values = memberCaptor.getAllValues();
    assertEquals(values.get(0).collect(toList()), asList(member1, member2));
    assertEquals(values.get(1).collect(toList()), emptyList());
    assertEquals(values.get(2).collect(toList()), asList(member3));
    verify(dataService, times(2)).delete(eq(RoleMetadata.ROLE), roleCaptor.capture());
    List<Stream<Role>> roleValues = roleCaptor.getAllValues();
    assertEquals(roleValues.get(0).collect(toList()), asList(role1, role2));
    assertEquals(roleValues.get(1).collect(toList()), asList(role3));
    verify(repository, times(2)).deleteById(groupCaptor.capture());
    List<String> groupValues = groupCaptor.getAllValues();
    assertEquals(groupValues.get(0), "test");
    assertEquals(groupValues.get(1), "test2");
    verify(aclService, times(2)).deleteAcl(identityCaptor.capture(), eq(true));
    List<GroupIdentity> identityValues = identityCaptor.getAllValues();
    assertEquals(identityValues.get(0), new GroupIdentity("name"));
    assertEquals(identityValues.get(1), new GroupIdentity("name2"));
  }
}
