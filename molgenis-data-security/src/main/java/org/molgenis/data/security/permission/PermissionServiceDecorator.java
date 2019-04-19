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
import org.molgenis.data.security.exception.InsufficientPermissionDeniedException;
import org.molgenis.data.security.exception.ReadPermissionDeniedException;
import org.molgenis.data.security.exception.SidPermissionException;
import org.molgenis.data.security.permission.model.LabelledObjectPermission;
import org.molgenis.data.security.permission.model.LabelledPermission;
import org.molgenis.data.security.permission.model.ObjectPermissions;
import org.molgenis.data.security.permission.model.Type;
import org.molgenis.data.security.permission.model.TypePermission;
import org.molgenis.security.core.PermissionSet;
import org.molgenis.security.core.utils.SecurityUtils;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Sid;

public class PermissionServiceDecorator implements PermissionService {

  private final UserRoleTools userRoleTools;
  private MutableAclService mutableAclService;
  private PermissionService permissionService;
  private DataService dataService;

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
  public Set<Type> getTypes() {
    return permissionService
        .getTypes()
        .stream()
        .filter(type -> dataService.hasEntityType(type.getEntityType()))
        .collect(toSet());
  }

  @Override
  public LabelledObjectPermission getPermission(
      ObjectIdentity objectIdentity, Set<Sid> sids, boolean isReturnInheritedPermissions) {
    checkReadPermission(objectIdentity.getType(), sids);
    return permissionService.getPermission(objectIdentity, sids, isReturnInheritedPermissions);
  }

  @Override
  public void createAcl(ObjectIdentity objectIdentity) {
    checkSuOrSystem(objectIdentity.getType(), objectIdentity.getIdentifier().toString());
    permissionService.createAcl(objectIdentity);
  }

  private void checkSuOrSystem(String type, String identifier) {
    if (!SecurityUtils.currentUserIsSuOrSystem()) {
      throw new InsufficientPermissionDeniedException(
          type, identifier, Collections.singletonList("superuser"));
    }
  }

  @Override
  public void createPermission(ObjectPermissions objectPermissions) {
    checkSuOrOwner(objectPermissions.getObjectIdentity());
    permissionService.createPermission(objectPermissions);
  }

  @Override
  public void createPermissions(Set<ObjectPermissions> permissions) {
    for (ObjectPermissions objectPermissions : permissions) {
      checkSuOrOwner(objectPermissions.getObjectIdentity());
    }
    permissionService.createPermissions(permissions);
  }

  @Override
  public void updatePermission(ObjectPermissions objectPermissions) {
    checkSuOrOwner(objectPermissions.getObjectIdentity());
    permissionService.updatePermission(objectPermissions);
  }

  @Override
  public void updatePermissions(Set<ObjectPermissions> permissions) {
    for (ObjectPermissions objectPermissions : permissions) {
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
      throw new InsufficientPermissionDeniedException(
          "type", typeId, Collections.singletonList("superuser"));
    }
  }

  @Override
  public void deleteType(String typeId) {
    checkSuOrSystem("type", typeId);
    permissionService.deleteType(typeId);
  }

  @Override
  public TypePermission getPermissionsForType(
      String typeId, Set<Sid> sids, boolean isReturnInherited) {
    checkReadPermission(typeId, sids);
    return permissionService.getPermissionsForType(typeId, sids, isReturnInherited);
  }

  @Override
  public TypePermission getPagedPermissionsForType(
      String typeId, Set<Sid> sids, int page, int pageSize) {
    checkReadPermission(typeId, sids);
    return permissionService.getPagedPermissionsForType(typeId, sids, page, pageSize);
  }

  @Override
  public Set<LabelledPermission> getAllPermissions(Set<Sid> sids, boolean isReturnInherited) {
    checkPermissionsOnSid(sids);
    return permissionService.getAllPermissions(sids, isReturnInherited);
  }

  @Override
  public Set<String> getAcls(String typeId, int page, int pageSize) {
    checkReadPermission(typeId, Collections.emptySet());
    return permissionService.getAcls(typeId, page, pageSize);
  }

  @Override
  public Set<String> getSuitablePermissionsForType(String typeId) {
    return permissionService.getSuitablePermissionsForType(typeId);
  }

  @Override
  public boolean exists(ObjectIdentity objectIdentity, Sid sid) {
    checkReadPermission(objectIdentity.getType(), Collections.singleton(sid));
    return permissionService.exists(objectIdentity, sid);
  }

  @Override
  public void grant(Map<ObjectIdentity, PermissionSet> objectIdentityPermissionMap, Sid sid) {
    for (ObjectIdentity objectIdentity : objectIdentityPermissionMap.keySet()) {
      checkSuOrOwner(objectIdentity);
    }
    permissionService.grant(objectIdentityPermissionMap, sid);
  }

  @Override
  public void grant(ObjectIdentity objectIdentity, PermissionSet permission, Sid sid) {
    checkSuOrOwner(objectIdentity);
    permissionService.grant(objectIdentity, permission, sid);
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
