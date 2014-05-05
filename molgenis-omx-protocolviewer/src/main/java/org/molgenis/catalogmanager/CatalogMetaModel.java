package org.molgenis.catalogmanager;

public class CatalogMetaModel
{
	private final String id;
	private final String name;
	private final boolean loaded;
	private final boolean activated;

	public CatalogMetaModel(String id, String name, boolean loaded, boolean activated)
	{
		this.id = id;
		this.name = name;
		this.loaded = loaded;
		this.activated = activated;
	}

	public String getId()
	{
		return this.id;
	}

	public String getName()
	{
		return this.name;
	}

	public boolean isLoaded()
	{
		return this.loaded;
	}

	public boolean isActivated()
	{
		return this.activated;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
		result = prime * result + (this.loaded ? 1231 : 1237);
		result = prime * result + (this.activated ? 1231 : 1237);
		result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		CatalogMetaModel other = (CatalogMetaModel) obj;
		if (this.id == null)
		{
			if (other.id != null) return false;
		}
		else if (!this.id.equals(other.id)) return false;
		if (this.loaded != other.loaded) return false;
		if (this.activated != other.activated) return false;
		if (this.name == null)
		{
			if (other.name != null) return false;
		}
		else if (!this.name.equals(other.name)) return false;
		return true;
	}
}
