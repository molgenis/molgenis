package org.molgenis.data.jobs.model;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;

import static org.molgenis.data.jobs.model.ScheduledJobTypeMetadata.*;

/**
 * Describes a schedulable Job type.
 */
public class ScheduledJobType extends StaticEntity
{
	public ScheduledJobType(Entity entity)
	{
		super(entity);
	}

	public ScheduledJobType(EntityType entityType)
	{
		super(entityType);
	}

	public ScheduledJobType(String name, EntityType entityType)
	{
		super(entityType);
		setName(name);
	}

	public String getName()
	{
		return getString(NAME);
	}

	public void setName(String name)
	{
		set(NAME, name);
	}

	String getLabel()
	{
		return getString(LABEL);
	}

	public void setLabel(String label)
	{
		set(LABEL, label);
	}

	String getDescription()
	{
		return getString(DESCRIPTION);
	}

	public void setDescription(String description)
	{
		set(DESCRIPTION, description);
	}

	public EntityType getJobExecutionType()
	{
		return getEntity(JOB_EXECUTION_TYPE, EntityType.class);
	}

	public void setJobExecutionType(EntityType jobExecutionType)
	{
		set(JOB_EXECUTION_TYPE, jobExecutionType);
	}

	public String getSchema()
	{
		return getString(SCHEMA);
	}

	public void setSchema(String schema)
	{
		set(SCHEMA, schema);
	}
}
