package org.molgenis.framework.ui;

import java.util.Map;

import static java.util.Objects.requireNonNull;

public class MolgenisPlugin
{
	private final String id;
	private final String name;
	private final String uri;
	private final String fullUri;
	private final Map<String, String> requiredSettingEntities;

	public MolgenisPlugin(String id, String name, String uri, String fullUri,
			Map<String, String> requiredSettingEntities)
	{
		this.id = requireNonNull(id);
		this.name = requireNonNull(name);
		this.uri = requireNonNull(uri);
		this.fullUri = requireNonNull(fullUri);
		this.requiredSettingEntities = requireNonNull(requiredSettingEntities);
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

	public String getFullUri()
	{
		return fullUri;
	}

	public Map<String, String> getRequiredSettingEntities()
	{
		return requiredSettingEntities;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + id.hashCode();
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