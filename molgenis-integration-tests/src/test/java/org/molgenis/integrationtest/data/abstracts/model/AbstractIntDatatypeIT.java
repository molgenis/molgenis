package org.molgenis.integrationtest.data.abstracts.model;

import static org.molgenis.MolgenisFieldTypes.INT;
import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_ID;
import static org.testng.Assert.assertEquals;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.EntityMetaData;
import org.molgenis.data.meta.EntityMetaDataImpl;

public class AbstractIntDatatypeIT extends AbstractDatatypeIT
{

	@Override
	public EntityMetaData createMetaData()
	{
		EntityMetaData entityMetaData = new EntityMetaDataImpl("IntegerTest");
		entityMetaData.addAttribute("col1", ROLE_ID).setDataType(INT).setNillable(false);
		entityMetaData.addAttribute("col2").setDataType(INT);
		entityMetaData.addAttribute("col3").setDataType(INT);

		return entityMetaData;
	}

	@Override
	public void populateTestEntity(Entity entity) throws Exception
	{
		entity.set("col1", 1);
		entity.set("col2", 3);
		entity.set("col3", -3);
	}

	@Override
	public void verifyTestEntityAfterInsert(Entity entity) throws Exception
	{
		assertEquals(entity.get("col1"), 1);
		assertEquals(entity.get("col2"), 3);
		assertEquals(entity.get("col3"), -3);
	}

}
