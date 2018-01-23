package org.molgenis.script.core;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;

import static org.molgenis.script.core.ScriptParameterMetaData.NAME;

public class ScriptParameter extends StaticEntity
{
	public ScriptParameter(Entity entity)
	{
		super(entity);
	}

	public ScriptParameter(EntityType entityType)
	{
		super(entityType);
	}

	public ScriptParameter(String name, EntityType entityType)
	{
		super(entityType);
		setName(name);
	}

	public ScriptParameter setName(String name)
	{
		set(NAME, name);
		return this;
	}

	public String getName()
	{
		return getString(NAME);
	}
}
