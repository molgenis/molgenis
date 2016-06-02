package org.molgenis.integrationtest.data;

import static org.molgenis.MolgenisFieldTypes.BOOL;
import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_ID;
import static org.testng.Assert.assertEquals;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.EntityMetaData;
import org.molgenis.data.meta.EntityMetaDataImpl;

public abstract class AbstractBoolDatatypeIT extends AbstractDatatypeIT
{

	@Override
	public EntityMetaData createMetaData()
	{
		EntityMetaData entityMetaData = new EntityMetaDataImpl("BoolTest");
		entityMetaData.addAttribute("col1", ROLE_ID).setDataType(BOOL).setNillable(false);
		entityMetaData.addAttribute("col2").setDataType(BOOL);
		entityMetaData.addAttribute("col3").setDataType(BOOL);

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
