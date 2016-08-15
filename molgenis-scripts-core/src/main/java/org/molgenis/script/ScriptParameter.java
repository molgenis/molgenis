package org.molgenis.script;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.support.StaticEntity;

import static org.molgenis.script.ScriptParameterMetaData.NAME;

public class ScriptParameter extends StaticEntity
{
	public ScriptParameter(Entity entity)
	{
		super(entity);
	}

	public ScriptParameter(EntityMetaData entityMeta)
	{
		super(entityMeta);
	}

	public ScriptParameter(String name, EntityMetaData entityMeta)
	{
		super(entityMeta);
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
