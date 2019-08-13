package org.molgenis.api.permissions;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Objects.requireNonNull;
import static org.molgenis.api.permissions.PermissionResponseUtils.getPermissionResponse;
import static org.molgenis.api.permissions.PermissionsController.BASE_URI; // NOSONAR
import static org.molgenis.security.core.SidUtils.createRoleSid;
import static org.molgenis.security.core.SidUtils.createUserSid;

import com.google.common.base.Strings;
import cz.jirutka.rsql.parser.RSQLParser;
import cz.jirutka.rsql.parser.RSQLParserException;
import cz.jirutka.rsql.parser.ast.Node;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import org.molgenis.api.ApiController;
import org.molgenis.api.model.response.ApiResponse;
import org.molgenis.api.model.response.PagedApiResponse;
import org.molgenis.api.permissions.exceptions.MissingUserOrRoleException;
import org.molgenis.api.permissions.exceptions.PageWithoutPageSizeException;
import org.molgenis.api.permissions.exceptions.UnsupportedPermissionQueryException;
import org.molgenis.api.permissions.exceptions.UserAndRoleException;
import org.molgenis.api.permissions.exceptions.rsql.PermissionQueryParseException;
import org.molgenis.api.permissions.model.request.DeletePermissionRequest;
import org.molgenis.api.permissions.model.request.ObjectPermissionsRequest;
import org.molgenis.api.permissions.model.request.PermissionRequest;
import org.molgenis.api.permissions.model.request.SetObjectPermissionRequest;
import org.molgenis.api.permissions.model.request.SetTypePermissionsRequest;
import org.molgenis.api.permissions.model.response.AllPermissionsResponse;
import org.molgenis.api.permissions.model.response.LabelledPermissionResponse;
import org.molgenis.api.permissions.model.response.ObjectPermissionResponse;
import org.molgenis.api.permissions.model.response.ObjectResponse;
import org.molgenis.api.permissions.model.response.PermissionResponse;
import org.molgenis.api.permissions.model.response.TypePermissionsResponse;
import org.molgenis.api.permissions.model.response.TypeResponse;
import org.molgenis.api.permissions.rsql.PermissionRsqlVisitor;
import org.molgenis.api.permissions.rsql.PermissionsQuery;
import org.molgenis.data.security.permission.EntityHelper;
import org.molgenis.data.security.permission.PermissionService;
import org.molgenis.data.security.permission.PermissionSetUtils;
import org.molgenis.data.security.permission.UserRoleTools;
import org.molgenis.data.security.permission.model.LabelledPermission;
import org.molgenis.data.security.permission.model.LabelledType;
import org.molgenis.data.security.permission.model.Permission;
import org.molgenis.security.acl.ObjectIdentityService;
import org.molgenis.security.core.PermissionSet;
import org.springframework.http.ResponseEntity;
import org.springframework.security.acls.model.Sid;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Api("Permissions API")
@RestController
@RequestMapping(BASE_URI)
@Transactional
public class PermissionsController extends ApiController {

  private static final String PERMISSION_API_IDENTIFIER = "permissions";
  public static final String BASE_URI = "/api/" + PERMISSION_API_IDENTIFIER;
  static final Integer DEFAULT_PAGE = 1;
  static final Integer DEFAULT_PAGESIZE = 100;

  private static final String TYPE = "type";
  public static final String TYPES = TYPE + "s";
  private static final String TYPE_ID = TYPE + "Id";

  private static final String OBJECT = "object";
  public static final String OBJECTS = OBJECT + "s";
  private static final String OBJECT_ID = OBJECT + "Id";

  private final PermissionService permissionService;
  private final RSQLParser rsqlParser;
  private final ObjectIdentityService objectIdentityService;
  private final UserRoleTools userRoleTools;
  private final EntityHelper entityHelper;

  public PermissionsController(
      PermissionService permissionService,
      RSQLParser rsqlParser,
      ObjectIdentityService objectIdentityService,
      UserRoleTools userRoleTools,
      EntityHelper entityHelper) {
    super(PERMISSION_API_IDENTIFIER, 1);
    this.permissionService = requireNonNull(permissionService);
    this.rsqlParser = requireNonNull(rsqlParser);
    this.objectIdentityService = requireNonNull(objectIdentityService);
    this.userRoleTools = requireNonNull(userRoleTools);
    this.entityHelper = requireNonNull(entityHelper);
  }

