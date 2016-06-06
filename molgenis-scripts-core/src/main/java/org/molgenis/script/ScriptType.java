package org.molgenis.script;

import static org.molgenis.script.ScriptTypeMetaData.NAME;
import static org.molgenis.script.ScriptTypeMetaData.SCRIPT_TYPE;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.SystemEntity;

/**
 * Script type entity
 */
public class ScriptType extends SystemEntity
{
	/**
	 * Constructs a script type based on the given entity
	 *
	 * @param entity decorated entity
	 */
	public ScriptType(Entity entity)
	{
		super(entity, SCRIPT_TYPE);
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
