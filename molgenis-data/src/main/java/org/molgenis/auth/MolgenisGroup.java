package org.molgenis.auth;

import static org.molgenis.auth.MolgenisGroupMetaData.ACTIVE;
import static org.molgenis.auth.MolgenisGroupMetaData.ID;
import static org.molgenis.auth.MolgenisGroupMetaData.NAME;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.support.StaticEntity;

public class MolgenisGroup extends StaticEntity
{
	public MolgenisGroup(Entity entity)
	{
		super(entity);
	}

	public MolgenisGroup(EntityMetaData entityMeta)
	{
		super(entityMeta);
	}

	public MolgenisGroup(String id, EntityMetaData entityMeta)
	{
		super(entityMeta);
		setId(id);
	}

	public boolean isActive()
	{
		Boolean active = getBoolean(ACTIVE);
		return active != null ? active : false;
	}

	public void setActive(boolean active)
	{
		set(ACTIVE, active);
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
