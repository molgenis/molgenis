package org.molgenis.auth;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.support.StaticEntity;

import static org.molgenis.auth.MolgenisGroupMetaData.*;

public class MolgenisGroup extends StaticEntity
{
	public MolgenisGroup(Entity entity)
	{
		super(entity);
		setDefaultValues();
	}

	public MolgenisGroup(EntityMetaData entityMeta)
	{
		super(entityMeta);
		setDefaultValues();
	}

	public MolgenisGroup(String id, EntityMetaData entityMeta)
	{
		super(entityMeta);
		setId(id);
		setDefaultValues();
	}

	public boolean isActive()
	{
		Boolean active = getBoolean(ACTIVE);
		return active != null ? active : false;
	}

	public void setActive(boolean active)
	{
		set(ACTIVE, active);
	}

	public String getId()
	{
		return getString(ID);
	}

	public void setId(String id)
	{
		set(ID, id);
	}

	public String getName()
	{
		return getString(NAME);
	}

	public void setName(String name)
	{
		set(NAME, name);
	}

	private void setDefaultValues()
	{
		setActive(true);
	}
}
