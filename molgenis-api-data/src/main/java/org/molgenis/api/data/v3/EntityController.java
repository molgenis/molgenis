package org.molgenis.api.data.v3;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.api.data.v3.SortV3Mapper.map;
import static org.molgenis.security.core.utils.SecurityUtils.currentUserIsAnonymous;
import static org.zalando.problem.Status.BAD_REQUEST;
import static org.zalando.problem.Status.CONFLICT;
import static org.zalando.problem.Status.FORBIDDEN;
import static org.zalando.problem.Status.INTERNAL_SERVER_ERROR;
import static org.zalando.problem.Status.NOT_FOUND;
import static org.zalando.problem.Status.UNAUTHORIZED;

import java.net.URI;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import org.molgenis.api.ApiController;
import org.molgenis.api.ApiNamespace;
import org.molgenis.api.data.v3.EntityCollection.Page;
import org.molgenis.api.model.Selection;
import org.molgenis.data.DataAlreadyExistsException;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.UnknownDataException;
import org.molgenis.data.security.exception.PermissionDeniedException;
import org.molgenis.i18n.BadRequestException;
import org.molgenis.i18n.CodedRuntimeException;
import org.molgenis.i18n.ForbiddenException;
import org.molgenis.util.ApplicationContextProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.zalando.problem.Status;

@RestController
@RequestMapping(EntityController.API_ENTITY_PATH)
class EntityController extends ApiController {
  private static final String API_ENTITY_ID = "entity";
  static final String API_ENTITY_PATH = ApiNamespace.API_PATH + '/' + API_ENTITY_ID;

  private final DataServiceV3 dataServiceV3;
  private final EntityMapper entityMapper;

  EntityController(DataServiceV3 dataServiceV3, EntityMapper entityMapper) {
    super(API_ENTITY_ID, 3);
    this.dataServiceV3 = requireNonNull(dataServiceV3);
    this.entityMapper = requireNonNull(entityMapper);
  }

  @PostMapping("/{entityTypeId}")
  ResponseEntity createEntity(
      @PathVariable("entityTypeId") String entityTypeId,
      @RequestBody Map<String, Object> entityMap) {
    Entity entity = dataServiceV3.create(entityTypeId, entityMap);
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequestUri()
            .replacePath(API_ENTITY_PATH)
            .pathSegment(entityTypeId, entity.getIdValue().toString())
            .build()
            .toUri();
    return ResponseEntity.created(location).build();
  }

  @GetMapping("/{entityTypeId}/{entityId}")
  EntityResponse getEntity(@Valid EntityRequest entityRequest) {
    Selection filter = entityRequest.getFilter();
    Selection expand = entityRequest.getExpand();

    Entity entity =
        dataServiceV3.find(
            entityRequest.getEntityTypeId(), entityRequest.getEntityId(), filter, expand);

    return entityMapper.map(entity, filter, expand);
  }

  @PutMapping("/{entityTypeId}/{entityId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void updateEntity(
      @PathVariable("entityTypeId") String entityTypeId,
      @PathVariable("entityId") String entityId,
      @RequestBody Map<String, Object> entityMap) {
    dataServiceV3.update(entityTypeId, entityId, entityMap);
  }

  @PatchMapping("/{entityTypeId}/{entityId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void updatePartialEntity(
      @PathVariable("entityTypeId") String entityTypeId,
      @PathVariable("entityId") String entityId,
      @RequestBody Map<String, Object> entityMap) {
    dataServiceV3.updatePartial(entityTypeId, entityId, entityMap);
  }

  @DeleteMapping("/{entityTypeId}/{entityId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void deleteEntity(@Valid DeleteEntityRequest deleteRequest) {
    dataServiceV3.delete(deleteRequest.getEntityTypeId(), deleteRequest.getEntityId());
  }

  // TODO refactor this proof-of-concept code
  @GetMapping("/{entityTypeId}")
  EntitiesResponse getEntities(@Valid EntitiesRequest entitiesRequest) {
    EntityCollection entityCollection = createEntityCollection(entitiesRequest);

    Selection filter = entitiesRequest.getFilter();
    Selection expand = entitiesRequest.getExpand();
    return entityMapper.map(entityCollection, filter, expand);
  }

  // TODO refactor this proof-of-concept code
  private EntityCollection createEntityCollection(EntitiesRequest entitiesRequest) {
    Query<Entity> query = createQuery(entitiesRequest);
    int offset = query.getOffset();
    int pageSize = query.getPageSize();

    List<Entity> entities = query.findAll().collect(toList());
    long totalElements = query.offset(0).pageSize(Integer.MAX_VALUE).count();

    return EntityCollection.builder()
        .setEntityTypeId(entitiesRequest.getEntityTypeId())
        .setEntities(entities)
        .setPage(
            Page.builder()
                .setOffset(offset)
                .setPageSize(pageSize)
                .setTotal((int) totalElements) // FIXME cast
                .build())
        .build();
  }

  // TODO refactor this proof-of-concept code
  private Query<Entity> createQuery(EntitiesRequest entitiesRequest) {
    Query<Entity> query =
        ApplicationContextProvider.getApplicationContext()
            .getBean(DataService.class)
            .query(entitiesRequest.getEntityTypeId())
            .offset(entitiesRequest.getNumber() * entitiesRequest.getSize())
            .pageSize(entitiesRequest.getSize());

    entitiesRequest.getQ().ifPresent(query::search);
    entitiesRequest.getSort().ifPresent(sort -> query.sort(map(sort)));

    return query;
  }

  @ExceptionHandler(UnknownDataException.class)
  public ResponseEntity<Problem> handleException(UnknownDataException exception) {
    return getProblemResponseEntity(
        NOT_FOUND, exception.getLocalizedMessage(), exception.getErrorCode());
  }

  @ExceptionHandler(DataAlreadyExistsException.class)
  public ResponseEntity<Problem> handleException(DataAlreadyExistsException exception) {
    return getProblemResponseEntity(
        CONFLICT, exception.getLocalizedMessage(), exception.getErrorCode());
  }

  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<Problem> handleException(BadRequestException exception) {
    return getProblemResponseEntity(
        BAD_REQUEST, exception.getLocalizedMessage(), exception.getErrorCode());
  }

  @ExceptionHandler(ForbiddenException.class)
  public ResponseEntity<Problem> handleException(ForbiddenException exception) {
    return getProblemResponseEntity(
        FORBIDDEN, exception.getLocalizedMessage(), exception.getErrorCode());
  }

  @ExceptionHandler(PermissionDeniedException.class)
  public ResponseEntity<Problem> handleException(PermissionDeniedException exception) {
    return getProblemResponseEntity(
        currentUserIsAnonymous() ? UNAUTHORIZED : FORBIDDEN,
        exception.getLocalizedMessage(),
        exception.getErrorCode());
  }

  @ExceptionHandler(CodedRuntimeException.class)
  public ResponseEntity<Problem> handleException(CodedRuntimeException exception) {
    return getProblemResponseEntity(
        INTERNAL_SERVER_ERROR, exception.getLocalizedMessage(), exception.getErrorCode());
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Problem> handleException(Exception exception) {
    return new ResponseEntity(
        new Problem(INTERNAL_SERVER_ERROR, exception.getLocalizedMessage(), "Unknown_error_code"),
        HttpStatus.INTERNAL_SERVER_ERROR);
  }

  private ResponseEntity<Problem> getProblemResponseEntity(
      Status status, String localizedMessage, String errorCode) {
    return new ResponseEntity(
        new Problem(status, localizedMessage, errorCode),
        HttpStatus.valueOf(status.getStatusCode()));
  }
}
