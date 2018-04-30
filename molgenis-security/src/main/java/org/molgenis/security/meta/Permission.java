package org.molgenis.security.meta;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;

import static org.molgenis.security.meta.PermissionMetadata.*;

public class Permission extends StaticEntity
{
	public Permission(Entity entity)
	{
		super(entity);
	}

	public Permission(EntityType entityType)
	{
		super(entityType);
	}

	public Permission(String name, EntityType entityType)
	{
		super(entityType);
		setName(name);
	}

	public void setName(String name)
	{
		set(NAME, name);
	}

	public String getName()
	{
		return getString(NAME);
	}

	public void setMask(int mask)
	{
		set(MASK, mask);
	}

	public int getMask()
	{
		return getInt(MASK);
	}

	public void setCode(String code)
	{
		set(CODE, code);
	}

	public String getCode()
	{
		return getString(CODE);
	}

}
