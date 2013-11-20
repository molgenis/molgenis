package org.molgenis.search;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.molgenis.framework.db.QueryRule;

public class MultiSearchRequest
{
	private List<String> documentTypes;
	private List<QueryRule> queryRules = new ArrayList<QueryRule>();
	private List<String> fieldsToReturn = new ArrayList<String>();

	public MultiSearchRequest()
	{
	}

	public MultiSearchRequest(List<String> documentTypes, List<QueryRule> queryRules, List<String> fieldsToReturn)
	{
		this.documentTypes = documentTypes;
		this.queryRules = queryRules;
		this.fieldsToReturn = fieldsToReturn;
	}

	public List<String> getDocumentType()
	{
		return documentTypes;
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
