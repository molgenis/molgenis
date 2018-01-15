package org.molgenis.data.security.auth;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;

import javax.annotation.Nullable;

import static org.molgenis.data.security.auth.AuthorityMetaData.ROLE;

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
	public String getRole()
	{
		return getString(ROLE);
	}

	public void setRole(String role)
	{
		set(ROLE, role);
	}
}
