package org.molgenis.data.security.permission;

import java.util.Map;
import java.util.Set;
import org.molgenis.data.security.permission.model.LabelledObjectPermission;
import org.molgenis.data.security.permission.model.LabelledPermission;
import org.molgenis.data.security.permission.model.ObjectPermissions;
import org.molgenis.data.security.permission.model.Type;
import org.molgenis.data.security.permission.model.TypePermission;
import org.molgenis.security.core.PermissionSet;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Sid;

public interface PermissionService {

  Set<Type> getTypes();

  LabelledObjectPermission getPermission(
      ObjectIdentity objectIdentity, Set<Sid> sids, boolean isReturnInheritedPermissions);

  void createAcl(ObjectIdentity objectIdentity);

  void createPermission(ObjectPermissions permissions);

  void createPermissions(Set<ObjectPermissions> permissions);

  void updatePermission(ObjectPermissions permissions);

  void updatePermissions(Set<ObjectPermissions> permissions);

  void deletePermission(Sid sid, ObjectIdentity objectIdentity);

  void addType(String typeId);

  void deleteType(String typeId);

  TypePermission getPermissionsForType(String typeId, Set<Sid> sids, boolean isReturnInherited);

  TypePermission getPagedPermissionsForType(String typeId, Set<Sid> sids, int page, int pageSize);

  Set<LabelledPermission> getAllPermissions(Set<Sid> sids, boolean isReturnInherited);

  Set<String> getAcls(String typeId, int page, int pageSize);

  Set<String> getSuitablePermissionsForType(String typeId);

  boolean exists(ObjectIdentity objectIdentity, Sid sid);

  void grant(Map<ObjectIdentity, PermissionSet> objectIdentityPermissionMap, Sid sid);

  void grant(ObjectIdentity objectIdentity, PermissionSet permission, Sid sid);
}
