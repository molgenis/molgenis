package org.molgenis.api.permissions;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.security.core.SidUtils.createSecurityContextSid;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.validation.constraints.NotEmpty;
import org.apache.commons.lang3.StringUtils;
import org.molgenis.api.permissions.exceptions.AclNotFoundException;
import org.molgenis.api.permissions.exceptions.InsufficientPermissionDeniedException;
import org.molgenis.api.permissions.exceptions.ReadPermissionDeniedException;
import org.molgenis.api.permissions.exceptions.SidPermissionException;
import org.molgenis.api.permissions.model.request.ObjectPermissionsRequest;
import org.molgenis.api.permissions.model.request.PermissionRequest;
import org.molgenis.api.permissions.model.response.ObjectPermission;
import org.molgenis.api.permissions.model.response.ObjectPermissionsResponse;
import org.molgenis.api.permissions.model.response.TypePermissionsResponse;
import org.molgenis.data.DataService;
import org.molgenis.security.core.utils.SecurityUtils;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Sid;

public class PermissionServiceDecorator implements PermissionsService {

  private final UserRoleTools userRoleTools;
  private MutableAclService mutableAclService;
  private PermissionsService permissionsService;
  private DataService dataService;
  private IdentityTools identityTools;

  public PermissionServiceDecorator(
      PermissionsService permissionsService,
      DataService dataService,
      IdentityTools identityTools,
      UserRoleTools userRoleTools,
      MutableAclService mutableAclService) {
    this.permissionsService = requireNonNull(permissionsService);
    this.dataService = requireNonNull(dataService);
    this.identityTools = requireNonNull(identityTools);
    this.userRoleTools = requireNonNull(userRoleTools);
    this.mutableAclService = requireNonNull(mutableAclService);
  }

  @Override
  public List<String> getTypes() {
    return permissionsService
        .getTypes()
        .stream()
        .filter(type -> dataService.hasEntityType(identityTools.getEntityTypeIdFromType(type)))
        .collect(toList());
  }

  @Override
  public List<ObjectPermission> getPermission(
      ObjectIdentity objectIdentity, Set<Sid> sids, boolean isReturnInheritedPermissions) {
    checkReadPermission(objectIdentity.getType(), sids);
    return permissionsService.getPermission(objectIdentity, sids, isReturnInheritedPermissions);
  }

  @Override
  public void createAcl(ObjectIdentity objectIdentity) {
    checkSuOrSystem(objectIdentity.getType(), objectIdentity.getIdentifier().toString());
    permissionsService.createAcl(objectIdentity);
  }

  private void checkSuOrSystem(String type, String identifier) {
    if (!SecurityUtils.currentUserIsSuOrSystem()) {
      throw new InsufficientPermissionDeniedException(
          type, identifier, Collections.singletonList("superuser"));
    }
  }

  @Override
  public void createPermission(
      List<PermissionRequest> permissionRequests, ObjectIdentity objectIdentity) {
    checkSuOrOwner(objectIdentity);
    permissionsService.createPermission(permissionRequests, objectIdentity);
  }

  @Override
  public void createPermissions(
      List<ObjectPermissionsRequest> objectPermissionsRequests, String typeId) {
    objectPermissionsRequests
        .stream()
        .forEach(
            request -> {
              ObjectIdentity objectIdentity =
                  identityTools.getObjectIdentity(typeId, request.getObjectId());
              checkSuOrOwner(objectIdentity);
            });
    permissionsService.createPermissions(objectPermissionsRequests, typeId);
  }

  @Override
  public void updatePermission(
      List<PermissionRequest> permissionRequests, ObjectIdentity objectIdentity) {
    try {
      checkSuOrOwner(objectIdentity);
      permissionsService.updatePermission(permissionRequests, objectIdentity);
    } catch (NotFoundException e) {
      throw new AclNotFoundException(objectIdentity.getType());
    }
  }

