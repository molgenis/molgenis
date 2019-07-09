package org.molgenis.api.data.v3;

import static java.util.Objects.requireNonNull;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import org.molgenis.api.ApiController;
import org.molgenis.api.ApiNamespace;
import org.molgenis.api.data.v3.EntityCollection.Page;
import org.molgenis.api.model.Query;
import org.molgenis.api.model.Selection;
import org.molgenis.api.model.Sort;
import org.molgenis.data.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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

@RestController
@RequestMapping(EntityController.API_ENTITY_PATH)
class EntityController extends ApiController {
  private static final Logger LOG = LoggerFactory.getLogger(EntityController.class);
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

  @DeleteMapping("/{entityTypeId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void deleteEntities(@Valid DeleteEntitiesRequest deleteRequest) {
    Query query = deleteRequest.getQ().orElse(new Query());
    dataServiceV3.delete(deleteRequest.getEntityTypeId(), query);
  }

  @GetMapping("/{entityTypeId}")
  EntitiesResponse getEntities(@Valid EntitiesRequest entitiesRequest) {
    EntityCollection entityCollection = createEntityCollection(entitiesRequest);

    Selection filter = entitiesRequest.getFilter();
    Selection expand = entitiesRequest.getExpand();
    return entityMapper.map(entityCollection, filter, expand);
  }

  private EntityCollection createEntityCollection(EntitiesRequest entitiesRequest) {
    Query query = entitiesRequest.getQ().orElse(new Query());
    Sort sort = entitiesRequest.getSort().orElse(Sort.create(Collections.emptyList()));
    List<Entity> entities =
        dataServiceV3.find(
            entitiesRequest.getEntityTypeId(),
            query,
            sort,
            entitiesRequest.getFilter(),
            entitiesRequest.getExpand(),
            entitiesRequest.getSize(),
            entitiesRequest.getNumber());

    int totalElements = dataServiceV3.count(entitiesRequest.getEntityTypeId(), query);

    return EntityCollection.builder()
        .setEntityTypeId(entitiesRequest.getEntityTypeId())
        .setEntities(entities)
        .setPage(
            Page.builder()
                .setOffset(entitiesRequest.getSize() * entitiesRequest.getNumber())
                .setPageSize(entitiesRequest.getSize())
                .setTotal(totalElements)
                .build())
        .build();
  }
}
