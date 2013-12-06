package org.molgenis.charts.requests;

import java.util.List;

import org.molgenis.data.QueryRule;

public class HeatMapRequest extends ChartRequest
{
	// The column attribute names
	private List<String> x;

	// The row label attribute name
	private String y;

	// The query rules to select the rows
	private List<QueryRule> queryRules;

	public List<String> getX()
	{
		return x;
	}

	public void setX(List<String> x)
	{
		this.x = x;
	}

	public String getY()
	{
		return y;
	}

	public void setY(String y)
	{
		this.y = y;
	}

	public List<QueryRule> getQueryRules()
	{
		return queryRules;
	}

	public void setQueryRules(List<QueryRule> queryRules)
	{
		this.queryRules = queryRules;
	}

}