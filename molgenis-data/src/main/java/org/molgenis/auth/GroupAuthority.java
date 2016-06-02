package org.molgenis.auth;

import static org.molgenis.auth.GroupAuthorityMetaData.ID;
import static org.molgenis.auth.GroupAuthorityMetaData.MOLGENISGROUP;

import org.molgenis.data.Entity;

public class GroupAuthority extends Authority
{
	public GroupAuthority(Entity entity)
	{
		super(entity);
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
		return getEntity(MOLGENISGROUP, MolgenisGroup.class);
	}

	public void setMolgenisGroup(MolgenisGroup molgenisGroup)
	{
		set(MOLGENISGROUP, molgenisGroup);
	}
}
