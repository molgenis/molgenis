package org.molgenis.jobs.model;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;

import javax.annotation.Nullable;

import static org.molgenis.jobs.model.ScheduledJobMetadata.*;

public class ScheduledJob extends StaticEntity
{
	public ScheduledJob(Entity entity)
	{
		super(entity);
	}

	public ScheduledJob(EntityType entityType)
	{
		super(entityType);
	}

	public ScheduledJob(String id, EntityType entityType)
	{
		super(entityType);
		setId(id);
	}

	public void setId(String identifier)
	{
		set(ID, identifier);
	}

	public String getId()
	{
		return getString(ID);
	}

	public String getName()
	{
		return getString(NAME);
	}

	@Nullable
	public String getDescription()
	{
		return getString(DESCRIPTION);
	}

	public String getCronExpression()
	{
		return getString(CRON_EXPRESSION);
	}

	public boolean isActive()
	{
		Boolean active = getBoolean(ACTIVE);
		return active != null && active;
	}

	@Nullable
	public String getFailureEmail()
	{
		return getString(FAILURE_EMAIL);
	}

	@Nullable
	public String getSuccessEmail()
	{
		return getString(SUCCESS_EMAIL);
	}

	@Nullable
	public String getUser()
	{
		return getString(USER);
	}

	public void setUser(String username)
	{
		set(USER, username);
	}

	public String getParameters()
	{
		return getString(PARAMETERS);
	}

	public ScheduledJobType getType()
	{
		return getEntity(TYPE, ScheduledJobType.class);
	}

	public void setType(ScheduledJobType type)
	{
		set(TYPE, type);
	}
}
