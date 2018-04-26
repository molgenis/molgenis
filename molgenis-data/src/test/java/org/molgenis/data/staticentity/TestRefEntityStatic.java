package org.molgenis.data.staticentity;

import org.molgenis.data.Entity;
import org.molgenis.data.EntityTestHarness;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;

public class TestRefEntityStatic extends StaticEntity
{
	public TestRefEntityStatic(Entity entity)
	{
		super(entity);
	}

	public TestRefEntityStatic(EntityType entityType)
	{
		super(entityType);
	}

	public TestRefEntityStatic(String id, EntityType entityType)
	{
		super(entityType);
		setId(id);
	}

	public void setId(String id)
	{
		set(EntityTestHarness.ATTR_REF_ID, id);
	}

	public String getId()
	{
		return getString(EntityTestHarness.ATTR_REF_ID);
	}

	public void setRefString(String refString)
	{
		set(EntityTestHarness.ATTR_REF_STRING, refString);
	}

	public String getRefString()
	{
		return getString(EntityTestHarness.ATTR_REF_STRING);
	}

	@Override
	public String toString()
	{
		return "RefEntity[" + getId() + "]";
	}
}
