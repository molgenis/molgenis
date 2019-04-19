package org.molgenis.data.security.permission;

import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;
import static org.molgenis.data.security.permission.PermissionSetUtils.COUNT;
import static org.molgenis.data.security.permission.PermissionSetUtils.READ;
import static org.molgenis.data.security.permission.PermissionSetUtils.READMETA;
import static org.molgenis.data.security.permission.PermissionSetUtils.WRITE;
import static org.molgenis.data.security.permission.PermissionSetUtils.WRITEMETA;
import static org.molgenis.data.security.permission.PermissionSetUtils.paramValueToPermissionSet;

import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import org.molgenis.data.security.permission.inheritance.model.InheritedPermissionsResult;
import org.molgenis.data.security.permission.model.LabelledObjectIdentity;
import org.molgenis.data.security.permission.model.LabelledObjectPermission;
import org.molgenis.data.security.permission.model.LabelledPermission;
import org.molgenis.data.security.permission.model.ObjectPermissions;
import org.molgenis.data.security.permission.model.Permission;
import org.molgenis.data.security.permission.model.Type;
import org.molgenis.data.security.permission.model.TypePermission;
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
  public Set<Type> getTypes() {
    Set types = new HashSet();
    for (String typeId : mutableAclClassService.getAclClassTypes()) {
      String entityTypeId = entityHelper.getEntityTypeIdFromType(typeId);
      String label = entityHelper.getLabel(typeId);
      types.add(Type.create(typeId, entityTypeId, label));
    }
    return types;
  }

  @Override
  public Set<String> getAcls(String typeId, int page, int pageSize) {
    entityHelper.checkEntityTypeExists(typeId);
    return objectIdentityService
        .getObjectIdentities(typeId, pageSize, (page - 1) * pageSize)
        .stream()
        .map(objectIdentity -> objectIdentity.getIdentifier().toString())
        .collect(toSet());
  }

  @Override
  public Set<String> getSuitablePermissionsForType(String typeId) {
    entityHelper.checkEntityTypeExists(typeId);
    Set<String> permissions;
    switch (typeId) {
      case EntityTypeIdentity.ENTITY_TYPE:
      case PackageIdentity.PACKAGE:
        permissions = Sets.newHashSet(READMETA, COUNT, READ, WRITE, WRITEMETA);
        break;
      case PLUGIN:
        permissions = Sets.newHashSet(READ);
        break;
      default: // RLS
        permissions = Sets.newHashSet(READ, WRITE);
        break;
    }
    return permissions;
  }

  @Override
  public LabelledObjectPermission getPermission(
      ObjectIdentity objectIdentity, Set<Sid> sids, boolean isReturnInheritedPermissions) {
    entityHelper.checkEntityExists(objectIdentity);
    Acl acl = mutableAclService.readAclById(objectIdentity);
    return LabelledObjectPermission.create(
        entityHelper.getLabelledObjectIdentity(objectIdentity),
        getPermissionForObjectIdentity(acl, sids, isReturnInheritedPermissions));
  }

  @Override
  public Set<LabelledPermission> getAllPermissions(Set<Sid> sids, boolean isReturnInherited) {
    Set<LabelledPermission> result = new LinkedHashSet<>();
    for (Type type : getTypes()) {
      String entityTypeId = type.getEntityType();
      if (dataService.hasEntityType(entityTypeId)) {
        Set<Sid> sidsToQuery;
        if (isReturnInherited) {
          sidsToQuery = userRoleTools.getInheritedSids(sids);
        } else {
          sidsToQuery = sids;
        }
        Set<LabelledObjectPermission> labelledObjectPermissions =
            getPermissionsForType(type.getId(), sidsToQuery, isReturnInherited)
                .getObjectPermissions();
        if (!labelledObjectPermissions.isEmpty()) {
          for (LabelledObjectPermission labelledPermission : labelledObjectPermissions) {
            LabelledObjectIdentity labelledObjectIdentity =
                labelledPermission.getLabelledObjectIdentity();
            for (Permission permission : labelledPermission.getPermissions()) {
              result.add(
                  LabelledPermission.create(
                      permission.getSid(),
                      labelledObjectIdentity,
                      permission.getPermission(),
                      permission.getInheritedPermissions()));
            }
          }
        }
      }
    }
    return result;
  }

  @Override
  public TypePermission getPagedPermissionsForType(
      String typeId, Set<Sid> sids, int page, int pageSize) {
    entityHelper.checkEntityTypeExists(typeId);

    List<ObjectIdentity> objectIdentities =
        objectIdentityService.getObjectIdentities(typeId, sids, pageSize, (page - 1) * pageSize);

    Map<ObjectIdentity, Acl> aclMap = new HashMap<>();
    if (!objectIdentities.isEmpty()) {
      aclMap = mutableAclService.readAclsById(objectIdentities, userRoleTools.sortSids(sids));
    }
    return TypePermission.create(
        typeId,
        entityHelper.getLabel(typeId),
        getPermissions(aclMap, objectIdentities, sids, false));
  }

  @Override
  public TypePermission getPermissionsForType(
      String typeId, Set<Sid> sids, boolean isReturnInherited) {
    entityHelper.checkEntityTypeExists(typeId);
    List<ObjectIdentity> objectIdentities;
    if (sids.isEmpty()) {
      objectIdentities = objectIdentityService.getObjectIdentities(typeId);
    } else {
      objectIdentities =
          objectIdentityService.getObjectIdentities(typeId, userRoleTools.getInheritedSids(sids));
    }
    Map<ObjectIdentity, Acl> aclMap = new HashMap<>();
    if (!objectIdentities.isEmpty()) {
      if (sids.isEmpty()) {
        aclMap = mutableAclService.readAclsById(objectIdentities);
      } else {
        aclMap = mutableAclService.readAclsById(objectIdentities, userRoleTools.sortSids(sids));
      }
    }
    Set<LabelledObjectPermission> permissions =
        getPermissions(aclMap, objectIdentities, sids, isReturnInherited);
    return TypePermission.create(typeId, entityHelper.getLabel(typeId), permissions);
  }

  @Override
  @Transactional
  public void createAcl(ObjectIdentity objectIdentity) {
    entityHelper.checkEntityExists(objectIdentity);
    mutableAclService.createAcl(objectIdentity);
  }

  @Override
  @Transactional
  public void createPermission(ObjectPermissions objectPermissions) {
    ObjectIdentity objectIdentity = objectPermissions.getObjectIdentity();
    entityHelper.checkEntityExists(objectIdentity);
    for (Permission permission : objectPermissions.getPermissions()) {
      MutableAcl acl = (MutableAcl) mutableAclService.readAclById(objectIdentity);
      if (!getSuitablePermissionsForType(objectIdentity.getType())
          .contains(permission.getPermission())) {
        throw new PermissionNotSuitableException(
            permission.getPermission(), objectIdentity.getType());
      }
      Sid sid = permission.getSid();
      if (getPermissionResponses(acl, false, singleton(sid)).isEmpty()) {
        acl.insertAce(
            acl.getEntries().size(),
            paramValueToPermissionSet(permission.getPermission()),
            sid,
            true);
        mutableAclService.updateAcl(acl);
      } else {
        throw new DuplicatePermissionException(objectIdentity, sid);
      }
    }
  }

  @Override
  @Transactional
  public void createPermissions(Set<ObjectPermissions> permissions) {
    for (ObjectPermissions objectPermissions : permissions) {
      createPermission(objectPermissions);
    }
  }

  @Override
  @Transactional
  public void updatePermission(ObjectPermissions objectPermissions) {
    ObjectIdentity objectIdentity = objectPermissions.getObjectIdentity();
    for (Permission permission : objectPermissions.getPermissions()) {
      entityHelper.checkEntityExists(objectIdentity);
      MutableAcl acl = (MutableAcl) mutableAclService.readAclById(objectIdentity);
      if (!getSuitablePermissionsForType(objectIdentity.getType())
          .contains(permission.getPermission())) {
        throw new PermissionNotSuitableException(
            permission.getPermission(), objectIdentity.getType());
      }
      Sid sid = permission.getSid();
      LabelledObjectPermission current =
          getPermission(objectIdentity, Collections.singleton(sid), false);
      if (current.getPermissions().isEmpty()) {
        throw new UnknownAceException(objectIdentity, sid, "update");
      }
      deleteAce(sid, acl);
      acl.insertAce(
          acl.getEntries().size(),
          paramValueToPermissionSet(permission.getPermission()),
          sid,
          true);
      mutableAclService.updateAcl(acl);
    }
  }

  @Override
  @Transactional
  public void updatePermissions(Set<ObjectPermissions> permissions) {
    for (ObjectPermissions objectPermissions : permissions) {
      entityHelper.checkEntityExists(objectPermissions.getObjectIdentity());
      updatePermission(objectPermissions);
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

  @Override
  public void grant(Map<ObjectIdentity, PermissionSet> objectIdentityPermissionMap, Sid sid) {
    for (Entry<ObjectIdentity, PermissionSet> entry : objectIdentityPermissionMap.entrySet()) {
      createPermission(
          ObjectPermissions.create(
              entry.getKey(),
              Collections.singleton(Permission.create(sid, entry.getValue().name(), null))));
    }
  }

  @Override
  public void grant(ObjectIdentity objectIdentity, PermissionSet permission, Sid sid) {
    createPermission(
        ObjectPermissions.create(
            objectIdentity,
            Collections.singleton(Permission.create(sid, permission.name(), null))));
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

  private Set<LabelledObjectPermission> getPermissions(
      Map<ObjectIdentity, Acl> acls,
      List<ObjectIdentity> objectIdentities,
      Set<Sid> sids,
      boolean isReturnInheritedPermissions) {
    Set<LabelledObjectPermission> result = new LinkedHashSet<>();
    objectIdentities.forEach(
        objectIdentity ->
            result.add(
                LabelledObjectPermission.create(
                    entityHelper.getLabelledObjectIdentity(objectIdentity),
                    getPermissionForObjectIdentity(
                        acls.get(objectIdentity), sids, isReturnInheritedPermissions))));
    return result
        .stream()
        .filter(labelledObjectPermission -> !labelledObjectPermission.getPermissions().isEmpty())
        .collect(toSet());
  }

  private Set<Permission> getPermissionForObjectIdentity(
      Acl acl, Set<Sid> sids, boolean isReturnInheritedPermissions) {
    Set<Permission> result = new LinkedHashSet<>();
    Map<String, Set<Permission>> resultMap = new HashMap<>();
    getPermissionsOnAceForSids(sids, acl, resultMap, isReturnInheritedPermissions);
    Collection<Set<Permission>> identityPermissionResponses = resultMap.values();
    for (Set<Permission> permissions : identityPermissionResponses) {
      if (!permissions.isEmpty()) {
        result.addAll(permissions);
      }
    }
    return result;
  }

  private void getPermissionsOnAceForSids(
      Set<Sid> sids,
      Acl acl,
      Map<String, Set<Permission>> result,
      boolean isReturnInheritedPermissions) {
    ObjectIdentity objectIdentity = acl.getObjectIdentity();
    Set<Permission> permissions = getPermissionResponses(acl, isReturnInheritedPermissions, sids);

    result.put(objectIdentity.getIdentifier().toString(), permissions);
  }

  private Set<Permission> getPermissionResponses(
      Acl acl, boolean isReturnInheritedPermissions, Set<Sid> sids) {
    Set<Permission> result = new LinkedHashSet<>();

    if (!sids.isEmpty()) {
      for (Sid sid : sids) {
        getPermissionResponsesForSingleSid(acl, isReturnInheritedPermissions, result, sid);
      }
    } else {
      for (Sid sid : userRoleTools.getAllAvailableSids()) {
        getPermissionResponsesForSingleSid(acl, isReturnInheritedPermissions, result, sid);
      }
    }
    return result;
  }

  private void getPermissionResponsesForSingleSid(
      Acl acl, boolean isReturnInheritedPermissions, Set<Permission> result, Sid sid) {
    String ownPermission = null;
    for (AccessControlEntry ace : acl.getEntries()) {
      if (sid.equals(ace.getSid())) {
        ownPermission = PermissionSetUtils.getPermissionStringValue(ace);
      }
    }
    List<LabelledPermission> labelledPermissions = new LinkedList<>();
    if (isReturnInheritedPermissions) {
      labelledPermissions.addAll(getInheritedPermissions(acl, sid));
    }
    if (ownPermission != null || !labelledPermissions.isEmpty()) {
      LinkedHashSet<LabelledPermission> deduplicatedInheritedPermissions =
          new LinkedHashSet<>(labelledPermissions);
      if (deduplicatedInheritedPermissions.isEmpty()) {
        deduplicatedInheritedPermissions = null;
      }
      result.add(Permission.create(sid, ownPermission, deduplicatedInheritedPermissions));
    }
  }

  private Set<LabelledPermission> getInheritedPermissions(Acl acl, Sid sid) {
    InheritedPermissionsResult inheritedPermissionsResult =
        inheritanceResolver.getInheritedPermissions(acl, sid);
    if (inheritanceResolver.isNotEmpty(inheritedPermissionsResult)) {
      return inheritanceResolver.convertToInheritedPermissions(inheritedPermissionsResult);
    }
    return Collections.emptySet();
  }
}
