package org.molgenis.dataexplorer.controller;

import org.molgenis.data.Entity;
import org.molgenis.data.support.QueryImpl;

import javax.validation.constraints.NotNull;
import java.util.List;

public class DataRequest
{
	public enum ColNames
	{
		ATTRIBUTE_NAMES, ATTRIBUTE_LABELS
	}

	public enum EntityValues
	{
		ENTITY_LABELS, ENTITY_IDS
	}

	public enum DownloadType
	{
		DOWNLOAD_TYPE_CSV, DOWNLOAD_TYPE_XLSX
	}

	@NotNull
	private String entityName;
	@NotNull
	private QueryImpl<Entity> query;
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

	public QueryImpl<Entity> getQuery()
	{
		return query;
	}

	public void setQuery(QueryImpl<Entity> query)
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
