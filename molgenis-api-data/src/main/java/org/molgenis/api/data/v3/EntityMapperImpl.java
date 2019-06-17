package org.molgenis.api.data.v3;

import static com.google.common.collect.Streams.stream;
import static java.util.stream.Collectors.toList;

import com.google.common.collect.Iterables;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.api.data.v3.EntityResponse.Builder;
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

  @Override
  public EntityResponse map(Entity entity, Selection filter, Selection expand) {
    Builder builder = EntityResponse.builder();

    if (filter.hasItems()) {
      Map<String, Object> data = createEntityResponseData(entity, filter, expand);
      builder.setItem(data);
    }

    URI uri = createEntityResponseUri(entity);
    return builder.setLinks(LinksResponse.create(null, uri, null)).build();
  }

  private Map<String, Object> createEntityResponseData(
      Entity entity, Selection filter, Selection expand) {
    Map<String, Object> dataMap = new LinkedHashMap<>();

    stream(entity.getEntityType().getAtomicAttributes())
        .filter(attribute -> filter.hasItem(attribute.getName()))
        .forEach(
            attribute -> dataMap.put(attribute.getName(), map(entity, attribute, filter, expand)));

    return dataMap;
  }

  private Object map(Entity entity, Attribute attribute, Selection filter, Selection expand) {
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
        value = mapReference(entity, attributeName, filter, expand);
        break;
      case CATEGORICAL_MREF:
      case MREF:
      case ONE_TO_MANY:
        value = mapReferences(entity, attributeName, filter, expand);
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

  private EntityResponse mapReference(
      Entity entity, String attributeName, Selection filter, Selection expand) {
    Entity refEntity = entity.getEntity(attributeName);
    if (refEntity == null) {
      return null;
    }

    Selection refFilter;
    Selection refExpand;
    if (expand.hasItem(attributeName)) {
      refFilter = filter.getSelection(attributeName);
      refExpand = expand.getSelection(attributeName);
    } else {
      refFilter = Selection.EMPTY_SELECTION;
      refExpand = Selection.EMPTY_SELECTION;
    }

    return map(refEntity, refFilter, refExpand);
  }

  private Object mapReferences(
      Entity entity, String attributeName, Selection filter, Selection expand) {
    if (expand.hasItem(attributeName)) {

      Selection refFilter = filter.getSelection(attributeName);
      Selection refExpand = expand.getSelection(attributeName);

      Iterable<Entity> refEntities = entity.getEntities(attributeName);
      List<EntityResponse> items =
          stream(refEntities)
              .skip(0)
              .limit(20)
              .map(refEntity -> map(refEntity, refFilter, refExpand))
              .collect(toList());
      int totalElements = Iterables.size(refEntities);
      int totalPages = totalElements > 0 ? (int) Math.ceil(totalElements / (double) 20) : 0;

      LinksResponse links =
          LinksResponse.create(null, createEntityResponseUri(entity, attributeName), null);
      PageResponse page = PageResponse.create(items.size(), totalElements, totalPages, 0);
      return EntitiesResponse.create(links, items, page);

    } else {
      URI uri = createEntityResponseUri(entity, attributeName);
      return EntitiesResponse.builder().setLinks(LinksResponse.create(null, uri, null)).build();
    }
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
