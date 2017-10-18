package org.molgenis.security.twofactor.model;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;

import java.time.Instant;
import java.util.Optional;

import static java.time.Instant.now;

public class UserSecret extends StaticEntity
{
	public UserSecret(Entity entity)
	{
		super(entity);
	}

	public UserSecret(EntityType entityType)
	{
		super(entityType);
	}

	public UserSecret(String id, EntityType entityType)
	{
		super(entityType);
		setId(id);
	}

	public String getId()
	{
		return getString(UserSecretMetaData.ID);
	}

	public void setId(String id)
	{
		set(UserSecretMetaData.ID, id);
	}

	public String getUserId()
	{
		return getString(UserSecretMetaData.USER_ID);
	}

	public void setUserId(String userId)
	{
		set(UserSecretMetaData.USER_ID, userId);
	}

	public String getSecret()
	{
		return getString(UserSecretMetaData.SECRET);
	}

	public void setSecret(String secret)
	{
		set(UserSecretMetaData.SECRET, secret);
	}

	public Instant getLastFailedAuthentication()
	{
		return getInstant(UserSecretMetaData.LAST_FAILED_AUTHENICATION);
	}

	public void setLastFailedAuthentication(Instant lastFailedAuthentication)
	{
		set(UserSecretMetaData.LAST_FAILED_AUTHENICATION, lastFailedAuthentication);
	}

	public int getFailedLoginAttempts()
	{
		return getInt(UserSecretMetaData.FAILED_LOGIN_ATTEMPTS);
	}

	public void setFailedLoginAttempts(int failedLoginAttempts)
	{
		set(UserSecretMetaData.FAILED_LOGIN_ATTEMPTS, failedLoginAttempts);
	}

	public void incrementFailedLoginAttempts()
	{
		setFailedLoginAttempts(getFailedLoginAttempts() + 1);
	}

	public boolean hasRecentFailedLoginAttempt(int blockedUserInterval)
	{
		return Optional.ofNullable(getLastFailedAuthentication())
					   .map(lastFailure -> lastFailure.plusSeconds(blockedUserInterval))
					   .filter(now()::isBefore)
					   .isPresent();
	}
}
