package org.molgenis.diseasematcher.controller;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.molgenis.data.QueryRule;

/**
 * 
 * @author tommydeboer
 * 
 */
public class FindRequest
{
	@NotNull
	private String datasetName;
	@NotNull
	private int num;
	@NotNull
	private int start;

	private List<QueryRule> q;

	public List<QueryRule> getQuery()
	{
		return q;
	}

	public void setQ(List<QueryRule> q)
	{
		this.q = q;
	}

	public String getDatasetName()
	{
		return datasetName;
	}

	public void setDatasetName(String datasetName)
	{
		this.datasetName = datasetName;
	}

	public int getNum()
	{
		return num;
	}

	public void setNum(int num)
	{
		this.num = num;
	}

	public int getStart()
	{
		return start;
	}

	public void setStart(int start)
	{
		this.start = start;
	}

}
