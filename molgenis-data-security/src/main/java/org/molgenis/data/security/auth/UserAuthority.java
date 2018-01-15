package org.molgenis.data.security.auth;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;

import static org.molgenis.data.security.auth.UserAuthorityMetaData.ID;
import static org.molgenis.data.security.auth.UserAuthorityMetaData.USER;

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

	public User getUser()
	{
		return getEntity(USER, User.class);
	}

	public void setUser(User user)
	{
		set(USER, user);
	}
}
