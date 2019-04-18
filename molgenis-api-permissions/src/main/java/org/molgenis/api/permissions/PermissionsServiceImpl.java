package org.molgenis.api.permissions;

import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.Comparator.comparing;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.api.permissions.PermissionSetUtils.COUNT;
import static org.molgenis.api.permissions.PermissionSetUtils.READ;
import static org.molgenis.api.permissions.PermissionSetUtils.READMETA;
import static org.molgenis.api.permissions.PermissionSetUtils.WRITE;
import static org.molgenis.api.permissions.PermissionSetUtils.WRITEMETA;
import static org.molgenis.api.permissions.PermissionSetUtils.paramValueToPermissionSet;
import static org.molgenis.api.permissions.UserRoleTools.getRole;
import static org.molgenis.api.permissions.UserRoleTools.getUser;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.constraints.NotEmpty;
import org.molgenis.api.permissions.exceptions.AclClassAlreadyExistsException;
import org.molgenis.api.permissions.exceptions.DuplicatePermissionException;
import org.molgenis.api.permissions.exceptions.InvalidTypeIdException;
import org.molgenis.api.permissions.exceptions.PermissionNotSuitableException;
import org.molgenis.api.permissions.exceptions.UnknownAceException;
import org.molgenis.api.permissions.inheritance.PermissionInheritanceResolver;
import org.molgenis.api.permissions.inheritance.model.InheritedPermissionsResult;
import org.molgenis.api.permissions.model.request.ObjectPermissionsRequest;
import org.molgenis.api.permissions.model.request.PermissionRequest;
import org.molgenis.api.permissions.model.response.InheritedPermission;
import org.molgenis.api.permissions.model.response.ObjectPermission;
import org.molgenis.api.permissions.model.response.ObjectPermissionsResponse;
import org.molgenis.api.permissions.model.response.TypePermissionsResponse;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.plugin.model.PluginIdentity;
import org.molgenis.data.security.EntityIdentity;
import org.molgenis.data.security.EntityIdentityUtils;
import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.data.security.PackageIdentity;
import org.molgenis.security.acl.AclClassService;
import org.molgenis.security.acl.MutableAclClassService;
import org.molgenis.security.acl.ObjectIdentityService;
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

public class PermissionsServiceImpl implements PermissionsService {

  private static final Logger LOG = LoggerFactory.getLogger(PermissionsServiceImpl.class);

  private final MutableAclService mutableAclService;
  private final AclClassService aclClassService;
  private final PermissionInheritanceResolver inheritanceResolver;
  private final ObjectIdentityService objectIdentityService;
  private final DataService dataService;
  private final MutableAclClassService mutableAclClassService;
  private final UserRoleTools userRoleTools;
  private final IdentityTools identityTools;

  PermissionsServiceImpl(
      MutableAclService mutableAclService,
      AclClassService aclClassService,
      PermissionInheritanceResolver inheritanceResolver,
      ObjectIdentityService objectIdentityService,
      DataService dataService,
      MutableAclClassService mutableAclClassService,
      UserRoleTools userRoleTools,
      IdentityTools identityTools) {
    this.mutableAclService = requireNonNull(mutableAclService);
    this.aclClassService = requireNonNull(aclClassService);
    this.inheritanceResolver = requireNonNull(inheritanceResolver);
    this.objectIdentityService = requireNonNull(objectIdentityService);
    this.dataService = requireNonNull(dataService);
    this.mutableAclClassService = requireNonNull(mutableAclClassService);
    this.userRoleTools = requireNonNull(userRoleTools);
    this.identityTools = requireNonNull(identityTools);
  }

  @Override
  public List<String> getTypes() {
    return new ArrayList(aclClassService.getAclClassTypes());
  }

  @Override
  public List<String> getAcls(String typeId, int page, int pageSize) {
    checkEntityTypeExists(typeId);
    return objectIdentityService
        .getObjectIdentities(typeId, pageSize, (page - 1) * pageSize)
        .stream()
        .map(objectIdentity -> objectIdentity.getIdentifier().toString())
        .collect(toList());
  }

