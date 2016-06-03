package org.molgenis.data.elasticsearch.reindex.job;

import org.molgenis.data.Entity;
import org.molgenis.data.elasticsearch.reindex.meta.ReindexJobExecutionMeta;
import org.molgenis.data.jobs.JobExecution;

/**
 * This entity is used to keep track of the status of the execution of a reindex job.
 */
public class ReindexJobExecution extends JobExecution
{
	public ReindexJobExecution(Entity entity)
	{
		super(entity);
		setDefaultValues();
	}

	public ReindexJobExecution(ReindexJobExecutionMeta reindexJobExecutionMeta)
	{
		super(reindexJobExecutionMeta);
		setDefaultValues();
	}

	public ReindexJobExecution(String identifier, ReindexJobExecutionMeta reindexJobExecutionMeta)
	{
		super(identifier, reindexJobExecutionMeta);
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