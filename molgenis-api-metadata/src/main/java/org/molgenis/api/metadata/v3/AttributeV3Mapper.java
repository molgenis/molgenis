package org.molgenis.api.metadata.v3;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.api.metadata.v3.MetadataUtils.getStringValue;
import static org.molgenis.api.metadata.v3.MetadataUtils.setAttributeType;
import static org.molgenis.api.metadata.v3.MetadataUtils.setBooleanValue;
import static org.molgenis.api.metadata.v3.MetadataUtils.setEnumOptions;
import static org.molgenis.api.metadata.v3.MetadataUtils.setSequenceNumber;
import static org.molgenis.data.meta.AttributeType.ONE_TO_MANY;
import static org.molgenis.data.meta.AttributeType.getValueString;
import static org.molgenis.data.meta.model.AttributeMetadata.DESCRIPTION;
import static org.molgenis.data.meta.model.AttributeMetadata.IS_READ_ONLY;
import static org.molgenis.data.meta.model.AttributeMetadata.IS_UNIQUE;
import static org.molgenis.data.meta.model.AttributeMetadata.LABEL;
import static org.molgenis.util.i18n.LanguageService.getLanguageCodes;
import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentRequestUri;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.api.convert.SortConverter;
import org.molgenis.api.data.SortMapper;
import org.molgenis.api.metadata.v3.exception.InvalidKeyException;
import org.molgenis.api.metadata.v3.exception.UnsupportedFieldException;
import org.molgenis.api.metadata.v3.model.AttributeResponse;
import org.molgenis.api.metadata.v3.model.AttributeResponseData;
import org.molgenis.api.metadata.v3.model.AttributeResponseData.Builder;
import org.molgenis.api.metadata.v3.model.AttributesResponse;
import org.molgenis.api.metadata.v3.model.Category;
import org.molgenis.api.metadata.v3.model.CreateAttributeRequest;
import org.molgenis.api.metadata.v3.model.CreateEntityTypeRequest;
import org.molgenis.api.metadata.v3.model.I18nValue;
import org.molgenis.api.metadata.v3.model.Range;
import org.molgenis.api.model.Sort.Order.Direction;
import org.molgenis.api.model.response.LinksResponse;
import org.molgenis.api.model.response.PageResponse;
import org.molgenis.api.support.LinksUtils;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityManager;
import org.molgenis.data.InvalidValueTypeException;
import org.molgenis.data.Repository;
import org.molgenis.data.Sort;
import org.molgenis.data.Sort.Order;
import org.molgenis.data.UnknownAttributeException;
import org.molgenis.data.UnknownEntityTypeException;
import org.molgenis.data.UnknownRepositoryException;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.AttributeMetadata;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.data.util.EntityTypeUtils;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
class AttributeV3Mapper {
  private static final String ATTRIBUTES = "attributes";

  private final AttributeFactory attributeFactory;
  private final MetaDataService metaDataService;
  private final SortMapper sortMapper;
  private final SortConverter sortConverter;
  private final EntityManager entityManager;
  private final EntityTypeMetadata entityTypeMetadata;

  AttributeV3Mapper(
      AttributeFactory attributeFactory,
      MetaDataService metaDataService,
      SortMapper sortMapper,
      SortConverter sortConverter,
      EntityManager entityManager,
      EntityTypeMetadata entityTypeMetadata) {
    this.attributeFactory = requireNonNull(attributeFactory);
    this.metaDataService = requireNonNull(metaDataService);
    this.sortMapper = requireNonNull(sortMapper);
    this.sortConverter = requireNonNull(sortConverter);
    this.entityManager = requireNonNull(entityManager);
    this.entityTypeMetadata = requireNonNull(entityTypeMetadata);
  }

  AttributesResponse mapAttributes(Attributes attributes, int size, int number, int total) {
    return AttributesResponse.create(
        LinksUtils.createLinksResponse(number, size, total),
        attributes.getAttributes().stream()
            .map(attribute -> this.mapAttribute(attribute, false))
            .collect(toList()),
        PageResponse.create(size, attributes.getTotal(), attributes.getTotal() / size, number));
  }

