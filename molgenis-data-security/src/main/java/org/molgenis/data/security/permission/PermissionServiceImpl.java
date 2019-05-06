package org.molgenis.data.security.permission;

import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;

import com.google.common.collect.Sets;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.molgenis.data.DataService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.security.EntityIdentity;
import org.molgenis.data.security.EntityIdentityUtils;
import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.data.security.PackageIdentity;
import org.molgenis.data.security.exception.AclClassAlreadyExistsException;
import org.molgenis.data.security.exception.DuplicatePermissionException;
import org.molgenis.data.security.exception.PermissionNotSuitableException;
import org.molgenis.data.security.exception.UnknownAceException;
import org.molgenis.data.security.permission.inheritance.PermissionInheritanceResolver;
import org.molgenis.data.security.permission.model.LabelledObject;
import org.molgenis.data.security.permission.model.LabelledPermission;
import org.molgenis.data.security.permission.model.LabelledType;
import org.molgenis.data.security.permission.model.Permission;
import org.molgenis.security.acl.MutableAclClassService;
import org.molgenis.security.acl.ObjectIdentityService;
import org.molgenis.security.core.PermissionSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.acls.model.AccessControlEntry;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.AlreadyExistsException;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Sid;
import org.springframework.transaction.annotation.Transactional;

public class PermissionServiceImpl implements PermissionService {

  private static final Logger LOG = LoggerFactory.getLogger(PermissionServiceImpl.class);
  public static final String PLUGIN = "plugin";

  private final MutableAclService mutableAclService;
  private final PermissionInheritanceResolver inheritanceResolver;
  private final ObjectIdentityService objectIdentityService;
  private final DataService dataService;
  private final MutableAclClassService mutableAclClassService;
  private final UserRoleTools userRoleTools;
  private final EntityHelper entityHelper;

  public PermissionServiceImpl(
      MutableAclService mutableAclService,
      PermissionInheritanceResolver inheritanceResolver,
      ObjectIdentityService objectIdentityService,
      DataService dataService,
      MutableAclClassService mutableAclClassService,
      UserRoleTools userRoleTools,
      EntityHelper entityHelper) {
    this.mutableAclService = requireNonNull(mutableAclService);
    this.inheritanceResolver = requireNonNull(inheritanceResolver);
    this.objectIdentityService = requireNonNull(objectIdentityService);
    this.dataService = requireNonNull(dataService);
    this.mutableAclClassService = requireNonNull(mutableAclClassService);
    this.userRoleTools = requireNonNull(userRoleTools);
    this.entityHelper = requireNonNull(entityHelper);
  }

  @Override
  public Set<LabelledType> getTypes() {
    Set<LabelledType> types = new HashSet<>();
    for (String typeId : mutableAclClassService.getAclClassTypes()) {
      String entityTypeId = entityHelper.getEntityTypeIdFromType(typeId);
      String label = entityHelper.getLabel(typeId);
      types.add(LabelledType.create(typeId, entityTypeId, label));
    }
    return types;
  }

  @Override
  public Set<LabelledObject> getObjects(String typeId, int page, int pageSize) {
    entityHelper.checkEntityTypeExists(typeId);
    return objectIdentityService
        .getObjectIdentities(typeId, pageSize, (page - 1) * pageSize)
        .stream()
        .map(this::getLabelledObject)
        .collect(toSet());
  }

  private LabelledObject getLabelledObject(ObjectIdentity objectIdentity) {
    String label =
        entityHelper.getLabel(objectIdentity.getType(), objectIdentity.getIdentifier().toString());
    return LabelledObject.create(objectIdentity.getIdentifier().toString(), label);
  }

  @Override
  public Set<PermissionSet> getSuitablePermissionsForType(String typeId) {
    entityHelper.checkEntityTypeExists(typeId);
    Set<PermissionSet> permissions;
    switch (typeId) {
      case EntityTypeIdentity.ENTITY_TYPE:
      case PackageIdentity.PACKAGE:
        permissions =
            Sets.newHashSet(
                PermissionSet.READMETA,
                PermissionSet.COUNT,
                PermissionSet.READ,
                PermissionSet.WRITE,
                PermissionSet.WRITEMETA);
        break;
      case PLUGIN:
        permissions = Sets.newHashSet(PermissionSet.READ);
        break;
      default: // RLS
        permissions = Sets.newHashSet(PermissionSet.READ, PermissionSet.WRITE);
        break;
    }
    return permissions;
  }

  @Override
  public Set<LabelledPermission> getPermissionsForObject(
      ObjectIdentity objectIdentity, Set<Sid> sids, boolean isReturnInheritedPermissions) {
    entityHelper.checkEntityExists(objectIdentity);
    Acl acl = mutableAclService.readAclById(objectIdentity);
    return getPermissionResponses(acl, isReturnInheritedPermissions, sids);
  }

