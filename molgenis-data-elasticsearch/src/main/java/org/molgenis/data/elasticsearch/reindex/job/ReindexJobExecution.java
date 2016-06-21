package org.molgenis.data.elasticsearch.reindex.job;

import org.molgenis.data.Entity;
import org.molgenis.data.elasticsearch.reindex.meta.ReindexJobExecutionMeta;
import org.molgenis.data.jobs.JobExecution;
import org.molgenis.data.meta.model.EntityMetaData;

/**
 * This entity is used to keep track of the status of the execution of a reindex job.
 */
public class ReindexJobExecution extends JobExecution
{
	public ReindexJobExecution(Entity entity)
	{
		super(entity);
	}

	public ReindexJobExecution(EntityMetaData entityMeta)
	{
		super(entityMeta);
		setDefaultValues();
	}

	public ReindexJobExecution(String identifier, EntityMetaData entityMeta)
	{
		super(identifier, entityMeta);
		setDefaultValues();
	}

	public String getReindexActionJobID()
	{
		return getString(ReindexJobExecutionMeta.REINDEX_ACTION_JOB_ID);
	}

	public void setReindexActionJobID(String id)
	{
		set(ReindexJobExecutionMeta.REINDEX_ACTION_JOB_ID, id);
	}

	private void setDefaultValues()
	{
		setType("Reindex");
	}
}