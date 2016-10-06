package org.molgenis.auth;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.support.StaticEntity;

import static org.molgenis.auth.GroupMetaData.*;

public class Group extends StaticEntity
{
	public Group(Entity entity)
	{
		super(entity);
		setDefaultValues();
	}

	public Group(EntityMetaData entityMeta)
	{
		super(entityMeta);
		setDefaultValues();
	}

	public Group(String id, EntityMetaData entityMeta)
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
