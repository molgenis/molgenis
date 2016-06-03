package org.molgenis.integrationtest.data;

import static org.molgenis.MolgenisFieldTypes.BOOL;
import static org.molgenis.MolgenisFieldTypes.COMPOUND;
import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_ID;
import static org.testng.Assert.assertEquals;

import java.util.Arrays;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.AttributeMetaData;
import org.molgenis.data.meta.EntityMetaData;
import org.molgenis.data.meta.EntityMetaDataImpl;
import org.molgenis.integrationtest.data.abstracts.model.AbstractDatatypeIT;

public abstract class AbstractCompoundDatatypeIT extends AbstractDatatypeIT
{
	@Override
	public EntityMetaData createMetaData()
	{
		EntityMetaData entityMetaData = new EntityMetaDataImpl("CompoundTest");
		entityMetaData.addAttribute("col1", ROLE_ID).setDataType(BOOL).setNillable(false);

		AttributeMetaData attributePart1 = new AttributeMetaData("col2").setDataType(BOOL);
		AttributeMetaData attributePart2 = new AttributeMetaData("compound1").setDataType(COMPOUND);
		attributePart2.setAttributeParts(
				Arrays.<AttributeMetaData> asList(new AttributeMetaData("col3").setDataType(BOOL)));

		entityMetaData.addAttribute("compound").setDataType(COMPOUND)
				.setAttributeParts(Arrays.<AttributeMetaData> asList(attributePart1, attributePart2));

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
