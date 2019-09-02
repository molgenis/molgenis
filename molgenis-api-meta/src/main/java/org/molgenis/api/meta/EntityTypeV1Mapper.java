package org.molgenis.api.meta;

import static java.util.Objects.requireNonNull;
import static org.molgenis.api.data.v3.EntityController.API_ENTITY_PATH;
import static org.molgenis.data.meta.model.TagMetadata.TAG;
import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentRequestUri;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.molgenis.api.meta.model.AttributeResponse;
import org.molgenis.api.meta.model.EntityTypeResponse;
import org.molgenis.api.meta.model.EntityTypesResponse;
import org.molgenis.api.model.Selection;
import org.molgenis.api.model.response.LinksResponse;
import org.molgenis.api.model.response.PageResponse;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeMetadata;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.model.Tag;
import org.molgenis.data.util.EntityTypeUtils;
import org.molgenis.web.support.ServletUriComponentsBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class EntityTypeV1Mapper {
  public static final String ATTRIBUTES = "attributes";
  public static final String PAGE = "page";
  public final AttributeMetadata attributeMetadata;
  public final EntityTypeMetadata entityTypeMetadata;

  public EntityTypeV1Mapper(
      AttributeMetadata attributeMetadata, EntityTypeMetadata entityTypeMetadata) {
    this.attributeMetadata = requireNonNull(attributeMetadata);
    this.entityTypeMetadata = requireNonNull(entityTypeMetadata);
  }

  public EntityTypesResponse map(
      EntityTypes entityTypes,
      Selection filter,
      Selection expand,
      int size,
      int number,
      int total) {
    List<EntityTypeResponse> results = new ArrayList<>();
    for (EntityType entityType : entityTypes.getEntityTypes()) {
      results.add(mapInternal(entityType, filter, expand));
    }

    return EntityTypesResponse.create(
        createLinksResponse(number, size, total),
        results,
        PageResponse.create(size, entityTypes.getTotal(), entityTypes.getTotal() / size, number));
  }

  private EntityTypeResponse mapInternal(
      EntityType entityType, Selection filter, Selection expand) {
    Map<String, Object> entityTypeMap = new HashMap<>();
    for (String attr : entityTypeMetadata.getAttributeNames()) {
      switch (attr) {
        case EntityTypeMetadata.ATTRIBUTES:
          setValue(
              entityTypeMap,
              ATTRIBUTES,
              mapAttributes(
                  entityType.getAllAttributes(),
                  filter.getSelection(ATTRIBUTES).orElse(Selection.EMPTY_SELECTION),
                  expand.hasItem(ATTRIBUTES)),
              filter);
          break;
        case EntityTypeMetadata.EXTENDS:
          setValue(
              entityTypeMap,
              EntityTypeMetadata.EXTENDS,
              entityType.getExtends() != null
                  ? mapInternal(entityType.getExtends(), filter, expand)
                  : null,
              filter);
          break;
        case EntityTypeMetadata.PACKAGE:
          Package pack = entityType.getPackage();
          URI packageURI = pack != null ? createEntityResponseUri(pack):null;
          setValue(
              entityTypeMap,
              EntityTypeMetadata.PACKAGE,
              LinksResponse.create(null, packageURI, null),
              filter);
          break;
        case EntityTypeMetadata.TAGS:
          setValue(
              entityTypeMap,
              EntityTypeMetadata.TAGS,
              StreamSupport.stream(entityType.getTags().spliterator(), false)
                  .map(this::mapTag)
                  .collect(Collectors.toList()),
              filter);
          break;
        default:
          setValue(entityTypeMap, attr, entityType.get(attr), filter);
      }
    }

    return EntityTypeResponse.create(entityType.getId(), createLinksResponse(), entityTypeMap);
  }

  private void setValue(Map<String, Object> valueMap, String attr, Object value, Selection filter) {
    if (filter.hasItem(attr) || !filter.hasItems()) {
      valueMap.put(attr, value);
    }
  }

  private LinksResponse createLinksResponse() {
    URI self = createEntitiesResponseUri();
    return LinksResponse.create(null, self, null);
  }

  private List<AttributeResponse> mapAttributes(
      Iterable<org.molgenis.data.meta.model.Attribute> allAttributes,
      Selection filter,
      boolean expand) {
    List<AttributeResponse> result = new ArrayList<>();
    for (Attribute attr : allAttributes) {
      Map<String, Object> attrMap = null;
      if (expand) {
        attrMap = getFilteredAttributeMap(filter, attr);
      }
      result.add(
          AttributeResponse.create(
              LinksResponse.create(null, createAttributeResponseUri(attr), null),
              new HashMap<>(attrMap)));
    }
    return result;
  }

  private Map<String, Object> getFilteredAttributeMap(Selection filter, Attribute attr) {
    Map<String, Object> attrMap;
    attrMap = new HashMap<>();
    for (String attrAttr : attributeMetadata.getAttributeNames()) {
      switch (attrAttr) {
        case AttributeMetadata.REF_ENTITY_TYPE:
          if (EntityTypeUtils.isReferenceType(attr)) {
            setValue(
                attrMap,
                AttributeMetadata.REF_ENTITY_TYPE,
                createEntityResponseUri(attr.getRefEntity()),
                filter);
          }
          break;
        case AttributeMetadata.PARENT:
          Attribute parent = attr.getParent();
          URI parentURI = parent != null ? createAttributeResponseUri(parent) : null;
          setValue(
              attrMap,
              AttributeMetadata.PARENT,
              parentURI,
              filter);
          break;
        case AttributeMetadata.CHILDREN:
          setValue(
              attrMap,
              AttributeMetadata.CHILDREN,
              StreamSupport.stream(attr.getChildren().spliterator(), false)
                  .map(this::createEntityResponseUri)
                  .collect(Collectors.toList()),
              filter);
          break;
        case AttributeMetadata.TAGS:
          setValue(
              attrMap,
              AttributeMetadata.TAGS,
              StreamSupport.stream(attr.getTags().spliterator(), false)
                  .map(this::createEntityResponseUri)
                  .collect(Collectors.toList()),
              filter);
          break;
        case AttributeMetadata.ENTITY:
          break;
        default:
          setValue(attrMap, attrAttr, attr.get(attrAttr), filter);
      }
    }
    return attrMap;
  }

  private LinksResponse createLinksResponse(int number, int size, int total) {
    URI self = createEntitiesResponseUri(number);
    URI previous = null;
    URI next = null;
    if (number > 0) {
      previous = createEntitiesResponseUri(number - 1);
    }
    if ((number * size) + size < total) {
      next = createEntitiesResponseUri(number + 1);
    }
    return LinksResponse.create(previous, self, next);
  }

  private LinksResponse mapTag(Tag tag) {
    return LinksResponse.create(null, createEntityResponseUri(tag), null);
  }

  private URI createEntitiesResponseUri() {
    UriComponentsBuilder builder = ServletUriComponentsBuilder.fromCurrentRequestDecodedQuery();
    return builder.build().toUri();
  }

  private URI createEntitiesResponseUri(Integer pageNumber) {
    UriComponentsBuilder builder = ServletUriComponentsBuilder.fromCurrentRequestDecodedQuery();
    if (pageNumber != null) {
      builder.replaceQueryParam(PAGE, pageNumber);
    }
    return builder.build().toUri();
  }

  private URI createEntityResponseUri(Entity entity) {
    UriComponentsBuilder uriComponentsBuilder =
        fromCurrentRequestUri()
            .replacePath(null)
            .path(API_ENTITY_PATH)
            .pathSegment(TAG)
            .pathSegment(entity.getIdValue().toString());
    return uriComponentsBuilder.build().toUri();
  }

  private URI createAttributeResponseUri(Attribute attr) {
    UriComponentsBuilder uriComponentsBuilder =
        fromCurrentRequestUri()
            .replacePath(null)
            .path(MetaApiController.API_META_PATH)
            .pathSegment(attr.getEntity().getId())
            .pathSegment(ATTRIBUTES)
            .pathSegment(attr.getIdentifier());
    return uriComponentsBuilder.build().toUri();
  }

  public EntityTypeResponse map(EntityType entityType, Selection filter, Selection expand) {
    return mapInternal(entityType, filter, expand);
  }
}
