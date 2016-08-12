package org.molgenis.auth;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.support.StaticEntity;

import static org.molgenis.auth.AuthorityMetaData.ROLE;

public abstract class Authority extends StaticEntity
{
	public Authority(Entity entity)
	{
		super(entity);
	}

	public Authority(EntityMetaData entityMeta)
	{
		super(entityMeta);
	}

	public String getRole()
	{
		return getString(ROLE);
	}

	public void setRole(String role)
	{
		set(ROLE, role);
	}
}
