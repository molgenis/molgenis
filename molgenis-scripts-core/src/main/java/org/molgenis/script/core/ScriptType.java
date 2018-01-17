package org.molgenis.script.core;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;

import static org.molgenis.script.core.ScriptTypeMetaData.NAME;

/**
 * Script type entity
 */
public class ScriptType extends StaticEntity
{
	public ScriptType(Entity entity)
	{
		super(entity);
	}

	/**
	 * Constructs a script type with the given meta data
	 *
	 * @param entityType script type meta data
	 */
	public ScriptType(EntityType entityType)
	{
		super(entityType);
	}

	/**
	 * Constructs a script type with the given type name and meta data
	 *
	 * @param name       script type name
	 * @param entityType script type meta data
	 */
	public ScriptType(String name, EntityType entityType)
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
