package org.molgenis.script;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.support.StaticEntity;

import static org.molgenis.script.ScriptTypeMetaData.NAME;

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
	 * @param entityMeta script type meta data
	 */
	public ScriptType(EntityMetaData entityMeta)
	{
		super(entityMeta);
	}

	/**
	 * Constructs a script type with the given type name and meta data
	 *
	 * @param name       script type name
	 * @param entityMeta script type meta data
	 */
	public ScriptType(String name, EntityMetaData entityMeta)
	{
		super(entityMeta);
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
