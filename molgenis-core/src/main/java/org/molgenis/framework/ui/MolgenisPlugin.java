package org.molgenis.framework.ui;

public class MolgenisPlugin
{
	private final String id;
	private final String name;
	private final String uri;

	public MolgenisPlugin(String id, String name, String uri)
	{
		if (id == null) throw new IllegalArgumentException("id is null");
		if (name == null) throw new IllegalArgumentException("name is null");
		if (uri == null) throw new IllegalArgumentException("uri is null");
		this.id = id;
		this.name = name;
		this.uri = uri;
	}

	public String getId()
	{
		return id;
	}

	public String getName()
	{
		return name;
	}

	public String getUrl()
	{
		return uri;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		MolgenisPlugin other = (MolgenisPlugin) obj;
		if (id == null)
		{
			if (other.id != null) return false;
		}
		else if (!id.equals(other.id)) return false;
		return true;
	}
}