  @Override
  public Set<String> getSuitablePermissionsForType(String typeId) {
    checkEntityTypeExists(typeId);
    Set<String> permissions;
    switch (typeId) {
      case EntityTypeIdentity.ENTITY_TYPE:
      case PackageIdentity.PACKAGE:
        permissions = Sets.newHashSet(READMETA, COUNT, READ, WRITE, WRITEMETA);
        break;
      case PluginIdentity.PLUGIN:
        permissions = Sets.newHashSet(READ);
        break;
      default: // RLS
        permissions = Sets.newHashSet(READ, WRITE);
        break;
    }
    return permissions;
  }

  @Override
  public List<ObjectPermission> getPermission(
      ObjectIdentity objectIdentity, Set<Sid> sids, boolean isReturnInheritedPermissions) {
    checkEntityExists(objectIdentity);
    List<ObjectIdentity> objectIdentities = Collections.singletonList(objectIdentity);
    Map<ObjectIdentity, Acl> aclMap;
    if (!sids.isEmpty()) {
      aclMap = mutableAclService.readAclsById(objectIdentities, userRoleTools.sortSids(sids));
    } else {
      aclMap = mutableAclService.readAclsById(objectIdentities);
    }
    List<ObjectPermissionsResponse> permissions =
        getPermissions(aclMap, objectIdentities, sids, isReturnInheritedPermissions);
    List<ObjectPermission> result;
    if (permissions.size() == 1) {
      result = permissions.get(0).getPermissions();
      result.sort(comparing(a -> getRoleOrUser(a.getUser(), a.getRole())));
    } else if (permissions.isEmpty()) {
      result = emptyList();
    } else {
      throw new IllegalStateException(
          "Multiple results originating from a single object identity should not be possible");
    }
    return result;
  }

  private String getRoleOrUser(String user, String role) {
    return Strings.isNullOrEmpty(user) ? role : user;
  }

  @Override
  public List<TypePermissionsResponse> getAllPermissions(Set<Sid> sids, boolean isReturnInherited) {
    List<TypePermissionsResponse> result = new LinkedList<>();
    for (String typeId : getTypes()) {
      String entityTypeId = identityTools.getEntityTypeIdFromType(typeId);
      if (dataService.hasEntityType(entityTypeId)) {
        Set<Sid> sidsToQuery;
        if (isReturnInherited) {
          sidsToQuery = userRoleTools.getInheritedSids(sids);
        } else {
          sidsToQuery = sids;
        }
        LinkedList<ObjectPermissionsResponse> permissions =
            Lists.newLinkedList(getPermissionsForType(typeId, sidsToQuery, isReturnInherited));
        if (!permissions.isEmpty()) {
          result.add(TypePermissionsResponse.create(typeId, getLabel(entityTypeId), permissions));
        }
      }
    }
    return result;
  }

  @Override
  public Collection<ObjectPermissionsResponse> getPagedPermissionsForType(
      String typeId, Set<Sid> sids, int page, int pageSize) {
    checkEntityTypeExists(typeId);

    List<ObjectIdentity> objectIdentities =
        objectIdentityService.getObjectIdentities(typeId, sids, pageSize, (page - 1) * pageSize);

    Map<ObjectIdentity, Acl> aclMap = new HashMap<>();
    if (!objectIdentities.isEmpty()) {
      aclMap = mutableAclService.readAclsById(objectIdentities, userRoleTools.sortSids(sids));
    }
    return getPermissions(aclMap, objectIdentities, sids, false);
  }

  @Override
  public Collection<ObjectPermissionsResponse> getPermissionsForType(
      String typeId, Set<Sid> sids, boolean isReturnInherited) {
    checkEntityTypeExists(typeId);
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
    return getPermissions(aclMap, objectIdentities, sids, isReturnInherited);
  }

  @Override
  @Transactional
  public void createAcl(ObjectIdentity objectIdentity) {
    checkEntityExists(objectIdentity);
    mutableAclService.createAcl(objectIdentity);
  }

