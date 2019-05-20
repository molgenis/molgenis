package org.molgenis.api.permissions.inheritance;

import java.util.Collections;
import org.molgenis.api.permissions.inheritance.model.InheritedAclPermissionsResult;
import org.molgenis.api.permissions.inheritance.model.InheritedPermissionsResult;
import org.molgenis.api.permissions.inheritance.model.InheritedUserPermissionsResult;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.Sid;

/**
 * Acl structure for these tests: Entity | package | parent package
 *
 * <p>User structure for these tests User / \ Role1 Role2 | Role3
 *
 * <p>Permissions: User has read permission on package Role2 has write on entity Role3 had writemeta
 * on parent package
 */
public class InheritanceTestUtils {
  public static InheritedPermissionsResult getInheritedPermissionsResult(
      Acl entityAcl, Acl packageAcl, Acl parentPackageAcl, Sid role1, Sid role2, Sid role3) {
    // Permissions on parentpackage
    InheritedUserPermissionsResult parentPackageAclPermissionsRole3 =
        InheritedUserPermissionsResult.create(role3, "WRITEMETA", Collections.emptyList());
    InheritedUserPermissionsResult parentPackageAclPermissionsRole1 =
        InheritedUserPermissionsResult.create(
            role1, null, Collections.singletonList(parentPackageAclPermissionsRole3));
    InheritedAclPermissionsResult parentAclPermissions =
        InheritedAclPermissionsResult.create(
            parentPackageAcl,
            null,
            Collections.singletonList(parentPackageAclPermissionsRole1),
            null);
    InheritedAclPermissionsResult parentPackageAclPermissions =
        InheritedAclPermissionsResult.create(
            parentPackageAcl,
            null,
            Collections.singletonList(parentPackageAclPermissionsRole1),
            null);
    // Permissions on package
    InheritedAclPermissionsResult packageAclPermissions =
        InheritedAclPermissionsResult.create(
            packageAcl, "READ", Collections.emptyList(), parentPackageAclPermissions);
    // Permissions on entity
    InheritedUserPermissionsResult entityPermissionRole2 =
        InheritedUserPermissionsResult.create(role2, "WRITE", Collections.emptyList());

    return InheritedPermissionsResult.create(
        Collections.singletonList(entityPermissionRole2), packageAclPermissions);
  }
}
