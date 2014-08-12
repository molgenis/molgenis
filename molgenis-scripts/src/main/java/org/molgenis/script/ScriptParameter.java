package org.molgenis.script;

import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.MapEntity;

public class ScriptParameter extends MapEntity
{
	private static final long serialVersionUID = 2005285224629134983L;
	public static final String ENTITY_NAME = "ScriptParameter";
	public static final String NAME = "name";
	public static final EntityMetaData META_DATA = new ScriptParameterMetaData();

	public ScriptParameter()
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
