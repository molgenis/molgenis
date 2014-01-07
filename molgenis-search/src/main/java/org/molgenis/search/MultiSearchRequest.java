package org.molgenis.search;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.molgenis.data.Query;
import org.molgenis.data.support.QueryImpl;

public class MultiSearchRequest
{
	private List<String> documentTypes;
	private QueryImpl query;
	private List<String> fieldsToReturn = new ArrayList<String>();

	public MultiSearchRequest()
	{
	}

	public MultiSearchRequest(List<String> documentTypes, Query query, List<String> fieldsToReturn)
	{
		this.documentTypes = documentTypes;
		this.query = new QueryImpl(query);
		this.fieldsToReturn = fieldsToReturn;
	}

	public List<String> getDocumentType()
	{
		return documentTypes;
	}

	public Query getQuery()
	{
		return query;
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