  List<AttributeResponse> mapInternal(
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
    builder.setType(getValueString(attr.getDataType()));
    builder.setIdAttribute(attr.isIdAttribute());
    builder.setLabelAttribute(attr.isLabelAttribute());
    builder.setLookupAttributeIndex(attr.getLookupAttributeIndex());
    if (EntityTypeUtils.isReferenceType(attr)) {
      builder.setRefEntityType(
          LinksResponse.create(null, createEntityTypeResponseUri(attr.getRefEntity()), null));
    }
    builder.setCascadeDelete(attr.getCascadeDelete());
    builder.setMappedBy(attr.getMappedBy() != null ? mapAttribute(attr.getMappedBy(), i18n) : null);
    if (attr.getDataType() == ONE_TO_MANY && attr.isMappedBy()) {
      builder.setOrderBy(map(attr));
    }
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
    if (attr.getDataType() == AttributeType.CATEGORICAL
        || attr.getDataType() == AttributeType.CATEGORICAL_MREF) {
      builder.setCategoricalOptions(getCategories(attr.getRefEntity()));
    }
    org.molgenis.data.Range range = attr.getRange();
    if (range != null) {
      builder.setRange(Range.create(range.getMin(), range.getMax()));
    }
    Attribute parent = attr.getParent();
    builder.setParentAttributeId(parent != null ? parent.getIdentifier() : null);
    builder.setNullableExpression(attr.getNullableExpression());
    builder.setVisibleExpression(attr.getVisibleExpression());
    builder.setValidationExpression(attr.getValidationExpression());
    builder.setDefaultValue(attr.getDefaultValue());

    return builder.build();
  }

  private List<Category> getCategories(EntityType entityType) {
    Repository<Entity> repository =
        metaDataService
            .getRepository(entityType)
            .orElseThrow(() -> new UnknownRepositoryException(entityType.getId()));
    Attribute idAttribute = entityType.getIdAttribute();
    Attribute labelAttribute = entityType.getLabelAttribute();
    return repository
        .query()
        .findAll()
        .map(
            entity ->
                Category.builder()
                    .setId(entity.get(idAttribute))
                    .setLabel(entity.get(labelAttribute).toString())
                    .build())
        .collect(toList());
  }

  private List<org.molgenis.api.model.Sort> map(Attribute attr) {
    List<org.molgenis.api.model.Sort> orders;

    Sort sort = attr.getOrderBy();
    if (sort == null) {
      orders = emptyList();
    } else {
      orders = new ArrayList<>(Iterables.size(sort));
      for (Order order : sort) {
        orders.add(
            org.molgenis.api.model.Sort.create(
                order.getAttr(), Direction.valueOf(order.getDirection().name())));
      }
    }

    return orders;
  }

  public Attribute toAttribute(CreateAttributeRequest attributeRequest, EntityType entityType) {
    return toAttribute(attributeRequest, entityType, null);
  }

  private Attribute toAttribute(
      CreateAttributeRequest attributeRequest,
      EntityType entityType,
      @Nullable @CheckForNull Integer index) {
    Attribute attribute = attributeFactory.create();

    String id = attributeRequest.getId();
    if (id != null) {
      attribute.setIdentifier(id);
    }
    attribute.setName(attributeRequest.getName());
    attribute.setEntity(entityType);
    Integer sequenceNumber = attributeRequest.getSequenceNumber();
    if (sequenceNumber == null) {
      sequenceNumber = index;
    }
    if (sequenceNumber != null) {
      attribute.setSequenceNumber(sequenceNumber);
    }
    String type = attributeRequest.getType();
    if (type != null) {
      attribute.setDataType(AttributeType.toEnum(type));
    }

    EntityType refEntityType;
    String refEntityTypeId = attributeRequest.getRefEntityType();
    if (refEntityTypeId != null) {
      refEntityType = (EntityType) entityManager.getReference(entityTypeMetadata, refEntityTypeId);
      attribute.setRefEntity(refEntityType);
    }
    if (attributeRequest.getCascadeDelete() != null) {
      attribute.setCascadeDelete(attributeRequest.getCascadeDelete());
    }
    String orderBy = attributeRequest.getOrderBy();
    attribute.setOrderBy(
        orderBy != null ? sortMapper.map(requireNonNull(sortConverter.convert(orderBy))) : null);
    attribute.setExpression(attributeRequest.getExpression());
    Boolean nullable = attributeRequest.getNullable();
    if (nullable != null) {
      attribute.setNillable(nullable);
    }
    Boolean auto = attributeRequest.getAuto();
    if (auto != null) {
      attribute.setAuto(auto);
    }
    Boolean visible = attributeRequest.getVisible();
    if (visible != null) {
      attribute.setVisible(visible);
    }
    processI18nLabel(attributeRequest.getLabel(), attribute);
    processI18nDescription(attributeRequest.getDescription(), attribute);
    Boolean aggregatable = attributeRequest.getAggregatable();
    if (aggregatable != null) {
      attribute.setAggregatable(aggregatable);
    }
    List<String> enumOptions = attributeRequest.getEnumOptions();
    if (enumOptions != null) {
      attribute.setEnumOptions(enumOptions);
    }
    Range range = attributeRequest.getRange();
    if (range != null) {
      attribute.setRange(map(range));
    }
    Boolean readonly = attributeRequest.getReadonly();
    if (readonly != null) {
      attribute.setReadOnly(readonly);
    }
    Boolean unique = attributeRequest.getUnique();
    if (unique != null) {
      attribute.setUnique(unique);
    }
    attribute.setNullableExpression(attributeRequest.getNullableExpression());
    attribute.setVisibleExpression(attributeRequest.getVisibleExpression());
    attribute.setValidationExpression(attributeRequest.getValidationExpression());
    attribute.setDefaultValue(attributeRequest.getDefaultValue());
    return attribute;
  }

