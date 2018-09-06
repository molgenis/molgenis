package org.molgenis.semanticsearch.explain.bean;

import com.google.auto.value.AutoValue;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeMetadata;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_ExplainedAttribute.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class ExplainedAttributeDto {
  public static ExplainedAttributeDto create(Attribute attribute) {
    return new AutoValue_ExplainedAttributeDto(
        attributeToMap(attribute), Collections.emptySet(), false);
  }

  public static ExplainedAttributeDto create(
      Attribute attribute,
      Iterable<ExplainedQueryString> explainedQueryStrings,
      boolean highQuality) {
    return new AutoValue_ExplainedAttributeDto(
        attributeToMap(attribute), Sets.newHashSet(explainedQueryStrings), highQuality);
  }

  public abstract Map<String, Object> getAttribute();

  public abstract Set<ExplainedQueryString> getExplainedQueryStrings();

  public abstract boolean isHighQuality();

  private static Map<String, Object> attributeToMap(Attribute attribute) {
    Map<String, Object> map = new HashMap<>();
    map.put(AttributeMetadata.NAME, attribute.getName());
    map.put(AttributeMetadata.LABEL, attribute.getLabel());
    map.put(AttributeMetadata.DESCRIPTION, attribute.getDescription());
    map.put(AttributeMetadata.TYPE, attribute.getDataType().toString());
    map.put(AttributeMetadata.IS_NULLABLE, attribute.isNillable());
    map.put(AttributeMetadata.IS_UNIQUE, attribute.isUnique());
    if (attribute.getRefEntity() != null) {
      map.put(AttributeMetadata.REF_ENTITY_TYPE, attribute.getRefEntity().getId());
    }
    return map;
  }
}
