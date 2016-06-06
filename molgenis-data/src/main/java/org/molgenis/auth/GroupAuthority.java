package org.molgenis.auth;

import static org.molgenis.auth.GroupAuthorityMetaData.GROUP_AUTHORITY;
import static org.molgenis.auth.GroupAuthorityMetaData.ID;
import static org.molgenis.auth.GroupAuthorityMetaData.MOLGENIS_GROUP;

import org.molgenis.data.Entity;

public class GroupAuthority extends Authority
{
	public GroupAuthority(Entity entity)
	{
		super(entity, GROUP_AUTHORITY);
	}

	public GroupAuthority(GroupAuthorityMetaData groupAuthorityMetaData)
	{
		super(groupAuthorityMetaData);
	}

	public GroupAuthority(String id, GroupAuthorityMetaData groupAuthorityMetaData)
	{
		super(groupAuthorityMetaData);
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

	public MolgenisGroup getMolgenisGroup()
	{
		return getEntity(MOLGENIS_GROUP, MolgenisGroup.class);
	}

	public void setMolgenisGroup(MolgenisGroup molgenisGroup)
	{
		set(MOLGENIS_GROUP, molgenisGroup);
	}
}