  Map<String, Attribute> toAttributes(
      List<Map<String, Object>> attributeValueMaps, EntityType entityType) {
    Map<String, Attribute> attributes = new HashMap<>();
    int index = 0;
    for (Map<String, Object> attributeValueMap : attributeValueMaps) {
      Attribute attr = toAttribute(attributeValueMap, entityType);
      if (attr.getSequenceNumber() == null) {
        attr.setSequenceNumber(index);
      }
      attributes.put(attr.getIdentifier(), attr);
      index++;
    }
    for (Map<String, Object> attributeValueMap : attributeValueMaps) {
      processParentAndMappedby(attributeValueMap, attributes, entityType);
    }
    return attributes;
  }

  private void processParentAndMappedby(
      Map<String, Object> attributeRequest,
      Map<String, Attribute> attributes,
      EntityType entityType) {
    String attributeId = attributeRequest.get("id").toString();
    for (Entry<String, Object> entry : attributeRequest.entrySet()) {
      if (entry.getKey().equals("mappedByAttribute")) {
        String mappedByAttrId = getStringValue(entry.getValue());
        Attribute mappedByAttr = attributes.get(mappedByAttrId);
        if (mappedByAttr == null) {
          Repository<Attribute> repository =
              metaDataService
                  .getRepository(AttributeMetadata.ATTRIBUTE_META_DATA, Attribute.class)
                  .orElseThrow(
                      () -> new UnknownRepositoryException(AttributeMetadata.ATTRIBUTE_META_DATA));
          mappedByAttr = repository.findOneById(mappedByAttrId);
        }
        if (mappedByAttr == null) {
          throw new UnknownAttributeException(entityType, mappedByAttrId);
        }
        Attribute attribute = attributes.get(attributeId);
        attribute.setMappedBy(mappedByAttr);
      } else if (entry.getKey().equals("parent")) {
        String parentId = getStringValue(entry.getValue());
        Attribute parentAttr = attributes.get(parentId);
        if (parentAttr == null) {
          parentAttr = getAttributeFromParent(entityType.getExtends(), parentId);
        }
        Attribute attribute = attributes.get(attributeId);
        attribute.setParent(parentAttr);
      }
    }
  }

