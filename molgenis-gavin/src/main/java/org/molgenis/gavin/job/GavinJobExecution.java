package org.molgenis.gavin.job;

import org.molgenis.data.Entity;
import org.molgenis.data.jobs.model.JobExecution;
import org.molgenis.data.meta.model.EntityType;

import static org.molgenis.gavin.job.meta.GavinJobExecutionMetaData.FILENAME;

public class GavinJobExecution extends JobExecution
{
	private static final long serialVersionUID = 1L;
	private static final String GAVIN = "gavin";

	public GavinJobExecution(Entity entity)
	{
		super(entity);
		setType(GAVIN);
	}

	public GavinJobExecution(EntityType entityType)
	{
		super(entityType);
		setType(GAVIN);
	}

	public GavinJobExecution(String identifier, EntityType entityType)
	{
		super(identifier, entityType);
		setType(GAVIN);
	}

	public String getFilename()
	{
		return getString(FILENAME);
	}

	public void setFilename(String fileName)
	{
		set(FILENAME, fileName);
	}
}
