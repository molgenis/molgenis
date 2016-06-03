package org.molgenis.data.elasticsearch.reindex.job;

import org.molgenis.data.DataService;
import org.molgenis.data.elasticsearch.reindex.meta.ReindexJobExecutionMeta;
import org.molgenis.data.jobs.JobExecution;

/**
 * This entity is used to keep track of the status of the execution of a reindex job.
 */
public class ReindexJobExecution extends JobExecution
{
	/**
	 * Auto generated
	 */
	private static final long serialVersionUID = -8650931033501051412L;

	public ReindexJobExecution(DataService dataService)
	{
		super(dataService, new ReindexJobExecutionMeta());
		setType("Reindex");
	}

	public String getReindexActionJobID()
	{
		return getString(ReindexJobExecutionMeta.REINDEX_ACTION_JOB_ID);
	}

	public void setReindexActionJobID(String id)
	{
		set(ReindexJobExecutionMeta.REINDEX_ACTION_JOB_ID, id);
	}
}