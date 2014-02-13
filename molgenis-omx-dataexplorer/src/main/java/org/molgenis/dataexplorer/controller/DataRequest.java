package org.molgenis.dataexplorer.controller;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.molgenis.data.support.QueryImpl;

public class DataRequest
{
	@NotNull
	private String entityName;
	@NotNull
	private QueryImpl query;
	@NotNull
	private List<String> attributeNames;

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
}
