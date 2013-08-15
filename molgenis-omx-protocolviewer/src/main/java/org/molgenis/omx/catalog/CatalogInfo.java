package org.molgenis.omx.catalog;

/**
 * Holds the id and name of a Catalog release
 * 
 * @author erwin
 * 
 */
public class CatalogInfo
{
	private final String id;
	private final String name;

	public CatalogInfo(String id, String name)
	{
		if (id == null) throw new IllegalArgumentException("Id is null");
		if (name == null) throw new IllegalArgumentException("Name is null");
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
