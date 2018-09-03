package org.molgenis.core.ui.admin.permission;

import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static java.util.Comparator.comparing;
import static java.util.Comparator.comparingInt;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.molgenis.core.ui.admin.permission.PermissionManagerController.URI;
import static org.molgenis.data.plugin.model.PluginMetadata.PLUGIN;
import static org.molgenis.data.security.auth.RoleMetadata.ROLE;
import static org.molgenis.data.security.auth.UserMetaData.USER;
import static org.molgenis.security.core.PermissionSet.*;
import static org.molgenis.security.core.SidUtils.createRoleSid;
import static org.molgenis.security.core.SidUtils.createUserSid;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.validation.Valid;
import org.molgenis.core.ui.admin.permission.exception.UnexpectedPermissionException;
import org.molgenis.data.DataService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.model.PackageMetadata;
import org.molgenis.data.meta.system.SystemEntityTypeRegistry;
import org.molgenis.data.plugin.model.Plugin;
import org.molgenis.data.plugin.model.PluginIdentity;
import org.molgenis.data.security.EntityIdentity;
import org.molgenis.data.security.EntityIdentityUtils;
import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.data.security.PackageIdentity;
import org.molgenis.data.security.auth.Role;
import org.molgenis.data.security.auth.User;
import org.molgenis.security.acl.MutableAclClassService;
import org.molgenis.security.core.PermissionRegistry;
import org.molgenis.security.core.PermissionSet;
import org.molgenis.security.permission.Permissions;
import org.molgenis.web.PluginController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.acls.model.*;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

@Controller
@Api("PermissionManager")
@RequestMapping(URI)
public class PermissionManagerController extends PluginController {
  private static final Logger LOG = LoggerFactory.getLogger(PermissionManagerController.class);

  public static final String URI = PluginController.PLUGIN_URI_PREFIX + "permissionmanager";
  public static final String RADIO = "radio-";

  private final DataService dataService;
  private final MutableAclService mutableAclService;
  private final MutableAclClassService mutableAclClassService;
  private final SystemEntityTypeRegistry systemEntityTypeRegistry;
  private final PermissionRegistry permissionRegistry;

  public PermissionManagerController(
      DataService dataService,
      MutableAclService mutableAclService,
      MutableAclClassService mutableAclClassService,
      SystemEntityTypeRegistry systemEntityTypeRegistry,
      PermissionRegistry permissionRegistry) {
    super(URI);
    this.dataService = requireNonNull(dataService);
    this.mutableAclService = requireNonNull(mutableAclService);
    this.mutableAclClassService = requireNonNull(mutableAclClassService);
    this.systemEntityTypeRegistry = requireNonNull(systemEntityTypeRegistry);
    this.permissionRegistry = requireNonNull(permissionRegistry);
  }

  @GetMapping
  public String init(Model model) {
    model.addAttribute(
        "users",
        Lists.newArrayList(
            getUsers()
                .stream()
                .filter(
                    user -> {
                      Boolean superuser = user.isSuperuser();
                      return superuser == null || !superuser;
                    })
                .collect(Collectors.toList())));
    model.addAttribute("roles", getRoles());
    model.addAttribute("entityTypes", getEntityTypeDtos());
    return "view-permissionmanager";
  }

  private List<EntityTypeRlsResponse> getEntityTypeDtos() {
    List<EntityType> entityTypes =
        getEntityTypes().filter(entityType -> !entityType.isAbstract()).collect(toList());
    Collection<String> aclClasses = mutableAclClassService.getAclClassTypes();
    entityTypes.sort(comparing(EntityType::getLabel));
    return entityTypes
        .stream()
        .map(
            entityType -> {
              boolean rlsEnabled = aclClasses.contains(EntityIdentityUtils.toType(entityType));
              boolean readOnly = systemEntityTypeRegistry.hasSystemEntityType(entityType.getId());
              return new EntityTypeRlsResponse(
                  entityType.getId(), entityType.getLabel(), rlsEnabled, readOnly);
            })
        .collect(toList());
  }

  @PreAuthorize("hasAnyRole('ROLE_SU')")
  @Transactional(readOnly = true)
  @GetMapping("/entityclass/role/{rolename}")
  @ResponseBody
  public Permissions getRoleEntityClassPermissions(@PathVariable String rolename) {
    Sid sid = createRoleSid(rolename);
    return getEntityTypePermissions(sid);
  }

