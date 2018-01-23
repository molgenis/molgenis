package org.molgenis.oneclickimporter.job;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.jobs.model.JobExecution;

import javax.annotation.Nullable;

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

	@Nullable
	public String getFile()
	{
		return getString(FILE);
	}

	public void setFile(String value)
	{
		set(FILE, value);
	}

	public String getEntityTypes()
	{
		return getString(ENTITY_TYPES);
	}

	public void setEntityTypes(String value)
	{
		set(ENTITY_TYPES, value);
	}

	@Nullable
	public String getPackage()
	{
		return getString(PACKAGE);
	}

	public void setPackage(String value)
	{
		set(PACKAGE, value);
	}
}
