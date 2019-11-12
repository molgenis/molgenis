package org.molgenis.api.metadata.v3;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.data.meta.AttributeType.ONE_TO_MANY;
import static org.molgenis.data.meta.AttributeType.getValueString;
import static org.molgenis.data.meta.model.AttributeMetadata.DESCRIPTION;
import static org.molgenis.data.meta.model.AttributeMetadata.LABEL;
import static org.molgenis.data.util.AttributeUtils.getI18nAttributeName;
import static org.molgenis.util.i18n.LanguageService.getLanguageCodes;
import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentRequestUri;

import com.google.common.collect.Iterables;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.molgenis.api.convert.SortConverter;
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
import org.molgenis.data.Entity;
import org.molgenis.data.EntityManager;
import org.molgenis.data.Repository;
import org.molgenis.data.Sort;
import org.molgenis.data.Sort.Order;
import org.molgenis.data.UnknownEntityTypeException;
import org.molgenis.data.UnknownRepositoryException;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.data.util.EntityTypeUtils;
import org.molgenis.web.support.MolgenisServletUriComponentsBuilder;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import org.w3c.dom.Attr;

@Component
public class AttributeV3Mapper {
  public static final String ATTRIBUTES = "attributes";
  public static final String PAGE = "page";

  private final AttributeFactory attributeFactory;
  private final MetaDataService metaDataService;
  private final SortMapper sortMapper;
  private final SortConverter sortConverter;
  private final EntityManager entityManager;
  private final EntityTypeMetadata entityTypeMetadata;

  public AttributeV3Mapper(
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
        createLinksResponse(number, size, total),
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
            entity -> {
              return Category.builder()
                  .setId(entity.get(idAttribute))
                  .setLabel(entity.get(labelAttribute).toString())
                  .build();
            })
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

  private Attribute toAttribute(
      CreateAttributeRequest attributeRequest, EntityType entityType, int index) {
    Attribute attribute = attributeFactory.create();

    attribute.setIdentifier(attributeRequest.getId());
    attribute.setName(attributeRequest.getName());
    attribute.setEntity(entityType);
    Integer sequenceNumber = attributeRequest.getSequenceNumber();
    if (sequenceNumber == null) {
      sequenceNumber = index;
    }
    attribute.setSequenceNumber(sequenceNumber);
    attribute.setDataType(AttributeType.toEnum(attributeRequest.getType()));

    EntityType refEntityType;
    String refEntityTypeId = attributeRequest.getRefEntityType();
    if (refEntityTypeId != null) {
      refEntityType = (EntityType) entityManager.getReference(entityTypeMetadata, refEntityTypeId);
      attribute.setRefEntity(refEntityType);
    }
    if (attributeRequest.isCascadeDelete() != null) {
      attribute.setCascadeDelete(attributeRequest.isCascadeDelete());
    }
    String orderBy = attributeRequest.getOrderBy();
    attribute.setOrderBy(orderBy != null ? sortMapper.map(sortConverter.convert(orderBy)) : null);
    attribute.setExpression(attributeRequest.getExpression());
    attribute.setNillable(attributeRequest.isNullable());
    attribute.setAuto(attributeRequest.isAuto());
    attribute.setVisible(attributeRequest.isVisible());
    processI18nLabel(attributeRequest.getLabel(), attribute);
    processI18nDescription(attributeRequest, attribute);
    attribute.setAggregatable(attributeRequest.isAggregatable());
    attribute.setEnumOptions(attributeRequest.getEnumOptions());
    Range range = attributeRequest.getRange();
    if (range != null) {
      attribute.setRange(map(range));
    }
    attribute.setReadOnly(attributeRequest.isReadonly());
    attribute.setUnique(attributeRequest.isUnique());
    attribute.setNullableExpression(attributeRequest.getNullableExpression());
    attribute.setVisibleExpression(attributeRequest.getVisibleExpression());
    attribute.setValidationExpression(attributeRequest.getValidationExpression());
    attribute.setDefaultValue(attributeRequest.getDefaultValue());
    return attribute;
  }

