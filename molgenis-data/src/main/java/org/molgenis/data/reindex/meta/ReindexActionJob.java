package org.molgenis.data.reindex.meta;

import static org.molgenis.data.reindex.meta.ReindexActionJobMetaData.COUNT;
import static org.molgenis.data.reindex.meta.ReindexActionMetaData.ID;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.EntityMetaData;
import org.molgenis.data.support.StaticEntity;

public class ReindexActionJob extends StaticEntity
{
	public ReindexActionJob(Entity entity)
	{
		super(entity);
	}

	public ReindexActionJob(EntityMetaData entityMeta)
	{
		super(entityMeta);
	}

	public ReindexActionJob(String id, EntityMetaData entityMeta)
	{
		super(entityMeta);
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
