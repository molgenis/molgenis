package org.molgenis.data.reindex.meta;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.support.StaticEntity;

import static org.molgenis.data.reindex.meta.ReindexActionGroupMetaData.COUNT;
import static org.molgenis.data.reindex.meta.ReindexActionMetaData.ID;

public class ReindexActionGroup extends StaticEntity
{
	public ReindexActionGroup(Entity entity)
	{
		super(entity);
	}

	public ReindexActionGroup(EntityMetaData entityMeta)
	{
		super(entityMeta);
	}

	public ReindexActionGroup(String id, EntityMetaData entityMeta)
	{
		super(entityMeta);
		set(ID, id);
	}

	public int getCount()
	{
		Integer count = getInt(COUNT);
		return count != null ? count : 0;
	}

	public ReindexActionGroup setCount(int count)
	{
		set(COUNT, count);
		return this;
	}
}
