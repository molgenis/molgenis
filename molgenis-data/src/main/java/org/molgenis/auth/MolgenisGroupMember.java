package org.molgenis.auth;

import static org.molgenis.auth.MolgenisGroupMemberMetaData.ID;
import static org.molgenis.auth.MolgenisGroupMemberMetaData.MOLGENIS_GROUP;
import static org.molgenis.auth.MolgenisGroupMemberMetaData.MOLGENIS_GROUP_MEMBER;
import static org.molgenis.auth.MolgenisGroupMemberMetaData.MOLGENIS_USER;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.SystemEntity;

public class MolgenisGroupMember extends SystemEntity
{
	public MolgenisGroupMember(Entity entity)
	{
		super(entity, MOLGENIS_GROUP_MEMBER);
	}

	public MolgenisGroupMember(MolgenisGroupMemberMetaData molgenisGroupMemberMetaData)
	{
		super(molgenisGroupMemberMetaData);
	}

	public MolgenisGroupMember(String id, MolgenisGroupMemberMetaData molgenisGroupMemberMetaData)
	{
		super(molgenisGroupMemberMetaData);
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
