package org.molgenis.data.security.permission.inheritance;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.molgenis.data.security.permission.EntityHelper;
import org.molgenis.data.security.permission.PermissionSetUtils;
import org.molgenis.data.security.permission.UserRoleTools;
import org.molgenis.data.security.permission.inheritance.model.InheritedAclPermissionsResult;
import org.molgenis.data.security.permission.inheritance.model.InheritedPermissionsResult;
import org.molgenis.data.security.permission.inheritance.model.InheritedUserPermissionsResult;
import org.molgenis.data.security.permission.model.LabelledObjectIdentity;
import org.molgenis.data.security.permission.model.LabelledPermission;
import org.molgenis.security.core.PermissionSet;
import org.springframework.security.acls.model.AccessControlEntry;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.Sid;
import org.springframework.stereotype.Component;

@Component
public class PermissionInheritanceResolver {
  private final UserRoleTools userRoleTools;
  private final EntityHelper entityHelper;

  PermissionInheritanceResolver(UserRoleTools userRoleTools, EntityHelper entityHelper) {
    this.userRoleTools = requireNonNull(userRoleTools);
    this.entityHelper = requireNonNull(entityHelper);
  }

  InheritedPermissionsResult getInheritedPermissionsResults(Acl acl, Sid sid) {
    List<InheritedUserPermissionsResult> inheritedUserPermissionsResult =
        getPermissionsForRoles(acl, sid);
    InheritedAclPermissionsResult inheritedAclPermissionsResult =
        getParentAclPermissions(acl, sid).orElse(null);
    return InheritedPermissionsResult.create(
        inheritedUserPermissionsResult, inheritedAclPermissionsResult);
  }

  private List<InheritedUserPermissionsResult> getPermissionsForRoles(Acl acl, Sid sid) {
    List<Sid> roles = userRoleTools.getRolesForSid(sid);
    List<InheritedUserPermissionsResult> inheritedUserPermissionsResults = new ArrayList<>();
    for (Sid role : roles) {
      PermissionSet ownPermission = getPermissionsForAcl(acl, role);
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

  private Optional<InheritedAclPermissionsResult> getParentAclPermissions(Acl acl, Sid sid) {
    InheritedAclPermissionsResult parentAclPermissions;
    List<InheritedUserPermissionsResult> parentRolePermissions;
    Acl parentAcl = acl.getParentAcl();
    if (parentAcl != null) {
      PermissionSet ownPermission = getPermissionsForAcl(parentAcl, sid);
      parentRolePermissions = getPermissionsForRoles(parentAcl, sid);
      parentAclPermissions =
          getParentAclPermissions(parentAcl, sid)
              .orElse(null); // Get permissions for parentAcl of the parentAcl - Recursive
      InheritedAclPermissionsResult inheritedAclPermissionsResult =
          InheritedAclPermissionsResult.create(
              parentAcl, ownPermission, parentRolePermissions, parentAclPermissions);
      if (isNotEmpty(inheritedAclPermissionsResult)) {
        return Optional.of(inheritedAclPermissionsResult);
      }
    }
    return Optional.empty();
  }

  private PermissionSet getPermissionsForAcl(Acl acl, Sid sid) {
    PermissionSet ownPermission = null;
    for (AccessControlEntry ace : acl.getEntries()) {
      if (ace.getSid().equals(sid)) {
        ownPermission = PermissionSetUtils.getPermissionSet(ace);
      }
    }
    return ownPermission;
  }

  public boolean isNotEmpty(InheritedPermissionsResult result) {
    return !(result.getRequestedAclParentRolesPermissions() == null
            || result.getRequestedAclParentRolesPermissions().isEmpty())
        || (result.getParentAclPermission() != null && isNotEmpty(result.getParentAclPermission()));
  }

  private boolean isNotEmpty(InheritedUserPermissionsResult result) {
    return result.getOwnPermission() != null
        || !(result.getInheritedUserPermissionsResult() == null
            || result.getInheritedUserPermissionsResult().isEmpty());
  }

  private boolean isNotEmpty(InheritedAclPermissionsResult result) {
    return result.getOwnPermission() != null
        || !(result.getParentRolePermissions() == null
            || result.getParentRolePermissions().isEmpty())
        || (result.getParentAclPermissions() != null
            && isNotEmpty(result.getParentAclPermissions()));
  }

  public Set<LabelledPermission> getInheritedPermissions(Acl acl, Sid sid) {
    InheritedPermissionsResult inheritedPermissionsResult =
        getInheritedPermissionsResults(acl, sid);
    return convertToInheritedPermissions(inheritedPermissionsResult);
  }

  Set<LabelledPermission> convertToInheritedPermissions(
      InheritedPermissionsResult inheritedPermissionsResult) {

    List<InheritedUserPermissionsResult> parentRolePermissions =
        inheritedPermissionsResult.getRequestedAclParentRolesPermissions();
    InheritedAclPermissionsResult parentAclPermissions =
        inheritedPermissionsResult.getParentAclPermission();
    return convertToInheritedPermissions(parentRolePermissions, parentAclPermissions);
  }

  private Set<LabelledPermission> convertToInheritedPermissions(
      List<InheritedUserPermissionsResult> parentRolePermissions,
      InheritedAclPermissionsResult parentAclPermission) {
    Set<LabelledPermission> inheritedPermissions =
        new HashSet<>(convertInheritedRolePermissions(parentRolePermissions));
    if (parentAclPermission != null) {
      Acl acl = parentAclPermission.getAcl();
      LabelledObjectIdentity parentObjectIdentity =
          entityHelper.getLabelledObjectIdentity(acl.getObjectIdentity());
      inheritedPermissions.add(
          convertInheritedAclPermissions(parentAclPermission, parentObjectIdentity));
    }
    return inheritedPermissions;
  }

  private LabelledPermission convertInheritedAclPermissions(
      InheritedAclPermissionsResult parentAclPermission, LabelledObjectIdentity objectIdentity) {
    PermissionSet ownPermission = parentAclPermission.getOwnPermission();
    Set<LabelledPermission> labelledPermissions =
        convertToInheritedPermissions(
            parentAclPermission.getParentRolePermissions(),
            parentAclPermission.getParentAclPermissions());
    return LabelledPermission.create(null, objectIdentity, ownPermission, labelledPermissions);
  }

  private Set<LabelledPermission> convertInheritedRolePermissions(
      List<InheritedUserPermissionsResult> requestedAclParentRolesPermissions) {
    Set<LabelledPermission> results = new HashSet<>();
    for (InheritedUserPermissionsResult parentRolePermission : requestedAclParentRolesPermissions) {
      PermissionSet ownPermission = parentRolePermission.getOwnPermission();
      Sid sid = parentRolePermission.getSid();
      Set<LabelledPermission> labelledPermissions = null;
      if (parentRolePermission.getInheritedUserPermissionsResult() != null) {
        labelledPermissions =
            convertInheritedRolePermissions(
                parentRolePermission.getInheritedUserPermissionsResult());
      }
      results.add(LabelledPermission.create(sid, null, ownPermission, labelledPermissions));
    }
    return results;
  }
}
