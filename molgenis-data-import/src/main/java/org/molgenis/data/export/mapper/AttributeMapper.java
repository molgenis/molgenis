package org.molgenis.data.export.mapper;

import static java.util.stream.Collectors.joining;
import static org.molgenis.data.importer.emx.EmxMetadataParser.AUTO;
import static org.molgenis.data.importer.emx.EmxMetadataParser.EMX_ATTRIBUTES_AGGREGATEABLE;
import static org.molgenis.data.importer.emx.EmxMetadataParser.EMX_ATTRIBUTES_DATA_TYPE;
import static org.molgenis.data.importer.emx.EmxMetadataParser.EMX_ATTRIBUTES_DEFAULT_VALUE;
import static org.molgenis.data.importer.emx.EmxMetadataParser.EMX_ATTRIBUTES_DESCRIPTION;
import static org.molgenis.data.importer.emx.EmxMetadataParser.EMX_ATTRIBUTES_ENTITY;
import static org.molgenis.data.importer.emx.EmxMetadataParser.EMX_ATTRIBUTES_ENUM_OPTIONS;
import static org.molgenis.data.importer.emx.EmxMetadataParser.EMX_ATTRIBUTES_EXPRESSION;
import static org.molgenis.data.importer.emx.EmxMetadataParser.EMX_ATTRIBUTES_ID_ATTRIBUTE;
import static org.molgenis.data.importer.emx.EmxMetadataParser.EMX_ATTRIBUTES_LABEL;
import static org.molgenis.data.importer.emx.EmxMetadataParser.EMX_ATTRIBUTES_LABEL_ATTRIBUTE;
import static org.molgenis.data.importer.emx.EmxMetadataParser.EMX_ATTRIBUTES_LOOKUP_ATTRIBUTE;
import static org.molgenis.data.importer.emx.EmxMetadataParser.EMX_ATTRIBUTES_MAPPED_BY;
import static org.molgenis.data.importer.emx.EmxMetadataParser.EMX_ATTRIBUTES_NAME;
import static org.molgenis.data.importer.emx.EmxMetadataParser.EMX_ATTRIBUTES_NILLABLE;
import static org.molgenis.data.importer.emx.EmxMetadataParser.EMX_ATTRIBUTES_PART_OF_ATTRIBUTE;
import static org.molgenis.data.importer.emx.EmxMetadataParser.EMX_ATTRIBUTES_RANGE_MAX;
import static org.molgenis.data.importer.emx.EmxMetadataParser.EMX_ATTRIBUTES_RANGE_MIN;
import static org.molgenis.data.importer.emx.EmxMetadataParser.EMX_ATTRIBUTES_READ_ONLY;
import static org.molgenis.data.importer.emx.EmxMetadataParser.EMX_ATTRIBUTES_REF_ENTITY;
import static org.molgenis.data.importer.emx.EmxMetadataParser.EMX_ATTRIBUTES_TAGS;
import static org.molgenis.data.importer.emx.EmxMetadataParser.EMX_ATTRIBUTES_UNIQUE;
import static org.molgenis.data.importer.emx.EmxMetadataParser.EMX_ATTRIBUTES_VALIDATION_EXPRESSION;
import static org.molgenis.data.importer.emx.EmxMetadataParser.EMX_ATTRIBUTES_VISIBLE;

import com.google.common.base.Strings;
import com.google.common.collect.Streams;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeMetadata;
import org.molgenis.data.meta.model.Tag;

public class AttributeMapper {

  public static Map<String, String> ATTRIBUTE_ATTRS;

