package org.molgenis.auth;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;

import static org.molgenis.auth.UserAuthorityMetaData.ID;
import static org.molgenis.auth.UserAuthorityMetaData.MOLGENIS_USER;

public class UserAuthority extends Authority
{
	public UserAuthority(Entity entity)
	{
		super(entity);
	}

	public UserAuthority(EntityType entityType)
	{
		super(entityType);
	}

	public UserAuthority(String id, EntityType entityType)
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
}
