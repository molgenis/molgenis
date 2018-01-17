package org.molgenis.data.index.job;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.jobs.model.JobExecution;

/**
 * This entity is used to keep track of the status of the execution of a reindex job.
 */
public class IndexJobExecution extends JobExecution
{
	public IndexJobExecution(Entity entity)
	{
		super(entity);
	}

	public IndexJobExecution(EntityType entityType)
	{
		super(entityType);
		setDefaultValues();
	}

	public IndexJobExecution(String identifier, EntityType entityType)
	{
		super(identifier, entityType);
		setDefaultValues();
	}

	public String getIndexActionJobID()
	{
		return getString(IndexJobExecutionMeta.INDEX_ACTION_JOB_ID);
	}

	public void setIndexActionJobID(String id)
	{
		set(IndexJobExecutionMeta.INDEX_ACTION_JOB_ID, id);
	}

	private void setDefaultValues()
	{
		setType("Index");
	}
}