package org.molgenis.integrationtest.data.harness;

import org.molgenis.data.support.MapEntity;

import static org.molgenis.integrationtest.data.harness.EntitiesHarness.ATTR_ID;

public class TestEntity extends MapEntity
{
	private static final long serialVersionUID = 1L;

	public String getId()
	{
		return getString(ATTR_ID);
	}

	public void setId(String id)
	{
		set(ATTR_ID, id);
	}
}