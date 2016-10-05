package org.molgenis.data.rest;

public class EntityTypeRequest
{
	private String[] attributes;
	private String[] expand;

	public String[] getAttributes()
	{
		return attributes;
	}

	public void setAttributes(String[] attributes)
	{
		this.attributes = attributes;
	}

	public String[] getExpand()
	{
		return expand;
	}

	public void setExpand(String[] expand)
	{
		this.expand = expand;
	}
}
