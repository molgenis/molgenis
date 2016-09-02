package org.molgenis.data.elasticsearch.index;

import org.molgenis.data.Entity;
import org.molgenis.data.elasticsearch.index.meta.IndexJobExecutionMeta;
import org.molgenis.data.jobs.model.JobExecution;
import org.molgenis.data.meta.model.EntityMetaData;

/**
 * This entity is used to keep track of the status of the execution of a reindex job.
 */
public class IndexJobExecution extends JobExecution
{
	public IndexJobExecution(Entity entity)
	{
		super(entity);
	}

	public IndexJobExecution(EntityMetaData entityMeta)
	{
		super(entityMeta);
		setDefaultValues();
	}

	public IndexJobExecution(String identifier, EntityMetaData entityMeta)
	{
		super(identifier, entityMeta);
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