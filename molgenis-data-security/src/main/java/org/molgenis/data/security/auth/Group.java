package org.molgenis.data.security.auth;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.support.StaticEntity;

import static org.molgenis.data.security.auth.GroupMetadata.*;

public class Group extends StaticEntity
{
	public Group(Entity entity)
	{
		super(entity);
	}

	public Group(EntityType entityType)
	{
		super(entityType);
	}

	public Group(String id, EntityType entityType)
	{
		super(entityType);
		setId(id);
	}

	public void setId(String id)
	{
		set(ID, id);
	}

	public String getId()
	{
		return getString(ID);
	}

	public void setName(String name)
	{
		set(NAME, name);
	}

	public String getName()
	{
		return getString(NAME);
	}

	public void setDescription(String description)
	{
		set(DESCRIPTION, description);
	}

	public String getDescription()
	{
		return getString(DESCRIPTION);
	}

	public void setPublic(boolean isPublic)
	{
		set(PUBLIC, isPublic);
	}

	public boolean isPublic()
	{
		return getBoolean(PUBLIC);
	}

	public Iterable<Role> getRoles()
	{
		return getEntities(ROLES, Role.class);
	}

	public void setRoles(Iterable<Role> roles)
	{
		set(ROLES, roles);
	}

	public void setRootPackage(Package rootPackage)
	{
		set(ROOT_PACKAGE, rootPackage);
	}

	public Package getRootPackage()
	{
		return getEntity(ROOT_PACKAGE, Package.class);
	}
}