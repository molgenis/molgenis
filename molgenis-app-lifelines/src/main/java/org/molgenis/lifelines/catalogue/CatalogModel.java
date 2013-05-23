package org.molgenis.lifelines.catalogue;

public class CatalogModel
{
	private final String id;
	private final String name;
	private final boolean loaded;

	public CatalogModel(String id, String name, boolean loaded)
	{
		this.id = id;
		this.name = name;
		this.loaded = loaded;
	}

	public String getId()
	{
		return id;
	}

	public String getName()
	{
		return name;
	}

	public boolean isLoaded()
	{
		return loaded;
	}

}
