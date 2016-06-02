package org.molgenis.integrationtest.data;

import static org.molgenis.MolgenisFieldTypes.STRING;
import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_ID;
import static org.testng.Assert.assertEquals;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.EntityMetaData;
import org.molgenis.data.meta.EntityMetaDataImpl;

public class AbstractStringDatatypeIT extends AbstractDatatypeIT
{
	@Override
	public EntityMetaData createMetaData()
	{
		EntityMetaData entityMetaData = new EntityMetaDataImpl("StringTest");
		entityMetaData.addAttribute("col1", ROLE_ID).setDataType(STRING).setNillable(false);
		entityMetaData.addAttribute("col2").setDataType(STRING);

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
