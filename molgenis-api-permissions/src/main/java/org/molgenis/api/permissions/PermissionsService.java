package org.molgenis.api.permissions;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.validation.constraints.NotEmpty;
import org.molgenis.api.permissions.model.request.ObjectPermissionsRequest;
import org.molgenis.api.permissions.model.request.PermissionRequest;
import org.molgenis.api.permissions.model.service.LabelledPermission;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Sid;

public interface PermissionsService {

  List<String> getTypes();

  List<LabelledPermission> getPermission(
      ObjectIdentity objectIdentity, Set<Sid> sids, boolean isReturnInheritedPermissions);

  void createAcl(ObjectIdentity objectIdentity);

  void createPermission(List<PermissionRequest> permissionRequests, ObjectIdentity objectIdentity);

  void createPermissions(List<ObjectPermissionsRequest> objectPermissionsRequests, String typeId);

  void updatePermission(List<PermissionRequest> permissionRequests, ObjectIdentity objectIdentity);

  void updatePermissions(
      @NotEmpty List<ObjectPermissionsRequest> objectPermissionsRequests, String typeId);

  void deletePermission(Sid sid, ObjectIdentity objectIdentity);

  void addType(String typeId);

  void deleteType(String typeId);

  Collection<LabelledPermission> getPermissionsForType(
      String typeId, Set<Sid> sids, boolean isReturnInherited);

  Collection<LabelledPermission> getPagedPermissionsForType(
      String typeId, Set<Sid> sids, int page, int pageSize);

  List<LabelledPermission> getAllPermissions(Set<Sid> sids, boolean isReturnInherited);

  List<String> getAcls(String typeId, int page, int pageSize);

  Set<String> getSuitablePermissionsForType(String typeId);
}
