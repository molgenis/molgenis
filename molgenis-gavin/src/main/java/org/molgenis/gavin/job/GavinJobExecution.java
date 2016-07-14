package org.molgenis.gavin.job;

import org.molgenis.data.Entity;
import org.molgenis.data.jobs.model.JobExecution;
import org.molgenis.data.meta.model.EntityMetaData;

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

	public GavinJobExecution(EntityMetaData entityMeta)
	{
		super(entityMeta);
		setType(GAVIN);
	}

	public GavinJobExecution(String identifier, EntityMetaData entityMeta)
	{
		super(identifier, entityMeta);
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
