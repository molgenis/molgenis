package org.molgenis.script;

import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.MapEntity;

public class ScriptType extends MapEntity
{
	private static final long serialVersionUID = -762140082046982877L;
	public static final String ENTITY_NAME = "ScriptType";
	public static final String NAME = "name";
	public static final EntityMetaData META_DATA = new ScriptTypeMetaData();

	public ScriptType(String name)
	{
		this();
		setName(name);
	}

	public ScriptType()
	{
		super(NAME);
	}

	public void setName(String name)
	{
		set(NAME, name);
	}

	public String getName()
	{
		return getString(NAME);
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return META_DATA;
	}

}
