package org.molgenis.data.security.auth;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;

import static org.molgenis.data.security.auth.RoleMetadata.ID;
import static org.molgenis.data.security.auth.RoleMetadata.NAME;

public class Role extends StaticEntity
{
	public Role(Entity entity)
	{
		super(entity);
	}

	public Role(EntityType entityType)
	{
		super(entityType);
	}

	public Role(String id, EntityType entityType)
	{
		super(entityType);
		setId(id);
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
}