  Map<String, Attribute> toAttributes(List<Map<String,Object>> attributeValueMaps, EntityType entityType){
    Map<String, Attribute> attributes = new HashMap<>();
    int index = 0;
    for(Map<String,Object> attributeValueMap : attributeValueMaps){
      Attribute attr = toAttribute(attributeValueMap, entityType);
      if(attr.getSequenceNumber() == null){
        attr.setSequenceNumber(index);
      }
      attributes.put(attr.getIdentifier(), attr);
      index++;
    }
    for(Map<String,Object> attributeValueMap : attributeValueMaps){
      processParentAndMappedby(attributeValueMap, attributes);
    }
    return attributes;
  }

  private void processParentAndMappedby(Map<String, Object> attributeRequest, Map<String, Attribute> attributes) {
    String attributeId = attributeRequest.get("id").toString();
    for(Entry<String, Object> entry : attributeRequest.entrySet()){
      if (entry.getKey().equals("mappedByAttribute")) {
        String mappedByAttrId = getStringValue(entry.getValue());
        Attribute mappedByAttr = attributes.get(mappedByAttrId);
        Attribute attribute = attributes.get(attributeId);
        attribute.setMappedBy(mappedByAttr);
      }else if (entry.getKey().equals("parent")) {
        String parentId = getStringValue(entry.getValue());
        Attribute parentAttr = attributes.get(parentId);
        Attribute attribute = attributes.get(attributeId);
        attribute.setMappedBy(parentAttr);
      }
    }
  }

  private Attribute toAttribute(
      Map<String, Object> attributeRequest, EntityType entityType) {
    Attribute attribute = attributeFactory.create();
    attribute.setEntity(entityType);

    for(Entry<String, Object> entry : attributeRequest.entrySet()){
        switch (entry.getKey()) {
          case "id":
            attribute.setIdentifier(getStringValue(entry.getValue()));
            break;
          case "name":
            attribute.setName(getStringValue(entry.getValue()));
            break;
          case "sequenceNumber":
            String sequenceString = getStringValue(entry.getValue());
            if (sequenceString != null) {
              attribute.setSequenceNumber(Integer.valueOf(sequenceString));
            }else{
              //FIXME: coded exception
              throw new RuntimeException("invalid value for "+ entry.getKey());
            }
            break;
          case "type":
            String typeString = getStringValue(entry.getValue());
            if (typeString != null) {
              attribute.setDataType(AttributeType.toEnum(typeString));
            }else{
              //FIXME: coded exception
              throw new RuntimeException("invalid value for "+ entry.getKey());
            }
            break;
          case "refEntityType":
            EntityType refEntityType;
            String refEntityTypeId = getStringValue(entry.getValue());
            if (refEntityTypeId != null) {
              refEntityType = (EntityType) entityManager
                  .getReference(entityTypeMetadata, refEntityTypeId);
              attribute.setRefEntity(refEntityType);
            }
            break;
          case "cascadeDelete":
            String cascadeValue = getStringValue(entry.getValue());
            if(cascadeValue!=null) {
              attribute.setCascadeDelete(Boolean.valueOf(cascadeValue));
            }else{
              attribute.setCascadeDelete(null);
            }
            break;
          case "orderBy":
            String orderBy = getStringValue(entry.getValue());
            attribute
                .setOrderBy(orderBy != null ? sortMapper.map(sortConverter.convert(orderBy)) : null);
            break;
          case "expression":
            attribute.setExpression(getStringValue(entry.getValue()));
            break;
          case "nullable":
            String nullableValue = getStringValue(entry.getValue());
            if(nullableValue!=null) {
              attribute.setCascadeDelete(Boolean.valueOf(nullableValue));
            }else{
              attribute.setCascadeDelete(null);
            }
            break;
          case "auto":
            String autoValue = getStringValue(entry.getValue());
            if(autoValue!=null) {
              attribute.setCascadeDelete(Boolean.valueOf(autoValue));
            }else{
              attribute.setCascadeDelete(null);
            }
            break;
          case "visible":
            String visibleValue = getStringValue(entry.getValue());
            if(visibleValue!=null) {
              attribute.setCascadeDelete(Boolean.valueOf(visibleValue));
            }else{
              attribute.setCascadeDelete(null);
            }
            break;
          case "label":
            I18nValue i18Label= mapI18nValue(entry.getValue());
            processI18nLabel(i18Label,attribute);
            break;
          case "description":
            I18nValue i18Description = mapI18nValue(entry.getValue());
            processI18nLabel(i18Description,attribute);
            break;
          case "aggregatable":
            attribute.setAggregatable(Boolean.valueOf(entry.getValue().toString()));
            break;
          case "enumOptions":
            List<String> options = null;
            if(options != null){
              if(entry.getValue() instanceof List){
              options = (List<String>) entry.getValue();
              }else{
                //FIXME: coded exception
                throw new RuntimeException("invalid value for "+ entry.getKey());
              }
            }
            attribute.setEnumOptions(options);
            break;
          case "range":
            Range range = mapRange(entry.getValue());
            if (range != null) {
              attribute.setRange(map(range));
            }
            break;
          case "readonly":
            String readOnlyValue = getStringValue(entry.getValue());
            if(readOnlyValue!=null) {
              attribute.setReadOnly(Boolean.valueOf(readOnlyValue));
            }else{
              //FIXME: coded exception
              throw new RuntimeException("invalid value for "+ entry.getKey());
            }
            break;
          case "unique":
            String uniqueValue = getStringValue(entry.getValue());
            if(uniqueValue!=null) {
              attribute.setUnique(Boolean.valueOf(uniqueValue));
            }else{
              //FIXME: coded exception
              throw new RuntimeException("invalid value for "+ entry.getKey());
            }
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
          case "mappedBy":
          case "parent":
            //Skip now and process after all attributes in the request have been processed
            break;
          default:
            //FIXME: coded exception
            throw new RuntimeException("not a attr value: "+entry.getKey());
        }
      }
    return attribute;
  }


