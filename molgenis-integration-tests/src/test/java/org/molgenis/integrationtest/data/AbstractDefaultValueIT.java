package org.molgenis.integrationtest.data;

import static org.molgenis.MolgenisFieldTypes.STRING;
import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;
import static org.testng.Assert.assertEquals;

import org.molgenis.data.EditableEntityMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;

public class AbstractDefaultValueIT extends AbstractDatatypeIT
{

	@Override
	public EntityMetaData createMetaData()
	{
		EditableEntityMetaData entityMetaData = new DefaultEntityMetaData("StringTest");
		entityMetaData.addAttribute("id", ROLE_ID).setDataType(STRING).setNillable(false);
		entityMetaData.addAttribute("col1").setDataType(STRING);
		entityMetaData.addAttribute("col2").setDataType(STRING).setDefaultValue("default");
		entityMetaData.addAttribute("col3").setDataType(STRING).setNillable(false).setDefaultValue("defaultRequired");
		entityMetaData.addAttribute("col4").setDataType(STRING).setDefaultValue("default");
		entityMetaData.addAttribute("col5").setDataType(STRING).setNillable(false).setDefaultValue("defaultRequired");
		entityMetaData.addAttribute("col6").setDataType(STRING).setDefaultValue("default");

		return entityMetaData;
	}

	@Override
	public void populateTestEntity(Entity entity) throws Exception
	{
		entity.set("id", "0");
		entity.set("col1", "col1value");
		entity.set("col6", "col6value");
	}

	@Override
	public void verifyTestEntityAfterInsert(Entity entity) throws Exception
	{
		assertEquals(entity.getString("id"), "0");
		assertEquals(entity.getString("col1"), "col1value");
		assertEquals(entity.getString("col2"), "default");
		assertEquals(entity.getString("col3"), "defaultRequired");
		assertEquals(entity.getString("col4"), "default");
		assertEquals(entity.getString("col5"), "defaultRequired");
		assertEquals(entity.getString("col6"), "col6value");
	}

	@Override
	public void updateTestEntity(Entity entity)
	{
		entity.set("id", "0");
		entity.set("col1", "updated_col1value");
		entity.set("col2", "updated_col2value");
		entity.set("col3", "updated_col3value");
		entity.set("col6", null);
	}

	@Override
	public void verifyTestEntityAfterUpdate(Entity entity) throws Exception
	{
		assertEquals(entity.getString("id"), "0");
		assertEquals(entity.getString("col1"), "updated_col1value");
		assertEquals(entity.getString("col2"), "updated_col2value");
		assertEquals(entity.getString("col3"), "updated_col3value");
		assertEquals(entity.getString("col4"), "default");
		assertEquals(entity.getString("col5"), "defaultRequired");
		assertEquals(entity.getString("col6"), "default");
	}

}
