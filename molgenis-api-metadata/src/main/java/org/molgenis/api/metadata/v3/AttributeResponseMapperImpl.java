package org.molgenis.api.metadata.v3;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.data.meta.AttributeType.ONE_TO_MANY;
import static org.molgenis.data.meta.AttributeType.getValueString;
import static org.molgenis.data.meta.model.AttributeMetadata.DESCRIPTION;
import static org.molgenis.data.meta.model.AttributeMetadata.LABEL;
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
import org.molgenis.api.metadata.v3.model.AttributeResponse;
import org.molgenis.api.metadata.v3.model.AttributeResponseData;
import org.molgenis.api.metadata.v3.model.AttributeResponseData.Builder;
import org.molgenis.api.metadata.v3.model.AttributesResponse;
import org.molgenis.api.metadata.v3.model.Category;
import org.molgenis.api.metadata.v3.model.I18nValue;
import org.molgenis.api.metadata.v3.model.Range;
import org.molgenis.api.model.Sort.Order.Direction;
import org.molgenis.api.model.response.LinksResponse;
import org.molgenis.api.model.response.PageResponse;
import org.molgenis.api.support.LinksUtils;
import org.molgenis.data.Entity;
import org.molgenis.data.InvalidValueTypeException;
import org.molgenis.data.Repository;
import org.molgenis.data.Sort;
import org.molgenis.data.Sort.Order;
import org.molgenis.data.UnknownRepositoryException;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.util.EntityTypeUtils;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
class AttributeResponseMapperImpl implements AttributeResponseMapper {
  private static final String ATTRIBUTES = "attributes";

  private final MetaDataService metaDataService;

  AttributeResponseMapperImpl(MetaDataService metaDataService) {
    this.metaDataService = requireNonNull(metaDataService);
  }

  @Override
  public AttributesResponse toAttributesResponse(Attributes attributes, int size, int number) {
    int total = attributes.getTotal();
    return AttributesResponse.create(
        LinksUtils.createLinksResponse(number, size, total),
        attributes.getAttributes().stream()
            .map(attribute -> this.toAttributeResponse(attribute, false))
            .collect(toList()),
        PageResponse.create(size, total, total / size, number));
  }

  @Override
  public List<AttributeResponse> mapInternal(
      Iterable<org.molgenis.data.meta.model.Attribute> allAttributes, boolean i18n) {
    List<AttributeResponse> result = new ArrayList<>();
    for (Attribute attr : allAttributes) {
      result.add(toAttributeResponse(attr, i18n));
    }
    return result;
  }

  @Override
  public AttributeResponse toAttributeResponse(Attribute attr, boolean i18n) {
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
    builder.setMappedBy(
        attr.getMappedBy() != null ? toAttributeResponse(attr.getMappedBy(), i18n) : null);
    if (attr.getDataType() == ONE_TO_MANY && attr.isMappedBy()) {
      builder.setOrderBy(map(attr));
    }
    builder.setLabel(attr.getLabel(LocaleContextHolder.getLocale().getLanguage()));
    builder.setDescription(attr.getDescription(LocaleContextHolder.getLocale().getLanguage()));
    if (i18n) {
      builder.setLabelI18n(getI18nAttrLabel(attr));
      getI18nAttrDesc(attr).ifPresent(builder::setDescriptionI18n);
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

  private I18nValue getI18nAttrLabel(Attribute attr) {
    String defaultValue = attr.getLabel();
    ImmutableMap<String, String> translations = MetadataUtils.getI18n(attr, LABEL);
    return I18nValue.create(defaultValue, translations);
  }

  private Optional<I18nValue> getI18nAttrDesc(Attribute attr) {
    String defaultValue = attr.getDescription();
    if (defaultValue == null) {
      return Optional.empty();
    }
    ImmutableMap<String, String> translations = MetadataUtils.getI18n(attr, DESCRIPTION);
    return Optional.of(I18nValue.create(defaultValue, translations));
  }
}
