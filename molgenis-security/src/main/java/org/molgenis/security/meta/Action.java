package org.molgenis.security.meta;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;

import static org.molgenis.security.meta.ActionMetadata.NAME;

public class Action extends StaticEntity
{
	public Action(Entity entity)
	{
		super(entity);
	}

	public Action(EntityType entityType)
	{
		super(entityType);
	}

	public Action(String name, EntityType entityType)
	{
		super(entityType);
		setName(name);
	}

	public void setName(String name)
	{
		set(NAME, name);
	}

	public String getName()
	{
		return getString(NAME);
	}
}
