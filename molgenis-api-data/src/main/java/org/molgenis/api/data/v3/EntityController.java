package org.molgenis.api.data.v3;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.api.data.v3.SortV3Mapper.map;

import java.util.List;
import javax.validation.Valid;
import org.molgenis.api.ApiController;
import org.molgenis.api.ApiNamespace;
import org.molgenis.api.data.v3.EntityCollection.Page;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.UnknownEntityException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(EntityController.API_ENTITY_PATH)
class EntityController extends ApiController {
  private static final String API_ENTITY_ID = "entity";
  static final String API_ENTITY_PATH = ApiNamespace.API_PATH + '/' + API_ENTITY_ID;

  private final DataService dataService;
  private final EntityMapper entityMapper;

  EntityController(DataService dataService, EntityMapper entityMapper) {
    super(API_ENTITY_ID, 3);
    this.dataService = requireNonNull(dataService);
    this.entityMapper = requireNonNull(entityMapper);
  }

  // FIXME convert entityId to correct type
  // TODO use fetch in dataservice call
  @GetMapping("/{entityTypeId}/{entityId}")
  EntityResponse getEntity(@Valid EntityRequest entityRequest) {
    String entityTypeId = entityRequest.getEntityTypeId();
    String entityId = entityRequest.getEntityId();

    Entity entity = dataService.findOneById(entityTypeId, entityId);
    if (entity == null) {
      throw new UnknownEntityException(entityTypeId, entityId);
    }

    Selection filter = entityRequest.getFilter();
    Selection expand = entityRequest.getExpand();
    return entityMapper.map(entity, filter, expand);
  }

  // TODO use fetch in dataservice call
  @GetMapping("/{entityTypeId}")
  EntitiesResponse getEntities(@Valid EntitiesRequest entitiesRequest) {
    EntityCollection entityCollection = createEntityCollection(entitiesRequest);

    Selection filter = entitiesRequest.getFilter();
    Selection expand = entitiesRequest.getExpand();
    return entityMapper.map(entityCollection, filter, expand);
  }

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

  private Query<Entity> createQuery(EntitiesRequest entitiesRequest) {
    Query<Entity> query =
        dataService
            .query(entitiesRequest.getEntityTypeId())
            .offset(entitiesRequest.getNumber() * entitiesRequest.getSize())
            .pageSize(entitiesRequest.getSize());

    entitiesRequest.getQ().ifPresent(query::search);
    entitiesRequest.getSort().ifPresent(sort -> query.sort(map(sort)));

    return query;
  }
}
