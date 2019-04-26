package org.molgenis.integrationtest.platform;

import static java.util.Objects.requireNonNull;

import java.util.Map;
import java.util.Map.Entry;
import org.molgenis.data.security.permission.PermissionService;
import org.molgenis.data.security.permission.model.Permission;
import org.molgenis.security.core.PermissionSet;
import org.molgenis.security.core.runas.RunAsSystem;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Sid;
import org.springframework.transaction.annotation.Transactional;

public class RunAsSystemPermissionService {
  private PermissionService permissionService;

  RunAsSystemPermissionService(PermissionService permissionService) {
    this.permissionService = requireNonNull(permissionService);
  }

  @Transactional
  @RunAsSystem
  public void grant(Map<ObjectIdentity, PermissionSet> objectPermissionMap, Sid sid) {
    for (Entry<ObjectIdentity, PermissionSet> entry : objectPermissionMap.entrySet()) {
      permissionService.createPermission(Permission.create(entry.getKey(), sid, entry.getValue()));
    }
  }

  @Transactional
  @RunAsSystem
  public void grant(ObjectIdentity objectIdentity, PermissionSet permissionSet, Sid sid) {
    permissionService.createPermission(Permission.create(objectIdentity, sid, permissionSet));
  }

  @Transactional(readOnly = true)
  @RunAsSystem
  public boolean exists(ObjectIdentity objectIdentity, Sid sid) {
    return permissionService.exists(objectIdentity, sid);
  }
}
