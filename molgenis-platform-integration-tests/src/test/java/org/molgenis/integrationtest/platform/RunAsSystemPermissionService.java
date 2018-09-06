package org.molgenis.integrationtest.platform;

import static java.util.Objects.requireNonNull;

import java.util.Map;
import org.molgenis.security.core.PermissionService;
import org.molgenis.security.core.PermissionSet;
import org.molgenis.security.core.runas.RunAsSystem;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Sid;
import org.springframework.transaction.annotation.Transactional;

public class RunAsSystemPermissionService implements PermissionService {
  private PermissionService permissionService;

  RunAsSystemPermissionService(PermissionService permissionService) {
    this.permissionService = requireNonNull(permissionService);
  }

  @Override
  @Transactional
  @RunAsSystem
  public void grant(Map<ObjectIdentity, PermissionSet> objectPermissionMap, Sid sid) {
    permissionService.grant(objectPermissionMap, sid);
  }

  @Override
  @Transactional
  @RunAsSystem
  public void grant(ObjectIdentity objectIdentity, PermissionSet permissionSet, Sid sid) {
    permissionService.grant(objectIdentity, permissionSet, sid);
  }
}
