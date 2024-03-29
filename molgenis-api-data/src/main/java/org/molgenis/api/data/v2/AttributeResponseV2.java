package org.molgenis.api.data.v2;

import static com.google.common.collect.Streams.stream;
import static org.molgenis.data.meta.AttributeType.COMPOUND;
import static org.molgenis.util.i18n.LanguageService.getCurrentUserLanguageCode;

import com.google.common.collect.Streams;
import java.util.List;
import java.util.stream.Collectors;
import org.molgenis.data.DataService;
import org.molgenis.data.Fetch;
import org.molgenis.data.Range;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.util.EntityTypeUtils;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.springframework.web.util.UriComponentsBuilder;

class AttributeResponseV2 {
  private final String href;
  private final AttributeType fieldType;
  private final String name;
  private final String label;
  private final String description;
  private final List<?> attributes;
  private final List<String> enumOptions;
  private final Integer maxLength;
  private final Object refEntity;
  private final String mappedBy;
  private final Boolean auto;
  private final Boolean nillable;
  private final Boolean readOnly;
  private final Object defaultValue;
  private final Boolean labelAttribute;
  private final Boolean unique;
  private final Boolean visible;
  private Boolean lookupAttribute;
  private Boolean isAggregatable;
  private Range range;
  private String expression;
  private String nullableExpression;
  private String visibleExpression;
  private String validationExpression;
  private List<CategoricalOptionV2> categoricalOptions;
  private List<TagResponseV2> tags;

  /**
   * Constructs AttributeResponseV2 using params
   *
   * @param fetch set of lowercase attribute names to include in response
   * @param includeCategories if set to true includes options list for CATEGORICAL and
   *     CATEGORICAL_MREF types in the attribute metadata
   */
  public AttributeResponseV2(
      UriComponentsBuilder uriBuilder,
      final String entityParentName,
      EntityType entityType,
      Attribute attr,
      Fetch fetch,
      UserPermissionEvaluator permissionService,
      DataService dataService,
      boolean includeCategories) {
    String attrName = attr.getName();
    this.href =
        UriUtils.createEntityTypeMetadataAttributeUriPath(uriBuilder, entityParentName, attrName);

    this.fieldType = attr.getDataType();
    this.name = attrName;
    this.label = attr.getLabel(getCurrentUserLanguageCode());
    this.description = attr.getDescription(getCurrentUserLanguageCode());
    this.enumOptions = attr.getDataType() == AttributeType.ENUM ? attr.getEnumOptions() : null;
    this.maxLength = attr.getMaxLength();
    this.expression = attr.getExpression();

    if (attr.hasRefEntity()) {
      EntityType refEntity = attr.getRefEntity();
      this.refEntity =
          new EntityTypeResponseV2(
              uriBuilder, refEntity, fetch, permissionService, dataService, includeCategories);

      if (includeCategories
          && (this.fieldType == AttributeType.CATEGORICAL
              || this.fieldType == AttributeType.CATEGORICAL_MREF)) {
        this.categoricalOptions =
            CategoricalUtils.getCategoricalOptionsForRefEntity(
                dataService, refEntity, getCurrentUserLanguageCode());
      }
    } else {
      this.refEntity = null;
    }

    this.tags =
        Streams.stream(attr.getTags())
            .map(
                tag ->
                    TagResponseV2.create(
                        tag.getRelationIri(),
                        tag.getRelationLabel(),
                        tag.getObjectIri(),
                        tag.getLabel()))
            .collect(Collectors.toList());

    Attribute mappedByAttr = attr.getMappedBy();
    this.mappedBy = mappedByAttr != null ? mappedByAttr.getName() : null;

    Iterable<Attribute> attrParts = attr.getChildren();
    if (attrParts != null) {
      // filter attribute parts
      attrParts = filterAttributes(fetch, attrParts);
      this.attributes =
          stream(attrParts)
              .map(
                  attrPart -> {
                    Fetch subAttrFetch;
                    if (fetch != null) {
                      if (attrPart.getDataType() == AttributeType.COMPOUND) {
                        subAttrFetch = fetch;
                      } else {
                        subAttrFetch = fetch.getFetch(attrPart);
                      }
                    } else if (EntityTypeUtils.isReferenceType(attrPart)) {
                      subAttrFetch =
                          AttributeFilterToFetchConverter.createDefaultAttributeFetch(
                              attrPart, getCurrentUserLanguageCode());
                    } else {
                      subAttrFetch = null;
                    }
                    return new AttributeResponseV2(
                        uriBuilder,
                        entityParentName,
                        entityType,
                        attrPart,
                        subAttrFetch,
                        permissionService,
                        dataService,
                        includeCategories);
                  })
              .collect(Collectors.toList());
    } else {
      this.attributes = null;
    }

    this.auto = attr.isAuto();
    this.nillable = attr.isNillable();
    this.readOnly = attr.isReadOnly();
    this.defaultValue = attr.getDefaultValue();
    this.labelAttribute = attr.equals(entityType.getLabelAttribute());
    this.unique = attr.isUnique();
    this.lookupAttribute = entityType.getLookupAttribute(attr.getName()) != null;
    this.isAggregatable = attr.isAggregatable();
    this.range = attr.getRange();
    this.visible = attr.isVisible();
    this.nullableExpression = attr.getNullableExpression();
    this.visibleExpression = attr.getVisibleExpression();
    this.validationExpression = attr.getValidationExpression();
  }

