package org.molgenis.data.semanticsearch.explain.bean;

import com.google.auto.value.AutoValue;
import com.google.common.collect.Sets;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.gson.AutoGson;

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
		return new AutoValue_ExplainedAttribute(attributeToMap(attribute), Collections.emptySet(),
				false);
	}

	public static ExplainedAttribute create(Attribute attribute,
			Iterable<ExplainedQueryString> explainedQueryStrings, boolean highQuality)
	{
		return new AutoValue_ExplainedAttribute(attributeToMap(attribute),
				Sets.newHashSet(explainedQueryStrings), highQuality);
	}

	public abstract Map<String, Object> getAttributeMetaData();

	public abstract Set<ExplainedQueryString> getExplainedQueryStrings();

	public abstract boolean isHighQuality();

	private static Map<String, Object> attributeToMap(Attribute attribute)
	{
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(AttributeMetaData.NAME, attribute.getName());
		map.put(AttributeMetaData.LABEL, attribute.getLabel());
		map.put(AttributeMetaData.DESCRIPTION, attribute.getDescription());
		map.put(AttributeMetaData.DATA_TYPE, attribute.getDataType().toString());
		map.put(AttributeMetaData.NILLABLE, attribute.isNillable());
		map.put(AttributeMetaData.UNIQUE, attribute.isUnique());
		if (attribute.getRefEntity() != null)
		{
			map.put(AttributeMetaData.REF_ENTITY, attribute.getRefEntity().getName());
		}
		return map;
	}
}