  @PreAuthorize("hasAnyRole('ROLE_SU')")
  @Transactional(readOnly = true)
  @GetMapping("/plugin/user/{username}")
  @ResponseBody
  public Permissions getUserPluginPermissions(@PathVariable String username) {
    Sid sid = createUserSid(username);
    return getPluginPermissions(sid);
  }

  @PreAuthorize("hasAnyRole('ROLE_SU')")
  @Transactional(readOnly = true)
  @GetMapping("/package/user/{username}")
  @ResponseBody
  public Permissions getUserPackagePermissions(@PathVariable String username) {
    Sid sid = createUserSid(username);
    return getPackagePermissions(sid);
  }

  @PreAuthorize("hasAnyRole('ROLE_SU')")
  @Transactional(readOnly = true)
  @GetMapping("/package/role/{rolename}")
  @ResponseBody
  public Permissions getRolePackagePermissions(@PathVariable String rolename) {
    Sid sid = createRoleSid(rolename);
    return getPackagePermissions(sid);
  }

  @PreAuthorize("hasAnyRole('ROLE_SU')")
  @Transactional(readOnly = true)
  @GetMapping("/entityclass/user/{username}")
  @ResponseBody
  public Permissions getUserEntityClassPermissions(@PathVariable String username) {
    Sid sid = createUserSid(username);
    return getEntityTypePermissions(sid);
  }

  @PreAuthorize("hasAnyRole('ROLE_SU')")
  @Transactional
  @PostMapping("/update/plugin/role")
  @ResponseStatus(HttpStatus.OK)
  public void updateRolePluginPermissions(@RequestParam String rolename, WebRequest webRequest) {
    Sid sid = createRoleSid(rolename);
    updatePluginPermissions(webRequest, sid);
  }

  @PreAuthorize("hasAnyRole('ROLE_SU')")
  @Transactional
  @PostMapping("/update/entityclass/role")
  @ResponseStatus(HttpStatus.OK)
  public void updateRoleEntityClassPermissions(
      @RequestParam String rolename, WebRequest webRequest) {
    Sid sid = createRoleSid(rolename);
    updateEntityTypePermissions(webRequest, sid);
  }

  @PreAuthorize("hasAnyRole('ROLE_SU')")
  @Transactional
  @PostMapping("/update/package/role")
  @ResponseStatus(HttpStatus.OK)
  public void updateRolePackagePermissions(@RequestParam String rolename, WebRequest webRequest) {
    Sid sid = createRoleSid(rolename);
    updatePackagePermissions(webRequest, sid);
  }

  @PreAuthorize("hasAnyRole('ROLE_SU')")
  @Transactional
  @PostMapping("/update/package/user")
  @ResponseStatus(HttpStatus.OK)
  public void updateUserPackagePermissions(@RequestParam String username, WebRequest webRequest) {
    Sid sid = createUserSid(username);
    updatePackagePermissions(webRequest, sid);
  }

  @PreAuthorize("hasAnyRole('ROLE_SU')")
  @Transactional
  @PostMapping("/update/plugin/user")
  @ResponseStatus(HttpStatus.OK)
  public void updateUserPluginPermissions(@RequestParam String username, WebRequest webRequest) {
    Sid sid = createUserSid(username);
    updatePluginPermissions(webRequest, sid);
  }

  private void removeSidPluginPermission(Plugin plugin, Sid sid) {
    ObjectIdentity objectIdentity = new PluginIdentity(plugin);
    removePermissionForSid(sid, objectIdentity);
  }

  private void createSidPluginPermission(Plugin plugin, Sid sid, PermissionSet pluginPermission) {
    ObjectIdentity objectIdentity = new PluginIdentity(plugin);
    createSidPermission(sid, objectIdentity, pluginPermission);
  }

  @PreAuthorize("hasAnyRole('ROLE_SU')")
  @Transactional(readOnly = true)
  @GetMapping("/plugin/role/{rolename}")
  @ResponseBody
  public Permissions getRolePluginPermissions(@PathVariable String rolename) {
    Sid sid = createRoleSid(rolename);
    return getPluginPermissions(sid);
  }

  /** package-private for testability */
  List<Plugin> getPlugins() {
    return dataService.findAll(PLUGIN, Plugin.class).collect(toList());
  }

  @PreAuthorize("hasAnyRole('ROLE_SU')")
  @Transactional
  @PostMapping("/update/entityclass/user")
  @ResponseStatus(HttpStatus.OK)
  public void updateUserEntityClassPermissions(
      @RequestParam String username, WebRequest webRequest) {
    Sid sid = createUserSid(username);
    updateEntityTypePermissions(webRequest, sid);
  }

