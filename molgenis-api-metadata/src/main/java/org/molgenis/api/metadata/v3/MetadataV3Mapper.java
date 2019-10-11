package org.molgenis.api.metadata.v3;

import static java.util.Objects.requireNonNull;
import static org.molgenis.api.data.v3.EntityController.API_ENTITY_PATH;
import static org.molgenis.data.meta.model.AttributeMetadata.DESCRIPTION;
import static org.molgenis.data.meta.model.AttributeMetadata.LABEL;
import static org.molgenis.data.util.AttributeUtils.getI18nAttributeName;
import static org.molgenis.util.i18n.LanguageService.getLanguageCodes;
import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentRequestUri;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.molgenis.api.convert.SortConverter;
import org.molgenis.api.metadata.v3.model.AttributeResponse;
import org.molgenis.api.metadata.v3.model.AttributeResponseData;
import org.molgenis.api.metadata.v3.model.AttributeResponseData.Builder;
import org.molgenis.api.metadata.v3.model.AttributesResponse;
import org.molgenis.api.metadata.v3.model.CreateAttributeRequest;
import org.molgenis.api.metadata.v3.model.CreateEntityTypeRequest;
import org.molgenis.api.metadata.v3.model.EntityTypeResponse;
import org.molgenis.api.metadata.v3.model.EntityTypeResponseData;
import org.molgenis.api.metadata.v3.model.EntityTypesResponse;
import org.molgenis.api.metadata.v3.model.I18nValue;
import org.molgenis.api.metadata.v3.model.PackageResponse;
import org.molgenis.api.model.response.LinksResponse;
import org.molgenis.api.model.response.PageResponse;
import org.molgenis.data.UnknownPackageException;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.model.PackageMetadata;
import org.molgenis.data.util.EntityTypeUtils;
import org.molgenis.util.UnexpectedEnumException;
import org.molgenis.web.support.MolgenisServletUriComponentsBuilder;
import org.springframework.context.i18n.LocaleContextHolder;
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

  public EntityTypesResponse toEntityTypesResponse(
      EntityTypes entityTypes, int size, int number, int total) {
    List<EntityTypeResponse> results = new ArrayList<>();
    for (EntityType entityType : entityTypes.getEntityTypes()) {
      results.add(mapInternal(entityType, false, true, false, false));
    }

    return EntityTypesResponse.create(
        createLinksResponse(number, size, total),
        results,
        PageResponse.create(size, entityTypes.getTotal(), entityTypes.getTotal() / size, number));
  }

  public EntityTypeResponse toEntityTypeResponse(
      EntityType entityType, boolean flattenAttrs, boolean i18n) {
    return mapInternal(entityType, flattenAttrs, true, true, i18n);
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

  private EntityTypeResponse mapInternal(
      EntityType entityType,
      boolean flattenAttrs,
      boolean includeData,
      boolean expandAttrs,
      boolean i18n) {
    EntityTypeResponse.Builder entityTypeResponseBuilder = EntityTypeResponse.builder();
    entityTypeResponseBuilder.setLinks(
        LinksResponse.create(null, createEntityTypeResponseUri(entityType), null));

    if (includeData) {
      EntityTypeResponseData.Builder builder = EntityTypeResponseData.builder();
      builder.setId(entityType.getId());
      Package pack = entityType.getPackage();
      if (pack != null) {
        builder.setPackage_(
            PackageResponse.builder()
                .setLinks(LinksResponse.create(null, createPackageResponseUri(pack), null))
                .build());
      }
      builder.setLabel(entityType.getLabel(LocaleContextHolder.getLocale().getLanguage()));
      builder.setDescription(
          entityType.getDescription(LocaleContextHolder.getLocale().getLanguage()));
      if (i18n) {
        builder.setLabelI18n(getI18nEntityTypeLabel(entityType));
        builder.setDescriptionI18n(getI18nEntityTypeDesc(entityType));
      }

      AttributesResponse.Builder attributesResponseBuilder =
          AttributesResponse.builder()
              .setLinks(LinksResponse.create(null, createAttributesResponseUri(entityType), null));
      if (expandAttrs) {
        attributesResponseBuilder.setItems(
            flattenAttrs
                ? mapInternal(entityType.getAllAttributes(), i18n)
                : mapInternal(entityType.getOwnAllAttributes(), i18n));
      }
      builder.setAttributes(attributesResponseBuilder.build());
      builder.setAbstract(entityType.isAbstract());
      EntityType parent = entityType.getExtends();
      builder.setExtends_(parent != null ? mapInternal(parent, false, false, false, i18n) : null);
      builder.setIndexingDepth(entityType.getIndexingDepth());
      entityTypeResponseBuilder.setData(builder.build());
    }

    return entityTypeResponseBuilder.build();
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
            .map(attribute -> this.mapAttribute(attribute, false))
            .collect(Collectors.toList()),
        PageResponse.create(size, attributes.getTotal(), attributes.getTotal() / size, number));
  }

  private List<AttributeResponse> mapInternal(
      Iterable<org.molgenis.data.meta.model.Attribute> allAttributes, boolean i18n) {
    List<AttributeResponse> result = new ArrayList<>();
    for (Attribute attr : allAttributes) {
      result.add(mapAttribute(attr, i18n));
    }
    return result;
  }

  AttributeResponse mapAttribute(Attribute attr, boolean i18n) {
    AttributeResponseData attribute = mapInternal(attr, i18n);
    return AttributeResponse.create(
        LinksResponse.create(null, createAttributeResponseUri(attr), null), attribute);
  }

  private AttributeResponseData mapInternal(Attribute attr, boolean i18n) {
    Builder builder = AttributeResponseData.builder();
    builder.setId(attr.getIdentifier());
    builder.setName(attr.getName());
    builder.setSequenceNr(attr.getSequenceNumber());
    builder.setType(mapAttributeType(attr.getDataType()));
    builder.setLookupAttributeIndex(attr.getLookupAttributeIndex());
    if (EntityTypeUtils.isReferenceType(attr)) {
      try {
        builder.setRefEntityType(LinksResponse.create(null, new URI("http://fix.me/"), null));
      } catch (URISyntaxException e) {
        throw new RuntimeException(e);
      }
    }
    builder.setCascadeDelete(attr.getCascadeDelete());
    builder.setMappedBy(attr.getMappedBy() != null ? mapAttribute(attr.getMappedBy(), i18n) : null);
    builder.setOrderBy(attr.getOrderBy());
    builder.setLabel(attr.getLabel(LocaleContextHolder.getLocale().getLanguage()));
    builder.setDescription(attr.getDescription(LocaleContextHolder.getLocale().getLanguage()));
    if (i18n) {
      builder.setLabelI18n(getI18nAttrLabel(attr));
      builder.setDescriptionI18n(getI18nAttrDesc(attr));
    }
    builder.setNullable(attr.isNillable());
    builder.setAuto(attr.isAuto());
    builder.setVisible(attr.isVisible());
    builder.setUnique(attr.isUnique());
    builder.setReadOnly(attr.isReadOnly());
    builder.setAggregatable(attr.isAggregatable());
    builder.setExpression(attr.getExpression());
    if (attr.getDataType() == AttributeType.ENUM) {
      builder.setEnumOptions(attr.getEnumOptions());
    }
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

  private String mapAttributeType(AttributeType attributeType) {
    String responseType;
    switch (attributeType) {
      case BOOL:
        responseType = "bool";
        break;
      case CATEGORICAL:
        responseType = "categorical";
        break;
      case CATEGORICAL_MREF:
        responseType = "categorical_mref";
        break;
      case COMPOUND:
        responseType = "compound";
        break;
      case DATE:
        responseType = "date";
        break;
      case DATE_TIME:
        responseType = "date_time";
        break;
      case DECIMAL:
        responseType = "decimal";
        break;
      case EMAIL:
        responseType = "email";
        break;
      case ENUM:
        responseType = "enum";
        break;
      case FILE:
        responseType = "file";
        break;
      case HTML:
        responseType = "html";
        break;
      case HYPERLINK:
        responseType = "hyperlink";
        break;
      case INT:
        responseType = "int";
        break;
      case LONG:
        responseType = "long";
        break;
      case MREF:
        responseType = "mref";
        break;
      case ONE_TO_MANY:
        responseType = "one_to_many";
        break;
      case SCRIPT:
        responseType = "script";
        break;
      case STRING:
        responseType = "string";
        break;
      case TEXT:
        responseType = "text";
        break;
      case XREF:
        responseType = "xref";
        break;
      default:
        throw new UnexpectedEnumException(attributeType);
    }
    return responseType;
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
    URI self = createEntitiesResponseUri();
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
    UriComponentsBuilder builder = MolgenisServletUriComponentsBuilder.fromCurrentRequestDecodedQuery();
    return builder.build().toUri();
  }

  private URI createEntitiesResponseUri(Integer pageNumber) {
    UriComponentsBuilder builder = MolgenisServletUriComponentsBuilder.fromCurrentRequestDecodedQuery();
    if (pageNumber != null) {
      builder.replaceQueryParam(PAGE, pageNumber);
    }
    return builder.build().toUri();
  }

  private URI createEntityTypeResponseUri(EntityType entityType) {
    UriComponentsBuilder uriComponentsBuilder =
        fromCurrentRequestUri()
            .replacePath(null)
            .path(MetadataApiController.API_META_PATH)
            .pathSegment(entityType.getId());
    return uriComponentsBuilder.build().toUri();
  }

  private URI createPackageResponseUri(Package aPackage) {
    UriComponentsBuilder uriComponentsBuilder =
        fromCurrentRequestUri()
            .replacePath(null)
            .path(API_ENTITY_PATH)
            .pathSegment(PackageMetadata.PACKAGE)
            .pathSegment(aPackage.getId());
    return uriComponentsBuilder.build().toUri();
  }

  private URI createAttributesResponseUri(EntityType entityType) {
    UriComponentsBuilder uriComponentsBuilder =
        fromCurrentRequestUri()
            .replacePath(null)
            .path(MetadataApiController.API_META_PATH)
            .pathSegment(entityType.getId())
            .pathSegment(ATTRIBUTES);
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
    getLanguageCodes()
        .forEach(code -> translations.put(code, attr.getString(getI18nAttributeName(LABEL, code))));
    return I18nValue.create(defaultValue, translations);
  }

  private I18nValue getI18nAttrDesc(Attribute attr) {
    String defaultValue = attr.getDescription();
    Map<String, String> translations = new HashMap<>();
    getLanguageCodes()
        .forEach(
            code ->
                translations.put(code, attr.getString(getI18nAttributeName(DESCRIPTION, code))));
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
