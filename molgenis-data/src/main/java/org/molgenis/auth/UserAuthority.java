package org.molgenis.auth;

import static org.molgenis.auth.UserAuthorityMetaData.ID;
import static org.molgenis.auth.UserAuthorityMetaData.MOLGENISUSER;

import org.molgenis.data.Entity;

public class UserAuthority extends Authority
{
	public UserAuthority(Entity entity)
	{
		super(entity);
	}

	public UserAuthority(UserAuthorityMetaData userAuthorityMetaData)
	{
		super(userAuthorityMetaData);
	}

	public UserAuthority(String id, UserAuthorityMetaData userAuthorityMetaData)
	{
		super(userAuthorityMetaData);
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
}
