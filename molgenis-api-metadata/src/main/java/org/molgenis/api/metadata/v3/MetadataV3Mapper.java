package org.molgenis.api.metadata.v3;

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
import java.util.Optional;
import java.util.stream.Collectors;
import org.molgenis.api.convert.SortConverter;
import org.molgenis.api.metadata.v3.model.Attribute.Builder;
import org.molgenis.api.metadata.v3.model.AttributeResponse;
import org.molgenis.api.metadata.v3.model.AttributesResponse;
import org.molgenis.api.metadata.v3.model.CreateAttributeRequest;
import org.molgenis.api.metadata.v3.model.CreateEntityTypeRequest;
import org.molgenis.api.metadata.v3.model.EntityTypeResponse;
import org.molgenis.api.metadata.v3.model.EntityTypesResponse;
import org.molgenis.api.metadata.v3.model.I18nValue;
import org.molgenis.api.model.response.LinksResponse;
import org.molgenis.api.model.response.PageResponse;
import org.molgenis.data.Entity;
import org.molgenis.data.UnknownPackageException;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.util.EntityTypeUtils;
import org.molgenis.web.support.ServletUriComponentsBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class MetadataV3Mapper {
  public static final String ATTRIBUTES = "attributes";
  public static final String PAGE = "page";
  private final EntityTypeFactory entityTypeFactory;
  private final AttributeFactory attributeFactory;
  private final MetaDataService metaDataService;
  private final SortMapper sortMapper;
  private final SortConverter sortConverter;

  public MetadataV3Mapper(
      EntityTypeFactory entityTypeFactory,
      AttributeFactory attributeFactory,
      MetaDataService metaDataService,
      SortMapper sortMapper,
      SortConverter sortConverter) {
    this.entityTypeFactory = requireNonNull(entityTypeFactory);
    this.attributeFactory = requireNonNull(attributeFactory);
    this.metaDataService = requireNonNull(metaDataService);
    this.sortMapper = requireNonNull(sortMapper);
    this.sortConverter = requireNonNull(sortConverter);
  }

  public EntityTypesResponse toEntityTypeResponse(
      EntityTypes entityTypes, int size, int number, int total) {
    List<EntityTypeResponse> results = new ArrayList<>();
    for (EntityType entityType : entityTypes.getEntityTypes()) {
      results.add(mapInternal(entityType));
    }

    return EntityTypesResponse.create(
        createLinksResponse(number, size, total),
        results,
        PageResponse.create(size, entityTypes.getTotal(), entityTypes.getTotal() / size, number));
  }

  public EntityTypeResponse toEntityTypeResponse(EntityType entityType) {
    return mapInternal(entityType);
  }

  public EntityType toEntityType(CreateEntityTypeRequest entityTypeRequest) {
    if (entityTypeRequest == null) {
      throw new IllegalStateException(); // FIXME
    }
    if ((entityTypeRequest.isAbstract() == null || !entityTypeRequest.isAbstract())
        && entityTypeRequest.getIdAttribute() == null) {
      throw new IllegalArgumentException(
          "ID attribute for EntityType ["
              + entityTypeRequest.getLabel()
              + "] cannot be null"); // FIXME
    }
    EntityType entityType = entityTypeFactory.create();
    entityType.setId(entityTypeRequest.getId());
    Optional<Package> pack = metaDataService.getPackage(entityTypeRequest.getPackage());
    entityType.setPackage(
        pack.orElseThrow(() -> new UnknownPackageException(entityTypeRequest.getPackage())));
    processI18nLabel(entityTypeRequest, entityType);
    processI18nDescription(entityTypeRequest, entityType);
    Map<String, Attribute> ownAttributes =
        toAttributes(entityTypeRequest.getAttributes(), entityTypeRequest, entityType);
    entityType.setOwnAllAttributes(ownAttributes.values());
    entityType.setAbstract(entityTypeRequest.isAbstract());
    Optional<EntityType> extendsEntityType =
        metaDataService.getEntityType(entityTypeRequest.getEntityTypeParent());
    if (extendsEntityType.isPresent()) {
      entityType.setExtends(extendsEntityType.get());
    }
    entityType.setBackend(metaDataService.getDefaultBackend().getName());
    return entityType;
  }

  private EntityTypeResponse mapInternal(EntityType entityType) {
    if (entityType == null) {
      throw new IllegalStateException(); // FIXME
    }
    org.molgenis.api.metadata.v3.model.EntityType.Builder builder =
        org.molgenis.api.metadata.v3.model.EntityType.builder();
    builder.setId(entityType.getId());
    Package pack = entityType.getPackage();
    builder.setPackage_(pack != null ? createEntityResponseUri(pack) : null);
    builder.setLabel(getI18nEntityTypeLabel(entityType));
    builder.setDescription(getI18nEntityTypeDesc(entityType));
    builder.setAttributes(mapInternal(entityType.getOwnAtomicAttributes()));
    builder.setLabelAttribute(getLabelAttribute(entityType));
    builder.setIdAttribute(getIdAttribute(entityType));
    builder.setAbstract_(entityType.isAbstract());
    EntityType parent = entityType.getExtends();
    builder.setExtends(parent != null ? mapInternal(parent) : null);
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
        attributes.getAttributes().stream().map(this::mapAttribute).collect(Collectors.toList()),
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
    org.molgenis.api.metadata.v3.model.Attribute attribute = mapInternal(attr);
    return AttributeResponse.create(
        LinksResponse.create(null, createAttributeResponseUri(attr), null), attribute);
  }

  private org.molgenis.api.metadata.v3.model.Attribute mapInternal(Attribute attr) {
    Builder builder = org.molgenis.api.metadata.v3.model.Attribute.builder();
    builder.setId(attr.getIdentifier());
    builder.setName(attr.getName());
    builder.setSequenceNr(attr.getSequenceNumber());
    builder.setType(attr.getDataType());
    builder.setLookupAttributeIndex(attr.getLookupAttributeIndex());
    if (EntityTypeUtils.isReferenceType(attr)) {
      builder.setRefEntityType(toEntityTypeResponse(attr.getRefEntity()));
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
    Attribute parent = attr.getParent();
    builder.setParentAttributeId(parent != null ? parent.getIdentifier() : null);
    builder.setNullableExpression(attr.getNullableExpression());
    builder.setVisibleExpression(attr.getVisibleExpression());
    builder.setValidationExpression(attr.getValidationExpression());
    builder.setDefaultValue(attr.getDefaultValue());

    return builder.build();
  }

  private Attribute toAttribute(CreateAttributeRequest attributeRequest, EntityType entityType) {
    Attribute attribute = attributeFactory.create();

    attribute.setIdentifier(attributeRequest.getId());
    attribute.setName(attributeRequest.getName());
    attribute.setEntity(entityType);
    attribute.setSequenceNumber(attributeRequest.getSequenceNumber());
    attribute.setDataType(AttributeType.toEnum(attributeRequest.getType()));
    Optional<EntityType> refEntityType =
        metaDataService.getEntityType(attributeRequest.getRefEntityType());
    if (refEntityType.isPresent()) {
      attribute.setRefEntity(refEntityType.get());
    }
    // FIXME: absent attr results in false.
    // attribute.setCascadeDelete(attributeRequest.isCascadeDelete());
    String orderBy = attributeRequest.getOrderBy();
    attribute.setOrderBy(orderBy != null ? sortMapper.map(sortConverter.convert(orderBy)) : null);
    attribute.setExpression(attributeRequest.getExpression());
    attribute.setNillable(attributeRequest.isNullable());
    attribute.setAuto(attributeRequest.isAuto());
    attribute.setVisible(attributeRequest.isVisible());
    processI18nLabel(attributeRequest, attribute);
    processI18nDescription(attributeRequest, attribute);
    attribute.setAggregatable(attributeRequest.isAggregatable());
    attribute.setEnumOptions(attributeRequest.getEnumOptions());
    attribute.setRangeMin(attributeRequest.getRangeMin());
    attribute.setRangeMax(attributeRequest.getRangeMax());
    attribute.setReadOnly(attributeRequest.isReadonly());
    attribute.setUnique(attributeRequest.isUnique());
    attribute.setNullableExpression(attributeRequest.getNullableExpression());
    attribute.setVisibleExpression(attributeRequest.getVisibleExpression());
    attribute.setValidationExpression(attributeRequest.getValidationExpression());
    attribute.setDefaultValue(attributeRequest.getDefaultValue());
    return attribute;
  }

  private Map<String, Attribute> toAttributes(
      List<CreateAttributeRequest> attributes,
      CreateEntityTypeRequest entityTypeRequest,
      EntityType entityType) {
    Map<String, Attribute> attributeMap = new HashMap<>();
    for (CreateAttributeRequest attributeRequest : attributes) {
      Attribute attr = toAttribute(attributeRequest, entityType);
      if (attributeRequest.getId().equals(entityTypeRequest.getIdAttribute())) {
        attr.setIdAttribute(true);
      }
      if (attributeRequest.getId().equals(entityType.getLabelAttribute())) {
        attr.setLabelAttribute(true);
      }
      if (entityTypeRequest.getLookupAttributes().contains(attributeRequest.getId())) {
        attr.setLookupAttributeIndex(
            entityTypeRequest.getLookupAttributes().indexOf(attributeRequest.getId()));
      }
      attributeMap.put(attributeRequest.getId(), attr);
    }

    resolveMappedBy(attributes, attributeMap, entityTypeRequest.getEntityTypeParent());

    return attributeMap;
  }

  private void resolveMappedBy(
      List<CreateAttributeRequest> attributes,
      Map<String, Attribute> attributeMap,
      String parentEntityId) {
    for (CreateAttributeRequest attributeRequest : attributes) {
      if (attributeRequest.getMappedByAttribute() != null) {
        Attribute mappedBy = null;
        if (attributeMap.containsKey(attributeRequest.getMappedByAttribute())) {
          mappedBy = attributeMap.get(attributeRequest.getMappedByAttribute());
        } else {
          mappedBy = getAttributeFromParent(parentEntityId, attributeRequest, mappedBy);
        }
        if (mappedBy != null) {
          Attribute attr = attributeMap.get(attributeRequest.getId());
          attr.setMappedBy(mappedBy);
        }
      }
    }
  }

  private Attribute getAttributeFromParent(
      String parentEntityId, CreateAttributeRequest attributeRequest, Attribute mappedBy) {
    Optional<EntityType> parentOptional = metaDataService.getEntityType(parentEntityId);
    if (parentOptional.isPresent()) {
      EntityType parent = parentOptional.get();
      mappedBy = parent.getAttribute(attributeRequest.getMappedByAttribute());
      EntityType parentExtends = parent.getExtends();
      if (mappedBy == null && parentExtends != null) {
        mappedBy = getAttributeFromParent(parentExtends.getId(), attributeRequest, mappedBy);
      }
    }
    return mappedBy;
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
            .path(MetadataApiController.API_META_PATH)
            .pathSegment(attr.getEntity().getId())
            .pathSegment(ATTRIBUTES)
            .pathSegment(attr.getIdentifier());
    return uriComponentsBuilder.build().toUri();
  }

  private void processI18nLabel(CreateEntityTypeRequest entityTypeRequest, EntityType entityType) {
    I18nValue i18nValue = entityTypeRequest.getLabel();
    if (i18nValue != null) {
      entityType.setLabel(i18nValue.getDefaultValue());
      getLanguageCodes()
          .forEach(
              languageCode ->
                  entityType.setLabel(languageCode, i18nValue.getTranslations().get(languageCode)));
    }
  }

  private void processI18nDescription(
      CreateEntityTypeRequest entityTypeRequest, EntityType entityType) {
    I18nValue i18nValue = entityTypeRequest.getDescription();
    if (i18nValue != null) {
      entityType.setDescription(i18nValue.getDefaultValue());
      getLanguageCodes()
          .forEach(
              languageCode ->
                  entityType.setDescription(
                      languageCode, i18nValue.getTranslations().get(languageCode)));
    }
  }

  private void processI18nLabel(CreateAttributeRequest attr, Attribute attribute) {
    I18nValue i18nValue = attr.getLabel();
    if (i18nValue != null) {
      attribute.setLabel(i18nValue.getDefaultValue());
      getLanguageCodes()
          .forEach(
              languageCode ->
                  attribute.setLabel(languageCode, i18nValue.getTranslations().get(languageCode)));
    }
  }

  private void processI18nDescription(
      CreateAttributeRequest attributeRequest, Attribute attribute) {
    I18nValue i18nValue = attributeRequest.getDescription();
    if (i18nValue != null) {
      attribute.setDescription(i18nValue.getDefaultValue());
      getLanguageCodes()
          .forEach(
              languageCode ->
                  attribute.setDescription(
                      languageCode, i18nValue.getTranslations().get(languageCode)));
    }
  }

  private I18nValue getI18nAttrLabel(Attribute attr) {
    String defaultValue = attr.getLabel();
    Map<String, String> translations = new HashMap<>();
    getLanguageCodes().forEach(code -> translations.put(code, attr.getLabel(code)));
    return I18nValue.create(defaultValue, translations);
  }

  private I18nValue getI18nAttrDesc(Attribute attr) {
    String defaultValue = attr.getDescription();
    Map<String, String> translations = new HashMap<>();
    getLanguageCodes().forEach(code -> translations.put(code, attr.getDescription(code)));
    return I18nValue.create(defaultValue, translations);
  }

  private I18nValue getI18nEntityTypeLabel(EntityType entityType) {
    String defaultValue = entityType.getLabel();
    Map<String, String> translations = new HashMap<>();
    getLanguageCodes().forEach(code -> translations.put(code, entityType.getLabel(code)));
    return I18nValue.create(defaultValue, translations);
  }

  private I18nValue getI18nEntityTypeDesc(EntityType entityType) {
    String defaultValue = entityType.getDescription();
    Map<String, String> translations = new HashMap<>();
    getLanguageCodes().forEach(code -> translations.put(code, entityType.getDescription(code)));
    return I18nValue.create(defaultValue, translations);
  }
}
