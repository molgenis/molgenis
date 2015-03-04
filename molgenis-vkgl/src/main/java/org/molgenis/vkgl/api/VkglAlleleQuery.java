package org.molgenis.vkgl.api;

public class VkglAlleleQuery
{
	private String parameterID;
	private String operator;
	private String source;
	private String reference;
	private String start;
	private String end;
	private String[] alleleSequence;
	
	public String getOperator()
	{
		return operator;
	}
	public void setOperator(String operator)
	{
		this.operator = operator;
	}
	public String getSource()
	{
		return source;
	}
	public void setSource(String source)
	{
		this.source = source;
	}
	public String getReference()
	{
		return reference;
	}
	public void setReference(String reference)
	{
		this.reference = reference;
	}
	public String getStart()
	{
		return start;
	}
	public void setStart(String start)
	{
		this.start = start;
	}
	public String getEnd()
	{
		return end;
	}
	public void setEnd(String end)
	{
		this.end = end;
	}
	public String[] getAlleleSequence()
	{
		return alleleSequence;
	}
	public void setAlleleSequence(String[] alleleSequence)
	{
		this.alleleSequence = alleleSequence;
	}
	public String getParameterID()
	{
		return parameterID;
	}
	public void setParameterID(String parameterID)
	{
		this.parameterID = parameterID;
	}
}
