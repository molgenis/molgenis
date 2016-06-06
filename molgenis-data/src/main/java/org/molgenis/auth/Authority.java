package org.molgenis.auth;

import static org.molgenis.auth.AuthorityMetaData.ROLE;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.EntityMetaData;
import org.molgenis.data.meta.SystemEntity;

public abstract class Authority extends SystemEntity
{
	public Authority(Entity entity, String entityName)
	{
		super(entity, entityName);
	}

	public Authority(EntityMetaData entityMetaData)
	{
		super(entityMetaData);
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
