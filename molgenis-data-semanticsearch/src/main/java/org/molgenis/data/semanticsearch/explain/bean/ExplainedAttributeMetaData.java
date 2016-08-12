package org.molgenis.data.semanticsearch.explain.bean;

import com.google.auto.value.AutoValue;
import com.google.common.collect.Sets;
import org.molgenis.data.meta.model.AttributeMetaData;
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
	public static ExplainedAttributeMetaData create(AttributeMetaData attributeMetaData)
	{
		return new AutoValue_ExplainedAttributeMetaData(attributeToMap(attributeMetaData), Collections.emptySet(),
				false);
	}

	public static ExplainedAttributeMetaData create(AttributeMetaData attributeMetaData,
			Iterable<ExplainedQueryString> explainedQueryStrings, boolean highQuality)
	{
		return new AutoValue_ExplainedAttributeMetaData(attributeToMap(attributeMetaData),
				Sets.newHashSet(explainedQueryStrings), highQuality);
	}

	public abstract Map<String, Object> getAttributeMetaData();

	public abstract Set<ExplainedQueryString> getExplainedQueryStrings();

	public abstract boolean isHighQuality();

	private static Map<String, Object> attributeToMap(AttributeMetaData attributeMetaData)
	{
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(AttributeMetaDataMetaData.NAME, attributeMetaData.getName());
		map.put(AttributeMetaDataMetaData.LABEL, attributeMetaData.getLabel());
		map.put(AttributeMetaDataMetaData.DESCRIPTION, attributeMetaData.getDescription());
		map.put(AttributeMetaDataMetaData.DATA_TYPE, attributeMetaData.getDataType().toString());
		map.put(AttributeMetaDataMetaData.NILLABLE, attributeMetaData.isNillable());
		map.put(AttributeMetaDataMetaData.UNIQUE, attributeMetaData.isUnique());
		if (attributeMetaData.getRefEntity() != null)
		{
			map.put(AttributeMetaDataMetaData.REF_ENTITY, attributeMetaData.getRefEntity().getName());
		}
		return map;
	}
}