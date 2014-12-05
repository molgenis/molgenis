package org.molgenis.dataexplorer.controller;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.molgenis.data.support.QueryImpl;

public class DataRequest
{
	public static enum ColNames
	{
		ATTRIBUTE_NAMES, ATTRIBUTE_LABELS
	}

	@NotNull
	private String entityName;
	@NotNull
	private QueryImpl query;
	@NotNull
	private List<String> attributeNames;
	@NotNull
	private ColNames colNames;

	public String getEntityName()
	{
		return entityName;
	}

	public void setEntityName(String entityName)
	{
		this.entityName = entityName;
	}

	public QueryImpl getQuery()
	{
		return query;
	}

	public void setQuery(QueryImpl query)
	{
		this.query = query;
	}

	public List<String> getAttributeNames()
	{
		return attributeNames;
	}

	public void setAttributeNames(List<String> attributeNames)
	{
		this.attributeNames = attributeNames;
	}

	public ColNames getColNames()
	{
		return colNames;
	}

	public void setColNames(ColNames colNames)
	{
		this.colNames = colNames;
	}

}
