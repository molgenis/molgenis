package org.molgenis.data.semanticsearch.explain.bean;

import com.google.auto.value.AutoValue;
import com.google.common.collect.Sets;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeMetaDataMetaData;
import org.molgenis.gson.AutoGson;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@AutoValue
@AutoGson(autoValueClass = AutoValue_ExplainedAttributeMetaData.class)
public abstract class ExplainedAttributeMetaData
{
	public static ExplainedAttributeMetaData create(Attribute attribute)
	{
		return new AutoValue_ExplainedAttributeMetaData(attributeToMap(attribute), Collections.emptySet(),
				false);
	}

	public static ExplainedAttributeMetaData create(Attribute attribute,
			Iterable<ExplainedQueryString> explainedQueryStrings, boolean highQuality)
	{
		return new AutoValue_ExplainedAttributeMetaData(attributeToMap(attribute),
				Sets.newHashSet(explainedQueryStrings), highQuality);
	}

	public abstract Map<String, Object> getAttributeMetaData();

	public abstract Set<ExplainedQueryString> getExplainedQueryStrings();

	public abstract boolean isHighQuality();

	private static Map<String, Object> attributeToMap(Attribute attribute)
	{
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(AttributeMetaDataMetaData.NAME, attribute.getName());
		map.put(AttributeMetaDataMetaData.LABEL, attribute.getLabel());
		map.put(AttributeMetaDataMetaData.DESCRIPTION, attribute.getDescription());
		map.put(AttributeMetaDataMetaData.DATA_TYPE, attribute.getDataType().toString());
		map.put(AttributeMetaDataMetaData.NILLABLE, attribute.isNillable());
		map.put(AttributeMetaDataMetaData.UNIQUE, attribute.isUnique());
		if (attribute.getRefEntity() != null)
		{
			map.put(AttributeMetaDataMetaData.REF_ENTITY, attribute.getRefEntity().getName());
		}
		return map;
	}
}