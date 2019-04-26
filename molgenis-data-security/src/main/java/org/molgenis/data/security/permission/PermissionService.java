package org.molgenis.data.security.permission;

import java.util.Map;
import java.util.Set;
import org.molgenis.data.security.permission.model.LabelledObject;
import org.molgenis.data.security.permission.model.LabelledPermission;
import org.molgenis.data.security.permission.model.LabelledType;
import org.molgenis.data.security.permission.model.Permission;
import org.molgenis.security.core.PermissionSet;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Sid;

public interface PermissionService {

  void createAcl(ObjectIdentity objectIdentity);

  void createPermission(Permission permissions);

  void createPermissions(Set<Permission> permissions);

  void updatePermission(Permission permissions);

  void updatePermissions(Set<Permission> permissions);

  void deletePermission(Sid sid, ObjectIdentity objectIdentity);

  void addType(String typeId);

  void deleteType(String typeId);

  Map<String, Set<LabelledPermission>> getPermissionsForType(
      String typeId, Set<Sid> sids, boolean includeInheritance);

  Map<String, Set<LabelledPermission>> getPermissionsForType(
      String typeId, Set<Sid> sids, int page, int pageSize);

  Set<LabelledPermission> getPermissionsForObject(
      ObjectIdentity objectIdentity, Set<Sid> sids, boolean includeInheritance);

  Set<LabelledPermission> getPermissions(Set<Sid> sids, boolean includeInheritance);

  Set<LabelledType> getTypes();

  Set<LabelledObject> getObjects(String typeId, int page, int pageSize);

  Set<PermissionSet> getSuitablePermissionsForType(String typeId);

  boolean exists(ObjectIdentity objectIdentity, Sid sid);
}
