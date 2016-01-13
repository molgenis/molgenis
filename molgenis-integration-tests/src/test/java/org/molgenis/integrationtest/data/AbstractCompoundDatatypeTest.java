package org.molgenis.integrationtest.data;

import static org.molgenis.MolgenisFieldTypes.BOOL;
import static org.molgenis.MolgenisFieldTypes.COMPOUND;
import static org.testng.Assert.assertEquals;

import java.util.Arrays;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EditableEntityMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;

public abstract class AbstractCompoundDatatypeTest extends AbstractDatatypeTest
{
	@Override
	public EntityMetaData createMetaData()
	{
		EditableEntityMetaData entityMetaData = new DefaultEntityMetaData("CompoundTest");
		entityMetaData.addAttribute("col1").setDataType(BOOL).setIdAttribute(true).setNillable(false);

		DefaultAttributeMetaData attributePart1 = new DefaultAttributeMetaData("col2").setDataType(BOOL);
		DefaultAttributeMetaData attributePart2 = new DefaultAttributeMetaData("compound1").setDataType(COMPOUND);
		attributePart2.setAttributesMetaData(Arrays.<AttributeMetaData> asList(new DefaultAttributeMetaData("col3")
				.setDataType(BOOL)));

		entityMetaData.addAttribute("compound").setDataType(COMPOUND)
				.setAttributesMetaData(Arrays.<AttributeMetaData> asList(attributePart1, attributePart2));

		return entityMetaData;
	}

	@Override
	public void populateTestEntity(Entity entity) throws Exception
	{
		entity.set("col1", false);
		entity.set("col2", false);
		entity.set("col3", true);
	}

	@Override
	public void verifyTestEntityAfterInsert(Entity entity) throws Exception
	{
		assertEquals(entity.getBoolean("col1"), Boolean.FALSE);
		assertEquals(entity.get("col2"), Boolean.FALSE);
		assertEquals(entity.get("col3"), Boolean.TRUE);
	}

	@Override
	public void updateTestEntity(Entity entity) throws Exception
	{
		entity.set("col2", true);
	}

	@Override
	public void verifyTestEntityAfterUpdate(Entity entity) throws Exception
	{
		assertEquals(entity.getBoolean("col1"), Boolean.FALSE);
		assertEquals(entity.get("col2"), Boolean.TRUE);
		assertEquals(entity.get("col3"), Boolean.TRUE);
	}

}
