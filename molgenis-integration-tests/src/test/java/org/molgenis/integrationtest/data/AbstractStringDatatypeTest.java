package org.molgenis.integrationtest.data;

import static org.molgenis.MolgenisFieldTypes.STRING;
import static org.testng.Assert.assertEquals;

import org.molgenis.data.EditableEntityMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;

public class AbstractStringDatatypeTest extends AbstractDatatypeTest
{
	@Override
	public EntityMetaData createMetaData()
	{
		EditableEntityMetaData entityMetaData = new DefaultEntityMetaData("StringTest");
		entityMetaData.addAttribute("col1").setDataType(STRING).setNillable(false).setIdAttribute(true);
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
