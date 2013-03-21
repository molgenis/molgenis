package org.molgenis.search;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Represents a search hit found by the SearchService
 * 
 * @author erwin
 * 
 */
public class Hit
{
	private final String id;// document id
	private final String documentType;// Document type (collection type)
	private final String href;// Link to REST api
	private final Map<String, Object> columnValueMap;// key: fieldname,
														// value:fieldvalue

	public Hit(String id, String documentType, String href, Map<String, Object> columnValueMap)
	{
		this.id = id;
		this.documentType = documentType;
		this.href = href;
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

	public String getHref()
	{
		return href;
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
