package org.molgenis.jobs.model.hello;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.jobs.model.JobExecution;

import static org.molgenis.jobs.model.hello.HelloWorldJobExecutionMetadata.DELAY;

/**
 * Dummy subclass to instantiate testable {@link JobExecution}s.
 */
public class HelloWorldJobExecution extends JobExecution
{
	public HelloWorldJobExecution(Entity entity)
	{
		super(entity);
		setType("Hello world");
	}

	public HelloWorldJobExecution(EntityType entityType)
	{
		super(entityType);
		setType("Hello world");
	}

	public HelloWorldJobExecution(String identifier, EntityType entityType)
	{
		super(identifier, entityType);
		setType("Hello world");
	}

	public int getDelay()
	{
		return getInt(DELAY);
	}

	public void setDelay(int delay)
	{
		set(DELAY, delay);
	}
}
