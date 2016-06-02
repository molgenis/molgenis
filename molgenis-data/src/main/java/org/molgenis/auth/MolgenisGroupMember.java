package org.molgenis.auth;

import static org.molgenis.auth.MolgenisGroupMemberMetaData.ID;
import static org.molgenis.auth.MolgenisGroupMemberMetaData.MOLGENISGROUP;
import static org.molgenis.auth.MolgenisGroupMemberMetaData.MOLGENISUSER;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.SystemEntity;

public class MolgenisGroupMember extends SystemEntity
{
	public MolgenisGroupMember(Entity entity)
	{
		super(entity);
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
		return getEntity(MOLGENISUSER, MolgenisUser.class);
	}

	public void setMolgenisUser(MolgenisUser molgenisUser)
	{
		set(MOLGENISUSER, molgenisUser);
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
