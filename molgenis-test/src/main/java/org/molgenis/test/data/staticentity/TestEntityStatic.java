package org.molgenis.test.data.staticentity;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.support.StaticEntity;

import static org.molgenis.test.data.EntityTestHarness.ATTR_ID;

public class TestEntityStatic extends StaticEntity
{
	public TestEntityStatic(Entity entity)
	{
		super(entity);
	}

	public TestEntityStatic(EntityMetaData entityMeta)
	{
		super(entityMeta);
	}

	public TestEntityStatic(String id, EntityMetaData entityMeta)
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
