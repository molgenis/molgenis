package org.molgenis.data.security.auth;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.model.PackageMetadata.PACKAGE;
import static org.molgenis.data.security.auth.RoleMetadata.ROLE;
import static org.molgenis.security.core.PermissionSet.READ;
import static org.molgenis.security.core.PermissionSet.WRITE;
import static org.molgenis.security.core.PermissionSet.WRITEMETA;
import static org.molgenis.security.core.SidUtils.createAuthoritySid;
import static org.molgenis.security.core.SidUtils.createRoleAuthority;
import static org.molgenis.security.core.SidUtils.createRoleSid;
import static org.molgenis.security.core.utils.SecurityUtils.AUTHORITY_USER;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.molgenis.data.DataService;
import org.molgenis.data.Fetch;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.model.PackageFactory;
import org.molgenis.data.meta.model.PackageMetadata;
import org.molgenis.data.security.GroupIdentity;
import org.molgenis.data.security.PackageIdentity;
import org.molgenis.data.security.exception.IsAlreadyMemberException;
import org.molgenis.data.security.exception.NotAValidGroupRoleException;
import org.molgenis.data.security.exception.VOGroupIsAlreadyMemberException;
import org.molgenis.data.security.permission.PermissionService;
import org.molgenis.data.security.permission.RoleMembershipService;
import org.molgenis.data.security.permission.VOGroupRoleMembershipService;
import org.molgenis.data.security.permission.model.Permission;
import org.molgenis.security.core.PermissionSet;
import org.molgenis.security.core.model.GroupValue;
import org.molgenis.security.core.runas.RunAsSystem;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.Sid;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GroupService {
  private final DataService dataService;
  private final PackageFactory packageFactory;
  private final GroupMetadata groupMetadata;
  private final RoleMembershipService roleMembershipService;
  private final VOGroupRoleMembershipService voGroupRoleMembershipService;
  private final RoleMembershipMetadata roleMembershipMetadata;
  private final VOGroupRoleMembershipMetadata voGroupRoleMembershipMetadata;
  private final MutableAclService aclService;
  private final PermissionService permissionService;

  public static final String MANAGER = "Manager";
  public static final String EDITOR = "Editor";
  public static final String VIEWER = "Viewer";

  public static final String AUTHORITY_MANAGER = createRoleAuthority(MANAGER.toUpperCase());
  public static final String AUTHORITY_EDITOR = createRoleAuthority(EDITOR.toUpperCase());
  public static final String AUTHORITY_VIEWER = createRoleAuthority(VIEWER.toUpperCase());

  public static final Set<String> DEFAULT_ROLES = ImmutableSet.of(MANAGER, EDITOR, VIEWER);
  private static final Map<String, PermissionSet> PERMISSION_SETS_PER_ROLE =
      ImmutableMap.of(MANAGER, WRITEMETA, EDITOR, WRITE, VIEWER, READ);

  @SuppressWarnings("java:S107")
  GroupService(
      PackageFactory packageFactory,
      DataService dataService,
      GroupMetadata groupMetadata,
      RoleMembershipService roleMembershipService,
      VOGroupRoleMembershipService voGroupRoleMembershipService,
      RoleMembershipMetadata roleMembershipMetadata,
      VOGroupRoleMembershipMetadata voGroupRoleMembershipMetadata,
      MutableAclService aclService,
      PermissionService permissionService) {
    this.packageFactory = requireNonNull(packageFactory);
    this.dataService = requireNonNull(dataService);
    this.groupMetadata = requireNonNull(groupMetadata);
    this.roleMembershipService = requireNonNull(roleMembershipService);
    this.voGroupRoleMembershipService = requireNonNull(voGroupRoleMembershipService);
    this.roleMembershipMetadata = requireNonNull(roleMembershipMetadata);
    this.voGroupRoleMembershipMetadata = requireNonNull(voGroupRoleMembershipMetadata);
    this.aclService = requireNonNull(aclService);
    this.permissionService = requireNonNull(permissionService);
  }

  /**
   * Creates {@link Group}, {@link Role} and {@link Package} entities and adds them to the
   * dataService.
   *
   * @param groupValue details of the group that should be created
   */
  @Transactional
  public void persist(GroupValue groupValue) {
    Package rootPackage = packageFactory.create(groupValue.getRootPackage());
    dataService.add(PACKAGE, rootPackage);
  }

  @RunAsSystem
  public Collection<Group> getGroups() {
    return dataService.findAll(GroupMetadata.GROUP, Group.class).collect(Collectors.toList());
  }

  /**
   * Get the group entity by its unique name.
   *
   * @param groupName unique group name
   * @return group with given name
   * @throws UnknownEntityException in case no group with given name can be retrieved
   */
  @RunAsSystem
  public Group getGroup(String groupName) {
    Fetch roleFetch = new Fetch().field(RoleMetadata.NAME).field(RoleMetadata.LABEL);
    Fetch fetch =
        new Fetch()
            .field(GroupMetadata.ROLES, roleFetch)
            .field(GroupMetadata.NAME)
            .field(GroupMetadata.LABEL)
            .field(GroupMetadata.DESCRIPTION)
            .field(GroupMetadata.ID)
            .field(GroupMetadata.PUBLIC)
            .field(GroupMetadata.ROOT_PACKAGE);

    Group group =
        dataService
            .query(GroupMetadata.GROUP, Group.class)
            .eq(GroupMetadata.NAME, groupName)
            .fetch(fetch)
            .findOne();
    if (group == null) {
      throw new UnknownEntityException(
          groupMetadata, groupMetadata.getAttribute(GroupMetadata.NAME), groupName);
    }
    return group;
  }

  /**
   * Add member to group. User can only be added to a role that belongs to the group. The user can
   * only have a single role within the group
   *
   * @param group group to add the user to in the given role
   * @param user user to be added in the given role to the given group
   * @param role role in which the given user is to be added to given group
   */
  @RunAsSystem
  public void addMember(final Group group, final User user, final Role role) {
    ArrayList<Role> groupRoles = newArrayList(group.getRoles());
    Collection<RoleMembership> memberships = roleMembershipService.getMemberships(groupRoles);
    boolean isGroupRole = isGroupRole(role, groupRoles);

    if (!isGroupRole) {
      throw new NotAValidGroupRoleException(role, group);
    }

    boolean isMember = memberships.stream().parallel().anyMatch(m -> m.getUser().equals(user));

    if (isMember) {
      throw new IsAlreadyMemberException(user, group);
    }

    roleMembershipService.addUserToRole(user, role);
  }

  /**
   * Add a VO group to group. VO group can only be added to a role that belongs to the group. The VO
   * group can only have a single role within the group
   *
   * @param group group to add the user to in the given role
   * @param voGroup the VO Group to be added in the given role to the given group
   * @param role role in which the given user is to be added to given group
   */
  @RunAsSystem
  public void addVOGroupMembership(final Group group, final VOGroup voGroup, final Role role) {
    ArrayList<Role> groupRoles = newArrayList(group.getRoles());
    if (!isGroupRole(role, groupRoles)) {
      throw new NotAValidGroupRoleException(role, group);
    }
    Collection<VOGroupRoleMembership> memberships =
        voGroupRoleMembershipService.getMemberships(groupRoles);

    boolean isMember =
        memberships.stream()
            .map(VOGroupRoleMembership::getVOGroup)
            .map(VOGroup::getName)
            .anyMatch(voGroup.getName()::equals);

    if (isMember) {
      throw new VOGroupIsAlreadyMemberException(voGroup, group);
    }

    voGroupRoleMembershipService.add(voGroup, role);
  }

  private boolean isGroupRole(Role role, ArrayList<Role> groupRoles) {
    return groupRoles.stream().anyMatch(groupRole -> groupRole.getName().equals(role.getName()));
  }

  @RunAsSystem
  public void removeMember(final Group group, final User user) {
    ArrayList<Role> groupRoles = newArrayList(group.getRoles());
    final RoleMembership membership = findRoleMembership(user, groupRoles);
    roleMembershipService.removeMembership(membership);
  }

  @RunAsSystem
  public void removeMember(final Group group, final VOGroup voGroup) {
    ArrayList<Role> groupRoles = newArrayList(group.getRoles());
    final VOGroupRoleMembership membership = findVORoleMembership(voGroup, groupRoles);
    voGroupRoleMembershipService.removeMembership(membership.getId());
  }

  @RunAsSystem
  public void updateMemberRole(final Group group, final User member, final Role newRole) {
    ArrayList<Role> groupRoles = newArrayList(group.getRoles());
    boolean isGroupRole = isGroupRole(newRole, groupRoles);

    if (!isGroupRole) {
      throw new NotAValidGroupRoleException(newRole, group);
    }

    final RoleMembership roleMembership = findRoleMembership(member, groupRoles);
    roleMembershipService.updateMembership(roleMembership, newRole);
  }

  @RunAsSystem
  public void updateMemberRole(final Group group, final VOGroup voGroup, final Role newRole) {
    ArrayList<Role> groupRoles = newArrayList(group.getRoles());
    boolean isGroupRole = isGroupRole(newRole, groupRoles);

    if (!isGroupRole) {
      throw new NotAValidGroupRoleException(newRole, group);
    }

    final VOGroupRoleMembership roleMembership = findVORoleMembership(voGroup, groupRoles);
    voGroupRoleMembershipService.updateMembership(roleMembership.getId(), newRole);
  }

  @RunAsSystem
  public boolean isGroupNameAvailable(final GroupValue groupValue) {
    String rootPackageName = groupValue.getRootPackage().getName();
    final Package existingPackage =
        dataService.query(PACKAGE, Package.class).eq(PackageMetadata.ID, rootPackageName).findOne();
    return existingPackage == null;
  }

  private UnknownEntityException unknownMembershipForUser(User user) {
    return new UnknownEntityException(
        roleMembershipMetadata,
        roleMembershipMetadata.getAttribute(RoleMembershipMetadata.USER),
        user.getUsername());
  }

  private UnknownEntityException unknownMembershipForVOGroup(VOGroup group) {
    return new UnknownEntityException(
        voGroupRoleMembershipMetadata,
        voGroupRoleMembershipMetadata.getAttribute(VOGroupRoleMembershipMetadata.VO_GROUP),
        group);
  }

  private RoleMembership findRoleMembership(User member, List<Role> groupRoles) {
    Collection<RoleMembership> memberships = roleMembershipService.getMemberships(groupRoles);
    return memberships.stream()
        .filter(m -> m.getUser().getId().equals(member.getId()))
        .findFirst()
        .orElseThrow(() -> unknownMembershipForUser(member));
  }

  private VOGroupRoleMembership findVORoleMembership(VOGroup voGroup, List<Role> groupRoles) {
    Collection<VOGroupRoleMembership> memberships =
        voGroupRoleMembershipService.getMemberships(groupRoles);
    return memberships.stream()
        .filter(m -> m.getVOGroup().getId().equals(voGroup.getId()))
        .findFirst()
        .orElseThrow(() -> unknownMembershipForVOGroup(voGroup));
  }

  public void deleteGroup(String groupName) {
    Group group =
        dataService
            .query(GroupMetadata.GROUP, Group.class)
            .eq(GroupMetadata.NAME, groupName)
            .findOne();
    if (group == null) {
      throw new UnknownEntityException(
          groupMetadata, groupMetadata.getAttribute(GroupMetadata.NAME), groupName);
    }
    dataService.delete(PACKAGE, group.getRootPackage());
  }

  public void updateExtendsRole(Group group, Role groupRole, Role memberRole) {
    List<Role> newIncludes = removeIncludedGroupRoles(group, memberRole);
    if (!isGroupRole(groupRole, newArrayList(group.getRoles()))) {
      throw new NotAValidGroupRoleException(groupRole, group);
    }
    newIncludes.add(groupRole);
    memberRole.setIncludes(newIncludes);
    dataService.update(ROLE, memberRole);
  }

  public void removeExtendsRole(Group group, Role memberRole) {
    List<Role> newIncludes = removeIncludedGroupRoles(group, memberRole);
    memberRole.setIncludes(newIncludes);
    dataService.update(ROLE, memberRole);
  }

  private List<Role> removeIncludedGroupRoles(Group group, Role memberRole) {
    ArrayList<Role> includes = newArrayList(memberRole.getIncludes());
    ArrayList<Role> groupRoles = newArrayList(group.getRoles());
    return includes.stream()
        .filter(role -> !isGroupRole(role, groupRoles))
        .collect(Collectors.toList());
  }

  /**
   * Grants default permissions on the root package and group to the roles of the group
   *
   * @param groupValue details of the group for which the permissions will be granted
   */
  public void grantDefaultPermissions(GroupValue groupValue) {
    PackageIdentity packageIdentity = new PackageIdentity(groupValue.getRootPackage().getName());
    GroupIdentity groupIdentity = new GroupIdentity(groupValue.getName());
    aclService.createAcl(groupIdentity);
    groupValue
        .getRoles()
        .forEach(
            roleValue -> {
              PermissionSet permissionSet = PERMISSION_SETS_PER_ROLE.get(roleValue.getLabel());
              Sid roleSid = createRoleSid(roleValue.getName());
              permissionService.createPermission(
                  Permission.create(packageIdentity, roleSid, permissionSet));
              permissionService.createPermission(
                  Permission.create(groupIdentity, roleSid, permissionSet));
            });
    if (groupValue.isPublic()) {
      permissionService.createPermission(
          Permission.create(groupIdentity, createAuthoritySid(AUTHORITY_USER), READ));
    }
  }
}
