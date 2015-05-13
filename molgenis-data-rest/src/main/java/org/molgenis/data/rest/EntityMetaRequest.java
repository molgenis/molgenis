package org.molgenis.data.rest;

public class EntityMetaRequest
{
	private Attributes attributes;
	private String[] expand;

	public Attributes getAttributes()
	{
		return attributes;
	}

	public void setAttributes(Attributes attributes)
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
