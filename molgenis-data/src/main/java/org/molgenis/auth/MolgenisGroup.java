package org.molgenis.auth;

import static org.molgenis.auth.MolgenisGroupMetaData.ACTIVE;
import static org.molgenis.auth.MolgenisGroupMetaData.ID;
import static org.molgenis.auth.MolgenisGroupMetaData.NAME;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.SystemEntity;

public class MolgenisGroup extends SystemEntity
{
	public MolgenisGroup(Entity entity)
	{
		super(entity);
	}

	public MolgenisGroup(MolgenisGroupMetaData molgenisGroupMetaData)
	{
		super(molgenisGroupMetaData);
	}

	public MolgenisGroup(String id, MolgenisGroupMetaData molgenisGroupMetaData)
	{
		super(molgenisGroupMetaData);
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