  @Override
  public void updatePermissions(
      @NotEmpty List<ObjectPermissionsRequest> objectPermissionsRequests, String typeId) {
    permissionsService.updatePermissions(objectPermissionsRequests, typeId);
  }

  @Override
  public void deletePermission(Sid sid, ObjectIdentity objectIdentity) {
    permissionsService.deletePermission(sid, objectIdentity);
  }

  @Override
  public void addType(String typeId) {
    if (SecurityUtils.currentUserIsSuOrSystem()) {
      permissionsService.addType(typeId);
    } else {
      throw new InsufficientPermissionDeniedException(
          "type", typeId, Collections.singletonList("superuser"));
    }
  }

  @Override
  public void deleteType(String typeId) {
    checkSuOrSystem("type", typeId);
    permissionsService.deleteType(typeId);
  }

  @Override
  public Collection<ObjectPermissionsResponse> getPermissionsForType(
      String typeId, Set<Sid> sids, boolean isReturnInherited) {
    checkReadPermission(typeId, sids);
    return permissionsService.getPermissionsForType(typeId, sids, isReturnInherited);
  }

  @Override
  public Collection<ObjectPermissionsResponse> getPagedPermissionsForType(
      String typeId, Set<Sid> sids, int page, int pageSize) {
    checkReadPermission(typeId, sids);
    return permissionsService.getPagedPermissionsForType(typeId, sids, page, pageSize);
  }

  @Override
  public List<TypePermissionsResponse> getAllPermissions(Set<Sid> sids, boolean isReturnInherited) {
    checkPermissionsOnSid(sids);
    return permissionsService.getAllPermissions(sids, isReturnInherited);
  }

  @Override
  public List<String> getAcls(String typeId, int page, int pageSize) {
    checkReadPermission(typeId, Collections.emptySet());
    return permissionsService.getAcls(typeId, page, pageSize);
  }

  @Override
  public Set<String> getSuitablePermissionsForType(String typeId) {
    return permissionsService.getSuitablePermissionsForType(typeId);
  }

  private void checkPermissionsOnSid(Set<Sid> sids) {
    List<Sid> forbiddenSids = getForbiddenSids(sids);
    if (!SecurityUtils.currentUserIsSuOrSystem() && !forbiddenSids.isEmpty()) {
      List<String> sidNames = forbiddenSids.stream().map(UserRoleTools::getName).collect(toList());
      throw new SidPermissionException(StringUtils.join(sidNames, ","));
    }
  }

  private void checkReadPermission(String typeId, Set<Sid> sids) {
    if (!isSuOrSelf(sids)) {
      throw new ReadPermissionDeniedException(typeId);
    }
  }

  private void checkSuOrOwner(ObjectIdentity objectIdentity) {
    MutableAcl acl = (MutableAcl) mutableAclService.readAclById(objectIdentity);
    Sid sid = createSecurityContextSid();
    boolean isOwner = acl.getOwner().equals(sid);
    if (!SecurityUtils.currentUserIsSuOrSystem() && !isOwner) {
      throw new InsufficientPermissionDeniedException(
          acl.getObjectIdentity(), Arrays.asList("superuser", "owner"));
    }
  }

  private boolean isSuOrSelf(Set<Sid> sids) {
    return (SecurityUtils.currentUserIsSuOrSystem())
        || !(sids.isEmpty() || !getForbiddenSids(sids).isEmpty());
  }

  private List<Sid> getForbiddenSids(Set<Sid> sids) {
    List<Sid> result = new LinkedList<>();
    // User are allowed to query for their own permissions including permissions from roles they
    // have.
    Sid currentUser = createSecurityContextSid();
    Set<Sid> roles = userRoleTools.getRoles(currentUser);
    for (Sid sid : sids) {
      if (!roles.contains(sid) && !(currentUser.equals(sid))) {
        result.add(sid);
      }
    }
    return result;
  }
}
