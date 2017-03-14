package org.molgenis.data.staticentity;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;

import static org.molgenis.data.EntityTestHarness.ATTR_ID;

public class TestEntityStatic extends StaticEntity
{
	public TestEntityStatic(Entity entity)
	{
		super(entity);
	}

	public TestEntityStatic(EntityType entityType)
	{
		super(entityType);
	}

	public TestEntityStatic(String id, EntityType entityType)
	{
		super(entityType);
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
