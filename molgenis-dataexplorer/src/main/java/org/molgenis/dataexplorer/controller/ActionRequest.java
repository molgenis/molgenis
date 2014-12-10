package org.molgenis.dataexplorer.controller;

import javax.validation.constraints.NotNull;

import org.molgenis.data.support.QueryImpl;

public class ActionRequest
{
	@NotNull
	private String actionId;

	@NotNull
	private String entityName;

	@NotNull
	private QueryImpl query;

	public String getActionId()
	{
		return actionId;
	}

	public void setActionId(String actionId)
	{
		this.actionId = actionId;
	}

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
}
