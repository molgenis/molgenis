package org.molgenis.model.elements;

import java.io.Serializable;

public class Dataset implements Serializable
{
	public Dataset(String name, String entity)
	{
		setName(name);
		setEntity(entity);
	}

	public String getEntity()
	{
		return entity;
	}

	public void setEntity(String entity)
	{
		this.entity = entity;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	@Override
	public String toString()
	{
		return "Dataset(" + name + ", " + entity + ")";
	}

	private static final long serialVersionUID = 899485395046608203L;
	private String name;
	private String entity;

}
