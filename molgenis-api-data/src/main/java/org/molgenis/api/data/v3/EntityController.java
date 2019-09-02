package org.molgenis.api.data.v3;

import static java.util.Objects.requireNonNull;

import java.net.URI;
import java.util.Map;
import javax.validation.Valid;
import org.molgenis.api.ApiController;
import org.molgenis.api.ApiNamespace;
import org.molgenis.api.data.v3.EntityCollection.Page;
import org.molgenis.api.data.v3.model.DeleteEntitiesRequest;
import org.molgenis.api.data.v3.model.DeleteEntityRequest;
import org.molgenis.api.data.v3.model.EntitiesResponse;
import org.molgenis.api.data.v3.model.EntityResponse;
import org.molgenis.api.data.v3.model.ReadEntitiesRequest;
import org.molgenis.api.data.v3.model.ReadEntityRequest;
import org.molgenis.api.data.v3.model.ReadSubresourceRequest;
import org.molgenis.api.model.Query;
import org.molgenis.api.model.Selection;
import org.molgenis.api.model.Sort;
import org.molgenis.data.Entity;
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
public class EntityController extends ApiController {
  private static final String API_ENTITY_ID = "data";
  public static final String API_ENTITY_PATH = ApiNamespace.API_PATH + '/' + API_ENTITY_ID;

  private final DataServiceV3 dataServiceV3;
  private final EntityMapper entityMapper;

  EntityController(DataServiceV3 dataServiceV3, EntityMapper entityMapper) {
    super(API_ENTITY_ID, 3);
    this.dataServiceV3 = requireNonNull(dataServiceV3);
    this.entityMapper = requireNonNull(entityMapper);
  }

  @PostMapping("/{entityTypeId}")
  public ResponseEntity createEntity(
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
  public EntityResponse getEntity(@Valid ReadEntityRequest entityRequest) {
    Selection filter = entityRequest.getFilter();
    Selection expand = entityRequest.getExpand();

    Entity entity =
        dataServiceV3.find(
            entityRequest.getEntityTypeId(), entityRequest.getEntityId(), filter, expand);

    return entityMapper.map(entity, filter, expand);
  }

  @GetMapping("/{entityTypeId}/{entityId}/{fieldId}")
  public EntitiesResponse getReferencedEntities(@Valid ReadSubresourceRequest entitiesRequest) {
    String entityTypeId = entitiesRequest.getEntityTypeId();
    String entityId = entitiesRequest.getEntityId();
    String fieldId = entitiesRequest.getFieldId();
    Selection filter = entitiesRequest.getFilter();
    Selection expand = entitiesRequest.getExpand();
    int size = entitiesRequest.getSize();
    int page = entitiesRequest.getPage();
    Sort sort = entitiesRequest.getSort();

    Entities entities =
        dataServiceV3.findSubresources(
            entityTypeId,
            entityId,
            fieldId,
            entitiesRequest.getQ().orElse(null),
            filter,
            expand,
            sort,
            size,
            page);

    EntityCollection entityCollection =
        EntityCollection.builder()
            .setEntityTypeId(entityTypeId)
            .setEntities(entities.getEntities())
            .build();

    return entityMapper.map(entityCollection, filter, expand, size, page, entities.getTotal());
  }

  @PutMapping("/{entityTypeId}/{entityId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void updateEntity(
      @PathVariable("entityTypeId") String entityTypeId,
      @PathVariable("entityId") String entityId,
      @RequestBody Map<String, Object> entityMap) {
    dataServiceV3.update(entityTypeId, entityId, entityMap);
  }

  @PatchMapping("/{entityTypeId}/{entityId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void updatePartialEntity(
      @PathVariable("entityTypeId") String entityTypeId,
      @PathVariable("entityId") String entityId,
      @RequestBody Map<String, Object> entityMap) {
    dataServiceV3.updatePartial(entityTypeId, entityId, entityMap);
  }

  @DeleteMapping("/{entityTypeId}/{entityId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteEntity(@Valid DeleteEntityRequest deleteRequest) {
    dataServiceV3.delete(deleteRequest.getEntityTypeId(), deleteRequest.getEntityId());
  }

  @DeleteMapping("/{entityTypeId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteEntities(@Valid DeleteEntitiesRequest deleteRequest) {
    Query query = deleteRequest.getQ().orElse(null);
    dataServiceV3.deleteAll(deleteRequest.getEntityTypeId(), query);
  }

  @GetMapping("/{entityTypeId}")
  public EntitiesResponse getEntities(@Valid ReadEntitiesRequest entitiesRequest) {
    String entityTypeId = entitiesRequest.getEntityTypeId();
    Selection filter = entitiesRequest.getFilter();
    Selection expand = entitiesRequest.getExpand();
    int size = entitiesRequest.getSize();
    int page = entitiesRequest.getPage();
    Sort sort = entitiesRequest.getSort();

    Entities entities =
        dataServiceV3.findAll(
            entityTypeId, entitiesRequest.getQ().orElse(null), filter, expand, sort, size, page);

    EntityCollection entityCollection =
        EntityCollection.builder()
            .setEntityTypeId(entityTypeId)
            .setEntities(entities.getEntities())
            .setPage(
                Page.builder()
                    .setOffset(size * page)
                    .setPageSize(size)
                    .setTotal(entities.getTotal())
                    .build())
            .build();

    return entityMapper.map(entityCollection, filter, expand, size, page, entities.getTotal());
  }
}
