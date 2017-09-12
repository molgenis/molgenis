package org.molgenis.auth;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;

import javax.annotation.Nullable;

import static org.molgenis.auth.AuthorityMetaData.ROLE;

public abstract class Authority extends StaticEntity
{
	public Authority(Entity entity)
	{
		super(entity);
	}

	public Authority(EntityType entityType)
	{
		super(entityType);
	}

	@Nullable
	public Role getRole()
	{
		return getEntity(ROLE, Role.class);
	}

	public void setRole(Role role)
	{
		set(ROLE, role);
	}
}
