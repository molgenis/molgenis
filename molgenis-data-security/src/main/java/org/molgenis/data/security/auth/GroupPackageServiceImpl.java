package org.molgenis.data.security.auth;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Streams.stream;
import static java.util.Objects.requireNonNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.molgenis.data.security.auth.GroupMetadata.GROUP;
import static org.molgenis.data.security.auth.RoleMetadata.NAME;
import static org.molgenis.data.security.auth.RoleMetadata.ROLE;
import static org.molgenis.security.core.GroupValueFactory.createRoleName;

import java.util.List;
import java.util.Map;
import org.molgenis.data.DataService;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.security.permission.RoleMembershipService;
import org.molgenis.security.core.GroupValueFactory;
import org.molgenis.security.core.model.GroupValue;
import org.molgenis.security.core.model.RoleValue;
import org.molgenis.security.core.utils.SecurityUtils;
import org.springframework.stereotype.Component;

@Component
public class GroupPackageServiceImpl implements GroupPackageService {
  private final GroupValueFactory groupValueFactory;
  private final RoleMembershipService roleMembershipService;
  private final GroupPermissionService groupPermissionService;
  private final RoleFactory roleFactory;
  private final DataService dataService;
  private final GroupFactory groupFactory;
  private final RoleMetadata roleMetadata;

  GroupPackageServiceImpl(
      GroupValueFactory groupValueFactory,
      RoleMembershipService roleMembershipService,
      GroupPermissionService groupPermissionService,
      RoleFactory roleFactory,
      DataService dataService,
      GroupFactory groupFactory,
      RoleMetadata roleMetadata) {

    this.groupValueFactory = requireNonNull(groupValueFactory);
    this.roleMembershipService = requireNonNull(roleMembershipService);
    this.groupPermissionService = requireNonNull(groupPermissionService);
    this.roleFactory = requireNonNull(roleFactory);
    this.dataService = requireNonNull(dataService);
    this.groupFactory = requireNonNull(groupFactory);
    this.roleMetadata = requireNonNull(roleMetadata);
  }

  @Override
  public void createGroup(Package rootPackage) {
    String groupName = rootPackage.getId();
    String groupLabel = rootPackage.getLabel();

    GroupValue groupValue =
        groupValueFactory.createGroup(groupName, groupLabel, GroupService.DEFAULT_ROLES);

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

    groupPermissionService.grantDefaultPermissions(groupValue);
    roleMembershipService.addUserToRole(
        SecurityUtils.getCurrentUsername(), getManagerRoleName(groupValue));
  }

  @Override
  public void createGroups(List<Package> packages) {
    packages.forEach(this::createGroup);
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
      throw new UnknownEntityException(roleMetadata, roleMetadata.getAttribute(NAME), rolename);
    }
    return result;
  }
}
