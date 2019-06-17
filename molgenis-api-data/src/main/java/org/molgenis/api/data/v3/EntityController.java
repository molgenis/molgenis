package org.molgenis.api.data.v3;

import static com.google.common.collect.Streams.stream;
import static java.util.Collections.singletonMap;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import org.molgenis.api.ApiController;
import org.molgenis.api.ApiNamespace;
import org.molgenis.api.model.response.LinksResponse;
import org.molgenis.api.model.response.PageResponse;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.Sort;
import org.molgenis.data.Sort.Direction;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.util.UnexpectedEnumException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

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
  // FIXME use fetch
  @GetMapping("/{entityTypeId}/{entityId}")
  EntityResponse getEntity(@Valid EntityRequest entityRequest) {
    String entityTypeId = entityRequest.getEntityTypeId();
    String entityId = entityRequest.getEntityId();

    Entity entity = dataService.findOneById(entityTypeId, entityId);
    if (entity == null) {
      throw new UnknownEntityException(entityTypeId, entityId);
    }

    Map<String, Selection> filterMap;
    if (entityRequest.getFilter() != null && !entityRequest.getFilter().isEmpty()) {
      filterMap = new HashMap<>();
      entityRequest.getFilter().forEach(key -> filterMap.put(key, Selection.FULL_SELECTION));
    } else {
      filterMap = null;
    }
    Map<String, Selection> expandMap;
    if (entityRequest.getExpand() != null && !entityRequest.getExpand().isEmpty()) {
      expandMap = new HashMap<>();
      entityRequest.getExpand().forEach(key -> expandMap.put(key, Selection.EMPTY_SELECTION));
    } else {
      expandMap = null;
    }
    return entityMapper.map(
        entity,
        filterMap != null ? new Selection(filterMap) : Selection.FULL_SELECTION,
        expandMap != null ? new Selection(expandMap) : Selection.EMPTY_SELECTION);
  }

  // FIXME convert entityId to correct type
  // FIXME use fetch
  @GetMapping("/{entityTypeId}/{entityId}/{attributeName}")
  EntitiesResponse getEntitySubEntities(
      @PathVariable(value = "entityTypeId") String entityTypeId,
      @PathVariable(value = "entityId") String entityId,
      @PathVariable(value = "attributeName") String attributeName,
      @RequestParam(value = "number", defaultValue = "0") @Min(0) int number,
      @RequestParam(value = "size", defaultValue = "100") @Size(max = 100) int size,
      @RequestParam(value = "filter", required = false) List<String> filterAttributeNames,
      @RequestParam(value = "expand", required = false) List<String> expandAttributeNames) {
    Entity entity = dataService.findOneById(entityTypeId, entityId);
    if (entity == null) {
      throw new UnknownEntityException(entityTypeId, entityId);
    }

    Set<String> filterAttributes =
        filterAttributeNames != null
            ? ImmutableSet.copyOf(filterAttributeNames)
            : ImmutableSet.of();
    Set<String> expandAttributes =
        expandAttributeNames != null
            ? ImmutableSet.copyOf(expandAttributeNames)
            : ImmutableSet.of();
    List<EntityResponse> items =
        stream(entity.getEntities(attributeName))
            .skip(number)
            .limit(size)
            .map(subEntity -> toEntityResponse(subEntity, filterAttributes, expandAttributes))
            .collect(toList());

    URI uri = ServletUriComponentsBuilder.fromCurrentRequestUri().build().toUri();
    LinksResponse linksResponse = LinksResponse.create(null, uri, null);

    PageResponse pageResponse = PageResponse.create(size, 100, 1, number);
    return EntitiesResponse.builder()
        .setLinks(linksResponse)
        .setItems(items)
        .setPage(pageResponse)
        .build();
  }

  @GetMapping("/{entityTypeId}")
  EntitiesResponse getEntities(@Valid EntitiesRequest entitiesRequest) {
    Query<Entity> query =
        dataService
            .query(entitiesRequest.getEntityTypeId())
            .offset(entitiesRequest.getNumber() * entitiesRequest.getSize())
            .pageSize(entitiesRequest.getSize());
    String q = entitiesRequest.getQ();
    if (q != null) {
      if (!entitiesRequest.getFilter().isEmpty()) {
        entitiesRequest.getFilter().forEach(item -> query.search(item, q).or());
      } else {
        query.search(q);
      }
    }

    List<String> sortList = entitiesRequest.getSort();
    if (!sortList.isEmpty()) {
      Sort sort = new Sort();
      sortList.forEach(
          sortItem -> {
            if (sortItem.charAt(0) == '+') {
              sort.on(sortItem.substring(1), Direction.ASC);
            } else if (sortItem.charAt(0) == '-') {
              sort.on(sortItem.substring(1), Direction.DESC);
            } else {
              sort.on(sortItem);
            }
          });
      query.sort(sort);
    }
    List<EntityResponse> items =
        query
            .findAll()
            .map(
                entity ->
                    toEntityResponse(
                        entity, entitiesRequest.getFilter(), entitiesRequest.getExpand()))
            .collect(toList());

    URI uri = ServletUriComponentsBuilder.fromCurrentRequestUri().build().toUri();
    LinksResponse linksResponse = LinksResponse.create(null, uri, null);

    int totalElements = (int) dataService.count(entitiesRequest.getEntityTypeId());
    int totalPages =
        entitiesRequest.getSize() > 0
            ? (int) Math.ceil(totalElements / (double) entitiesRequest.getSize())
            : 0;
    PageResponse pageResponse =
        PageResponse.create(
            entitiesRequest.getSize(), totalElements, totalPages, entitiesRequest.getNumber());
    return EntitiesResponse.builder()
        .setLinks(linksResponse)
        .setItems(items)
        .setPage(pageResponse)
        .build();
  }

  private EntityResponse toEntityResponse(
      Entity entity, Set<String> filterAttributeNames, Set<String> expandAttributeNames) {
    URI uri =
        ServletUriComponentsBuilder.fromCurrentRequestUri()
            .replacePath(API_ENTITY_PATH)
            .replaceQuery(null)
            .fragment(null)
            .pathSegment(entity.getEntityType().getId(), entity.getIdValue().toString())
            .build()
            .toUri();
    Map<String, Object> entityData = map(entity, filterAttributeNames, expandAttributeNames);

    return EntityResponse.builder()
        .setLinks(LinksResponse.create(null, uri, null))
        .setData(entityData)
        .build();
  }

  private Map<String, Object> map(
      Entity entity, Set<String> filterAttributeNames, Set<String> expandAttributeNames) {
    Map<String, Object> entityData = new LinkedHashMap<>();

    stream(entity.getEntityType().getAtomicAttributes())
        .filter(
            attribute ->
                filterAttributeNames.isEmpty()
                    || filterAttributeNames.contains(attribute.getName()))
        .forEach(
            attribute -> {
              String attributeName = attribute.getName();
              Object value;

              AttributeType attributeType = attribute.getDataType();
              switch (attributeType) {
                case BOOL:
                  value = entity.getBoolean(attributeName);
                  break;
                case CATEGORICAL:
                case FILE:
                case XREF:
                  Entity xrefEntity = entity.getEntity(attributeName);
                  if (xrefEntity != null) {
                    if (expandAttributeNames.contains(attributeName)) {
                      value = toEntityResponse(xrefEntity, ImmutableSet.of(), ImmutableSet.of());
                    } else {
                      value = mapReference(xrefEntity);
                    }
                  } else {
                    value = null;
                  }
                  break;
                case CATEGORICAL_MREF:
                case MREF:
                case ONE_TO_MANY:
                  if (expandAttributeNames.contains(attributeName)) {
                    value =
                        getEntitySubEntities(
                            entity.getEntityType().getId(),
                            entity.getIdValue().toString(),
                            attributeName,
                            0,
                            100,
                            ImmutableList.of(),
                            ImmutableList.of());
                  } else {
                    value = mapCollectionReference(entity, attribute);
                  }
                  break;
                case DATE:
                  value = entity.getLocalDate(attributeName);
                  break;
                case DATE_TIME:
                  value = entity.getInstant(attributeName);
                  break;
                case DECIMAL:
                  value = entity.getDouble(attributeName);
                  break;
                case EMAIL:
                case ENUM:
                case HTML:
                case HYPERLINK:
                case SCRIPT:
                case STRING:
                case TEXT:
                  value = entity.getString(attributeName);
                  break;
                case INT:
                  value = entity.getInt(attributeName);
                  break;
                case LONG:
                  value = entity.getLong(attributeName);
                  break;
                case COMPOUND:
                default:
                  throw new UnexpectedEnumException(attributeType);
              }

              entityData.put(attributeName, value);
            });
    return entityData;
  }

  private Map<String, Object> mapReference(Entity entity) {
    URI referencedUri =
        ServletUriComponentsBuilder.fromCurrentRequestUri()
            .replacePath(API_ENTITY_PATH)
            .pathSegment(entity.getEntityType().getId())
            .pathSegment(entity.getIdValue().toString())
            .replaceQuery(null)
            .build()
            .toUri();
    return singletonMap("href", referencedUri);
  }

  private Map<String, Object> mapCollectionReference(Entity entity, Attribute attribute) {
    URI referencedUri =
        ServletUriComponentsBuilder.fromCurrentRequestUri()
            .replacePath(API_ENTITY_PATH)
            .pathSegment(
                entity.getEntityType().getId(), entity.getIdValue().toString(), attribute.getName())
            .replaceQuery(null)
            .build()
            .toUri();
    return singletonMap("href", referencedUri);
  }
}
