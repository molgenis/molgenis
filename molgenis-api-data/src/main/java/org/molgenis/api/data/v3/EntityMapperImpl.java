package org.molgenis.api.data.v3;

import static com.google.common.collect.Streams.stream;
import static java.util.stream.Collectors.toList;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.api.data.v3.EntityCollection.Page;
import org.molgenis.api.model.response.LinksResponse;
import org.molgenis.api.model.response.PageResponse;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.IllegalAttributeTypeException;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.util.UnexpectedEnumException;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class EntityMapperImpl implements EntityMapper {
  private static final int MAX_DEPTH = 2;

  @Override
  public EntityResponse map(Entity entity, Selection filter, Selection expand) {
    return mapRecursive(entity, filter, expand, 0);
  }

  @Override
  public EntitiesResponse map(
      EntityCollection entityCollection, Selection filter, Selection expand) {
    EntitiesResponse.Builder builder = mapRecursive(entityCollection, filter, expand, 0);

    URI self = createEntitiesResponseUri(entityCollection.getEntityTypeId());
    LinksResponse linksResponse = LinksResponse.create(null, self, null);

    Page page = entityCollection.getPage();
    if (page != null) {
      PageResponse pageResponse =
          PageResponse.create(
              entityCollection.getSize(),
              page.getTotal(),
              page.getTotal() > 0
                  ? (int) Math.ceil(page.getTotal() / (double) page.getPageSize())
                  : 0,
              page.getOffset() / page.getPageSize());
      builder.setPage(pageResponse);
    }

    return builder.setLinks(linksResponse).build();
  }

  private EntityResponse mapRecursive(
      Entity entity, Selection filter, Selection expand, int depth) {
    if (depth > MAX_DEPTH) {
      throw new IllegalArgumentException("max_depth exceeded: " + depth);
    }
    EntityResponse.Builder builder = EntityResponse.builder();

    if (filter.hasItems()) {
      Map<String, Object> dataMap = new LinkedHashMap<>();
      stream(entity.getEntityType().getAtomicAttributes())
          .filter(attribute -> filter.hasItem(attribute.getName()))
          .forEach(
              attribute ->
                  dataMap.put(
                      attribute.getName(), mapRecursive(entity, attribute, filter, expand, depth)));

      builder.setData(dataMap);
    }

    URI uri = createEntityResponseUri(entity);
    return builder.setLinks(LinksResponse.create(null, uri, null)).build();
  }

  private EntitiesResponse.Builder mapRecursive(
      EntityCollection entityCollection, Selection filter, Selection expand, int depth) {
    if (depth > MAX_DEPTH) {
      throw new IllegalArgumentException("max_depth exceeded: " + depth);
    }
    EntitiesResponse.Builder builder = EntitiesResponse.builder();

    if (filter.hasItems()) {
      List<EntityResponse> entityResponses =
          entityCollection.getEntities().stream()
              .map(entity -> mapRecursive(entity, filter, expand, depth))
              .collect(toList());
      builder.setItems(entityResponses);
    }

    return builder;
  }

  private Object mapRecursive(
      Entity entity, Attribute attribute, Selection filter, Selection expand, int depth) {
    Object value;

    String attributeName = attribute.getName();
    AttributeType attributeType = attribute.getDataType();
    switch (attributeType) {
      case BOOL:
        value = entity.getBoolean(attributeName);
        break;
      case CATEGORICAL:
      case FILE:
      case XREF:
        value = mapReference(entity, attribute, filter, expand, depth + 1);
        break;
      case CATEGORICAL_MREF:
      case MREF:
      case ONE_TO_MANY:
        value = mapReferences(entity, attribute, filter, expand, depth + 1);
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
        throw new IllegalAttributeTypeException(attributeType);
      default:
        throw new UnexpectedEnumException(attributeType);
    }
    return value;
  }

  private @Nullable @CheckForNull EntityResponse mapReference(
      Entity entity, Attribute attribute, Selection filter, Selection expand, int depth) {
    Entity refEntity = entity.getEntity(attribute.getName());
    if (refEntity == null) {
      // note that returning an empty EntityResponse with a link and no data would not make sense,
      // since the link would results in a 404 when requested.
      return null;
    }

    Selection refFilter = getReferenceFilter(attribute, filter, expand);
    Selection refExpand = getReferenceExpand(attribute, expand);
    return mapRecursive(refEntity, refFilter, refExpand, depth);
  }

  private EntitiesResponse mapReferences(
      Entity entity, Attribute attribute, Selection filter, Selection expand, int depth) {
    URI uri = createEntityResponseUri(entity, attribute.getName());
    if (expand.hasItem(attribute.getName())) {
      String refEntityTypeId = attribute.getRefEntity().getId();
      List<Entity> refEntities = stream(entity.getEntities(attribute.getName())).collect(toList());

      EntityCollection entityCollection =
          EntityCollection.builder()
              .setEntityTypeId(refEntityTypeId)
              .setEntities(refEntities)
              .setEntityId(entity.getIdValue().toString())
              .build();

      Selection refFilter = getReferenceFilter(attribute, filter, expand);
      Selection refExpand = getReferenceExpand(attribute, expand);
      return mapRecursive(entityCollection, refFilter, refExpand, depth)
          .setLinks(LinksResponse.create(null, uri, null))
          .build();
    } else {

      return EntitiesResponse.builder().setLinks(LinksResponse.create(null, uri, null)).build();
    }
  }

  private Selection getReferenceFilter(Attribute attribute, Selection filter, Selection expand) {
    return expand.hasItem(attribute.getName())
        ? filter.getSelection(attribute.getName()).orElse(Selection.FULL_SELECTION)
        : Selection.EMPTY_SELECTION;
  }

  private Selection getReferenceExpand(Attribute attribute, Selection expand) {
    return expand.hasItem(attribute.getName())
        ? expand.getSelection(attribute.getName()).orElse(Selection.EMPTY_SELECTION)
        : Selection.EMPTY_SELECTION;
  }

  private URI createEntitiesResponseUri(String entityTypeId) {
    return ServletUriComponentsBuilder.fromCurrentRequestUri()
        .replacePath(null)
        .path(EntityController.API_ENTITY_PATH)
        .pathSegment(entityTypeId)
        .build()
        .toUri();
  }

  private URI createEntityResponseUri(Entity entity) {
    return createEntityResponseUri(entity, null);
  }

  private URI createEntityResponseUri(Entity entity, @Nullable @CheckForNull String attributeName) {
    UriComponentsBuilder uriComponentsBuilder =
        ServletUriComponentsBuilder.fromCurrentRequestUri()
            .replacePath(null)
            .path(EntityController.API_ENTITY_PATH)
            .pathSegment(entity.getEntityType().getId(), entity.getIdValue().toString());
    if (attributeName != null) {
      uriComponentsBuilder.pathSegment(attributeName);
    }
    return uriComponentsBuilder.build().toUri();
  }
}
