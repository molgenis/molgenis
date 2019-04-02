package org.molgenis.api.permissions.inheritance;

import static java.util.Objects.requireNonNull;
import static org.molgenis.api.permissions.PermissionApiService.ENTITY_PREFIX;
import static org.molgenis.api.permissions.SidConversionUtils.getRole;
import static org.molgenis.security.core.SidUtils.getRoleName;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.apache.commons.lang3.StringUtils;
import org.molgenis.api.permissions.PermissionSetUtils;
import org.molgenis.api.permissions.SidConversionUtils;
import org.molgenis.api.permissions.exceptions.InsufficientInheritancePermissionsException;
import org.molgenis.api.permissions.exceptions.InvalidTypeIdException;
import org.molgenis.api.permissions.inheritance.model.InheritedAclPermissionsResult;
import org.molgenis.api.permissions.inheritance.model.InheritedPermissionsResult;
import org.molgenis.api.permissions.inheritance.model.InheritedUserPermissionsResult;
import org.molgenis.api.permissions.model.response.InheritedPermission;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.data.meta.model.PackageMetadata;
import org.molgenis.data.plugin.model.PluginIdentity;
import org.molgenis.data.plugin.model.PluginMetadata;
import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.data.security.EntityTypePermission;
import org.molgenis.data.security.GroupIdentity;
import org.molgenis.data.security.PackageIdentity;
import org.molgenis.data.security.auth.GroupMetadata;
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
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Sid;
import org.springframework.stereotype.Component;

@Component
public class PermissionInheritanceResolver {

  private final DataService dataService;
  private final UserPermissionEvaluator userPermissionEvaluator;
  private final UserService userService;

  public PermissionInheritanceResolver(
      DataService dataService,
      UserPermissionEvaluator userPermissionEvaluator,
      UserService userService) {
    this.dataService = requireNonNull(dataService);
    this.userPermissionEvaluator = requireNonNull(userPermissionEvaluator);
    this.userService = requireNonNull(userService);
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
    return !(result.getRequestedAclParentRolesPermissions() == null
            || result.getRequestedAclParentRolesPermissions().isEmpty())
        || (result.getParentAclPermission() != null && isNotEmpty(result.getParentAclPermission()));
  }

  private boolean isNotEmpty(InheritedUserPermissionsResult result) {
    return StringUtils.isNotEmpty(result.getOwnPermission())
        || !(result.getInheritedUserPermissionsResult() == null
            || result.getInheritedUserPermissionsResult().isEmpty());
  }

  private boolean isNotEmpty(InheritedAclPermissionsResult result) {
    return StringUtils.isNotEmpty(result.getOwnPermission())
        || !(result.getParentRolePermissions() == null
            || result.getParentRolePermissions().isEmpty())
        || (result.getParentAclPermissions() != null
            && isNotEmpty(result.getParentAclPermissions()));
  }

  public List<InheritedPermission> convertToInheritedPermissions(
      InheritedPermissionsResult inheritedPermissionsResult) {
    List<InheritedUserPermissionsResult> parentRolePermissions =
        inheritedPermissionsResult.getRequestedAclParentRolesPermissions();
    InheritedAclPermissionsResult parentAclPermissions =
        inheritedPermissionsResult.getParentAclPermission();
    return convertToInheritedPermissions(parentRolePermissions, parentAclPermissions);
  }

  private List<InheritedPermission> convertToInheritedPermissions(
      List<InheritedUserPermissionsResult> parentRolePermissions,
      InheritedAclPermissionsResult parentAclPermission) {
    List<InheritedPermission> inheritedPermissions =
        new ArrayList<>(convertInheritedRolePermissions(parentRolePermissions));
    if (parentAclPermission != null) {
      inheritedPermissions.add(convertInheritedAclPermissions(parentAclPermission));
    }
    return inheritedPermissions;
  }

  private InheritedPermission convertInheritedAclPermissions(
      InheritedAclPermissionsResult parentAclPermission) {
    Acl acl = parentAclPermission.getAcl();
    ObjectIdentity objectIdentity = acl.getObjectIdentity();
    String ownPermission = parentAclPermission.getOwnPermission();
    List<InheritedPermission> inheritedPermissions =
        convertToInheritedPermissions(
            parentAclPermission.getParentRolePermissions(),
            parentAclPermission.getParentAclPermissions());
    return InheritedPermission.create(
        null,
        objectIdentity.getType(),
        getLabel(getEntityTypeIdFromClass(objectIdentity.getType())),
        objectIdentity.getIdentifier().toString(),
        getLabel(
            getEntityTypeIdFromClass(objectIdentity.getType()),
            objectIdentity.getIdentifier().toString()),
        ownPermission,
        inheritedPermissions);
  }

  private List<InheritedPermission> convertInheritedRolePermissions(
      List<InheritedUserPermissionsResult> requestedAclParentRolesPermissions) {
    List<InheritedPermission> results = new ArrayList<>();
    for (InheritedUserPermissionsResult parentRolePermission : requestedAclParentRolesPermissions) {
      String ownPermission = parentRolePermission.getOwnPermission();
      Sid sid = parentRolePermission.getSid();
      List<InheritedPermission> inheritedPermissions = null;
      if (parentRolePermission.getInheritedUserPermissionsResult() != null) {
        inheritedPermissions =
            convertInheritedRolePermissions(
                parentRolePermission.getInheritedUserPermissionsResult());
      }
      results.add(
          InheritedPermission.create(
              getRole(sid), null, null, null, null, ownPermission, inheritedPermissions));
    }
    return results;
  }

  private String getLabel(String entityTypeId, String identifier) {
    Entity entity = dataService.getRepository(entityTypeId).findOneById(identifier);
    if (entity == null) {
      throw new UnknownEntityException(entityTypeId, identifier);
    }
    return entity.getLabelValue().toString();
  }

  private String getLabel(String entityTypeId) {
    return dataService.getRepository(entityTypeId).getEntityType().getLabel();
  }

  String getEntityTypeIdFromClass(String typeId) {
    String result;
    switch (typeId) {
      case PackageIdentity.PACKAGE:
        result = PackageMetadata.PACKAGE;
        break;
      case EntityTypeIdentity.ENTITY_TYPE:
        result = EntityTypeMetadata.ENTITY_TYPE_META_DATA;
        break;
      case PluginIdentity.PLUGIN:
        result = PluginMetadata.PLUGIN;
        break;
      case GroupIdentity.GROUP:
        result = GroupMetadata.GROUP;
        break;
      default:
        if (typeId.startsWith(ENTITY_PREFIX)) {
          result = typeId.substring(7);
        } else {
          throw new InvalidTypeIdException(typeId);
        }
        break;
    }
    return result;
  }
}
