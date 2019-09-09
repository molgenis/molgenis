package org.molgenis.api.meta;

import static java.util.Objects.requireNonNull;
import static org.molgenis.api.data.v3.EntityController.API_ENTITY_PATH;
import static org.molgenis.data.meta.model.TagMetadata.TAG;
import static org.molgenis.util.i18n.LanguageService.getLanguageCodes;
import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentRequestUri;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.molgenis.api.meta.model.Attribute.Builder;
import org.molgenis.api.meta.model.AttributeResponse;
import org.molgenis.api.meta.model.AttributesResponse;
import org.molgenis.api.meta.model.EntityTypeResponse;
import org.molgenis.api.meta.model.EntityTypesResponse;
import org.molgenis.api.meta.model.I18nResponse;
import org.molgenis.api.model.response.LinksResponse;
import org.molgenis.api.model.response.PageResponse;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeMetadata;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeMetadata;
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

  public EntityTypesResponse map(EntityTypes entityTypes, int size, int number, int total) {
    List<EntityTypeResponse> results = new ArrayList<>();
    for (EntityType entityType : entityTypes.getEntityTypes()) {
      results.add(mapInternal(entityType));
    }

    return EntityTypesResponse.create(
        createLinksResponse(number, size, total),
        results,
        PageResponse.create(size, entityTypes.getTotal(), entityTypes.getTotal() / size, number));
  }

  private EntityTypeResponse mapInternal(EntityType entityType) {
    org.molgenis.api.meta.model.EntityType.Builder builder =
        org.molgenis.api.meta.model.EntityType.builder();
    builder.setId(entityType.getId());
    builder.setPackage_(
        entityType.getPackage() != null ? createEntityResponseUri(entityType.getPackage()) : null);
    builder.setLabel(getI18nEntityTypeLabel(entityType));
    builder.setDescription(getI18nEntityTypeDesc(entityType));
    builder.setAttributes(mapInternal(entityType.getOwnAtomicAttributes()));
    builder.setLabelAttribute(getLabelAttribute(entityType));
    builder.setIdAttribute(getIdAttribute(entityType));
    builder.setAbstract_(entityType.isAbstract());
    builder.setExtends(
        entityType.getExtends() != null ? mapInternal(entityType.getExtends()) : null);
    builder.setBackend(entityType.getBackend());
    builder.setIndexingDepth(entityType.getIndexingDepth());

    return EntityTypeResponse.create(entityType.getId(), createLinksResponse(), builder.build());
  }

  private String getIdAttribute(EntityType entityType) {
    for (Attribute attribute : entityType.getOwnAllAttributes()) {
      if (attribute.isIdAttribute()) {
        return attribute.getIdValue().toString();
      }
    }
    return null;
  }

  private String getLabelAttribute(EntityType entityType) {
    for (Attribute attribute : entityType.getOwnAllAttributes()) {
      if (attribute.isLabelAttribute()) {
        return attribute.getIdValue().toString();
      }
    }
    return null;
  }

  private LinksResponse createLinksResponse() {
    URI self = createEntitiesResponseUri();
    return LinksResponse.create(null, self, null);
  }

  AttributesResponse mapAttributes(Attributes attributes, int size, int number, int total) {
    return AttributesResponse.create(
        createLinksResponse(number, size, total),
        attributes.getAttributes().stream()
            .map(attr -> mapAttribute(attr))
            .collect(Collectors.toList()),
        PageResponse.create(size, attributes.getTotal(), attributes.getTotal() / size, number));
  }

  private List<AttributeResponse> mapInternal(
      Iterable<org.molgenis.data.meta.model.Attribute> allAttributes) {
    List<AttributeResponse> result = new ArrayList<>();
    for (Attribute attr : allAttributes) {
      result.add(mapAttribute(attr));
    }
    return result;
  }

  AttributeResponse mapAttribute(Attribute attr) {
    org.molgenis.api.meta.model.Attribute attribute = mapInternal(attr);
    return AttributeResponse.create(
        LinksResponse.create(null, createAttributeResponseUri(attr), null), attribute);
  }

  private org.molgenis.api.meta.model.Attribute mapInternal(Attribute attr) {
    Builder builder = org.molgenis.api.meta.model.Attribute.builder();
    builder.setId(attr.getIdentifier());
    builder.setName(attr.getName());
    builder.setSequenceNr(attr.getSequenceNumber());
    builder.setType(attr.getDataType());
    builder.setLookupAttributeIndex(attr.getLookupAttributeIndex());
    if (EntityTypeUtils.isReferenceType(attr)) {
      builder.setRefEntityType(map(attr.getRefEntity()));
    }
    builder.setCascadeDelete(attr.getCascadeDelete());
    builder.setMappedBy(attr.getMappedBy() != null ? mapAttribute(attr.getMappedBy()) : null);
    builder.setOrderBy(attr.getOrderBy());
    builder.setLabel(getI18nAttrLabel(attr));
    builder.setDescription(getI18nAttrDesc(attr));
    builder.setNullable(attr.isNillable());
    builder.setAuto(attr.isAuto());
    builder.setVisible(attr.isVisible());
    builder.setUnique(attr.isUnique());
    builder.setReadOnly(attr.isReadOnly());
    builder.setAggregatable(attr.isAggregatable());
    builder.setExpression(attr.getExpression());
    builder.setEnumOptions(attr.getEnumOptions().toString());
    builder.setRangeMin(attr.getRangeMin());
    builder.setRangeMax(attr.getRangeMax());
    builder.setParentAttributeId(
        attr.getParent() != null ? attr.getParent().getIdentifier() : null);
    builder.setNullableExpression(attr.getNullableExpression());
    builder.setVisibleExpression(attr.getVisibleExpression());
    builder.setValidationExpression(attr.getValidationExpression());
    builder.setDefaultValue(attr.getDefaultValue());

    return builder.build();
  }

  private I18nResponse getI18nAttrLabel(Attribute attr) {
    String defaultValue = attr.getLabel();
    Map<String, String> translations = new HashMap<>();
    getLanguageCodes().forEach(code -> translations.put(code, attr.getLabel(code)));
    return I18nResponse.create(defaultValue, translations);
  }

  private I18nResponse getI18nAttrDesc(Attribute attr) {
    String defaultValue = attr.getDescription();
    Map<String, String> translations = new HashMap<>();
    getLanguageCodes().forEach(code -> translations.put(code, attr.getDescription(code)));
    return I18nResponse.create(defaultValue, translations);
  }

  private I18nResponse getI18nEntityTypeLabel(EntityType entityType) {
    String defaultValue = entityType.getLabel();
    Map<String, String> translations = new HashMap<>();
    getLanguageCodes().forEach(code -> translations.put(code, entityType.getLabel(code)));
    return I18nResponse.create(defaultValue, translations);
  }

  private I18nResponse getI18nEntityTypeDesc(EntityType entityType) {
    String defaultValue = entityType.getDescription();
    Map<String, String> translations = new HashMap<>();
    getLanguageCodes().forEach(code -> translations.put(code, entityType.getDescription(code)));
    return I18nResponse.create(defaultValue, translations);
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

  public EntityTypeResponse map(EntityType entityType) {
    return mapInternal(entityType);
  }
}
