package org.molgenis.script;

import org.molgenis.data.DataService;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.DefaultEntity;

public class ScriptType extends DefaultEntity
{
	private static final long serialVersionUID = -762140082046982877L;
	public static final String ENTITY_NAME = "ScriptType";
	public static final String NAME = "name";
	public static final EntityMetaData META_DATA = new ScriptTypeMetaData();

	public ScriptType(String name, DataService dataService)
	{
		this(dataService);
		setName(name);
	}

	public ScriptType(DataService dataService)
	{
		super(META_DATA, dataService);
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
