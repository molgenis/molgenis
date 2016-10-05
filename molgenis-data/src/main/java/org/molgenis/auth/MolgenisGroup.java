package org.molgenis.auth;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;

import static org.molgenis.auth.MolgenisGroupMetaData.*;

public class MolgenisGroup extends StaticEntity
{
	public MolgenisGroup(Entity entity)
	{
		super(entity);
		setDefaultValues();
	}

	public MolgenisGroup(EntityType entityType)
	{
		super(entityType);
		setDefaultValues();
	}

	public MolgenisGroup(String id, EntityType entityType)
	{
		super(entityType);
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
