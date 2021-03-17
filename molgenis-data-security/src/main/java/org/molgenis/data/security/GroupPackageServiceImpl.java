package org.molgenis.data.security;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Streams.stream;
import static java.util.Objects.requireNonNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.molgenis.data.security.auth.GroupMetadata.GROUP;
import static org.molgenis.data.security.auth.RoleMetadata.NAME;
import static org.molgenis.data.security.auth.RoleMetadata.ROLE;
import static org.molgenis.data.security.auth.VOGroupRoleMembershipMetadata.VOGROUP_ROLE_MEMBERSHIP;
import static org.molgenis.security.core.GroupValueFactory.createRoleName;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.molgenis.data.DataService;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.security.auth.Group;
import org.molgenis.data.security.auth.GroupFactory;
import org.molgenis.data.security.auth.GroupMetadata;
import org.molgenis.data.security.auth.GroupPackageService;
import org.molgenis.data.security.auth.GroupService;
import org.molgenis.data.security.auth.Role;
import org.molgenis.data.security.auth.RoleFactory;
import org.molgenis.data.security.auth.RoleMembership;
import org.molgenis.data.security.auth.RoleMembershipMetadata;
import org.molgenis.data.security.auth.RoleMetadata;
import org.molgenis.data.security.auth.VOGroupRoleMembership;
import org.molgenis.data.security.auth.VOGroupRoleMembershipMetadata;
import org.molgenis.data.security.permission.RoleMembershipService;
import org.molgenis.security.core.GroupValueFactory;
import org.molgenis.security.core.model.GroupValue;
import org.molgenis.security.core.model.RoleValue;
import org.molgenis.security.core.utils.SecurityUtils;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.stereotype.Component;

@Component
public class GroupPackageServiceImpl implements GroupPackageService {
  private final GroupValueFactory groupValueFactory;
  private final RoleMembershipService roleMembershipService;
  private final RoleFactory roleFactory;
  private final DataService dataService;
  private final GroupFactory groupFactory;
  private final MutableAclService mutableAclService;
  private final GroupService groupService;

  GroupPackageServiceImpl(
      GroupValueFactory groupValueFactory,
      RoleMembershipService roleMembershipService,
      RoleFactory roleFactory,
      DataService dataService,
      GroupFactory groupFactory,
      MutableAclService mutableAclService,
      GroupService groupService) {

    this.groupValueFactory = requireNonNull(groupValueFactory);
    this.roleMembershipService = requireNonNull(roleMembershipService);
    this.roleFactory = requireNonNull(roleFactory);
    this.dataService = requireNonNull(dataService);
    this.groupFactory = requireNonNull(groupFactory);
    this.mutableAclService = requireNonNull(mutableAclService);
    this.groupService = requireNonNull(groupService);
  }

  @Override
  public void createGroup(Package rootPackage) {
    String groupName = rootPackage.getId();
    String groupLabel = rootPackage.getLabel();

    GroupValue groupValue =
        groupValueFactory.createGroup(
            groupName, groupLabel, GroupService.DEFAULT_ROLES, rootPackage.getId());

    Map<String, Role> roles =
        groupValue.getRoles().stream()
            .map(roleFactory::create)
            .collect(toMap(Role::getName, identity()));

    roles
        .values()
        .forEach(role -> addIncludedRolesBasedOnLabels(role, roles, groupValue.getName()));

    Group group = groupFactory.create(groupValue);
    group.setRootPackage(rootPackage);
    group.setRoles(roles.values());

    dataService.add(GROUP, group);
    roles.values().forEach(role -> role.setGroup(group));

    dataService.add(ROLE, roles.values().stream());

    groupService.grantDefaultPermissions(groupValue);
    roleMembershipService.addUserToRole(
        SecurityUtils.getCurrentUsername(), getManagerRoleName(groupValue));
  }

  @Override
  public void createGroups(List<Package> packages) {
    packages.forEach(this::createGroup);
  }

  @Override
  public void deleteGroup(Package rootPackage) {
    Group group = getGroup(rootPackage);
    if (group != null) {
      deleteRoles(group);
      dataService.delete(GROUP, group);
      mutableAclService.deleteAcl(new GroupIdentity(group.getName()), true);
    }
  }

  private Group getGroup(Package rootPackage) {
    return dataService
        .query(GroupMetadata.GROUP, Group.class)
        .eq(GroupMetadata.ROOT_PACKAGE, rootPackage.getId())
        .findOne();
  }

  private void deleteRoles(Group group) {
    Iterable<Role> roles = group.getRoles();
    // Check if role is included elsewhere, if it is remove the inclusion
    stream(roles)
        .flatMap(this::includesRole)
        .forEach(role -> groupService.removeExtendsRole(group, role));
    roles.forEach(this::deleteMembers);
    roles.forEach(this::deleteVOGroupMembers);
    dataService.delete(RoleMetadata.ROLE, stream(roles));
  }

  private Stream<Role> includesRole(Role role) {
    return dataService.query(ROLE, Role.class).eq(RoleMetadata.INCLUDES, role.getId()).findAll();
  }

  private void deleteMembers(Role role) {
    Stream<RoleMembership> memberships =
        dataService
            .query(RoleMembershipMetadata.ROLE_MEMBERSHIP, RoleMembership.class)
            .eq(RoleMembershipMetadata.ROLE, role.getId())
            .findAll();
    dataService.delete(RoleMembershipMetadata.ROLE_MEMBERSHIP, memberships);
  }

  private void deleteVOGroupMembers(Role role) {
    Stream<VOGroupRoleMembership> memberships =
        dataService
            .query(VOGROUP_ROLE_MEMBERSHIP, VOGroupRoleMembership.class)
            .eq(VOGroupRoleMembershipMetadata.ROLE, role.getId())
            .findAll();
    dataService.delete(VOGROUP_ROLE_MEMBERSHIP, memberships);
  }

  private String getManagerRoleName(GroupValue groupValue) {
    return groupValue.getRoles().stream()
        .filter(role -> role.getLabel().equals(GroupService.MANAGER))
        .map(RoleValue::getName)
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("Manager role is missing"));
  }

  private void addIncludedRolesBasedOnLabels(
      Role role, Map<String, Role> groupRoles, String groupName) {
    List<Role> toInclude = newArrayList();
    Role defaultRole = findRoleNamed(role.getLabel().toUpperCase());
    toInclude.add(defaultRole);

    stream(defaultRole.getIncludes())
        .map(includedRole -> createRoleName(groupName, includedRole.getLabel()))
        .map(groupRoles::get)
        .forEach(toInclude::add);

    role.setIncludes(toInclude);
  }

  private Role findRoleNamed(String rolename) {
    Role result = dataService.query(ROLE, Role.class).eq(RoleMetadata.NAME, rolename).findOne();
    if (result == null) {
      RoleMetadata roleMetadata = roleFactory.getEntityType();
      throw new UnknownEntityException(roleMetadata, roleMetadata.getAttribute(NAME), rolename);
    }
    return result;
  }
}
