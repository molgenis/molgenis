package org.molgenis.integrationtest.data.abstracts.model;

import static org.molgenis.MolgenisFieldTypes.DECIMAL;
import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_ID;
import static org.testng.Assert.assertEquals;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.EntityMetaData;
import org.molgenis.data.meta.EntityMetaDataImpl;

public class AbstractDecimalDatatypeIT extends AbstractDatatypeIT
{
	@Override
	public EntityMetaData createMetaData()
	{
		EntityMetaData entityMetaData = new EntityMetaDataImpl("DecimalTest");
		entityMetaData.addAttribute("col1", ROLE_ID).setDataType(DECIMAL).setNillable(false);
		entityMetaData.addAttribute("col2").setDataType(DECIMAL);
		entityMetaData.addAttribute("col3").setDataType(DECIMAL);

		return entityMetaData;
	}

	@Override
	public void populateTestEntity(Entity entity) throws Exception
	{
		entity.set("col1", 2.9);
		entity.set("col2", 3.1);
		entity.set("col3", 5);
	}

	@Override
	public void verifyTestEntityAfterInsert(Entity entity) throws Exception
	{
		assertEquals(entity.get("col1"), 2.9);
		assertEquals(entity.get("col2"), 3.1);
		assertEquals(entity.get("col3"), 5.0);
	}

	@Override
	public void updateTestEntity(Entity entity) throws Exception
	{
		entity.set("col2", 88.0);
	}

	@Override
	public void verifyTestEntityAfterUpdate(Entity entity) throws Exception
	{
		assertEquals(entity.get("col1"), 2.9);
		assertEquals(entity.get("col2"), 88.0);
		assertEquals(entity.get("col3"), 5.0);
	}

}
