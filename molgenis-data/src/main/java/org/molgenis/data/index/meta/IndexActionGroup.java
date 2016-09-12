package org.molgenis.data.index.meta;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.support.StaticEntity;

import static org.molgenis.data.index.meta.IndexActionGroupMetaData.COUNT;
import static org.molgenis.data.index.meta.IndexActionMetaData.ID;

public class IndexActionGroup extends StaticEntity
{
	public IndexActionGroup(Entity entity)
	{
		super(entity);
	}

	public IndexActionGroup(EntityMetaData entityMeta)
	{
		super(entityMeta);
	}

	public IndexActionGroup(String id, EntityMetaData entityMeta)
	{
		super(entityMeta);
		set(ID, id);
	}

	public String getId()
	{
		return getString(ID);
	}

	public int getCount()
	{
		Integer count = getInt(COUNT);
		return count != null ? count : 0;
	}

	public IndexActionGroup setCount(int count)
	{
		set(COUNT, count);
		return this;
	}
}
