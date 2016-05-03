package org.molgenis.data.elasticsearch.reindex.job;

import org.molgenis.data.DataService;
import org.molgenis.data.jobs.JobExecution;
import org.springframework.stereotype.Component;

import static org.molgenis.data.elasticsearch.reindex.meta.ReindexJobExecutionMetaData.REINDEX_ACTION_JOB_ID;
import static org.molgenis.data.elasticsearch.reindex.meta.ReindexJobExecutionMetaData.REINDEX_JOB_EXECUTION_META_DATA;

/**
 * This entity is used to groups the reindex actions.
 */
public class ReindexJobExecution extends JobExecution
{
	public ReindexJobExecution(DataService dataService)
	{
		super(dataService, REINDEX_JOB_EXECUTION_META_DATA);
		setType("Reindex");
	}

	public String getReindexActionJobID()
	{
		return getString(REINDEX_ACTION_JOB_ID);
	}

	public void setReindexActionJobID(String id)
	{
		set(REINDEX_ACTION_JOB_ID, id);
	}
}
