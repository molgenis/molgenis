package org.molgenis.integrationtest.data.abstracts.model;

import static org.testng.Assert.assertEquals;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.EntityMetaData;

public class AbstractTextDatatypeIT extends AbstractDatatypeIT
{
	@Override
	public EntityMetaData createMetaData()
	{
		EntityMetaData entityMetaData = null; //new EntityMetaData("TextTest");
		//		entityMetaData.addAttribute("identifier", ROLE_ID).setDataType(INT).setNillable(false);// Cannot use
		//																								// TEXT as
		//																								// id
		//																								// attribute
		//		entityMetaData.addAttribute("col1").setDataType(TEXT).setNillable(false);
		//		entityMetaData.addAttribute("col2").setDataType(TEXT); // FIXME

		return entityMetaData;
	}

	@Override
	public void populateTestEntity(Entity entity) throws Exception
	{
		entity.set("identifier", 1);
		entity.set("col1", "lorem");
		entity.set("col2", "ipsum");
	}

	@Override
	public void verifyTestEntityAfterInsert(Entity entity) throws Exception
	{
		assertEquals(entity.get("identifier"), 1);
		assertEquals(entity.get("col1"), "lorem");
		assertEquals(entity.get("col2"), "ipsum");
	}

	@Override
	public void updateTestEntity(Entity entity) throws Exception
	{
		entity.set("col2", "xxx");
	}

	@Override
	public void verifyTestEntityAfterUpdate(Entity entity) throws Exception
	{
		assertEquals(entity.get("identifier"), 1);
		assertEquals(entity.get("col1"), "lorem");
		assertEquals(entity.get("col2"), "xxx");
	}

}
