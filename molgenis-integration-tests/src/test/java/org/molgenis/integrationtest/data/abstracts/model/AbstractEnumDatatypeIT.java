package org.molgenis.integrationtest.data.abstracts.model;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.EntityMetaData;
import org.molgenis.fieldtypes.EnumField;

public class AbstractEnumDatatypeIT extends AbstractDatatypeIT
{
	@Override
	public EntityMetaData createMetaData()
	{
		EntityMetaData entityMetaData = null; //new EntityMetaData("EnumTest");

		EnumField enumField = new EnumField();
		enumField.setEnumOptions(Arrays.asList("ONE", "TWO"));
		//		entityMetaData.addAttribute("identifier", ROLE_ID).setDataType(enumField).setNillable(false);
		//		entityMetaData.addAttribute("col1").setDataType(enumField); // FIXME

		return entityMetaData;
	}

	@Override
	public void populateTestEntity(Entity entity) throws Exception
	{
		entity.set("identifier", "ONE");
		entity.set("col1", "TWO");
	}

	@Override
	public void verifyTestEntityAfterInsert(Entity entity) throws Exception
	{
		assertEquals(entity.get("identifier"), "ONE");
		assertEquals(entity.get("col1"), "TWO");
	}

	@Override
	public void updateTestEntity(Entity entity) throws Exception
	{
		entity.set("col1", "ONE");
	}

	@Override
	public void verifyTestEntityAfterUpdate(Entity entity) throws Exception
	{
		assertEquals(entity.get("identifier"), "ONE");
		assertEquals(entity.get("col1"), "ONE");
	}
}
