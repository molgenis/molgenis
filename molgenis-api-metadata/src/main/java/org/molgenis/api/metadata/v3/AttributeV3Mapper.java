package org.molgenis.api.metadata.v3;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.AttributeType.getValueString;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.molgenis.api.convert.SortConverter;
import org.molgenis.api.metadata.v3.model.AttributeResponse;
import org.molgenis.api.metadata.v3.model.AttributeResponseData;
import org.molgenis.api.metadata.v3.model.AttributeResponseData.Builder;
import org.molgenis.api.metadata.v3.model.AttributeSort;
import org.molgenis.api.metadata.v3.model.AttributeSort.Order.Direction;
import org.molgenis.api.metadata.v3.model.AttributesResponse;
import org.molgenis.api.metadata.v3.model.CreateAttributeRequest;
import org.molgenis.api.metadata.v3.model.CreateEntityTypeRequest;
import org.molgenis.api.metadata.v3.model.I18nValue;
import org.molgenis.api.metadata.v3.model.Range;
import org.molgenis.api.model.response.LinksResponse;
import org.molgenis.api.model.response.PageResponse;
import org.molgenis.data.EntityManager;
import org.molgenis.data.Sort;
import org.molgenis.data.Sort.Order;
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

@Component
public class AttributeV3Mapper {
  public static final String ATTRIBUTES = "attributes";
  public static final String PAGE = "page";

  private final EntityTypeFactory entityTypeFactory;
  private final AttributeFactory attributeFactory;
  private final MetaDataService metaDataService;
  private final SortMapper sortMapper;
  private final SortConverter sortConverter;
  private final EntityManager entityManager;
  private final EntityTypeMetadata entityTypeMetadata;

  public AttributeV3Mapper(
      EntityTypeFactory entityTypeFactory,
      AttributeFactory attributeFactory,
      MetaDataService metaDataService,
      SortMapper sortMapper,
      SortConverter sortConverter,
      EntityManager entityManager,
      EntityTypeMetadata entityTypeMetadata) {
    this.entityTypeFactory = requireNonNull(entityTypeFactory);
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
            .collect(Collectors.toList()),
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
    builder.setOrderBy(map(attr));
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
    builder.setRange(Range.create(attr.getRangeMin(), attr.getRangeMax()));
    Attribute parent = attr.getParent();
    builder.setParentAttributeId(parent != null ? parent.getIdentifier() : null);
    builder.setNullableExpression(attr.getNullableExpression());
    builder.setVisibleExpression(attr.getVisibleExpression());
    builder.setValidationExpression(attr.getValidationExpression());
    builder.setDefaultValue(attr.getDefaultValue());

    return builder.build();
  }

  private List<AttributeSort> map(Attribute attr) {
    List<AttributeSort> orders = new ArrayList<>();

    Sort sort = attr.getOrderBy();
    for (Order order : sort) {
      orders.add(
          AttributeSort.create(order.getAttr(), Direction.valueOf(order.getDirection().name())));
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
      if (attributeRequest.getId().equals(entityType.getLabelAttribute())) {
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
}
