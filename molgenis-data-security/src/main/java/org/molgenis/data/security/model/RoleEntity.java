package org.molgenis.data.security.model;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;
import org.molgenis.security.core.model.Role;

import static org.molgenis.data.security.model.RoleMetadata.ID;
import static org.molgenis.data.security.model.RoleMetadata.LABEL;

public class RoleEntity extends StaticEntity
{
	public RoleEntity(Entity entity)
	{
		super(entity);
	}

	public RoleEntity(EntityType entityType)
	{
		super(entityType);
	}

	public RoleEntity(String id, EntityType entityType)
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

	public String getLabel()
	{
		return getString(LABEL);
	}

	public void setLabel(String label)
	{
		set(LABEL, label);
	}

	public Role toRole()
	{
		return Role.builder().id(getId()).label(getLabel()).build();
	}
}