package org.molgenis.semanticsearch.explain.bean;

import com.google.auto.value.AutoValue;
import com.google.common.collect.Sets;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeMetadata;
import org.molgenis.util.AutoGson;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@AutoValue
@AutoGson(autoValueClass = AutoValue_ExplainedAttribute.class)
public abstract class ExplainedAttribute
{
	public static ExplainedAttribute create(Attribute attribute)
	{
		return new AutoValue_ExplainedAttribute(attributeToMap(attribute), Collections.emptySet(), false);
	}

	public static ExplainedAttribute create(Attribute attribute, Iterable<ExplainedQueryString> explainedQueryStrings,
			boolean highQuality)
	{
		return new AutoValue_ExplainedAttribute(attributeToMap(attribute), Sets.newHashSet(explainedQueryStrings),
				highQuality);
	}

	public abstract Map<String, Object> getAttribute();

	public abstract Set<ExplainedQueryString> getExplainedQueryStrings();

	public abstract boolean isHighQuality();

	private static Map<String, Object> attributeToMap(Attribute attribute)
	{
		Map<String, Object> map = new HashMap<>();
		map.put(AttributeMetadata.NAME, attribute.getName());
		map.put(AttributeMetadata.LABEL, attribute.getLabel());
		map.put(AttributeMetadata.DESCRIPTION, attribute.getDescription());
		map.put(AttributeMetadata.TYPE, attribute.getDataType().toString());
		map.put(AttributeMetadata.IS_NULLABLE, attribute.isNillable());
		map.put(AttributeMetadata.IS_UNIQUE, attribute.isUnique());
		if (attribute.getRefEntity() != null)
		{
			map.put(AttributeMetadata.REF_ENTITY_TYPE, attribute.getRefEntity().getId());
		}
		return map;
	}
}