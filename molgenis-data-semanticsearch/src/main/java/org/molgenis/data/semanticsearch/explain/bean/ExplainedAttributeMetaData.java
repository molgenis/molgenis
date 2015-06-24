package org.molgenis.data.semanticsearch.explain.bean;

import java.util.Collections;
import java.util.Set;

import org.molgenis.data.AttributeMetaData;

public class ExplainedAttributeMetaData
{
	private final AttributeMetaData attributeMetaData;
	private final Set<ExplainedQueryString> explainedQueryStrings;

	public ExplainedAttributeMetaData(AttributeMetaData attributeMetaData)
	{
		this(attributeMetaData, Collections.emptySet());
	}

	public ExplainedAttributeMetaData(AttributeMetaData attributeMetaData,
			Set<ExplainedQueryString> explainedQueryStrings)
	{
		this.attributeMetaData = attributeMetaData;
		this.explainedQueryStrings = explainedQueryStrings;
	}

	public AttributeMetaData getAttributeMetaData()
	{
		return attributeMetaData;
	}

	public Set<ExplainedQueryString> getExplainedQueryStrings()
	{
		return explainedQueryStrings;
	}
}