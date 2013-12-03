package org.molgenis.search;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.molgenis.data.Query;
import org.molgenis.data.support.QueryImpl;

public class SearchRequest
{
	private String documentType;
	private QueryImpl query;
	private final List<String> fieldsToReturn;

	public SearchRequest()
	{
		fieldsToReturn = new ArrayList<String>();
	}

	public SearchRequest(String documentType, Query query, List<String> fieldsToReturn)
	{
		this.documentType = documentType;
		this.query = new QueryImpl(query);
		this.fieldsToReturn = fieldsToReturn;
	}

	public String getDocumentType()
	{
		return documentType;
	}

	public Query getQuery()
	{
		if (query == null)
		{
			query = new QueryImpl();
		}

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
