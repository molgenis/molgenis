package org.molgenis.data.security.auth;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.security.GroupIdentity;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.security.acls.model.MutableAclService;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class GroupRepositoryDecoratorTest extends AbstractMockitoTest {
  @Mock private DataService dataService;
  @Mock private MutableAclService aclService;
  @Mock private Repository<Group> delegateRepository;

  @Captor private ArgumentCaptor<Stream<Role>> roleCaptor;
  @Captor private ArgumentCaptor<Stream<RoleMembership>> memberCaptor;
  @Captor private ArgumentCaptor<String> groupCaptor;
  @Captor private ArgumentCaptor<GroupIdentity> identityCaptor;

  private GroupRepositoryDecorator groupRepositoryDecorator;

  @BeforeMethod
  public void setUp() {
    groupRepositoryDecorator =
        new GroupRepositoryDecorator(delegateRepository, dataService, aclService);
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
    when(group.getRoles()).thenReturn(roles);

    RoleMembership member1 = mock(RoleMembership.class);
    RoleMembership member2 = mock(RoleMembership.class);
    List<RoleMembership> members = Arrays.asList(member1, member2);

    when(dataService.findOneById(GroupMetadata.GROUP, "test", Group.class)).thenReturn(group);

    @SuppressWarnings("unchecked")
    Query<RoleMembership> query = mock(Query.class, RETURNS_SELF);
    when(dataService.query(RoleMembershipMetadata.ROLE_MEMBERSHIP, RoleMembership.class))
        .thenReturn(query);
    @SuppressWarnings("unchecked")
    Query<RoleMembership> role1Query = mock(Query.class);
    when(role1Query.findAll()).thenReturn(members.stream());
    doReturn(role1Query).when(query).eq(RoleMembershipMetadata.ROLE, "role1");
    @SuppressWarnings("unchecked")
    Query<RoleMembership> role2Query = mock(Query.class);
    when(role2Query.findAll()).thenReturn(Stream.empty());
    doReturn(role2Query).when(query).eq(RoleMembershipMetadata.ROLE, "role2");

    groupRepositoryDecorator.delete(group);

    verify(dataService, times(2))
        .delete(eq(RoleMembershipMetadata.ROLE_MEMBERSHIP), memberCaptor.capture());
    List<Stream<RoleMembership>> values = memberCaptor.getAllValues();
    assertEquals(values.get(1).collect(toList()), emptyList());
    assertEquals(values.get(0).collect(toList()), asList(member1, member2));
    verify(dataService).delete(eq(RoleMetadata.ROLE), roleCaptor.capture());
    assertEquals(roleCaptor.getValue().collect(toList()), asList(role1, role2));
    verify(delegateRepository).deleteById("test");
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
    when(group.getRoles()).thenReturn(roles);

    @SuppressWarnings("unchecked")
    Query<RoleMembership> query = mock(Query.class, RETURNS_DEEP_STUBS);
    when(dataService.query(RoleMembershipMetadata.ROLE_MEMBERSHIP, RoleMembership.class))
        .thenReturn(query);

    @SuppressWarnings("unchecked")
    Query<RoleMembership> role1Query = mock(Query.class, RETURNS_DEEP_STUBS);
    doReturn(role1Query).when(query).eq(RoleMembershipMetadata.ROLE, "role1");
    when(role1Query.findAll()).thenReturn(members.stream());

    @SuppressWarnings("unchecked")
    Query<RoleMembership> role2Query = mock(Query.class, RETURNS_DEEP_STUBS);
    doReturn(role2Query).when(query).eq(RoleMembershipMetadata.ROLE, "role2");
    when(role2Query.findAll()).thenReturn(Stream.empty());

    groupRepositoryDecorator.deleteById("test");

    verify(dataService, times(2))
        .delete(eq(RoleMembershipMetadata.ROLE_MEMBERSHIP), memberCaptor.capture());
    List<Stream<RoleMembership>> values = memberCaptor.getAllValues();
    assertEquals(values.get(1).collect(toList()), emptyList());
    assertEquals(values.get(0).collect(toList()), asList(member1, member2));
    verify(dataService).delete(eq(RoleMetadata.ROLE), roleCaptor.capture());
    assertEquals(roleCaptor.getValue().collect(toList()), asList(role1, role2));
    verify(delegateRepository).deleteById("test");
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

    doAnswer(
            invocation -> {
              Consumer<List<Entity>> consumer = invocation.getArgument(1);
              consumer.accept(asList(group, group2));
              return null;
            })
        .when(delegateRepository)
        .forEachBatched(any(), any(), eq(1000));

    doReturn(group).when(dataService).findOneById(GroupMetadata.GROUP, "test", Group.class);
    when(group.getRoles()).thenReturn(roles);
    doReturn(group2).when(dataService).findOneById(GroupMetadata.GROUP, "test2", Group.class);
    when(group2.getRoles()).thenReturn(roles2);

    @SuppressWarnings("unchecked")
    Query<RoleMembership> query = mock(Query.class, RETURNS_DEEP_STUBS);
    when(dataService.query(RoleMembershipMetadata.ROLE_MEMBERSHIP, RoleMembership.class))
        .thenReturn(query);

    @SuppressWarnings("unchecked")
    Query<RoleMembership> role1Query = mock(Query.class, RETURNS_DEEP_STUBS);
    doReturn(role1Query).when(query).eq(RoleMembershipMetadata.ROLE, "role1");
    when(role1Query.findAll()).thenReturn(members.stream());

    @SuppressWarnings("unchecked")
    Query<RoleMembership> role2Query = mock(Query.class, RETURNS_DEEP_STUBS);
    doReturn(role2Query).when(query).eq(RoleMembershipMetadata.ROLE, "role2");
    when(role2Query.findAll()).thenReturn(Stream.empty());

    @SuppressWarnings("unchecked")
    Query<RoleMembership> role3Query = mock(Query.class, RETURNS_DEEP_STUBS);
    doReturn(role3Query).when(query).eq(RoleMembershipMetadata.ROLE, "role3");
    when(role3Query.findAll()).thenReturn(members2.stream());

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
    verify(delegateRepository, times(2)).deleteById(groupCaptor.capture());
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
    when(group.getRoles()).thenReturn(roles);
    doReturn(group2).when(dataService).findOneById(GroupMetadata.GROUP, "test2", Group.class);
    when(group2.getRoles()).thenReturn(roles2);

    @SuppressWarnings("unchecked")
    Query<RoleMembership> query = mock(Query.class, RETURNS_DEEP_STUBS);
    when(dataService.query(RoleMembershipMetadata.ROLE_MEMBERSHIP, RoleMembership.class))
        .thenReturn(query);

    @SuppressWarnings("unchecked")
    Query<RoleMembership> role1Query = mock(Query.class, RETURNS_DEEP_STUBS);
    doReturn(role1Query).when(query).eq(RoleMembershipMetadata.ROLE, "role1");
    when(role1Query.findAll()).thenReturn(members.stream());

    @SuppressWarnings("unchecked")
    Query<RoleMembership> role2Query = mock(Query.class, RETURNS_DEEP_STUBS);
    doReturn(role2Query).when(query).eq(RoleMembershipMetadata.ROLE, "role2");
    when(role2Query.findAll()).thenReturn(Stream.empty());

    @SuppressWarnings("unchecked")
    Query<RoleMembership> role3Query = mock(Query.class, RETURNS_DEEP_STUBS);
    doReturn(role3Query).when(query).eq(RoleMembershipMetadata.ROLE, "role3");
    when(role3Query.findAll()).thenReturn(members2.stream());

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
    verify(delegateRepository, times(2)).deleteById(groupCaptor.capture());
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
    when(group.getRoles()).thenReturn(roles);
    doReturn(group2).when(dataService).findOneById(GroupMetadata.GROUP, "test2", Group.class);
    when(group2.getRoles()).thenReturn(roles2);

    @SuppressWarnings("unchecked")
    Query<RoleMembership> query = mock(Query.class, RETURNS_DEEP_STUBS);
    when(dataService.query(RoleMembershipMetadata.ROLE_MEMBERSHIP, RoleMembership.class))
        .thenReturn(query);

    @SuppressWarnings("unchecked")
    Query<RoleMembership> role1Query = mock(Query.class, RETURNS_DEEP_STUBS);
    doReturn(role1Query).when(query).eq(RoleMembershipMetadata.ROLE, "role1");
    when(role1Query.findAll()).thenReturn(members.stream());

    @SuppressWarnings("unchecked")
    Query<RoleMembership> role2Query = mock(Query.class, RETURNS_DEEP_STUBS);
    doReturn(role2Query).when(query).eq(RoleMembershipMetadata.ROLE, "role2");
    when(role2Query.findAll()).thenReturn(Stream.empty());

    @SuppressWarnings("unchecked")
    Query<RoleMembership> role3Query = mock(Query.class, RETURNS_DEEP_STUBS);
    doReturn(role3Query).when(query).eq(RoleMembershipMetadata.ROLE, "role3");
    when(role3Query.findAll()).thenReturn(members2.stream());

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
    verify(delegateRepository, times(2)).deleteById(groupCaptor.capture());
    List<String> groupValues = groupCaptor.getAllValues();
    assertEquals(groupValues.get(0), "test");
    assertEquals(groupValues.get(1), "test2");
    verify(aclService, times(2)).deleteAcl(identityCaptor.capture(), eq(true));
    List<GroupIdentity> identityValues = identityCaptor.getAllValues();
    assertEquals(identityValues.get(0), new GroupIdentity("name"));
    assertEquals(identityValues.get(1), new GroupIdentity("name2"));
  }

  @Test(expectedExceptions = UnknownEntityException.class)
  public void testDeleteGroupUnknown() {
    groupRepositoryDecorator.deleteById("unknownGroupId");
  }
}
