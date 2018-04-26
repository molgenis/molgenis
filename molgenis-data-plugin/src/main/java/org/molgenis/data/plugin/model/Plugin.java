package org.molgenis.data.plugin.model;

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

	public String getDescription()
	{
		return getString(PluginMetadata.DESCRIPTION);
	}

	public Plugin setDescription(String description)
	{
		set(PluginMetadata.DESCRIPTION, description);
		return this;
	}
}