  @PostMapping(value = TYPES + "/{" + TYPE_ID + "}")
  @ApiOperation(
      value = "Create a type this enables row level secure an entity",
      code = 201,
      response = ResponseEntity.class)
  public ResponseEntity enableRLS(
      HttpServletRequest request, @PathVariable(value = TYPE_ID) String typeId) {
    permissionService.addType(typeId);
    return ResponseEntity.created(getUriFromRequest(request)).build();
  }

  private URI getUriFromRequest(HttpServletRequest request) {
    return ServletUriComponentsBuilder.fromRequestUri(request).build().toUri();
  }

  @DeleteMapping(value = TYPES + "/{" + TYPE_ID + "}")
  @ApiOperation(
      value = "Delete a type this removes row level security from an entity",
      code = 204,
      response = ResponseEntity.class)
  public ResponseEntity disableRLS(@PathVariable(value = TYPE_ID) String typeId) {
    permissionService.deleteType(typeId);
    return ResponseEntity.noContent().build();
  }

  @GetMapping(value = TYPES)
  @ApiOperation(value = "Get a list of ACL types in the system", response = ApiResponse.class)
  public ApiResponse getRlsEntities() {
    return ApiResponse.create(convertTypes(permissionService.getLabelledTypes()));
  }

  @GetMapping(value = TYPES + "/permissions/{" + TYPE_ID + "}")
  @ApiOperation(
      value = "Get a list of permissions that can be used on a type",
      response = ApiResponse.class)
  public ApiResponse getSuitablePermissions(@PathVariable(value = TYPE_ID) String typeId) {
    return ApiResponse.create(
        permissionService.getSuitablePermissionsForType(typeId).stream()
            .map(PermissionSetUtils::getPermissionStringValue)
            .collect(Collectors.toSet()));
  }

  @PostMapping(value = OBJECTS + "/{" + TYPE_ID + "}/{" + OBJECT_ID + "}")
  @ApiOperation(value = "Create a type for a entity", code = 201, response = ResponseEntity.class)
  public ResponseEntity createAcl(
      HttpServletRequest request,
      @PathVariable(TYPE_ID) String typeId,
      @PathVariable(OBJECT_ID) String identifier) {
    permissionService.createAcl(entityHelper.getObjectIdentity(typeId, identifier));
    return ResponseEntity.created(getUriFromRequest(request)).build();
  }

  @GetMapping(value = OBJECTS + "/{" + TYPE_ID + "}")
  @ApiOperation(
      value =
          "Get a list object's for a type. Typically this is a row in a row level secured entity.",
      response = PagedApiResponse.class)
  public PagedApiResponse getAcls(
      @PathVariable(value = TYPE_ID) String typeId,
      @RequestParam(value = "page", required = false) Integer page,
      @RequestParam(value = "pageSize", required = false) Integer pageSize) {
    validateQueryParams(page, pageSize, false);
    if (page == null) {
      page = DEFAULT_PAGE;
      pageSize = DEFAULT_PAGESIZE;
    }

    Set<ObjectResponse> data =
        permissionService.getObjects(typeId, page, pageSize).stream()
            .map(
                labelledObject ->
                    ObjectResponse.create(labelledObject.getId(), labelledObject.getLabel()))
            .collect(Collectors.toSet());
    int totalItems = objectIdentityService.getNrOfObjectIdentities(typeId);
    return getPermissionResponse("", page, pageSize, totalItems, data);
  }

