package org.molgenis.data.elasticsearch.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Query;
import org.molgenis.data.support.QueryImpl;

public class SearchRequest
{
	private String documentType;
	private QueryImpl query;
	private final List<String> fieldsToReturn;
	private AttributeMetaData aggregateField1;
	private AttributeMetaData aggregateField2;
	private AttributeMetaData aggregateFieldDistinct;
	private Integer aggregateAnonymizationThreshold;

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

	public SearchRequest(String documentType, Query query, List<String> fieldsToReturn,
			AttributeMetaData aggregateField1, AttributeMetaData aggregateField2,
			AttributeMetaData aggregateFieldDistinct, Integer aggregateAnonymizationThreshold)
	{
		this(documentType, query, fieldsToReturn);
		this.aggregateField1 = aggregateField1;
		this.aggregateField2 = aggregateField2;
		this.aggregateFieldDistinct = aggregateFieldDistinct;
		this.aggregateAnonymizationThreshold = aggregateAnonymizationThreshold;
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

	public AttributeMetaData getAggregateField1()
	{
		return aggregateField1;
	}

	public AttributeMetaData getAggregateField2()
	{
		return aggregateField2;
	}

	public AttributeMetaData getAggregateFieldDistinct()
	{
		return aggregateFieldDistinct;
	}

	public Integer getAggregateAnonymizationThreshold()
	{
		return aggregateAnonymizationThreshold;
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
		result = prime * result
				+ ((aggregateAnonymizationThreshold == null) ? 0 : aggregateAnonymizationThreshold.hashCode());
		result = prime * result + ((aggregateField1 == null) ? 0 : aggregateField1.hashCode());
		result = prime * result + ((aggregateField2 == null) ? 0 : aggregateField2.hashCode());
		result = prime * result + ((aggregateFieldDistinct == null) ? 0 : aggregateFieldDistinct.hashCode());
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
		if (aggregateAnonymizationThreshold == null)
		{
			if (other.aggregateAnonymizationThreshold != null) return false;
		}
		else if (!aggregateAnonymizationThreshold.equals(other.aggregateAnonymizationThreshold)) return false;
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