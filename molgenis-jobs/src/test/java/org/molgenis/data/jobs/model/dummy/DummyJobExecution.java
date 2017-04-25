package org.molgenis.data.jobs.model.dummy;

import org.molgenis.data.Entity;
import org.molgenis.data.jobs.model.JobExecution;
import org.molgenis.data.meta.model.EntityType;

/**
 * Dummy subclass plus metadata to instantiate a testable JobExecution.
 */
public class DummyJobExecution extends JobExecution
{
	public DummyJobExecution(Entity entity)
	{
		super(entity);
	}

	public DummyJobExecution(EntityType entityType)
	{
		super(entityType);
	}

	public DummyJobExecution(String identifier, EntityType entityType)
	{
		super(identifier, entityType);
	}
}
