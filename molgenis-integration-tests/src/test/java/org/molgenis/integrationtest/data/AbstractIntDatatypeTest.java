package org.molgenis.integrationtest.data;

import static org.molgenis.MolgenisFieldTypes.INT;
import static org.testng.Assert.assertEquals;

import org.molgenis.data.EditableEntityMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;

public class AbstractIntDatatypeTest extends AbstractDatatypeTest
{

	@Override
	public EntityMetaData createMetaData()
	{
		EditableEntityMetaData entityMetaData = new DefaultEntityMetaData("IntegerTest");
		entityMetaData.addAttribute("col1").setDataType(INT).setIdAttribute(true).setNillable(false);
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
