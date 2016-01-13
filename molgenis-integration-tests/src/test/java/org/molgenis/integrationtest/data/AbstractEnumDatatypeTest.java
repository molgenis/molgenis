package org.molgenis.integrationtest.data;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;

import org.molgenis.data.EditableEntityMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.fieldtypes.EnumField;

public class AbstractEnumDatatypeTest extends AbstractDatatypeTest
{
	@Override
	public EntityMetaData createMetaData()
	{
		EditableEntityMetaData entityMetaData = new DefaultEntityMetaData("EnumTest");

		EnumField enumField = new EnumField();
		enumField.setEnumOptions(Arrays.asList("ONE", "TWO"));
		entityMetaData.addAttribute("identifier").setDataType(enumField).setIdAttribute(true).setNillable(false);
		entityMetaData.addAttribute("col1").setDataType(enumField);

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
