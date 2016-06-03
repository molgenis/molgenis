package org.molgenis.data.reindex.meta;

import static org.molgenis.data.reindex.meta.ReindexActionMetaData.ID;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.SystemEntity;

public class ReindexActionJob extends SystemEntity
{
	public ReindexActionJob(Entity entity)
	{
		super(entity);
	}

	public ReindexActionJob(ReindexActionMetaData reindexActionMetaData)
	{
		super(reindexActionMetaData);
	}

	public ReindexActionJob(String id, ReindexActionMetaData reindexActionMetaData)
	{
		super(reindexActionMetaData);
		set(ID, id);
	}
}
