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
		entityMetaData.addAttribute("attrNormal");
		entityMetaData.addAttribute("attrDefault").setDefaultValue("DEFAULT VALUE");
		entityMetaData.addAttribute("attrDefaultToNull").setDefaultValue("DEFAULT VALUE");
		entityMetaData.addAttribute("attrNull").setDefaultValue("DEFAULT VALUE");

		return entityMetaData;
	}

	@Override
	public void populateTestEntity(Entity entity) throws Exception
	{
		entity.set("id", "0");
		entity.set("attrNormal", "inserted value");
		// attrDefault -> DEFAULT VALUE
		// attrDefaultNull -> DEFAULT VALUE
		entity.set("attrNullToDefault", null);
		entity.set("attrNull", null);
	}

	@Override
	public void verifyTestEntityAfterInsert(Entity entity) throws Exception
	{
		assertEquals(entity.getString("id"), "0");
		assertEquals(entity.getString("attrNormal"), "inserted value");
		assertEquals(entity.getString("attrDefault"), "DEFAULT VALUE");
		assertEquals(entity.getString("attrDefaultToNull"), "DEFAULT VALUE");
		assertEquals(entity.getString("attrNull"), null);
	}

	@Override
	public void updateTestEntity(Entity entity)
	{
		entity.set("id", "0");
		entity.set("attrNormal", "inserted value 2");
		// attrDefault -> stays DEFAULT VALUE
		entity.set("attrDefaultToNull", null);
		// attrNull -> stays null
	}

	@Override
	public void verifyTestEntityAfterUpdate(Entity entity) throws Exception
	{
		assertEquals(entity.getString("id"), "0");
		assertEquals(entity.getString("attrNormal"), "inserted value 2");
		assertEquals(entity.getString("attrDefault"), "DEFAULT VALUE");
		assertEquals(entity.getString("attrDefaultToNull"), null);
		assertEquals(entity.getString("attrNull"), null);
	}

}