  /**
   * Default AttributeResponseV2 with @param includeCategories set to false
   *
   * @param fetch set of lowercase attribute names to include in response
   */
  public AttributeResponseV2(
      UriComponentsBuilder uriBuilder,
      final String entityParentName,
      EntityType entityType,
      Attribute attr,
      Fetch fetch,
      UserPermissionEvaluator permissionService,
      DataService dataService) {
    this(
        uriBuilder,
        entityParentName,
        entityType,
        attr,
        fetch,
        permissionService,
        dataService,
        false);
  }

  public static Iterable<Attribute> filterAttributes(Fetch fetch, Iterable<Attribute> attrs) {
    if (fetch != null) {
      return stream(attrs)
          .filter(attr -> filterAttributeRec(fetch, attr))
          .collect(Collectors.toList());
    } else {
      return attrs;
    }
  }

  public static boolean filterAttributeRec(Fetch fetch, Attribute attr) {
    if (attr.getDataType() == COMPOUND) {
      for (Attribute attrPart : attr.getChildren()) {
        if (filterAttributeRec(fetch, attrPart)) {
          return true;
        }
      }
      return false;
    } else {
      return fetch.hasField(attr);
    }
  }

  public String getHref() {
    return href;
  }

  public AttributeType getFieldType() {
    return fieldType;
  }

  public String getName() {
    return name;
  }

  public String getLabel() {
    return label;
  }

  public String getDescription() {
    return description;
  }

  public List<?> getAttributes() {
    return attributes;
  }

  public List<String> getEnumOptions() {
    return enumOptions;
  }

  public Integer getMaxLength() {
    return maxLength;
  }

  public Object getRefEntity() {
    return refEntity;
  }

  public String getMappedBy() {
    return mappedBy;
  }

  public boolean isAuto() {
    return auto;
  }

  public boolean isNillable() {
    return nillable;
  }

  public boolean isReadOnly() {
    return readOnly;
  }

  public Object getDefaultValue() {
    return defaultValue;
  }

  public boolean isLabelAttribute() {
    return labelAttribute;
  }

  public boolean isUnique() {
    return unique;
  }

  public boolean isVisible() {
    return visible;
  }

  public Boolean getLookupAttribute() {
    return lookupAttribute;
  }

  public Boolean isAggregatable() {
    return isAggregatable;
  }

  public Range getRange() {
    return range;
  }

  public String getExpression() {
    return expression;
  }

  public String getNullableExpression() {
    return nullableExpression;
  }

  public String getVisibleExpression() {
    return visibleExpression;
  }

  public String getValidationExpression() {
    return validationExpression;
  }

  public List<CategoricalOptionV2> getCategoricalOptions() {
    return categoricalOptions;
  }

  public List<TagResponseV2> getTags() {
    return tags;
  }
}
