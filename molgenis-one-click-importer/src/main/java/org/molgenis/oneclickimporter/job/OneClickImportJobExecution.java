package org.molgenis.oneclickimporter.job;

import org.molgenis.data.Entity;
import org.molgenis.data.jobs.model.JobExecution;
import org.molgenis.data.meta.model.EntityType;

import static org.molgenis.oneclickimporter.job.OneClickImportJobExecutionMetadata.*;

public class OneClickImportJobExecution extends JobExecution
{
	public OneClickImportJobExecution(Entity entity)
	{
		super(entity);
		setType(ONE_CLICK_IMPORT_JOB_TYPE);
	}

	public OneClickImportJobExecution(EntityType entityType)
	{
		super(entityType);
		setType(ONE_CLICK_IMPORT_JOB_TYPE);
	}

	public OneClickImportJobExecution(String identifier, EntityType entityType)
	{
		super(identifier, entityType);
		setType(ONE_CLICK_IMPORT_JOB_TYPE);
	}

	public String getFile()
	{
		return getString(FILE);
	}

	public void setFile(String value)
	{
		set(FILE, value);
	}

	public String getEntityTypeId()
	{
		return getString(ENTITY_TYPE);
	}

	public void setEntityTypeId(String value)
	{
		set(ENTITY_TYPE, value);
	}
}
