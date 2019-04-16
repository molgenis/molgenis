package org.molgenis.api.permissions;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.validation.constraints.NotEmpty;
import org.molgenis.api.permissions.model.request.ObjectPermissionsRequest;
import org.molgenis.api.permissions.model.request.PermissionRequest;
import org.molgenis.api.permissions.model.response.ObjectPermissionsResponse;
import org.molgenis.api.permissions.model.response.PermissionResponse;
import org.molgenis.api.permissions.model.response.TypePermissionsResponse;
import org.springframework.security.acls.model.Sid;

public interface PermissionsService {

  List<String> getTypes();

  List<PermissionResponse> getPermission(
      String typeId, String identifier, Set<Sid> sids, boolean isReturnInheritedPermissions);

  void createAcl(String typeId, String identifier);

  void createPermission(
      List<PermissionRequest> permissionRequests, String typeId, String identifier);

  void createPermissions(List<ObjectPermissionsRequest> objectPermissionsRequests, String typeId);

  void updatePermission(
      List<PermissionRequest> permissionRequests, String typeId, String identifier);

  void updatePermissions(
      @NotEmpty List<ObjectPermissionsRequest> objectPermissionsRequests, String typeId);

  void deletePermission(Sid sid, String typeId, String identifier);

  void addType(String typeId);

  void deleteType(String typeId);

  Collection<ObjectPermissionsResponse> getPermissionsForType(
      String typeId, Set<Sid> sids, boolean isReturnInherited);

  Collection<ObjectPermissionsResponse> getPagedPermissionsForType(
      String typeId, Set<Sid> sids, int page, int pageSize);

  List<TypePermissionsResponse> getAllPermissions(Set<Sid> sids, boolean isReturnInherited);

  List<String> getAcls(String typeId, int page, int pageSize);

  Set<String> getSuitablePermissionsForType(String typeId);
}
