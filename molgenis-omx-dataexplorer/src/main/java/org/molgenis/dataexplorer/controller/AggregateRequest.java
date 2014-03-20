package org.molgenis.dataexplorer.controller;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.molgenis.data.QueryRule;

public class AggregateRequest
{
	@NotNull
	private String entityName;

	private String xAxisAttributeName;

	private String yAxisAttributeName;

	private List<QueryRule> q;

	public String getEntityName()
	{
		return entityName;
	}

	public void setEntityName(String entityName)
	{
		this.entityName = entityName;
	}

	public String getXAxisAttributeName()
	{
		return xAxisAttributeName;
	}

	public void setXAxisAttributeName(String xAxisAttributeName)
	{
		this.xAxisAttributeName = xAxisAttributeName;
	}

	public String getYAxisAttributeName()
	{
		return yAxisAttributeName;
	}

	public void setYAxisAttributeName(String yAxisAttributeName)
	{
		this.yAxisAttributeName = yAxisAttributeName;
	}

	public List<QueryRule> getQ()
	{
		return q;
	}

	public void setQ(List<QueryRule> q)
	{
		this.q = q;
	}
}
