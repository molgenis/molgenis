package org.molgenis.auth;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;

import static org.molgenis.auth.MolgenisGroupMemberMetaData.*;

public class MolgenisGroupMember extends StaticEntity
{
	public MolgenisGroupMember(Entity entity)
	{
		super(entity);
	}

	public MolgenisGroupMember(EntityType entityType)
	{
		super(entityType);
	}

	public MolgenisGroupMember(String id, EntityType entityType)
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

	public MolgenisUser getMolgenisUser()
	{
		return getEntity(MOLGENIS_USER, MolgenisUser.class);
	}

	public void setMolgenisUser(MolgenisUser molgenisUser)
	{
		set(MOLGENIS_USER, molgenisUser);
	}

	public MolgenisGroup getMolgenisGroup()
	{
		return getEntity(MOLGENIS_GROUP, MolgenisGroup.class);
	}

	public void setMolgenisGroup(MolgenisGroup molgenisGroup)
	{
		set(MOLGENIS_GROUP, molgenisGroup);
	}
}
