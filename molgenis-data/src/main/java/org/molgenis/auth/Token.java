package org.molgenis.auth;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;

import java.util.Date;

import static org.molgenis.auth.TokenMetaData.*;

public class Token extends StaticEntity
{
	public Token(Entity entity)
	{
		super(entity);
	}

	public Token(EntityType entityType)
	{
		super(entityType);
	}

	public Token(String id, EntityType entityType)
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

	public String getToken()
	{
		return getString(TOKEN_ATTR);
	}

	public void setToken(String token)
	{
		set(TOKEN_ATTR, token);
	}

	public Date getExpirationDate()
	{
		return getUtilDate(EXPIRATIONDATE);
	}

	public void setExpirationDate(Date expirationDate)
	{
		set(EXPIRATIONDATE, expirationDate);
	}

	public Date getCreationDate()
	{
		return getUtilDate(CREATIONDATE);
	}

	public void setCreationDate(Date creationDate)
	{
		set(CREATIONDATE, creationDate);
	}

	public String getDescription()
	{
		return getString(DESCRIPTION);
	}

	public void setDescription(String description)
	{
		set(DESCRIPTION, description);
	}
}
