package org.molgenis.auth;

import static org.molgenis.auth.UserAuthorityMetaData.ID;
import static org.molgenis.auth.UserAuthorityMetaData.MOLGENIS_USER;
import static org.molgenis.auth.UserAuthorityMetaData.USER_AUTHORITY;

import org.molgenis.data.Entity;

public class UserAuthority extends Authority
{
	public UserAuthority(Entity entity)
	{
		super(entity, USER_AUTHORITY);
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
		return getEntity(MOLGENIS_USER, MolgenisUser.class);
	}

	public void setMolgenisUser(MolgenisUser molgenisUser)
	{
		set(MOLGENIS_USER, molgenisUser);
	}
}