  @Override
  @Transactional
  public void createPermission(
      List<PermissionRequest> permissionRequests, ObjectIdentity objectIdentity) {
    checkEntityExists(objectIdentity);
    MutableAcl acl = (MutableAcl) mutableAclService.readAclById(objectIdentity);
    for (PermissionRequest permissionRequest : permissionRequests) {
      if (!getSuitablePermissionsForType(objectIdentity.getType())
          .contains(permissionRequest.getPermission())) {
        throw new PermissionNotSuitableException(
            permissionRequest.getPermission(), objectIdentity.getType());
      }
      Sid sid = userRoleTools.getSid(permissionRequest.getUser(), permissionRequest.getRole());
      if (getPermissionResponses(acl, false, singleton(sid)).isEmpty()) {
        acl.insertAce(
            acl.getEntries().size(),
            paramValueToPermissionSet(permissionRequest.getPermission()),
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
  public void createPermissions(
      List<ObjectPermissionsRequest> objectPermissionsRequests, String typeId) {
    for (ObjectPermissionsRequest request : objectPermissionsRequests) {
      createPermission(
          request.getPermissions(), identityTools.getObjectIdentity(typeId, request.getObjectId()));
    }
  }

  @Override
  @Transactional
  public void updatePermission(
      List<PermissionRequest> permissionRequests, ObjectIdentity objectIdentity) {
    checkEntityExists(objectIdentity);
    MutableAcl acl = (MutableAcl) mutableAclService.readAclById(objectIdentity);

    for (PermissionRequest permissionRequest : permissionRequests) {
      if (!getSuitablePermissionsForType(objectIdentity.getType())
          .contains(permissionRequest.getPermission())) {
        throw new PermissionNotSuitableException(
            permissionRequest.getPermission(), objectIdentity.getType());
      }
      Sid sid = userRoleTools.getSid(permissionRequest.getUser(), permissionRequest.getRole());
      List<ObjectPermission> current =
          getPermission(objectIdentity, Collections.singleton(sid), false);
      if (current.isEmpty()) {
        throw new UnknownAceException(objectIdentity, sid, "update");
      }
      deleteAce(sid, acl);
      acl.insertAce(
          acl.getEntries().size(),
          paramValueToPermissionSet(permissionRequest.getPermission()),
          sid,
          true);
      mutableAclService.updateAcl(acl);
    }
  }

  @Override
  @Transactional
  public void updatePermissions(
      @NotEmpty List<ObjectPermissionsRequest> objectPermissionsRequests, String typeId) {
    checkEntityTypeExists(typeId);
    for (ObjectPermissionsRequest permissions : objectPermissionsRequests) {
      updatePermission(
          permissions.getPermissions(),
          identityTools.getObjectIdentity(typeId, permissions.getObjectId()));
    }
  }

  @Override
  @Transactional
  public void deletePermission(Sid sid, ObjectIdentity objectIdentity) {
    checkEntityExists(objectIdentity);
    MutableAcl acl = (MutableAcl) mutableAclService.readAclById(objectIdentity, singletonList(sid));
    if (acl == null) {
      throw new UnknownAceException(objectIdentity, sid, "delete");
    }
    deleteAce(sid, acl);
  }

  @Override
  @Transactional
  public void addType(String typeId) {
    checkEntityTypeExists(typeId);
    EntityType entityType =
        dataService.getEntityType(identityTools.getEntityTypeIdFromType(typeId));
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
    checkEntityTypeExists(typeId);
    mutableAclClassService.deleteAclClass(typeId);
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

  private List<ObjectPermissionsResponse> getPermissions(
      Map<ObjectIdentity, Acl> acls,
      List<ObjectIdentity> objectIdentities,
      Set<Sid> sids,
      boolean isReturnInheritedPermissions) {
    List<ObjectPermissionsResponse> result = new LinkedList<>();
    objectIdentities.forEach(
        objectIdentity ->
            getPermissionForObjectIdentity(
                acls, sids, result, objectIdentity, isReturnInheritedPermissions));
    return result;
  }

  private void getPermissionForObjectIdentity(
      Map<ObjectIdentity, Acl> acls,
      Set<Sid> sids,
      List<ObjectPermissionsResponse> result,
      ObjectIdentity objectIdentity,
      boolean isReturnInheritedPermissions) {
    Acl acl = acls.get(objectIdentity);

    Map<String, ObjectPermissionsResponse> resultMap = new HashMap<>();
    getPermissionsOnAceForSids(sids, acl, resultMap, isReturnInheritedPermissions);
    Collection<ObjectPermissionsResponse> identityPermissionResponses = resultMap.values();
    for (ObjectPermissionsResponse identityPermission : identityPermissionResponses) {
      if (!identityPermission.getPermissions().isEmpty()) {
        result.add(identityPermission);
      }
    }
  }

  private void getPermissionsOnAceForSids(
      Set<Sid> sids,
      Acl acl,
      Map<String, ObjectPermissionsResponse> result,
      boolean isReturnInheritedPermissions) {
    String identifier = acl.getObjectIdentity().getIdentifier().toString();
    List<ObjectPermission> objectPermissionRespons =
        getPermissionResponses(acl, isReturnInheritedPermissions, sids);

    result.put(
        identifier,
        ObjectPermissionsResponse.create(
            identifier,
            getLabel(
                identityTools.getEntityTypeIdFromType(acl.getObjectIdentity().getType()),
                identifier),
            objectPermissionRespons));
  }

  private List<ObjectPermission> getPermissionResponses(
      Acl acl, boolean isReturnInheritedPermissions, Set<Sid> sids) {
    List<ObjectPermission> result = new LinkedList<>();

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
      Acl acl, boolean isReturnInheritedPermissions, List<ObjectPermission> result, Sid sid) {
    String ownPermission = null;
    for (AccessControlEntry ace : acl.getEntries()) {
      if (sid.equals(ace.getSid())) {
        ownPermission = PermissionSetUtils.getPermissionStringValue(ace);
      }
    }
    List<InheritedPermission> inheritedPermissions = new LinkedList<>();
    if (isReturnInheritedPermissions) {
      inheritedPermissions.addAll(getInheritedPermissions(acl, sid));
    }
    if (ownPermission != null || !inheritedPermissions.isEmpty()) {
      LinkedHashSet<InheritedPermission> deduplicatedInheritedPermissions =
          new LinkedHashSet<>(inheritedPermissions);
      if (deduplicatedInheritedPermissions.isEmpty()) {
        deduplicatedInheritedPermissions = null;
      }
      result.add(
          ObjectPermission.create(
              getRole(sid), getUser(sid), ownPermission, deduplicatedInheritedPermissions));
    }
  }

  private List<InheritedPermission> getInheritedPermissions(Acl acl, Sid sid) {
    InheritedPermissionsResult inheritedPermissionsResult =
        inheritanceResolver.getInheritedPermissions(acl, sid);
    if (inheritanceResolver.isNotEmpty(inheritedPermissionsResult)) {
      return inheritanceResolver.convertToInheritedPermissions(inheritedPermissionsResult);
    }
    return Collections.emptyList();
  }

  private String getLabel(String entityTypeId, String identifier) {
    Entity entity = dataService.getRepository(entityTypeId).findOneById(identifier);
    if (entity == null) {
      throw new UnknownEntityException(entityTypeId, identifier);
    }
    return entity.getLabelValue().toString();
  }

  private String getLabel(String entityTypeId) {
    return dataService.getRepository(entityTypeId).getEntityType().getLabel();
  }

  private void checkEntityExists(ObjectIdentity objectIdentity) {
    checkEntityTypeExists(objectIdentity.getType());
    String entityTypeId = identityTools.getEntityTypeIdFromType(objectIdentity.getType());
    if (dataService.findOneById(entityTypeId, objectIdentity.getIdentifier()) == null) {
      throw new UnknownEntityException(entityTypeId, objectIdentity.getIdentifier());
    }
  }

  private void checkEntityTypeExists(String typeId) {
    String entityTypeId = identityTools.getEntityTypeIdFromType(typeId);
    if (!dataService.hasEntityType(entityTypeId)) {
      throw new InvalidTypeIdException(typeId);
    }
  }
}
