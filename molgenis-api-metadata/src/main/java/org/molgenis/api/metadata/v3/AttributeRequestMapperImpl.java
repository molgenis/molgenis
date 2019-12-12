package org.molgenis.api.metadata.v3;

import static java.util.Objects.requireNonNull;
import static org.molgenis.api.metadata.v3.MetadataUtils.getStringValue;
import static org.molgenis.api.metadata.v3.MetadataUtils.setAttributeType;
import static org.molgenis.api.metadata.v3.MetadataUtils.setBooleanValue;
import static org.molgenis.api.metadata.v3.MetadataUtils.setEnumOptions;
import static org.molgenis.api.metadata.v3.MetadataUtils.setSequenceNumber;
import static org.molgenis.data.meta.model.AttributeMetadata.IS_READ_ONLY;
import static org.molgenis.data.meta.model.AttributeMetadata.IS_UNIQUE;
import static org.molgenis.util.i18n.LanguageService.getLanguageCodes;

import com.google.common.collect.Maps;
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
import org.molgenis.api.metadata.v3.model.CreateAttributeRequest;
import org.molgenis.api.metadata.v3.model.CreateEntityTypeRequest;
import org.molgenis.api.metadata.v3.model.I18nValue;
import org.molgenis.api.metadata.v3.model.Range;
import org.molgenis.data.DataConverter;
import org.molgenis.data.EntityManager;
import org.molgenis.data.Repository;
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
import org.springframework.stereotype.Component;

@Component
class AttributeRequestMapperImpl implements AttributeRequestMapper {
  private final AttributeFactory attributeFactory;
  private final MetaDataService metaDataService;
  private final SortMapper sortMapper;
  private final SortConverter sortConverter;
  private final EntityManager entityManager;
  private final EntityTypeMetadata entityTypeMetadata;

  AttributeRequestMapperImpl(
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

  @Override
  public Attribute toAttribute(CreateAttributeRequest attributeRequest, EntityType entityType) {
    return toAttribute(attributeRequest, entityType, null);
  }

  private Attribute toAttribute(
      CreateAttributeRequest attributeRequest,
      EntityType entityType,
      @Nullable @CheckForNull Integer index) {
    Attribute attribute = attributeFactory.create();

    // set id/label/lookupIndex before setting other properties
    attribute.setIdAttribute(attributeRequest.getIdAttribute());
    attribute.setLabelAttribute(attributeRequest.getLabelAttribute());
    attribute.setLookupAttributeIndex(attributeRequest.getLookupAttributeIndex());

    String id = attributeRequest.getId();
    if (id != null) {
      attribute.setIdentifier(id);
    }
    attribute.setName(attributeRequest.getName());
    attribute.setEntity(entityType);
    Integer sequenceNumber = attributeRequest.getSequenceNr();
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

  @Override
  public Map<String, Attribute> toAttributes(
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

  @Override
  public void updateAttribute(Attribute attribute, Map<String, Object> attributeValues) {
    attributeValues.forEach((key, value) -> updateAttributeValue(attribute, key, value));
  }

  private void updateAttributeValue(Attribute attribute, String key, Object value) {
    switch (key) {
      case "id":
        attribute.setIdentifier(getStringValue(value));
        break;
      case "name":
        attribute.setName(getStringValue(value));
        break;
      case "sequenceNr":
        setSequenceNumber(attribute, value);
        break;
      case "type":
        setAttributeType(attribute, value);
        break;
      case "refEntityType":
        setRefEntityType(attribute, value);
        break;
      case "cascadeDelete":
        String cascadeValue = getStringValue(value);
        Boolean isCascade = cascadeValue != null ? Boolean.valueOf(cascadeValue) : null;
        attribute.setCascadeDelete(isCascade);
        break;
      case "orderBy":
        String orderBy = getStringValue(value);
        attribute.setOrderBy(
            orderBy != null ? sortMapper.map(sortConverter.convert(orderBy)) : null);
        break;
      case "expression":
        attribute.setExpression(getStringValue(value));
        break;
      case "nullable":
        String nullableValue = getStringValue(value);
        attribute.setNillable(Boolean.valueOf(nullableValue));
        break;
      case "auto":
        String autoValue = getStringValue(value);
        attribute.setAuto(Boolean.valueOf(autoValue));
        break;
      case "visible":
        String visibleValue = getStringValue(value);
        attribute.setVisible(Boolean.valueOf(visibleValue));
        break;
      case "label":
        I18nValue i18Label = I18nValueMapper.toI18nValue(value);
        processI18nLabel(i18Label, attribute);
        break;
      case "description":
        I18nValue i18Description = I18nValueMapper.toI18nValue(value);
        processI18nDescription(i18Description, attribute);
        break;
      case "aggregatable":
        attribute.setAggregatable(Boolean.valueOf(value.toString()));
        break;
      case "enumOptions":
        setEnumOptions(attribute, value);
        break;
      case "range":
        Range range = mapRange(value);
        if (range != null) {
          attribute.setRange(map(range));
        }
        break;
      case "readonly":
        setBooleanValue(attribute, value, IS_READ_ONLY);
        break;
      case "unique":
        setBooleanValue(attribute, value, IS_UNIQUE);
        break;
      case "defaultValue":
        attribute.setDefaultValue(getStringValue(value));
        break;
      case "nullableExpression":
        attribute.setNullableExpression(getStringValue(value));
        break;
      case "visibleExpression":
        attribute.setVisibleExpression(getStringValue(value));
        break;
      case "validationExpression":
        attribute.setValidationExpression(getStringValue(value));
        break;
      case "mappedByAttribute":
      case "parent":
        // Skip now and process after all attributes in the request have been processed
        break;
      case "tags":
        throw new UnsupportedFieldException(key);
      case "idAttribute":
        attribute.setIdAttribute(DataConverter.toBoolean(value));
        break;
      case "labelAttribute":
        attribute.setLabelAttribute(DataConverter.toBoolean(value));
        break;
      case "lookupAttributeIndex":
        attribute.setLookupAttributeIndex(DataConverter.toInt(value));
        break;
      default:
        throw new InvalidKeyException("attribute", key);
    }
  }

  private Attribute toAttribute(Map<String, Object> attributeRequest, EntityType entityType) {
    Attribute attribute = attributeFactory.create();
    attribute.setEntity(entityType);
    updateAttribute(attribute, attributeRequest);
    return attribute;
  }

  private void setRefEntityType(Attribute attribute, Object value) {
    EntityType refEntityType;
    String refEntityTypeId = getStringValue(value);
    if (refEntityTypeId != null) {
      refEntityType = (EntityType) entityManager.getReference(entityTypeMetadata, refEntityTypeId);
      attribute.setRefEntity(refEntityType);
    } else {
      throw new UnknownEntityTypeException(refEntityTypeId);
    }
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

  @Override
  public List<Attribute> toAttributes(
      List<CreateAttributeRequest> attributes,
      CreateEntityTypeRequest entityTypeRequest,
      EntityType entityType) {
    Map<String, Attribute> mappedAttributes = Maps.newHashMapWithExpectedSize(attributes.size());
    AtomicInteger index = new AtomicInteger(0);
    for (CreateAttributeRequest attribute : attributes) {
      Attribute mappedAttribute = toAttribute(attribute, entityType, index.getAndIncrement());
      mappedAttributes.put(mappedAttribute.getIdentifier(), mappedAttribute);
    }

    resolveMappedBy(attributes, mappedAttributes, entityTypeRequest.getExtends());

    return new ArrayList<>(mappedAttributes.values());
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
}