  @PreAuthorize("hasAnyRole('ROLE_SU')")
  @Transactional
  @PostMapping("/update/entityclass/rls")
  @ResponseStatus(HttpStatus.OK)
  public void updateEntityClassRls(@Valid @RequestBody EntityTypeRlsRequest entityTypeRlsRequest) {
    String entityTypeId = entityTypeRlsRequest.getId();
    if (systemEntityTypeRegistry.hasSystemEntityType(entityTypeId)) {
      throw new IllegalArgumentException("Updating system entity type not allowed");
    }

    EntityType entityType = dataService.getEntityType(entityTypeId);
    String aclClassType = EntityIdentityUtils.toType(entityType);
    boolean hasAclClass = mutableAclClassService.hasAclClass(aclClassType);
    if (entityTypeRlsRequest.isRlsEnabled()) {
      if (!hasAclClass) {
        mutableAclClassService.createAclClass(
            aclClassType, EntityIdentityUtils.toIdType(entityType));
        dataService
            .findAll(entityType.getId())
            .forEach(entity -> mutableAclService.createAcl(new EntityIdentity(entity)));
      }
    } else {
      if (hasAclClass) {
        mutableAclClassService.deleteAclClass(aclClassType);
      }
    }
  }

  @ApiOperation(
      value = "Get all permission sets",
      response = PermissionSetResponse.class,
      responseContainer = "List")
  @GetMapping("/permissionSets")
  @ResponseBody
  public List<PermissionSetResponse> getPermissionSets() {
    return permissionRegistry
        .getPermissionSets()
        .entrySet()
        .stream()
        .map(entry -> PermissionSetResponse.create(entry.getKey(), entry.getValue()))
        .sorted(comparing(PermissionSetResponse::getPermissions, comparingInt(List::size)))
        .collect(toList());
  }

  private static PermissionSet paramValueToPermissionSet(String paramValue) {
    switch (paramValue.toUpperCase()) {
      case "READMETA":
        return PermissionSet.READMETA;
      case "COUNT":
        return PermissionSet.COUNT;
      case "READ":
        return PermissionSet.READ;
      case "WRITE":
        return PermissionSet.WRITE;
      case "WRITEMETA":
        return PermissionSet.WRITEMETA;
      default:
        throw new IllegalArgumentException(format("Unknown PermissionSet '%s'", paramValue));
    }
  }

  private void updatePluginPermissions(WebRequest webRequest, Sid sid) {
    for (Plugin plugin : getPlugins()) {
      String param = RADIO + plugin.getId();
      String value = webRequest.getParameter(param);
      if (value != null) {
        if (!value.equals("none")) {
          createSidPluginPermission(plugin, sid, paramValueToPermissionSet(value));
        } else {
          removeSidPluginPermission(plugin, sid);
        }
      }
    }
  }

  private void updatePackagePermissions(WebRequest webRequest, Sid sid) {
    for (Package pack : getPackages()) {
      String param = RADIO + pack.getId();
      String value = webRequest.getParameter(param);
      if (value != null) {
        if (!value.equals("none")) {
          createSidPackagePermission(pack, sid, paramValueToPermissionSet(value));
        } else {
          removeSidPackagePermission(pack, sid);
        }
      }
    }
  }

  private Permissions getPackagePermissions(Sid sid) {
    return getPermissions(sid, getPackages().stream().map(PackageIdentity::new).collect(toList()));
  }

  private Permissions getPluginPermissions(Sid sid) {
    return getPermissions(sid, getPlugins().stream().map(PluginIdentity::new).collect(toList()));
  }

  private Permissions getPermissions(Sid sid, List<ObjectIdentity> objectIdentities) {
    Map<ObjectIdentity, Acl> aclMap =
        mutableAclService.readAclsById(objectIdentities, singletonList(sid));
    Set<String> ids =
        objectIdentities
            .stream()
            .map(ObjectIdentity::getIdentifier)
            .map(Object::toString)
            .collect(toSet());
    return Permissions.create(ids, getPermissions(aclMap, sid));
  }

  private Multimap<String, String> getPermissions(Map<ObjectIdentity, Acl> acls, Sid sid) {
    Multimap<String, String> result = LinkedHashMultimap.create();
    acls.forEach(
        (objectIdentity, acl) -> {
          String id = objectIdentity.getIdentifier().toString();
          acl.getEntries()
              .stream()
              .filter(ace -> ace.getSid().equals(sid))
              .map(this::getPermissionString)
              .forEach(permission -> result.put(id, permission));
        });
    return result;
  }

