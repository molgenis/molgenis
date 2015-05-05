package org.molgenis.data.semanticsearch.explain.bean;

import java.util.Map.Entry;
import java.util.Set;

import org.molgenis.data.AttributeMetaData;

public class ExplainedAttributeMetaData
{
	private final AttributeMetaData attributeMetaData;
	private final Set<Entry<String, Double>> explainedQueryStrings;

	public ExplainedAttributeMetaData(AttributeMetaData attributeMetaData,
			Set<Entry<String, Double>> explainedQueryStrings)
	{
		this.attributeMetaData = attributeMetaData;
		this.explainedQueryStrings = explainedQueryStrings;
	}

	public AttributeMetaData getAttributeMetaData()
	{
		return attributeMetaData;
	}

	public Set<Entry<String, Double>> getExplainedQueryStrings()
	{
		return explainedQueryStrings;
	}
}