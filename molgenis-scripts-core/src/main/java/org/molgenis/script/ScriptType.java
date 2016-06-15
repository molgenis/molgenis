package org.molgenis.script;

import static org.molgenis.script.ScriptTypeMetaData.NAME;

import org.molgenis.data.Entity;
import org.molgenis.data.support.StaticEntity;

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
	 * @param scriptTypeMetaData script type meta data
	 */
	public ScriptType(ScriptTypeMetaData scriptTypeMetaData)
	{
		super(scriptTypeMetaData);
	}

	/**
	 * Constructs a script type with the given type name and meta data
	 *
	 * @param name               script type name
	 * @param scriptTypeMetaData script type meta data
	 */
	public ScriptType(String name, ScriptTypeMetaData scriptTypeMetaData)
	{
		super(scriptTypeMetaData);
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
