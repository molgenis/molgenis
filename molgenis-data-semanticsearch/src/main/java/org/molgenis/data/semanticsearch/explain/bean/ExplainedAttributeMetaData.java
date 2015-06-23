package org.molgenis.data.semanticsearch.explain.bean;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
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

	public String getExplanation()
	{
		return StringUtils.join(explainedQueryStrings.stream().map(this::combineExplanation)
				.collect(Collectors.toSet()), " ; ");
	}

	private String combineExplanation(ExplainedQueryString explainedQueryString)
	{
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("The term [").append(explainedQueryString.getMatchedTerms())
				.append("] is matched to related key words [").append(explainedQueryString.getQueryString())
				.append("] with similarity [").append(explainedQueryString.getScore()).append("%]");
		return stringBuilder.toString();
	}
}