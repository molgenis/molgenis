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

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((documentType == null) ? 0 : documentType.hashCode());
		result = prime * result + ((fieldsToReturn == null) ? 0 : fieldsToReturn.hashCode());
		result = prime * result + ((query == null) ? 0 : query.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		SearchRequest other = (SearchRequest) obj;
		if (documentType == null)
		{
			if (other.documentType != null) return false;
		}
		else if (!documentType.equals(other.documentType)) return false;
		if (fieldsToReturn == null)
		{
			if (other.fieldsToReturn != null) return false;
		}
		else if (!fieldsToReturn.equals(other.fieldsToReturn)) return false;
		if (query == null)
		{
			if (other.query != null) return false;
		}
		else if (!query.equals(other.query)) return false;
		return true;
	}

}