  @GetMapping(value = "{" + TYPE_ID + "}/{" + OBJECT_ID + "}")
  @ApiOperation(value = "Gets permissions on a single object", response = ApiResponse.class)
  public ApiResponse getPermissionsForObject(
      @PathVariable(TYPE_ID) String typeId,
      @PathVariable(OBJECT_ID) String identifier,
      @RequestParam(value = "q", required = false) String queryString,
      @RequestParam(value = "inheritance", defaultValue = "false", required = false)
          boolean inheritance) {
    Set<Sid> sids = getSidsFromQuery(queryString);

    Set<LabelledPermission> labelledObjectPermissions =
        permissionService.getPermissionsForObject(
            entityHelper.getObjectIdentity(typeId, identifier), sids, inheritance);
    ObjectPermissionResponse permissionResponse =
        convertToObjectResponse(typeId, identifier, labelledObjectPermissions);
    return ApiResponse.create(permissionResponse);
  }

  @GetMapping(value = "{" + TYPE_ID + "}")
  @ApiOperation(
      value = "Gets all permissions for all objects of a certain type",
      response = PagedApiResponse.class)
  public PagedApiResponse getPermissionsForType(
      @PathVariable(value = TYPE_ID) String typeId,
      @RequestParam(value = "q", required = false) String queryString,
      @RequestParam(value = "page", required = false) Integer page,
      @RequestParam(value = "pageSize", required = false) Integer pageSize,
      @RequestParam(value = "inheritance", defaultValue = "false", required = false)
          boolean inheritance) {
    validateQueryParams(page, pageSize, inheritance);
    Set<Sid> sids = getSidsFromQuery(queryString);

    PagedApiResponse response;
    TypePermissionsResponse permissionsResponse;
    Map<String, Set<LabelledPermission>> typePermission;
    if (page != null) {
      typePermission = permissionService.getPermissionsForType(typeId, sids, page, pageSize);
      permissionsResponse = convertToTypeResponse(typeId, typePermission);
      Integer totalItems = objectIdentityService.getNrOfObjectIdentities(typeId, sids);
      response =
          getPermissionResponse(queryString, page, pageSize, totalItems, permissionsResponse);
    } else {
      typePermission = permissionService.getPermissionsForType(typeId, sids, inheritance);
      permissionsResponse = convertToTypeResponse(typeId, typePermission);
      response = getPermissionResponse(queryString, permissionsResponse);
    }
    return response;
  }

  @GetMapping()
  @ApiOperation(
      value = "Gets all permissions for one or more users or roles",
      response = ApiResponse.class)
  public ApiResponse getPermissionsForUser(
      @RequestParam(value = "q", required = false) String queryString,
      @RequestParam(value = "inheritance", defaultValue = "false", required = false)
          boolean inheritance) {
    Set<Sid> sids = getSidsFromQuery(queryString);
    Set<LabelledPermission> permissions = permissionService.getPermissions(sids, inheritance);
    return ApiResponse.create(convertToAllPermissionsResponse(permissions));
  }

  @PatchMapping(value = "{" + TYPE_ID + "}/{" + OBJECT_ID + "}")
  @ApiOperation(
      value = "Update a permission on a single object for one or more users or roles",
      code = 204,
      response = ResponseEntity.class)
  public ResponseEntity setPermission(
      @PathVariable(value = TYPE_ID) String typeId,
      @PathVariable(value = OBJECT_ID) String identifier,
      @RequestBody SetObjectPermissionRequest request) {
    Set<Permission> permissions = convertRequests(request.getPermissions(), typeId, identifier);
    permissionService.updatePermissions(permissions);
    return ResponseEntity.noContent().build();
  }

  @PatchMapping(value = "{" + TYPE_ID + "}")
  @ApiOperation(
      value = "Update a list of permissions on objects of a certain type",
      code = 204,
      response = ResponseEntity.class)
  public ResponseEntity setTypePermissions(
      @PathVariable(value = TYPE_ID) String typeId,
      @RequestBody SetTypePermissionsRequest request) {
    Set<Permission> permissions = convertRequests(request.getObjects(), typeId);
    permissionService.updatePermissions(permissions);
    return ResponseEntity.noContent().build();
  }

  @PostMapping(value = "{" + TYPE_ID + "}")
  @ApiOperation(
      value = "Create a list of permissions on an type for a single user or role",
      code = 201,
      response = ResponseEntity.class)
  public ResponseEntity<Object> createPermissions(
      HttpServletRequest request,
      @PathVariable(value = TYPE_ID) String typeId,
      @RequestBody SetTypePermissionsRequest setTypePermissionsRequest) {
    Set<Permission> permissions = convertRequests(setTypePermissionsRequest.getObjects(), typeId);
    permissionService.createPermissions(permissions);
    return ResponseEntity.created(getUriFromRequest(request)).build();
  }

