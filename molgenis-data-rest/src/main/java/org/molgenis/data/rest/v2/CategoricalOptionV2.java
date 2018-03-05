package org.molgenis.data.rest.v2;

public class CategoricalOptionV2
{
	private Object id;
	private Object label;

	public CategoricalOptionV2(Object id, Object label)
	{
		this.id = id;
		this.label = label;
	}

	public Object getId()
	{
		return id;
	}

	public Object getLabel()
	{
		return label;
	}
}
