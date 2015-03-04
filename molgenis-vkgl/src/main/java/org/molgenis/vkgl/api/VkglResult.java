package org.molgenis.vkgl.api;

import java.util.List;

public class VkglResult
{
	private String resultType;
	private List<String> xAxisAlleles;
	private List<String> yAxisAlleles;
	private String chromosome;
	private int position;
	private String reference;
	private List<List<Long>> result;
		
	public List<String> getReferenceAllele()
	{
		return xAxisAlleles;
	}
	public void setXAxisAlleles(List<String> xAxisAlleles)
	{
		this.xAxisAlleles = xAxisAlleles;
	}
	public List<String> getYAxisAlleles()
	{
		return yAxisAlleles;
	}
	public void setYAxisAlleles(List<String> yAxisAlleles)
	{
		this.yAxisAlleles = yAxisAlleles;
	}
	public String getChromosome()
	{
		return chromosome;
	}
	public void setChromosome(String chromosome)
	{
		this.chromosome = chromosome;
	}
	public int getPosition()
	{
		return position;
	}
	public void setPosition(int position)
	{
		this.position = position;
	}
	public String getReference()
	{
		return reference;
	}
	public void setReference(String reference)
	{
		this.reference = reference;
	}
	public List<List<Long>> getResult()
	{
		return result;
	}
	public void setResult(List<List<Long>> result)
	{
		this.result = result;
	}
	public String getResultType()
	{
		return resultType;
	}
	public void setResultType(String resultType)
	{
		this.resultType = resultType;
	}
}