  @Override
  public Set<LabelledPermission> getPermissions(Set<Sid> sids, boolean isReturnInherited) {
    Set<LabelledPermission> result = new LinkedHashSet<>();
    for (LabelledType type : getTypes()) {
      String entityTypeId = type.getEntityType();
      if (dataService.hasEntityType(entityTypeId)) {
        Set<Sid> sidsToQuery;
        if (isReturnInherited) {
          sidsToQuery = userRoleTools.getInheritedSids(sids);
        } else {
          sidsToQuery = sids;
        }
        Map<String, Set<LabelledPermission>> permissions =
            getPermissionsForType(type.getId(), sidsToQuery, isReturnInherited);
        if (!permissions.isEmpty()) {
          for (Set<LabelledPermission> labelledPermissions : permissions.values()) {
            result.addAll(labelledPermissions);
          }
        }
      }
    }
    return result;
  }

  @Override
  public Map<String, Set<LabelledPermission>> getPermissionsForType(
      String typeId, Set<Sid> sids, int page, int pageSize) {
    entityHelper.checkEntityTypeExists(typeId);

    List<ObjectIdentity> objectIdentities =
        objectIdentityService.getObjectIdentities(typeId, sids, pageSize, (page - 1) * pageSize);

    Map<ObjectIdentity, Acl> aclMap = new LinkedHashMap<>();
    if (!objectIdentities.isEmpty()) {
      aclMap = mutableAclService.readAclsById(objectIdentities, userRoleTools.sortSids(sids));
    }
    return getPermissions(aclMap, objectIdentities, sids, false);
  }

  @Override
  public Map<String, Set<LabelledPermission>> getPermissionsForType(
      String typeId, Set<Sid> sids, boolean isReturnInherited) {
    entityHelper.checkEntityTypeExists(typeId);
    List<ObjectIdentity> objectIdentities = getObjectIdentities(typeId, sids, isReturnInherited);
    Map<ObjectIdentity, Acl> aclMap = readAcls(sids, objectIdentities);

    return getPermissions(aclMap, objectIdentities, sids, isReturnInherited);
  }

  private List<ObjectIdentity> getObjectIdentities(
      String typeId, Set<Sid> sids, boolean isReturnInherited) {
    List<ObjectIdentity> objectIdentities;
    if (sids.isEmpty()) {
      objectIdentities = objectIdentityService.getObjectIdentities(typeId);
    } else {
      if (isReturnInherited) {
        objectIdentities =
            objectIdentityService.getObjectIdentities(typeId, userRoleTools.getInheritedSids(sids));
      } else {
        objectIdentities = objectIdentityService.getObjectIdentities(typeId, sids);
      }
    }
    return objectIdentities;
  }

  private Map<ObjectIdentity, Acl> readAcls(Set<Sid> sids, List<ObjectIdentity> objectIdentities) {
    Map<ObjectIdentity, Acl> aclMap = new LinkedHashMap<>();
    if (!objectIdentities.isEmpty()) {
      if (sids.isEmpty()) {
        aclMap = mutableAclService.readAclsById(objectIdentities);
      } else {
        aclMap = mutableAclService.readAclsById(objectIdentities, userRoleTools.sortSids(sids));
      }
    }
    return aclMap;
  }

  @Override
  @Transactional
  public void createAcl(ObjectIdentity objectIdentity) {
    entityHelper.checkEntityExists(objectIdentity);
    mutableAclService.createAcl(objectIdentity);
  }

  @Override
  @Transactional
  public void createPermission(Permission permission) {
    ObjectIdentity objectIdentity = permission.getObjectIdentity();
    entityHelper.checkEntityExists(objectIdentity);
    MutableAcl acl = (MutableAcl) mutableAclService.readAclById(objectIdentity);
    if (!getSuitablePermissionsForType(objectIdentity.getType())
        .contains(permission.getPermission())) {
      throw new PermissionNotSuitableException(
          permission.getPermission().name(), objectIdentity.getType());
    }
    Sid sid = permission.getSid();
    if (getPermissionResponses(acl, false, singleton(sid)).isEmpty()) {
      acl.insertAce(acl.getEntries().size(), permission.getPermission(), sid, true);
      mutableAclService.updateAcl(acl);
    } else {
      throw new DuplicatePermissionException(objectIdentity, sid);
    }
  }

  @Override
  @Transactional
  public void createPermissions(Set<Permission> permissions) {
    for (Permission permission : permissions) {
      createPermission(permission);
    }
  }

  @Override
  @Transactional
  public void updatePermission(Permission permission) {
    ObjectIdentity objectIdentity = permission.getObjectIdentity();
    entityHelper.checkEntityExists(objectIdentity);
    MutableAcl acl = (MutableAcl) mutableAclService.readAclById(objectIdentity);
    if (!getSuitablePermissionsForType(objectIdentity.getType())
        .contains(permission.getPermission())) {
      throw new PermissionNotSuitableException(
          permission.getPermission().name(), objectIdentity.getType());
    }
    Sid sid = permission.getSid();
    Set<LabelledPermission> current =
        getPermissionsForObject(objectIdentity, singleton(sid), false);
    if (current.isEmpty()) {
      throw new UnknownAceException(objectIdentity, sid, "update");
    }
    deleteAce(sid, acl);
    acl.insertAce(acl.getEntries().size(), permission.getPermission(), sid, true);
    mutableAclService.updateAcl(acl);
  }

