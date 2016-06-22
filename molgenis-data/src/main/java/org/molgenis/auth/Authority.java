package org.molgenis.auth;

import static org.molgenis.auth.AuthorityMetaData.ROLE;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.support.StaticEntity;

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
