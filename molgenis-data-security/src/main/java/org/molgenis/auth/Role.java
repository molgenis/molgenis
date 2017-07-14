package org.molgenis.auth;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;

import static org.molgenis.auth.GroupMetaData.ID;

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

	public Role setId(String id)
	{
		set(ID, id);
		return this;
	}

	public String getLabel()
	{
		return getString(RoleMetadata.LABEL);
	}

	public Role setLabel(String label)
	{
		set(RoleMetadata.LABEL, label);
		return this;
	}
}
