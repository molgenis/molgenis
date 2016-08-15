package org.molgenis.data.elasticsearch.util;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Collections;
import java.util.Map;

/**
 * Represents a search hit found by the SearchService
 *
 * @author erwin
 */
public class Hit
{
	private final String id;// document id
	private final String documentType;// Document type (collection type)
	private final Map<String, Object> columnValueMap;// key: fieldname,
	// value:fieldvalue

	public Hit(String id, String documentType, Map<String, Object> columnValueMap)
	{
		this.id = id;
		this.documentType = documentType;
		this.columnValueMap = columnValueMap;
	}

	public String getId()
	{
		return id;
	}

	public String getDocumentType()
	{
		return documentType;
	}

	public Map<String, Object> getColumnValueMap()
	{
		return Collections.unmodifiableMap(columnValueMap);
	}

	@Override
	public String toString()
	{
		return ToStringBuilder.reflectionToString(this);
	}

}