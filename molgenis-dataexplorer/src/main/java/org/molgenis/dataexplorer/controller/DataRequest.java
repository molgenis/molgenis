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

	public static enum EntityValues
	{
		ENTITY_LABELS, ENTITY_IDS
	}

	public static enum DownloadType
	{
		DOWNLOAD_TYPE_CSV, DOWNLOAD_TYPE_XLSX
	}

	@NotNull
	private String entityName;
	@NotNull
	private QueryImpl query;
	@NotNull
	private List<String> attributeNames;
	@NotNull
	private ColNames colNames;
	@NotNull
	private EntityValues entityValues;
	@NotNull
	private DownloadType downloadType;

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

	public DownloadType getDownloadType()
	{
		return downloadType;
	}

	public void setDownloadType(DownloadType downloadType)
	{
		this.downloadType = downloadType;
	}

	public EntityValues getEntityValues()
	{
		return entityValues;
	}

	public void setEntityValues(EntityValues entityValues)
	{
		this.entityValues = entityValues;
	}

}
