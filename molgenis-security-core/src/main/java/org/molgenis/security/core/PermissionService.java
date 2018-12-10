package org.molgenis.security.core;

import java.util.Map;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Sid;

public interface PermissionService {
  void grant(Map<ObjectIdentity, PermissionSet> objectPermissionMap, Sid sid);

  void grant(ObjectIdentity objectIdentity, PermissionSet permissionSet, Sid sid);

  /** @return whether permissions are granted to a {@link Sid} for an {@link ObjectIdentity}. */
  boolean exists(ObjectIdentity objectIdentity, Sid sid);
}
