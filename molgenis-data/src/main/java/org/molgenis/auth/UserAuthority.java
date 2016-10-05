package org.molgenis.auth;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityMetaData;

import static org.molgenis.auth.UserAuthorityMetaData.ID;
import static org.molgenis.auth.UserAuthorityMetaData.USER;

public class UserAuthority extends Authority
{
	public UserAuthority(Entity entity)
	{
		super(entity);
	}

	public UserAuthority(EntityMetaData entityMeta)
	{
		super(entityMeta);
	}

	public UserAuthority(String id, EntityMetaData entityMeta)
	{
		super(entityMeta);
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

	public User getUser()
	{
		return getEntity(USER, User.class);
	}

	public void setUser(User user)
	{
		set(USER, user);
	}
}
