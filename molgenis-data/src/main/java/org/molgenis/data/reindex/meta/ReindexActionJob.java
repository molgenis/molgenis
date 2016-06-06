package org.molgenis.data.reindex.meta;

import static org.molgenis.data.reindex.meta.ReindexActionJobMetaData.COUNT;
import static org.molgenis.data.reindex.meta.ReindexActionJobMetaData.REINDEX_ACTION_JOB;
import static org.molgenis.data.reindex.meta.ReindexActionMetaData.ID;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.SystemEntity;

public class ReindexActionJob extends SystemEntity
{
	public ReindexActionJob(Entity entity)
	{
		super(entity, REINDEX_ACTION_JOB);
	}

	public ReindexActionJob(ReindexActionJobMetaData reindexActionJobMetaData)
	{
		super(reindexActionJobMetaData);
	}

	public ReindexActionJob(String id, ReindexActionJobMetaData reindexActionJobMetaData)
	{
		super(reindexActionJobMetaData);
		set(ID, id);
	}

	public int getCount()
	{
		Integer count = getInt(COUNT);
		return count != null ? count : 0;
	}

	public ReindexActionJob setCount(int count)
	{
		set(COUNT, count);
		return this;
	}
}