  @Override
  @Transactional
  public void updatePermissions(Set<Permission> permissions) {
    for (Permission permission : permissions) {
      entityHelper.checkEntityExists(permission.getObjectIdentity());
      updatePermission(permission);
    }
  }

  @Override
  @Transactional
  public void deletePermission(Sid sid, ObjectIdentity objectIdentity) {
    entityHelper.checkEntityExists(objectIdentity);
    MutableAcl acl = (MutableAcl) mutableAclService.readAclById(objectIdentity, singletonList(sid));
    if (acl == null) {
      throw new UnknownAceException(objectIdentity, sid, "delete");
    }
    deleteAce(sid, acl);
  }

  @Override
  @Transactional
  public void addType(String typeId) {
    entityHelper.checkEntityTypeExists(typeId);
    EntityType entityType = dataService.getEntityType(entityHelper.getEntityTypeIdFromType(typeId));
    if (!mutableAclClassService.getAclClassTypes().contains(typeId)) {
      mutableAclClassService.createAclClass(typeId, EntityIdentityUtils.toIdType(entityType));
      // Create ACL's for existing rows
      dataService
          .findAll(entityType.getId())
          .forEach(
              entity -> {
                try {
                  mutableAclService.createAcl(new EntityIdentity(entity));
                } catch (AlreadyExistsException e) {
                  LOG.warn(
                      "Acl for entity '{}' of type '{}' already exists",
                      entity.getIdValue(),
                      entityType.getIdValue());
                }
              });
    } else {
      throw new AclClassAlreadyExistsException(typeId);
    }
  }

  @Override
  @Transactional
  public void deleteType(String typeId) {
    entityHelper.checkEntityTypeExists(typeId);
    mutableAclClassService.deleteAclClass(typeId);
  }

  @Override
  public boolean exists(ObjectIdentity objectIdentity, Sid sid) {
    MutableAcl acl = (MutableAcl) mutableAclService.readAclById(objectIdentity, singletonList(sid));
    return acl.getEntries().stream().anyMatch(ace -> ace.getSid().equals(sid));
  }

  private void deleteAce(Sid sid, MutableAcl acl) {
    int nrEntries = acl.getEntries().size();
    for (int i = nrEntries - 1; i >= 0; i--) {
      AccessControlEntry accessControlEntry = acl.getEntries().get(i);
      if (accessControlEntry.getSid().equals(sid)) {
        acl.deleteAce(i);
        mutableAclService.updateAcl(acl);
      }
    }
  }

  private Map<String, Set<LabelledPermission>> getPermissions(
      Map<ObjectIdentity, Acl> acls,
      List<ObjectIdentity> objectIdentities,
      Set<Sid> sids,
      boolean isReturnInheritedPermissions) {
    Map<String, Set<LabelledPermission>> result = new LinkedHashMap<>();
    objectIdentities.forEach(
        objectIdentity ->
            result.put(
                objectIdentity.getIdentifier().toString(),
                getPermissionResponses(
                    acls.get(objectIdentity), isReturnInheritedPermissions, sids)));
    return result;
  }

  private Set<LabelledPermission> getPermissionResponses(
      Acl acl, boolean isReturnInheritedPermissions, Set<Sid> sids) {
    Set<LabelledPermission> result = new LinkedHashSet<>();
    if (sids.isEmpty()) {
      sids = userRoleTools.getAllAvailableSids();
    }
    for (Sid sid : userRoleTools.sortSids(sids)) {
      getPermissionResponsesForSingleSid(acl, isReturnInheritedPermissions, result, sid);
    }
    return result;
  }

  private void getPermissionResponsesForSingleSid(
      Acl acl, boolean isReturnInheritedPermissions, Set<LabelledPermission> result, Sid sid) {
    PermissionSet ownPermission = null;
    for (AccessControlEntry ace : acl.getEntries()) {
      if (sid.equals(ace.getSid())) {
        ownPermission = PermissionSetUtils.getPermissionSet(ace);
      }
    }
    Set<LabelledPermission> inheritedPermissions = new LinkedHashSet<>();
    if (isReturnInheritedPermissions) {
      inheritedPermissions.addAll(inheritanceResolver.getInheritedPermissions(acl, sid));
    }
    if (ownPermission != null || !inheritedPermissions.isEmpty()) {
      inheritedPermissions = inheritedPermissions.isEmpty() ? null : inheritedPermissions;
      result.add(
          LabelledPermission.create(
              sid,
              entityHelper.getLabelledObjectIdentity(acl.getObjectIdentity()),
              ownPermission,
              inheritedPermissions));
    }
  }
}
