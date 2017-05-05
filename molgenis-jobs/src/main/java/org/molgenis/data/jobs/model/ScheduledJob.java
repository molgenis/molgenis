package org.molgenis.data.jobs.model;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;
import org.quartz.Job;

import static org.molgenis.data.jobs.model.ScheduledJobMetadata.*;

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

	public String getDescription()
	{
		return getString(DESCRIPTION);
	}

	public String getCronExpression()
	{
		return getString(CRONEXPRESSION);
	}

	public boolean isActive()
	{
		Boolean active = getBoolean(ACTIVE);
		return active != null ? active.booleanValue() : false;
	}

	public String getFailureEmail()
	{
		return getString(FAILURE_EMAIL);
	}

	public String getParameters()
	{
		return getString(PARAMETERS);
	}

	public JobType getType()
	{
		String type = getString(TYPE);
		return type != null ? ScheduledJobMetadata.JobType.valueOf(type) : null;
	}

	public String getGroup()
	{
		return getType().name();
	}

	public Class<? extends Job> getJobClass()
	{
		return getType().getJobClass();
	}
}
