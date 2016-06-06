package org.molgenis.integrationtest.data.abstracts.model;

import static org.testng.Assert.assertEquals;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.EntityMetaData;

public class AbstractStringDatatypeIT extends AbstractDatatypeIT
{
	@Override
	public EntityMetaData createMetaData()
	{
		EntityMetaData entityMetaData = new EntityMetaData("StringTest");
		//		entityMetaData.addAttribute("col1", ROLE_ID).setDataType(STRING).setNillable(false); // FIXME
		//		entityMetaData.addAttribute("col2").setDataType(STRING);

		return entityMetaData;
	}

	@Override
	public void populateTestEntity(Entity entity) throws Exception
	{
		entity.set("col1", "col1value");
		entity.set("col2", "col2value");
	}

	@Override
	public void verifyTestEntityAfterInsert(Entity entity) throws Exception
	{
		assertEquals(entity.get("col1"), "col1value");
		assertEquals(entity.get("col2"), "col2value");
	}

	@Override
	public void updateTestEntity(Entity entity) throws Exception
	{
		entity.set("col2", "xxx");
	}

	@Override
	public void verifyTestEntityAfterUpdate(Entity entity) throws Exception
	{
		assertEquals(entity.get("col1"), "col1value");
		assertEquals(entity.get("col2"), "xxx");
	}
}