  private String getStringValue(Object value) {
    return value != null ? value.toString():null;
  }

  //FIXME: move to utility class
  I18nValue mapI18nValue(Object value) {
    I18nValue.Builder builder = I18nValue.builder();
    if(value!=null && value instanceof Map){
      Map valueMap = (Map)value;
      Object defaultValue = valueMap.get("defaultValue");
      if(defaultValue != null){
        builder.setDefaultValue(defaultValue.toString());
      }
      Object translations = valueMap.get("translations");
      if(translations!=null && translations instanceof Map) {
        Map<?,?> translationsMap = (Map)translations;
        Map<String, String> typedTranslations = new HashMap<>();
        for(Entry entry : translationsMap.entrySet()){
         if(entry.getKey() != null && entry.getValue() != null){
           typedTranslations.put(entry.getKey().toString(),entry.getValue().toString());
         }
        }
        builder.setTranslations(typedTranslations);
      }
      }
    return builder.build();
  }

  private Range mapRange(Object value) {
    Long minLong = null;
    Long maxLong = null;
    if(value!=null && value instanceof Map) {
      Map valueMap = (Map) value;
      Object min = valueMap.get("min");
      if(min != null){
        minLong = Double.valueOf(min.toString()).longValue();
      }
      Object max = valueMap.get("max");
      if(max != null){
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
      if (attributeRequest.getId().equals(entityTypeRequest.getIdAttribute())) {
        attr.setIdAttribute(true);
      }
      Attribute labelAttribute = entityType.getLabelAttribute();
      if (labelAttribute != null
          && attributeRequest.getId().equals(labelAttribute.getIdentifier())) {
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
    UriComponentsBuilder builder =
        MolgenisServletUriComponentsBuilder.fromCurrentRequestDecodedQuery();
    return builder.build().toUri();
  }

  private URI createEntitiesResponseUri(Integer pageNumber) {
    UriComponentsBuilder builder =
        MolgenisServletUriComponentsBuilder.fromCurrentRequestDecodedQuery();
    if (pageNumber != null) {
      builder.replaceQueryParam(PAGE, pageNumber);
    }
    return builder.build().toUri();
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

  private void processI18nDescription(
      CreateAttributeRequest attributeRequest, Attribute attribute) {
    I18nValue i18nValue = attributeRequest.getDescription();
    if (i18nValue != null) {
      attribute.setDescription(i18nValue.getDefaultValue());
      Map<String, String> translations = i18nValue.getTranslations();
      if (translations != null) {
        getLanguageCodes()
            .forEach(
                languageCode -> {
                  attribute.setDescription(languageCode, translations.get(languageCode));
                });
      }
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
}
