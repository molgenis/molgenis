package org.molgenis.catalog;

public class CatalogModelItem
{
	private final String id;
	private final String name;

	public CatalogModelItem(String id, String name)
	{
		if (id == null) throw new IllegalArgumentException("id is null");
		if (name == null) throw new IllegalArgumentException("name is null");
		this.id = id;
		this.name = name;
	}

	public String getId()
	{
		return id;
	}

	public String getName()
	{
		return name;
	}
}