  private Attribute toAttribute(Map<String, Object> attributeRequest, EntityType entityType) {
    Attribute attribute = attributeFactory.create();
    attribute.setEntity(entityType);

    for (Entry<String, Object> entry : attributeRequest.entrySet()) {
      switch (entry.getKey()) {
        case "id":
          attribute.setIdentifier(getStringValue(entry.getValue()));
          break;
        case "name":
          attribute.setName(getStringValue(entry.getValue()));
          break;
        case "sequenceNumber":
          setSequenceNumber(attribute, entry);
          break;
        case "type":
          setAttributeType(attribute, entry);
          break;
        case "refEntityType":
          setRefEntityType(attribute, entry);
          break;
        case "cascadeDelete":
          String cascadeValue = getStringValue(entry.getValue());
          Boolean isCascade = cascadeValue != null ? Boolean.valueOf(cascadeValue) : null;
          attribute.setCascadeDelete(isCascade);
          break;
        case "orderBy":
          String orderBy = getStringValue(entry.getValue());
          attribute.setOrderBy(
              orderBy != null ? sortMapper.map(sortConverter.convert(orderBy)) : null);
          break;
        case "expression":
          attribute.setExpression(getStringValue(entry.getValue()));
          break;
        case "nullable":
          String nullableValue = getStringValue(entry.getValue());
          attribute.setNillable(Boolean.valueOf(nullableValue));
          break;
        case "auto":
          String autoValue = getStringValue(entry.getValue());
          attribute.setAuto(Boolean.valueOf(autoValue));
          break;
        case "visible":
          String visibleValue = getStringValue(entry.getValue());
          attribute.setVisible(Boolean.valueOf(visibleValue));
          break;
        case "label":
          I18nValue i18Label = mapI18nValue(entry.getValue());
          processI18nLabel(i18Label, attribute);
          break;
        case "description":
          I18nValue i18Description = mapI18nValue(entry.getValue());
          processI18nDescription(i18Description, attribute);
          break;
        case "aggregatable":
          attribute.setAggregatable(Boolean.valueOf(entry.getValue().toString()));
          break;
        case "enumOptions":
          setEnumOptions(attribute, entry);
          break;
        case "range":
          Range range = mapRange(entry.getValue());
          if (range != null) {
            attribute.setRange(map(range));
          }
          break;
        case "readonly":
          setBooleanValue(attribute, entry, IS_READ_ONLY);
          break;
        case "unique":
          setBooleanValue(attribute, entry, IS_UNIQUE);
          break;
        case "defaultValue":
          attribute.setDefaultValue(getStringValue(entry.getValue()));
          break;
        case "nullableExpression":
          attribute.setNullableExpression(getStringValue(entry.getValue()));
          break;
        case "visibleExpression":
          attribute.setVisibleExpression(getStringValue(entry.getValue()));
          break;
        case "validationExpression":
          attribute.setValidationExpression(getStringValue(entry.getValue()));
          break;
        case "mappedByAttribute":
        case "parent":
          // Skip now and process after all attributes in the request have been processed
          break;
        case "tags":
          throw new UnsupportedFieldException(entry.getKey());
        default:
          throw new InvalidKeyException("attribute", entry.getKey());
      }
    }
    return attribute;
  }

  private void setRefEntityType(Attribute attribute, Entry<String, Object> entry) {
    EntityType refEntityType;
    String refEntityTypeId = getStringValue(entry.getValue());
    if (refEntityTypeId != null) {
      refEntityType = (EntityType) entityManager.getReference(entityTypeMetadata, refEntityTypeId);
      attribute.setRefEntity(refEntityType);
    } else {
      throw new UnknownEntityTypeException(refEntityTypeId);
    }
  }

  I18nValue mapI18nValue(Object value) {
    I18nValue.Builder builder = I18nValue.builder();
    if (value instanceof Map) {
      Map valueMap = (Map) value;
      Object defaultValue = valueMap.get("defaultValue");
      if (defaultValue != null) {
        builder.setDefaultValue(defaultValue.toString());
      }
      Object translations = valueMap.get("translations");
      if (translations instanceof Map) {
        Map<?, ?> translationsMap = (Map) translations;
        Map<String, String> typedTranslations = new HashMap<>();
        for (Entry entry : translationsMap.entrySet()) {
          if (entry.getKey() != null && entry.getValue() != null) {
            typedTranslations.put(entry.getKey().toString(), entry.getValue().toString());
          }
        }
        builder.setTranslations(typedTranslations);
      }
    } else {
      throw new InvalidValueTypeException(value.toString(), "I18nValue", null);
    }
    return builder.build();
  }

  private Range mapRange(Object value) {
    Long minLong = null;
    Long maxLong = null;
    if (value instanceof Map) {
      Map valueMap = (Map) value;
      Object min = valueMap.get("min");
      if (min != null) {
        minLong = Double.valueOf(min.toString()).longValue();
      }
      Object max = valueMap.get("max");
      if (max != null) {
        maxLong = Double.valueOf(max.toString()).longValue();
      }
    }
    return Range.create(minLong, maxLong);
  }

  private org.molgenis.data.Range map(Range range) {
    return new org.molgenis.data.Range(range.getMin(), range.getMax());
  }

