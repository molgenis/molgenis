package org.molgenis.data.elasticsearch.util;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.support.QueryImpl;

public class SearchRequest
{
	private String documentType;
	private QueryImpl<Entity> query;
	private Attribute aggregateField1;
	private Attribute aggregateField2;
	private Attribute aggregateFieldDistinct;

	public SearchRequest()
	{
	}

	public SearchRequest(String documentType, Query<Entity> query)
	{
		this.documentType = documentType;
		this.query = new QueryImpl<>(query);
	}

	public SearchRequest(String documentType, Query<Entity> query, Attribute aggregateField1,
			Attribute aggregateField2, Attribute aggregateFieldDistinct)
	{
		this(documentType, query);
		this.aggregateField1 = aggregateField1;
		this.aggregateField2 = aggregateField2;
		this.aggregateFieldDistinct = aggregateFieldDistinct;
	}

	public String getDocumentType()
	{
		return documentType;
	}

	public Query<Entity> getQuery()
	{
		if (query == null)
		{
			query = new QueryImpl<>();
		}

		return query;
	}

	public Attribute getAggregateField1()
	{
		return aggregateField1;
	}

	public Attribute getAggregateField2()
	{
		return aggregateField2;
	}

	public Attribute getAggregateFieldDistinct()
	{
		return aggregateFieldDistinct;
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
		result = prime * result + ((aggregateField1 == null) ? 0 : aggregateField1.hashCode());
		result = prime * result + ((aggregateField2 == null) ? 0 : aggregateField2.hashCode());
		result = prime * result + ((aggregateFieldDistinct == null) ? 0 : aggregateFieldDistinct.hashCode());
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
		if (aggregateField1 == null)
		{
			if (other.aggregateField1 != null) return false;
		}
		else if (!aggregateField1.equals(other.aggregateField1)) return false;
		if (aggregateField2 == null)
		{
			if (other.aggregateField2 != null) return false;
		}
		else if (!aggregateField2.equals(other.aggregateField2)) return false;
		if (aggregateFieldDistinct == null)
		{
			if (other.aggregateFieldDistinct != null) return false;
		}
		else if (!aggregateFieldDistinct.equals(other.aggregateFieldDistinct)) return false;
		if (query == null)
		{
			if (other.query != null) return false;
		}
		else if (!query.equals(other.query)) return false;
		return true;
	}

}