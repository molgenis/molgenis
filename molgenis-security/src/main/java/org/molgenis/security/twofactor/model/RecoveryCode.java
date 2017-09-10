package org.molgenis.security.twofactor.model;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;

public class RecoveryCode extends StaticEntity
{
	public RecoveryCode(Entity entity)
	{
		super(entity);
	}

	public RecoveryCode(EntityType entityType)
	{
		super(entityType);
	}

	public RecoveryCode(String id, EntityType entityType)
	{
		super(entityType);
		setId(id);
	}

	public String getId()
	{
		return getString(RecoveryCodeMetadata.ID);
	}

	public void setId(String id)
	{
		set(RecoveryCodeMetadata.ID, id);
	}

	public String getUserId()
	{
		return getString(RecoveryCodeMetadata.USER_ID);
	}

	public void setUserId(String userId)
	{
		set(RecoveryCodeMetadata.USER_ID, userId);
	}

	public String getCode()
	{
		return getString(RecoveryCodeMetadata.CODE);
	}

	public void setCode(String code)
	{
		set(RecoveryCodeMetadata.CODE, code);
	}
}
