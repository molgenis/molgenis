package org.molgenis.ui;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;

public class Plugin extends StaticEntity
{
	public Plugin(Entity entity)
	{
		super(entity);
	}

	public Plugin(EntityType entityType)
	{
		super(entityType);
	}

	public Plugin(String id, EntityType entityType)
	{
		super(entityType);
		setId(id);
	}

	public String getId()
	{
		return getString(PluginMetadata.ID);
	}

	public Plugin setId(String id)
	{
		set(PluginMetadata.ID, id);
		return this;
	}

	public String getLabel()
	{
		return getString(PluginMetadata.LABEL);
	}

	public Plugin setLabel(String label)
	{
		set(PluginMetadata.LABEL, label);
		return this;
	}

	public String getUri()
	{
		return getString(PluginMetadata.URI);
	}

	public Plugin setUri(String uri)
	{
		set(PluginMetadata.URI, uri);
		return this;
	}

	public String getFullUri()
	{
		return getString(PluginMetadata.FULL_URI);
	}

	public Plugin setFullUri(String uri)
	{
		set(PluginMetadata.FULL_URI, uri);
		return this;
	}
}
