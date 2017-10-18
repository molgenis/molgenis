package org.molgenis.data.security.model;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;

import javax.annotation.Nullable;
import java.time.Instant;

import static java.time.Instant.now;
import static org.molgenis.data.security.model.TokenMetaData.*;

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

	public UserEntity getUser()
	{
		return getEntity(USER, UserEntity.class);
	}

	public void setUser(UserEntity user)
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

	@Nullable
	public Instant getExpirationDate()
	{
		return getInstant(EXPIRATIONDATE);
	}

	public void setExpirationDate(Instant expirationDate)
	{
		set(EXPIRATIONDATE, expirationDate);
	}

	public Instant getCreationDate()
	{
		return getInstant(CREATIONDATE);
	}

	public void setCreationDate(Instant creationDate)
	{
		set(CREATIONDATE, creationDate);
	}

	@Nullable
	public String getDescription()
	{
		return getString(DESCRIPTION);
	}

	public void setDescription(String description)
	{
		set(DESCRIPTION, description);
	}

	public boolean isExpired()
	{
		return (getExpirationDate() != null) && getExpirationDate().isBefore(now());
	}
}