  @PostMapping(value = "{" + TYPE_ID + "}/{" + OBJECT_ID + "}")
  @ApiOperation(
      value = "Create a permission on an object for a single user or role",
      code = 201,
      response = ResponseEntity.class)
  public ResponseEntity<Object> createPermission(
      HttpServletRequest request,
      @PathVariable(value = TYPE_ID) String typeId,
      @PathVariable(value = OBJECT_ID) String identifier,
      @RequestBody SetObjectPermissionRequest setIdentityPermissionRequest) {
    Set<Permission> permissions =
        convertRequests(setIdentityPermissionRequest.getPermissions(), typeId, identifier);
    permissionService.createPermissions(permissions);
    return ResponseEntity.created(getUriFromRequest(request)).build();
  }

  @DeleteMapping(value = "{" + TYPE_ID + "}/{" + OBJECT_ID + "}")
  @ApiOperation(
      value = "Delete a permission on an object for a single user or role",
      code = 204,
      response = ResponseEntity.class)
  public ResponseEntity deletePermission(
      @PathVariable(value = TYPE_ID) String typeId,
      @PathVariable(value = OBJECT_ID) String identifier,
      @RequestBody DeletePermissionRequest request) {
    Sid sid = getSid(request.getUser(), request.getRole());
    permissionService.deletePermission(sid, entityHelper.getObjectIdentity(typeId, identifier));
    return ResponseEntity.noContent().build();
  }

  private Set<Sid> getSidsFromQuery(String queryString) {
    Set<Sid> sids = Collections.emptySet();
    if (!Strings.isNullOrEmpty(queryString)) {
      try {
        Node node = rsqlParser.parse(queryString);
        PermissionsQuery permissionsQuery = node.accept(new PermissionRsqlVisitor());
        sids =
            new LinkedHashSet<>(
                userRoleTools.getSids(permissionsQuery.getUsers(), permissionsQuery.getRoles()));
      } catch (RSQLParserException e) {
        throw new PermissionQueryParseException(e);
      }
    }
    return sids;
  }

  private void validateQueryParams(Integer page, Integer pageSize, boolean inheritance) {
    if ((page == null && pageSize != null) || (page != null && pageSize == null)) {
      throw new PageWithoutPageSizeException();
    }
    if (page != null && inheritance) {
      throw new UnsupportedPermissionQueryException();
    }
  }

  private Set<TypeResponse> convertTypes(Set<LabelledType> types) {
    return types.stream()
        .map(type -> TypeResponse.create(type.getId(), type.getEntityType(), type.getLabel()))
        .collect(Collectors.toSet());
  }

  private ObjectPermissionResponse convertToObjectResponse(
      String typeId, String id, Set<LabelledPermission> labelledObjectPermissions) {
    Set<PermissionResponse> permissions = convertToPermissions(labelledObjectPermissions);
    String label = entityHelper.getLabel(typeId, id);
    return ObjectPermissionResponse.create(id, label, permissions);
  }

  private TypePermissionsResponse convertToTypeResponse(
      String typeId, Map<String, Set<LabelledPermission>> typePermissions) {
    Set<ObjectPermissionResponse> objectPermissions = new LinkedHashSet<>();
    for (Entry<String, Set<LabelledPermission>> entry : typePermissions.entrySet()) {
      Set<LabelledPermission> permissions = entry.getValue();
      if (!permissions.isEmpty()) {
        LabelledPermission labelledObjectPermission =
            permissions.stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Empty set of permissions"));
        objectPermissions.add(
            convertToObjectResponse(
                typeId,
                labelledObjectPermission.getLabelledObjectIdentity().getIdentifier().toString(),
                permissions));
      }
    }
    String label = entityHelper.getLabel(typeId);
    return TypePermissionsResponse.create(typeId, label, objectPermissions);
  }

