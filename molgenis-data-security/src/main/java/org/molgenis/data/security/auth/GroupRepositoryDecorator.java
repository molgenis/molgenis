package org.molgenis.data.security.auth;

import static com.google.common.collect.Streams.stream;
import static java.util.Objects.requireNonNull;

import java.util.stream.Stream;
import org.molgenis.data.AbstractRepositoryDecorator;
import org.molgenis.data.DataService;
import org.molgenis.data.Repository;
import org.molgenis.data.security.GroupIdentity;
import org.springframework.security.acls.model.MutableAclService;

public class GroupRepositoryDecorator extends AbstractRepositoryDecorator<Group> {

  private final DataService dataService;
  private final MutableAclService mutableAclService;

  GroupRepositoryDecorator(
      Repository<Group> delegateRepository,
      DataService dataService,
      MutableAclService mutableAclService) {
    super(delegateRepository);
    this.dataService = requireNonNull(dataService);
    this.mutableAclService = requireNonNull(mutableAclService);
  }

  @Override
  public void delete(Group group) {
    deleteGroup(group.getId());
  }

  @Override
  public void deleteById(Object id) {
    deleteGroup(id);
  }

  @Override
  public void deleteAll() {
    forEachBatched(entities -> delete(entities.stream()), 1000);
  }

  @Override
  public void delete(Stream<Group> groups) {
    groups.forEach(this::delete);
  }

  @Override
  public void deleteAll(Stream<Object> groupIds) {
    groupIds.forEach(this::deleteById);
  }

  private void deleteGroup(Object id) {
    Group group = dataService.findOneById(GroupMetadata.GROUP, id, Group.class);
    Iterable<Role> roles = group.getRoles();
    roles.forEach(this::removeMembers);
    dataService.delete(RoleMetadata.ROLE, stream(roles));
    delegate().deleteById(id);
    mutableAclService.deleteAcl(new GroupIdentity(group.getName()), true);
  }

  private void removeMembers(Role role) {
    Stream<RoleMembership> memberships =
        dataService
            .query(RoleMembershipMetadata.ROLE_MEMBERSHIP, RoleMembership.class)
            .eq(RoleMembershipMetadata.ROLE, role.getId())
            .findAll();
    dataService.delete(RoleMembershipMetadata.ROLE_MEMBERSHIP, memberships);
  }
}
