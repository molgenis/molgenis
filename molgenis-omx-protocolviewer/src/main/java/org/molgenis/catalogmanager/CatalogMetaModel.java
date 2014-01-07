package org.molgenis.catalogmanager;

public class CatalogMetaModel
{
	private final String id;
	private final String name;
	private final boolean loaded;

	public CatalogMetaModel(String id, String name, boolean loaded)
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

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + (loaded ? 1231 : 1237);
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		CatalogMetaModel other = (CatalogMetaModel) obj;
		if (id == null)
		{
			if (other.id != null) return false;
		}
		else if (!id.equals(other.id)) return false;
		if (loaded != other.loaded) return false;
		if (name == null)
		{
			if (other.name != null) return false;
		}
		else if (!name.equals(other.name)) return false;
		return true;
	}
}