  static {
    ATTRIBUTE_ATTRS = new LinkedHashMap<>();
    ATTRIBUTE_ATTRS.put(EMX_ATTRIBUTES_NAME, AttributeMetadata.NAME);
    ATTRIBUTE_ATTRS.put(EMX_ATTRIBUTES_LABEL, AttributeMetadata.LABEL);
    ATTRIBUTE_ATTRS.put(EMX_ATTRIBUTES_DESCRIPTION, AttributeMetadata.DESCRIPTION);
    ATTRIBUTE_ATTRS.put(EMX_ATTRIBUTES_ENTITY, AttributeMetadata.ENTITY);
    ATTRIBUTE_ATTRS.put(EMX_ATTRIBUTES_DATA_TYPE, AttributeMetadata.TYPE);
    ATTRIBUTE_ATTRS.put(EMX_ATTRIBUTES_REF_ENTITY, AttributeMetadata.REF_ENTITY_TYPE);
    ATTRIBUTE_ATTRS.put(EMX_ATTRIBUTES_NILLABLE, AttributeMetadata.IS_NULLABLE);
    ATTRIBUTE_ATTRS.put(EMX_ATTRIBUTES_UNIQUE, AttributeMetadata.IS_UNIQUE);
    ATTRIBUTE_ATTRS.put(EMX_ATTRIBUTES_VISIBLE, AttributeMetadata.IS_VISIBLE);
    ATTRIBUTE_ATTRS.put(EMX_ATTRIBUTES_ID_ATTRIBUTE, AttributeMetadata.IS_ID_ATTRIBUTE);
    ATTRIBUTE_ATTRS.put(EMX_ATTRIBUTES_LABEL_ATTRIBUTE, AttributeMetadata.IS_LABEL_ATTRIBUTE);
    ATTRIBUTE_ATTRS.put(EMX_ATTRIBUTES_READ_ONLY, AttributeMetadata.IS_READ_ONLY);
    ATTRIBUTE_ATTRS.put(EMX_ATTRIBUTES_AGGREGATEABLE, AttributeMetadata.IS_AGGREGATABLE);
    ATTRIBUTE_ATTRS.put(EMX_ATTRIBUTES_LOOKUP_ATTRIBUTE, AttributeMetadata.LOOKUP_ATTRIBUTE_INDEX);
    ATTRIBUTE_ATTRS.put(EMX_ATTRIBUTES_ENUM_OPTIONS, AttributeMetadata.ENUM_OPTIONS);
    ATTRIBUTE_ATTRS.put(EMX_ATTRIBUTES_PART_OF_ATTRIBUTE, AttributeMetadata.PARENT);
    ATTRIBUTE_ATTRS.put(EMX_ATTRIBUTES_RANGE_MAX, AttributeMetadata.RANGE_MAX);
    ATTRIBUTE_ATTRS.put(EMX_ATTRIBUTES_RANGE_MIN, AttributeMetadata.RANGE_MIN);
    ATTRIBUTE_ATTRS.put(EMX_ATTRIBUTES_MAPPED_BY, AttributeMetadata.MAPPED_BY);
    ATTRIBUTE_ATTRS.put(EMX_ATTRIBUTES_EXPRESSION, AttributeMetadata.EXPRESSION);
    ATTRIBUTE_ATTRS.put(
        EMX_ATTRIBUTES_VALIDATION_EXPRESSION, AttributeMetadata.VALIDATION_EXPRESSION);
    ATTRIBUTE_ATTRS.put(EMX_ATTRIBUTES_DEFAULT_VALUE, AttributeMetadata.DEFAULT_VALUE);
    ATTRIBUTE_ATTRS.put(EMX_ATTRIBUTES_TAGS, AttributeMetadata.TAGS);
  }

  private AttributeMapper() {}

  public static List<Object> map(Attribute attr) {
    List<Object> row = new ArrayList<>();
    for (Entry<String, String> entry : ATTRIBUTE_ATTRS.entrySet()) {
      switch (entry.getKey()) {
        case EMX_ATTRIBUTES_ID_ATTRIBUTE:
          row.add(getIdAttrValue(attr));
          break;
        case EMX_ATTRIBUTES_TAGS:
          row.add(getTagsValue(attr));
          break;
        case EMX_ATTRIBUTES_ENTITY:
          row.add(attr.getEntity() != null ? attr.getEntity().getId() : "");
          break;
        case EMX_ATTRIBUTES_REF_ENTITY:
          row.add(attr.getRefEntity() != null ? attr.getRefEntity().getId() : "");
          break;
        case EMX_ATTRIBUTES_LOOKUP_ATTRIBUTE:
          row.add(getLookupValue(attr));
          break;
        case EMX_ATTRIBUTES_NILLABLE:
          row.add(getNullableValue(attr));
          break;
        case EMX_ATTRIBUTES_VISIBLE:
          row.add(getVisibleValue(attr));
          break;
        case EMX_ATTRIBUTES_PART_OF_ATTRIBUTE:
          row.add(getPartOfValue(attr));
          break;
        case EMX_ATTRIBUTES_MAPPED_BY:
          row.add(getMappedByValue(attr));
          break;
        case EMX_ATTRIBUTES_ENUM_OPTIONS:
          row.add(getEnumOptions(attr));
          break;
        default:
          Object value = attr.get(entry.getValue());
          row.add(value != null ? value.toString() : "");
      }
    }
    return row;
  }

  private static String getMappedByValue(Attribute attr) {
    Attribute mappedBy = attr.getMappedBy();
    return mappedBy != null ? mappedBy.getName() : "";
  }

  private static String getEnumOptions(Attribute attr) {

    return attr.getEnumOptions() != null ? String.join(",", attr.getEnumOptions()) : "";
  }

  private static String getLookupValue(Attribute attr) {
    Boolean lookup = attr.getLookupAttributeIndex() != null;
    return lookup.toString();
  }

  private static Object getTagsValue(Attribute attr) {
    return Streams.stream(attr.getTags()).map(Tag::getId).collect(joining(","));
  }

  private static Object getVisibleValue(Attribute attr) {
    String visibleExpression = attr.getVisibleExpression();
    return Strings.isNullOrEmpty(visibleExpression)
        ? String.valueOf(attr.isVisible())
        : visibleExpression;
  }

  private static Object getNullableValue(Attribute attr) {
    String nullableExpression = attr.getNullableExpression();
    return Strings.isNullOrEmpty(nullableExpression)
        ? String.valueOf(attr.isNillable())
        : nullableExpression;
  }

  private static String getPartOfValue(Attribute attr) {
    Attribute partOfAttribute = attr.getParent();
    return partOfAttribute != null ? partOfAttribute.getName() : "";
  }

  private static Object getIdAttrValue(Attribute attr) {
    Object isId = String.valueOf(attr.isIdAttribute());
    if (attr.isAuto()) {
      isId = AUTO;
    }
    return isId;
  }
}
