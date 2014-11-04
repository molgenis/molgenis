package org.molgenis.diseasematcher.controller;

import java.util.List;

import org.molgenis.omx.diseasemapping.Disease;

/**
 * Response object for when diseases were requested. Stores the results and paging information.
 * 
 * @author tommydeboer
 */
public class FindDiseasesResponse
{
	private int num;
	private int start;
	private int total;
	private List<Disease> diseases;

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

	public int getTotal()
	{
		return total;
	}

	public void setTotal(int total)
	{
		this.total = total;
	}

	public List<Disease> getDiseases()
	{
		return diseases;
	}

	public void setDiseases(List<Disease> diseases)
	{
		this.diseases = diseases;
	}

}
