package org.molgenis.api.permissions;

import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
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
import static org.molgenis.security.core.SidUtils.createSecurityContextSid;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.constraints.NotEmpty;
import org.apache.commons.lang3.StringUtils;
import org.molgenis.api.permissions.exceptions.AclClassAlreadyExistsException;
import org.molgenis.api.permissions.exceptions.AclNotFoundException;
import org.molgenis.api.permissions.exceptions.DuplicatePermissionException;
import org.molgenis.api.permissions.exceptions.InsufficientPermissionDeniedException;
import org.molgenis.api.permissions.exceptions.InvalidTypeIdException;
import org.molgenis.api.permissions.exceptions.PermissionNotSuitableException;
import org.molgenis.api.permissions.exceptions.ReadPermissionDeniedException;
import org.molgenis.api.permissions.exceptions.SidPermissionException;
import org.molgenis.api.permissions.exceptions.UnknownAceException;
import org.molgenis.api.permissions.inheritance.PermissionInheritanceResolver;
import org.molgenis.api.permissions.inheritance.model.InheritedPermissionsResult;
import org.molgenis.api.permissions.model.request.ObjectPermissionsRequest;
import org.molgenis.api.permissions.model.request.PermissionRequest;
import org.molgenis.api.permissions.model.response.InheritedPermission;
import org.molgenis.api.permissions.model.response.ObjectPermissionsResponse;
import org.molgenis.api.permissions.model.response.PermissionResponse;
import org.molgenis.api.permissions.model.response.TypePermissionsResponse;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.data.meta.model.PackageMetadata;
import org.molgenis.data.plugin.model.PluginIdentity;
import org.molgenis.data.plugin.model.PluginMetadata;
import org.molgenis.data.security.EntityIdentity;
import org.molgenis.data.security.EntityIdentityUtils;
import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.data.security.GroupIdentity;
import org.molgenis.data.security.PackageIdentity;
import org.molgenis.data.security.auth.GroupMetadata;
import org.molgenis.data.util.EntityUtils;
import org.molgenis.security.acl.AclClassService;
import org.molgenis.security.acl.MutableAclClassService;
import org.molgenis.security.acl.ObjectIdentityService;
import org.molgenis.security.core.utils.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.model.AccessControlEntry;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.AlreadyExistsException;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Sid;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PermissionsApiService {
  private static final Logger LOG = LoggerFactory.getLogger(PermissionsApiService.class);

  public static final String ENTITY_PREFIX = "entity-";
  private final MutableAclService mutableAclService;
  private final AclClassService aclClassService;
  private final PermissionInheritanceResolver inheritanceResolver;
  private final ObjectIdentityService objectIdentityService;
  private final DataService dataService;
  private final MutableAclClassService mutableAclClassService;
  private final UserRoleTools userRoleTools;

  public PermissionsApiService(
      MutableAclService mutableAclService,
      AclClassService aclClassService,
      PermissionInheritanceResolver inheritanceResolver,
      ObjectIdentityService objectIdentityService,
      DataService dataService,
      MutableAclClassService mutableAclClassService,
      UserRoleTools userRoleTools) {
    this.mutableAclService = requireNonNull(mutableAclService);
    this.aclClassService = requireNonNull(aclClassService);
    this.inheritanceResolver = requireNonNull(inheritanceResolver);
    this.objectIdentityService = requireNonNull(objectIdentityService);
    this.dataService = requireNonNull(dataService);
    this.mutableAclClassService = requireNonNull(mutableAclClassService);
    this.userRoleTools = requireNonNull(userRoleTools);
  }

  public List<String> getClasses() {
    return aclClassService
        .getAclClassTypes()
        .stream()
        .filter(type -> dataService.hasEntityType(getEntityTypeIdFromClass(type)))
        .collect(toList());
  }

  public List<String> getAcls(String typeId, int page, int pageSize) {
    checkEntityTypeExists(typeId);
    checkReadPermission(typeId, Collections.emptySet());
    return objectIdentityService
        .getObjectIdentities(typeId, pageSize, (page - 1) * pageSize)
        .stream()
        .map(objectIdentity -> objectIdentity.getIdentifier().toString())
        .collect(toList());
  }

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

  public List<PermissionResponse> getPermission(
      String typeId, String identifier, Set<Sid> sids, boolean isReturnInheritedPermissions) {
    checkEntityExists(typeId, identifier);
    checkReadPermission(typeId, sids);

    List<ObjectIdentity> objectIdentities =
        Collections.singletonList(getObjectIdentity(typeId, identifier));

    Map<ObjectIdentity, Acl> aclMap;
    if (!sids.isEmpty()) {
      aclMap = mutableAclService.readAclsById(objectIdentities, getSortedSidList(sids));
    } else {
      aclMap = mutableAclService.readAclsById(objectIdentities);
    }
    List<ObjectPermissionsResponse> permissions =
        getPermissions(aclMap, objectIdentities, sids, isReturnInheritedPermissions);
    List<PermissionResponse> result;
    if (permissions.size() == 1) {
      result = permissions.get(0).getPermissions();
      result.sort(Comparator.comparing(a -> getRoleOrUser(a.getUser(), a.getRole())));
    } else if (permissions.isEmpty()) {
      result = emptyList();
    } else {
      throw new IllegalStateException(
          "Multiple results originating from a single object identity should not be possible");
    }
    return result;
  }

  private Comparable getRoleOrUser(String user, String role) {
    return Strings.isNullOrEmpty(user) ? role : user;
  }

  public List<TypePermissionsResponse> getAllPermissions(Set<Sid> sids, boolean isReturnInherited) {
    checkPermissionsOnSid(sids);

    List<TypePermissionsResponse> result = new LinkedList<>();
    for (String typeId : getClasses()) {
      String entityTypeId = getEntityTypeIdFromClass(typeId);
      if (dataService.hasEntityType(entityTypeId)) {
        Set<Sid> sidsToQuery;
        if (isReturnInherited) {
          sidsToQuery = getInheritedSids(sids);
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

  public Collection<ObjectPermissionsResponse> getPagedPermissionsForType(
      String typeId, Set<Sid> sids, int page, int pageSize) {
    checkEntityTypeExists(typeId);
    checkReadPermission(typeId, sids);

    List<ObjectIdentity> objectIdentities =
        objectIdentityService.getObjectIdentities(typeId, sids, pageSize, (page - 1) * pageSize);

    Map<ObjectIdentity, Acl> aclMap = new HashMap<>();
    if (!objectIdentities.isEmpty()) {
      aclMap = mutableAclService.readAclsById(objectIdentities, getSortedSidList(sids));
    }
    return getPermissions(aclMap, objectIdentities, sids, false);
  }

  public Collection<ObjectPermissionsResponse> getPermissionsForType(
      String typeId, Set<Sid> sids, boolean isReturnInherited) {
    checkEntityTypeExists(typeId);
    checkReadPermission(typeId, sids);
    List<ObjectIdentity> objectIdentities;
    if (sids.isEmpty()) {
      objectIdentities = objectIdentityService.getObjectIdentities(typeId);
    } else {
      objectIdentities = objectIdentityService.getObjectIdentities(typeId, getInheritedSids(sids));
    }
    Map<ObjectIdentity, Acl> aclMap = new HashMap<>();
    if (!objectIdentities.isEmpty()) {
      if (sids.isEmpty()) {
        aclMap = mutableAclService.readAclsById(objectIdentities);
      } else {
        aclMap = mutableAclService.readAclsById(objectIdentities, getSortedSidList(sids));
      }
    }
    return getPermissions(aclMap, objectIdentities, sids, isReturnInherited);
  }

  private LinkedHashSet getInheritedSids(Set<Sid> sids) {
    LinkedList<Sid> result = new LinkedList<>();
    result.addAll(sids);
    result.addAll(userRoleTools.getRoles(sids));
    return new LinkedHashSet(result);
  }

  @Transactional
  public void createAcl(String typeId, String identifier) {
    checkEntityExists(typeId, identifier);
    ObjectIdentity objectIdentity = getObjectIdentity(typeId, identifier);
    if (SecurityUtils.currentUserIsSuOrSystem()) {
      mutableAclService.createAcl(objectIdentity);
    } else {
      throw new InsufficientPermissionDeniedException(objectIdentity, "superuser or owner");
    }
  }

  private void checkEntityExists(String typeId, String identifier) {
    checkEntityTypeExists(typeId);
    String entityTypeId = getEntityTypeIdFromClass(typeId);
    if (dataService.findOneById(entityTypeId, identifier) == null) {
      throw new UnknownEntityException(entityTypeId, identifier);
    }
  }

  private void checkEntityTypeExists(String typeId) {
    String entityTypeId = getEntityTypeIdFromClass(typeId);
    if (!dataService.hasEntityType(entityTypeId)) {
      throw new InvalidTypeIdException(typeId);
    }
  }

  @Transactional
  public void createPermission(
      List<PermissionRequest> permissionRequests, String typeId, String identifier) {
    checkEntityExists(typeId, identifier);
    MutableAcl acl =
        (MutableAcl) mutableAclService.readAclById(getObjectIdentity(typeId, identifier));

    if (isSuOrOwner(acl)) {
      for (PermissionRequest permissionRequest : permissionRequests) {
        if (!getSuitablePermissionsForType(typeId).contains(permissionRequest.getPermission())) {
          throw new PermissionNotSuitableException(permissionRequest.getPermission(), typeId);
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
          throw new DuplicatePermissionException(typeId, identifier, sid);
        }
      }
    } else {
      throw new InsufficientPermissionDeniedException(
          acl.getObjectIdentity(), "superuser or owner");
    }
  }

  @Transactional
  public void createPermissions(
      List<ObjectPermissionsRequest> objectPermissionsRequests, String typeId) {
    for (ObjectPermissionsRequest request : objectPermissionsRequests) {
      createPermission(request.getPermissions(), typeId, request.getObjectId());
    }
  }

  @Transactional
  public void updatePermission(
      List<PermissionRequest> permissionRequests, String typeId, String identifier) {
    checkEntityExists(typeId, identifier);
    try {
      MutableAcl acl =
          (MutableAcl) mutableAclService.readAclById(getObjectIdentity(typeId, identifier));

      if (isSuOrOwner(acl)) {
        for (PermissionRequest permissionRequest : permissionRequests) {
          if (!getSuitablePermissionsForType(typeId).contains(permissionRequest.getPermission())) {
            throw new PermissionNotSuitableException(permissionRequest.getPermission(), typeId);
          }
          Sid sid = userRoleTools.getSid(permissionRequest.getUser(), permissionRequest.getRole());
          List<PermissionResponse> current =
              getPermission(typeId, identifier, Collections.singleton(sid), false);
          if (current.isEmpty()) {
            throw new UnknownAceException(typeId, identifier, sid, "update");
          }
          deleteAce(sid, acl);
          acl.insertAce(
              acl.getEntries().size(),
              paramValueToPermissionSet(permissionRequest.getPermission()),
              sid,
              true);
          mutableAclService.updateAcl(acl);
        }
      } else {
        throw new InsufficientPermissionDeniedException(
            acl.getObjectIdentity(), "superuser or owner");
      }
    } catch (NotFoundException e) {
      throw new AclNotFoundException(typeId);
    }
  }

  @Transactional
  public void updatePermissions(
      @NotEmpty List<ObjectPermissionsRequest> objectPermissionsRequests, String typeId) {
    checkEntityTypeExists(typeId);
    for (ObjectPermissionsRequest permissions : objectPermissionsRequests) {
      updatePermission(permissions.getPermissions(), typeId, permissions.getObjectId());
    }
  }

  @Transactional
  public void deletePermission(Sid sid, String typeId, String identifier) {
    checkEntityExists(typeId, identifier);
    MutableAcl acl =
        (MutableAcl)
            mutableAclService.readAclById(
                getObjectIdentity(typeId, identifier), singletonList(sid));
    if (acl == null) {
      throw new UnknownAceException(typeId, identifier, sid, "delete");
    }
    deleteAce(sid, acl);
  }

  @Transactional
  public void addClass(String typeId) {
    checkEntityTypeExists(typeId);
    if (SecurityUtils.currentUserIsSuOrSystem()) {
      EntityType entityType = dataService.getEntityType(getEntityTypeIdFromClass(typeId));
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
    } else {
      throw new InsufficientPermissionDeniedException("type", typeId, "superuser");
    }
  }

  @Transactional
  public void deleteClass(String typeId) {
    checkEntityTypeExists(typeId);
    mutableAclClassService.deleteAclClass(typeId);
  }

  private void deleteAce(Sid sid, MutableAcl acl) {
    if (isSuOrOwner(acl)) {
      int nrEntries = acl.getEntries().size();
      for (int i = nrEntries - 1; i >= 0; i--) {
        AccessControlEntry accessControlEntry = acl.getEntries().get(i);
        if (accessControlEntry.getSid().equals(sid)) {
          acl.deleteAce(i);
          mutableAclService.updateAcl(acl);
        }
      }
    } else {
      throw new InsufficientPermissionDeniedException(acl.getObjectIdentity(), "su or owner");
    }
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

  private boolean isSuOrOwner(Acl acl) {
    Sid sid = createSecurityContextSid();
    boolean isOwner = acl.getOwner().equals(sid);
    return SecurityUtils.currentUserIsSuOrSystem() || isOwner;
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

  private String getEntityTypeIdFromClass(String typeId) {
    String result;
    switch (typeId) {
      case PackageIdentity.PACKAGE:
        result = PackageMetadata.PACKAGE;
        break;
      case EntityTypeIdentity.ENTITY_TYPE:
        result = EntityTypeMetadata.ENTITY_TYPE_META_DATA;
        break;
      case PluginIdentity.PLUGIN:
        result = PluginMetadata.PLUGIN;
        break;
      case GroupIdentity.GROUP:
        result = GroupMetadata.GROUP;
        break;
      default:
        if (typeId.startsWith(ENTITY_PREFIX)) {
          result = typeId.substring(7);
        } else {
          throw new InvalidTypeIdException(typeId);
        }
        break;
    }
    return result;
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
    List<PermissionResponse> permissionResponses =
        getPermissionResponses(acl, isReturnInheritedPermissions, sids);

    result.put(
        identifier,
        ObjectPermissionsResponse.create(
            identifier,
            getLabel(getEntityTypeIdFromClass(acl.getObjectIdentity().getType()), identifier),
            permissionResponses));
  }

  private List<PermissionResponse> getPermissionResponses(
      Acl acl, boolean isReturnInheritedPermissions, Set<Sid> sids) {
    List<PermissionResponse> result = new LinkedList<>();

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
      Acl acl, boolean isReturnInheritedPermissions, List<PermissionResponse> result, Sid sid) {
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
          PermissionResponse.create(
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

  private ObjectIdentity getObjectIdentity(String classId, String objectIdentifier) {
    return new ObjectIdentityImpl(classId, getTypedValue(objectIdentifier, classId));
  }

  private Serializable getTypedValue(String untypedId, String classId) {
    if (classId.startsWith(ENTITY_PREFIX)) {
      EntityType entityType = dataService.getEntityType(getEntityTypeIdFromClass(classId));
      return (Serializable) EntityUtils.getTypedValue(untypedId, entityType.getIdAttribute(), null);
    } else {
      return untypedId;
    }
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

  private LinkedList getSortedSidList(Set<Sid> sids) {
    LinkedList<Sid> result = new LinkedList<>(sids);
    result.sort(Comparator.comparing(UserRoleTools::getName));
    return result;
  }
}
