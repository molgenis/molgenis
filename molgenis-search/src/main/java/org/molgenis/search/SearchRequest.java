package org.molgenis.search;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.molgenis.framework.db.QueryRule;

public class SearchRequest
{
	private String documentType;
	private List<QueryRule> queryRules = new ArrayList<QueryRule>();
	private List<String> fieldsToReturn = new ArrayList<String>();

	public SearchRequest()
	{
	}

	public SearchRequest(String documentType, List<QueryRule> queryRules, List<String> fieldsToReturn)
	{
		this.documentType = documentType;
		this.queryRules = queryRules;
		this.fieldsToReturn = fieldsToReturn;
	}

	public String getDocumentType()
	{
		return documentType;
	}

	public List<QueryRule> getQueryRules()
	{
		return queryRules;
	}

	public List<String> getFieldsToReturn()
	{
		return fieldsToReturn;
	}

	@Override
	public String toString()
	{
		return ToStringBuilder.reflectionToString(this);
	}

}