  private Set<PermissionResponse> convertToPermissions(Set<LabelledPermission> permissions) {
    LinkedHashSet<PermissionResponse> result = new LinkedHashSet();
    permissions.stream()
        .map(
            labelledPermission ->
                PermissionResponse.create(
                    UserRoleTools.getUsername(labelledPermission.getSid()).orElse(null),
                    UserRoleTools.getRolename(labelledPermission.getSid()).orElse(null),
                    PermissionSetUtils.getPermissionStringValue(labelledPermission).orElse(null),
                    convertLabelledPermissions(labelledPermission.getInheritedPermissions())))
        .forEach(result::add);
    return result;
  }

  private AllPermissionsResponse convertToAllPermissionsResponse(
      Set<LabelledPermission> permissions) {
    return AllPermissionsResponse.create(convertLabelledPermissions(permissions));
  }

  private Set<LabelledPermissionResponse> convertLabelledPermissions(
      Set<LabelledPermission> objectPermissionResponses) {
    Set<LabelledPermissionResponse> permissionResponses = null;
    if (objectPermissionResponses != null) {
      permissionResponses = new LinkedHashSet<>();
      for (LabelledPermission permission : objectPermissionResponses) {
        PermissionSet permissionSet = permission.getPermission();
        String permissionString = null;
        if (permissionSet != null) {
          permissionString = PermissionSetUtils.getPermissionStringValue(permissionSet);
        }
        LabelledPermissionResponse labelledResponse;
        if (permission.getLabelledObjectIdentity() != null) {
          ObjectResponse objectResponse =
              ObjectResponse.create(
                  permission.getLabelledObjectIdentity().getIdentifier().toString(),
                  permission.getLabelledObjectIdentity().getIdentifierLabel());
          TypeResponse typeResponse =
              TypeResponse.create(
                  permission.getLabelledObjectIdentity().getType(),
                  permission.getLabelledObjectIdentity().getEntityTypeId(),
                  permission.getLabelledObjectIdentity().getTypeLabel());
          labelledResponse =
              LabelledPermissionResponse.create(
                  UserRoleTools.getUsername(permission.getSid()).orElse(null),
                  UserRoleTools.getRolename(permission.getSid()).orElse(null),
                  objectResponse,
                  typeResponse,
                  permissionString,
                  convertLabelledPermissions(permission.getInheritedPermissions()));
        } else {
          labelledResponse =
              LabelledPermissionResponse.create(
                  UserRoleTools.getUsername(permission.getSid()).orElse(null),
                  UserRoleTools.getRolename(permission.getSid()).orElse(null),
                  null,
                  null,
                  permissionString,
                  convertLabelledPermissions(permission.getInheritedPermissions()));
        }
        permissionResponses.add(labelledResponse);
      }
    }
    return permissionResponses;
  }

  private Set<Permission> convertRequests(
      List<PermissionRequest> requests, String typeId, String identifier) {
    Set<Permission> permissions = new HashSet<>();
    for (PermissionRequest permissionRequest : requests) {
      permissions.add(
          Permission.create(
              entityHelper.getObjectIdentity(typeId, identifier),
              getSid(permissionRequest.getUser(), permissionRequest.getRole()),
              PermissionSetUtils.paramValueToPermissionSet(permissionRequest.getPermission())));
    }
    return permissions;
  }

  private Set<Permission> convertRequests(List<ObjectPermissionsRequest> requests, String typeId) {
    Set<Permission> permissions = new HashSet<>();
    for (ObjectPermissionsRequest request : requests) {
      permissions.addAll(convertRequests(request.getPermissions(), typeId, request.getObjectId()));
    }
    return permissions;
  }

  private Sid getSid(String user, String role) {
    if (isNullOrEmpty(user) && isNullOrEmpty(role)) {
      throw new MissingUserOrRoleException();
    } else if (!isNullOrEmpty(user) && !isNullOrEmpty(role)) {
      throw new UserAndRoleException();
    } else if (!isNullOrEmpty(user)) {
      userRoleTools.checkUserExists(user);
      return createUserSid(user);
    }
    userRoleTools.checkRoleExists(role);
    return createRoleSid(role);
  }
}
