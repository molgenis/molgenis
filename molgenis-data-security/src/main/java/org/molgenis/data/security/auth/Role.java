package org.molgenis.data.security.auth;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;

import static org.molgenis.data.security.auth.RoleMetadata.*;

public class Role extends StaticEntity
{
	public Role(Entity entity)
	{
		super(entity);
	}

	public Role(EntityType entityType)
	{
		super(entityType);
		setDefaultValues();
	}

	public Role(String id, EntityType entityType)
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
