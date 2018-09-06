package org.molgenis.data.security.auth;

import static org.molgenis.data.security.auth.GroupService.*;
import static org.molgenis.security.core.PermissionSet.*;
import static org.molgenis.security.core.SidUtils.createAuthoritySid;
import static org.molgenis.security.core.SidUtils.createRoleSid;
import static org.molgenis.security.core.utils.SecurityUtils.AUTHORITY_USER;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Objects;
import org.molgenis.data.security.GroupIdentity;
import org.molgenis.data.security.PackageIdentity;
import org.molgenis.security.core.PermissionService;
import org.molgenis.security.core.PermissionSet;
import org.molgenis.security.core.model.GroupValue;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.Sid;
import org.springframework.stereotype.Service;

@Service
public class GroupPermissionService {
  private final MutableAclService aclService;
  private final PermissionService permissionService;

  private static final Map<String, PermissionSet> PERMISSION_SETS_PER_ROLE =
      ImmutableMap.of(MANAGER, WRITEMETA, EDITOR, WRITE, VIEWER, READ);

  public GroupPermissionService(MutableAclService aclService, PermissionService permissionService) {
    this.aclService = Objects.requireNonNull(aclService);
    this.permissionService = Objects.requireNonNull(permissionService);
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
              permissionService.grant(packageIdentity, permissionSet, roleSid);
              permissionService.grant(groupIdentity, permissionSet, roleSid);
            });
    if (groupValue.isPublic()) {
      permissionService.grant(groupIdentity, READ, createAuthoritySid(AUTHORITY_USER));
    }
  }
}
