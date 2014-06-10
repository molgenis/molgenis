package org.molgenis.diseasematcher.controller;

import java.util.List;

/**
 * 
 * @author tommydeboer
 * 
 */
public class FindGenesResponse
{
	private int num;
	private int start;
	private int total;
	private List<String> genes;

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

	public List<String> getGenes()
	{
		return genes;
	}

	public void setGenes(List<String> genes)
	{
		this.genes = genes;
	}

}
