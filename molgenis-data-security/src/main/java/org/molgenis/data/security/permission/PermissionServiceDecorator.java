package org.molgenis.data.security.permission;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.molgenis.security.core.SidUtils.createSecurityContextSid;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.DataService;
import org.molgenis.data.security.exception.InsufficientPermissionsException;
import org.molgenis.data.security.exception.ReadPermissionDeniedException;
import org.molgenis.data.security.exception.SidPermissionException;
import org.molgenis.data.security.permission.model.LabelledObject;
import org.molgenis.data.security.permission.model.LabelledPermission;
import org.molgenis.data.security.permission.model.LabelledType;
import org.molgenis.data.security.permission.model.Permission;
import org.molgenis.security.core.PermissionSet;
import org.molgenis.security.core.utils.SecurityUtils;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Sid;

public class PermissionServiceDecorator implements PermissionService {

  private final UserRoleTools userRoleTools;
  private final MutableAclService mutableAclService;
  private final PermissionService permissionService;
  private final DataService dataService;

  public PermissionServiceDecorator(
      PermissionService permissionService,
      DataService dataService,
      UserRoleTools userRoleTools,
      MutableAclService mutableAclService) {
    this.permissionService = requireNonNull(permissionService);
    this.dataService = requireNonNull(dataService);
    this.userRoleTools = requireNonNull(userRoleTools);
    this.mutableAclService = requireNonNull(mutableAclService);
  }

  @Override
  public Set<LabelledType> getTypes() {
    return permissionService
        .getTypes()
        .stream()
        .filter(type -> dataService.hasEntityType(type.getEntityType()))
        .collect(toSet());
  }

  @Override
  public Set<LabelledPermission> getPermissionsForObject(
      ObjectIdentity objectIdentity, Set<Sid> sids, boolean isReturnInheritedPermissions) {
    checkReadPermission(objectIdentity.getType(), sids);
    return permissionService.getPermissionsForObject(
        objectIdentity, sids, isReturnInheritedPermissions);
  }

  @Override
  public void createAcl(ObjectIdentity objectIdentity) {
    checkSuOrSystem(objectIdentity.getType(), objectIdentity.getIdentifier().toString());
    permissionService.createAcl(objectIdentity);
  }

  private void checkSuOrSystem(String type, String identifier) {
    if (!SecurityUtils.currentUserIsSuOrSystem()) {
      throw new InsufficientPermissionsException(
          type, identifier, Collections.singletonList("superuser"));
    }
  }

  @Override
  public void createPermission(Permission objectPermissions) {
    checkSuOrOwner(objectPermissions.getObjectIdentity());
    permissionService.createPermission(objectPermissions);
  }

  @Override
  public void createPermissions(Set<Permission> permissions) {
    for (Permission objectPermissions : permissions) {
      checkSuOrOwner(objectPermissions.getObjectIdentity());
    }
    permissionService.createPermissions(permissions);
  }

  @Override
  public void updatePermission(Permission objectPermissions) {
    checkSuOrOwner(objectPermissions.getObjectIdentity());
    permissionService.updatePermission(objectPermissions);
  }

  @Override
  public void updatePermissions(Set<Permission> permissions) {
    for (Permission objectPermissions : permissions) {
      checkSuOrOwner(objectPermissions.getObjectIdentity());
    }
    permissionService.updatePermissions(permissions);
  }

  @Override
  public void deletePermission(Sid sid, ObjectIdentity objectIdentity) {
    checkSuOrOwner(objectIdentity);
    permissionService.deletePermission(sid, objectIdentity);
  }

  @Override
  public void addType(String typeId) {
    if (SecurityUtils.currentUserIsSuOrSystem()) {
      permissionService.addType(typeId);
    } else {
      throw new InsufficientPermissionsException(
          "type", typeId, Collections.singletonList("superuser"));
    }
  }

  @Override
  public void deleteType(String typeId) {
    checkSuOrSystem("type", typeId);
    permissionService.deleteType(typeId);
  }

  @Override
  public Map<String, Set<LabelledPermission>> getPermissionsForType(
      String typeId, Set<Sid> sids, boolean isReturnInherited) {
    checkReadPermission(typeId, sids);
    return permissionService.getPermissionsForType(typeId, sids, isReturnInherited);
  }

  @Override
  public Map<String, Set<LabelledPermission>> getPermissionsForType(
      String typeId, Set<Sid> sids, int page, int pageSize) {
    checkReadPermission(typeId, sids);
    return permissionService.getPermissionsForType(typeId, sids, page, pageSize);
  }

  @Override
  public Set<LabelledPermission> getPermissions(Set<Sid> sids, boolean isReturnInherited) {
    checkPermissionsOnSid(sids);
    return permissionService.getPermissions(sids, isReturnInherited);
  }

  @Override
  public Set<LabelledObject> getObjects(String typeId, int page, int pageSize) {
    checkReadPermission(typeId, Collections.emptySet());
    return permissionService.getObjects(typeId, page, pageSize);
  }

  @Override
  public Set<PermissionSet> getSuitablePermissionsForType(String typeId) {
    return permissionService.getSuitablePermissionsForType(typeId);
  }

  @Override
  public boolean exists(ObjectIdentity objectIdentity, Sid sid) {
    checkReadPermission(objectIdentity.getType(), Collections.singleton(sid));
    return permissionService.exists(objectIdentity, sid);
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
      throw new InsufficientPermissionsException(
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
