package org.molgenis.oneclickimporter.job;

import org.molgenis.data.Entity;
import org.molgenis.data.jobs.model.JobExecution;
import org.molgenis.data.meta.model.EntityType;

import javax.annotation.Nullable;
import java.util.List;

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

	public Iterable<EntityType> getEntityTypes()
	{
		return getEntities(ENTITY_TYPES, EntityType.class);
	}

	public void setEntityTypes(List<EntityType> values)
	{
		set(ENTITY_TYPES, values);
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