  Map<String, Attribute> toAttributes(
      List<CreateAttributeRequest> attributes,
      CreateEntityTypeRequest entityTypeRequest,
      EntityType entityType) {
    Map<String, Attribute> attributeMap = new HashMap<>();
    AtomicInteger index = new AtomicInteger(0);
    for (CreateAttributeRequest attributeRequest : attributes) {
      Attribute attr = toAttribute(attributeRequest, entityType, index.getAndIncrement());
      String id = attributeRequest.getId();
      if (id != null && id.equals(entityTypeRequest.getIdAttribute())) {
        attr.setIdAttribute(true);
      }
      Attribute labelAttribute = entityType.getLabelAttribute();
      if (labelAttribute != null && id != null && id.equals(labelAttribute.getIdentifier())) {
        attr.setLabelAttribute(true);
      }
      List<String> lookupAttributes = entityTypeRequest.getLookupAttributes();
      if (lookupAttributes != null && lookupAttributes.contains(attributeRequest.getId())) {
        attr.setLookupAttributeIndex(lookupAttributes.indexOf(attributeRequest.getId()));
      }
      attributeMap.put(attributeRequest.getId(), attr);
    }

    resolveMappedBy(attributes, attributeMap, entityTypeRequest.getExtends());

    return attributeMap;
  }

  private void resolveMappedBy(
      List<CreateAttributeRequest> attributes,
      Map<String, Attribute> attributeMap,
      String parentEntityId) {
    for (CreateAttributeRequest attributeRequest : attributes) {
      if (attributeRequest.getMappedByAttribute() != null) {
        Attribute mappedBy;
        if (attributeMap.containsKey(attributeRequest.getMappedByAttribute())) {
          mappedBy = attributeMap.get(attributeRequest.getMappedByAttribute());
        } else {
          mappedBy = getAttributeFromParent(parentEntityId, attributeRequest);
        }
        if (mappedBy != null) {
          Attribute attr = attributeMap.get(attributeRequest.getId());
          attr.setMappedBy(mappedBy);
        }
      }
    }
  }

  private Attribute getAttributeFromParent(
      String parentEntityId, CreateAttributeRequest attributeRequest) {
    Optional<EntityType> parentOptional = metaDataService.getEntityType(parentEntityId);
    Attribute mappedBy = null;
    if (parentOptional.isPresent()) {
      EntityType parent = parentOptional.get();
      mappedBy = parent.getAttribute(attributeRequest.getMappedByAttribute());
      EntityType parentExtends = parent.getExtends();
      if (mappedBy == null && parentExtends != null) {
        mappedBy = getAttributeFromParent(parentExtends.getId(), attributeRequest);
      }
    }
    return mappedBy;
  }

  private Attribute getAttributeFromParent(EntityType parent, String attributeId) {
    Attribute attribute = null;
    if (parent != null) {
      attribute = parent.getAttribute(attributeId);
      EntityType parentExtends = parent.getExtends();
      if (attribute == null && parentExtends != null) {
        attribute = getAttributeFromParent(parentExtends, attributeId);
      }
    }
    return attribute;
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

  private URI createEntityTypeResponseUri(EntityType entityType) {
    UriComponentsBuilder uriComponentsBuilder =
        fromCurrentRequestUri()
            .replacePath(null)
            .path(MetadataApiController.API_META_PATH)
            .pathSegment(entityType.getId());
    return uriComponentsBuilder.build().toUri();
  }

  private void processI18nLabel(I18nValue i18nValue, Attribute attribute) {
    if (i18nValue != null) {
      attribute.setLabel(i18nValue.getDefaultValue());
      Map<String, String> translations = i18nValue.getTranslations();
      if (translations != null) {
        getLanguageCodes()
            .forEach(
                languageCode -> attribute.setLabel(languageCode, translations.get(languageCode)));
      }
    }
  }

  private void processI18nDescription(I18nValue i18nValue, Attribute attribute) {
    if (i18nValue != null) {
      attribute.setDescription(i18nValue.getDefaultValue());
      Map<String, String> translations = i18nValue.getTranslations();
      if (translations != null) {
        getLanguageCodes()
            .forEach(
                languageCode ->
                    attribute.setDescription(languageCode, translations.get(languageCode)));
      }
    }
  }

  private I18nValue getI18nAttrLabel(Attribute attr) {
    String defaultValue = attr.getLabel();
    ImmutableMap<String, String> translations = MetadataUtils.getI18n(attr, LABEL);
    return I18nValue.create(defaultValue, translations);
  }

  private I18nValue getI18nAttrDesc(Attribute attr) {
    String defaultValue = attr.getDescription();
    ImmutableMap<String, String> translations = MetadataUtils.getI18n(attr, DESCRIPTION);
    return I18nValue.create(defaultValue, translations);
  }
}