  private String getPermissionString(AccessControlEntry ace) {
    switch (ace.getPermission().getMask()) {
      case READ_META_MASK:
        return "readmeta";
      case COUNT_MASK:
        return "count";
      case READ_MASK:
        return "read";
      case WRITE_MASK:
        return "write";
      case WRITEMETA_MASK:
        return "writemeta";
      default:
        throw new UnexpectedPermissionException(ace.getPermission());
    }
  }

  private void removeSidEntityTypePermission(EntityType entityType, Sid sid) {
    ObjectIdentity objectIdentity = new EntityTypeIdentity(entityType);
    removePermissionForSid(sid, objectIdentity);
  }

  private void createSidEntityTypePermission(
      EntityType entityType, Sid sid, PermissionSet permissionSet) {
    ObjectIdentity objectIdentity = new EntityTypeIdentity(entityType);
    createSidPermission(sid, objectIdentity, permissionSet);
  }

  private void createSidPackagePermission(Package pack, Sid sid, PermissionSet permissionSet) {
    ObjectIdentity objectIdentity = new PackageIdentity(pack);
    createSidPermission(sid, objectIdentity, permissionSet);
  }

  private void removeSidPackagePermission(Package pack, Sid sid) {
    ObjectIdentity objectIdentity = new PackageIdentity(pack);
    removePermissionForSid(sid, objectIdentity);
  }

  private void removePermissionForSid(Sid sid, ObjectIdentity objectIdentity) {
    MutableAcl acl = (MutableAcl) mutableAclService.readAclById(objectIdentity, singletonList(sid));

    boolean aclUpdated = deleteAceIfExists(sid, acl);
    if (aclUpdated) {
      mutableAclService.updateAcl(acl);
    }
  }

  private void updateEntityTypePermissions(WebRequest webRequest, Sid sid) {
    getEntityTypes()
        .forEach(
            entityType -> {
              String param = RADIO + entityType.getId();
              String value = webRequest.getParameter(param);
              if (value != null) {
                if (!value.equals("none")) {
                  createSidEntityTypePermission(entityType, sid, paramValueToPermissionSet(value));
                } else {
                  removeSidEntityTypePermission(entityType, sid);
                }
              }
            });
  }

  private Permissions getEntityTypePermissions(Sid sid) {
    return getPermissions(
        sid,
        getEntityTypes().collect(toList()).stream().map(EntityTypeIdentity::new).collect(toList()));
  }

  private void createSidPermission(
      Sid sid,
      ObjectIdentity objectIdentity,
      org.springframework.security.acls.model.Permission permission) {
    MutableAcl acl = (MutableAcl) mutableAclService.readAclById(objectIdentity, singletonList(sid));

    deleteAceIfExists(sid, acl);
    acl.insertAce(0, permission, sid, true);
    mutableAclService.updateAcl(acl);
  }

  /** package-private for testability */
  List<User> getUsers() {
    return dataService.findAll(USER, User.class).collect(toList());
  }

  /** package-private for testability */
  List<Role> getRoles() {
    return dataService.findAll(ROLE, Role.class).collect(toList());
  }

  List<Package> getPackages() {
    return dataService.findAll(PackageMetadata.PACKAGE, Package.class).collect(toList());
  }

  private boolean deleteAceIfExists(Sid sid, MutableAcl acl) {
    boolean aclUpdated = false;
    int nrEntries = acl.getEntries().size();
    for (int i = nrEntries - 1; i >= 0; i--) {
      AccessControlEntry accessControlEntry = acl.getEntries().get(i);
      if (accessControlEntry.getSid().equals(sid)) {
        acl.deleteAce(i);
        aclUpdated = true;
      }
    }
    return aclUpdated;
  }

  private Stream<EntityType> getEntityTypes() {
    return dataService.findAll(EntityTypeMetadata.ENTITY_TYPE_META_DATA, EntityType.class);
  }

  @ExceptionHandler(RuntimeException.class)
  @ResponseBody
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public Map<String, String> handleRuntimeException(RuntimeException e) {
    LOG.error(null, e);
    return Collections.singletonMap(
        "errorMessage",
        "An error occurred. Please contact the administrator.<br />Message:" + e.getMessage());
  }
}
