package org.molgenis.integrationtest.data;

import static org.testng.Assert.assertEquals;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.EditableEntityMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;

public class AbstractComputedAttributesTest extends AbstractDatatypeTest
{
	@Override
	public EntityMetaData createMetaData()
	{
		EditableEntityMetaData entityMetaData = new DefaultEntityMetaData("CalculatedAttrTest");
		entityMetaData.addAttribute("identifier").setIdAttribute(true).setNillable(false);
		entityMetaData.addAttribute("intAttr").setDataType(MolgenisFieldTypes.INT);
		entityMetaData.addAttribute("computedAttr").setDataType(MolgenisFieldTypes.INT).setExpression("intAttr");

		return entityMetaData;
	}

	@Override
	public void populateTestEntity(Entity entity) throws Exception
	{
		entity.set("identifier", "one");
		entity.set("intAttr", 23);
	}

	@Override
	public void verifyTestEntityAfterInsert(Entity entity) throws Exception
	{
		assertEquals(entity.get("intAttr"), 23);
		assertEquals(entity.get("computedAttr"), 23);
	}

	@Override
	public void updateTestEntity(Entity entity) throws Exception
	{
		entity.set("intAttr", 2);
	}

	@Override
	public void verifyTestEntityAfterUpdate(Entity entity) throws Exception
	{
		assertEquals(entity.get("intAttr"), 2);
		assertEquals(entity.get("computedAttr"), 2);
	}
}
