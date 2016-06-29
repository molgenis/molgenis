package org.molgenis.test.data;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.support.StaticEntity;

import static org.molgenis.test.data.EntityTestHarness.ATTR_ID;

public class TestEntity extends StaticEntity
{
	public TestEntity(Entity entity)
	{
		super(entity);
	}

	public TestEntity(EntityMetaData entityMeta)
	{
		super(entityMeta);
	}

	public TestEntity(String id, EntityMetaData entityMeta)
	{
		super(entityMeta);
		setId(id);
	}

	public void setId(String id)
	{
		set(ATTR_ID, id);
	}

	public String getId()
	{
		return getString(ATTR_ID);
	}
}
