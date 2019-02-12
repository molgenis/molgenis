package org.molgenis.api.permissions.inheritance;

import static java.util.Objects.requireNonNull;
import static org.molgenis.security.core.SidUtils.getRoleName;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.apache.commons.lang3.StringUtils;
import org.molgenis.api.permissions.PermissionSetUtils;
import org.molgenis.api.permissions.SidConversionUtils;
import org.molgenis.api.permissions.exceptions.InsufficientInheritancePermissionsException;
import org.molgenis.api.permissions.inheritance.model.InheritedAclPermissionsResult;
import org.molgenis.api.permissions.inheritance.model.InheritedPermissionsResult;
import org.molgenis.api.permissions.inheritance.model.InheritedUserPermissionsResult;
import org.molgenis.data.DataService;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.data.security.EntityTypePermission;
import org.molgenis.data.security.auth.Role;
import org.molgenis.data.security.auth.RoleMembership;
import org.molgenis.data.security.auth.RoleMembershipMetadata;
import org.molgenis.data.security.auth.RoleMetadata;
import org.molgenis.data.security.auth.User;
import org.molgenis.data.security.user.UnknownUserException;
import org.molgenis.data.security.user.UserService;
import org.molgenis.security.core.SidUtils;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.AccessControlEntry;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.Sid;
import org.springframework.stereotype.Component;

@Component
public class PermissionInheritanceResolver {

  private final DataService dataService;
  private final UserPermissionEvaluator userPermissionEvaluator;
  private final UserService userService;
  private final InheritedPermissionFactory inheritedPermissionFactory;

  public PermissionInheritanceResolver(
      DataService dataService,
      UserPermissionEvaluator userPermissionEvaluator,
      UserService userService,
      InheritedPermissionFactory inheritedPermissionFactory) {
    this.dataService = requireNonNull(dataService);
    this.userPermissionEvaluator = requireNonNull(userPermissionEvaluator);
    this.userService = requireNonNull(userService);
    this.inheritedPermissionFactory = requireNonNull(inheritedPermissionFactory);
  }

  public InheritedPermissionFactory getInheritedPermissionFactory() {
    return inheritedPermissionFactory;
  }

  public InheritedPermissionsResult getInheritedPermissions(Acl acl, Sid sid) {
    List<InheritedUserPermissionsResult> inheritedUserPermissionsResult =
        getPermissionsForRoles(acl, sid);
    InheritedAclPermissionsResult inheritedAclPermissionsResult = getParentAclPermissions(acl, sid);
    return InheritedPermissionsResult.create(
        inheritedUserPermissionsResult, inheritedAclPermissionsResult);
  }

  private List<InheritedUserPermissionsResult> getPermissionsForRoles(Acl acl, Sid sid) {
    List<Sid> roles = getRoles(sid);
    List<InheritedUserPermissionsResult> inheritedUserPermissionsResults = new ArrayList<>();
    for (Sid role : roles) {
      String ownPermission = getPermissionsForAcl(acl, role);
      List<InheritedUserPermissionsResult> parentRolePernissionResult =
          getPermissionsForRoles(acl, role);
      InheritedUserPermissionsResult inheritedUserPermissionsResult =
          InheritedUserPermissionsResult.create(role, ownPermission, parentRolePernissionResult);
      if (isNotEmpty(inheritedUserPermissionsResult)) {
        inheritedUserPermissionsResults.add(inheritedUserPermissionsResult);
      }
    }
    return inheritedUserPermissionsResults;
  }

  private InheritedAclPermissionsResult getParentAclPermissions(Acl acl, Sid sid) {
    InheritedAclPermissionsResult parentAclPermissions;
    List<InheritedUserPermissionsResult> parentRolePermissions;
    Acl parentAcl = acl.getParentAcl();
    if (parentAcl != null) {
      String ownPermission = getPermissionsForAcl(parentAcl, sid);
      parentRolePermissions = getPermissionsForRoles(parentAcl, sid);
      parentAclPermissions =
          getParentAclPermissions(
              parentAcl, sid); // Get permissions for parentAcl of the parentAcl - Recursive
      InheritedAclPermissionsResult inheritedAclPermissionsResult =
          InheritedAclPermissionsResult.create(
              parentAcl, ownPermission, parentRolePermissions, parentAclPermissions);
      if (isNotEmpty(inheritedAclPermissionsResult)) {
        return inheritedAclPermissionsResult;
      }
    }
    return null;
  }

  private String getPermissionsForAcl(Acl acl, Sid sid) {
    String ownPermission = null;
    for (AccessControlEntry ace : acl.getEntries()) {
      if (ace.getSid().equals(sid)) {
        ownPermission = PermissionSetUtils.getPermissionStringValue(ace);
      }
    }
    return ownPermission;
  }

  public List<Sid> getRoles(Sid sid) {
    List<Sid> roles = new ArrayList<>();
    if (sid instanceof PrincipalSid) {
      roles = getRolesForUser(sid);
    } else if (sid instanceof GrantedAuthoritySid) {
      String role = ((GrantedAuthoritySid) sid).getGrantedAuthority();
      roles = getParentRoles(getRoleName(role));
    }
    return roles;
  }

  private List<Sid> getParentRoles(String roleName) {
    if (userPermissionEvaluator.hasPermission(
        new EntityTypeIdentity(RoleMetadata.ROLE), EntityTypePermission.READ_DATA)) {
      Role role =
          dataService
              .getRepository(RoleMetadata.ROLE, Role.class)
              .query()
              .eq(RoleMetadata.NAME, roleName)
              .findOne();
      if (role == null) {
        throw new UnknownEntityException(RoleMetadata.ROLE, roleName);
      }
      return StreamSupport.stream(role.getIncludes().spliterator(), false)
          .map(parentRole -> SidUtils.createRoleSid(parentRole.getName()))
          .collect(Collectors.toList());
    } else {
      throw new InsufficientInheritancePermissionsException();
    }
  }

  private List<Sid> getRolesForUser(Sid sid) {
    String username = SidConversionUtils.getUser(sid);
    if (userPermissionEvaluator.hasPermission(
        new EntityTypeIdentity(RoleMembershipMetadata.ROLE_MEMBERSHIP),
        EntityTypePermission.READ_DATA)) {
      User user = userService.getUser(username);
      if (user == null) {
        throw new UnknownUserException(username);
      }
      return dataService
          .getRepository(RoleMembershipMetadata.ROLE_MEMBERSHIP, RoleMembership.class)
          .query()
          .eq(RoleMembershipMetadata.USER, user.getId())
          .findAll()
          .map(roleMembership -> SidUtils.createRoleSid(roleMembership.getRole().getName()))
          .collect(Collectors.toList());
    } else {
      throw new InsufficientInheritancePermissionsException();
    }
  }

  public boolean isNotEmpty(InheritedPermissionsResult result) {
    return !(result.getRequestedAclParentRolesPermissions().isEmpty()
            || result.getRequestedAclParentRolesPermissions() == null)
        || (result.getParentAclPermission() != null && isNotEmpty(result.getParentAclPermission()));
  }

  public boolean isNotEmpty(InheritedUserPermissionsResult result) {
    return StringUtils.isNotEmpty(result.getOwnPermission())
        || !result.getInheritedUserPermissionsResult().isEmpty();
  }

  public boolean isNotEmpty(InheritedAclPermissionsResult result) {
    return StringUtils.isNotEmpty(result.getOwnPermission())
        || !result.getParentRolePermissions().isEmpty()
        || (result.getParentAclPermissions() != null
            && isNotEmpty(result.getParentAclPermissions()));
  }
}
