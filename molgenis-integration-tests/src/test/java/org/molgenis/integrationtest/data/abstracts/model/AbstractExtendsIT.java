package org.molgenis.integrationtest.data.abstracts.model;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.EntityMetaData;

public class AbstractExtendsIT extends AbstractDatatypeIT
{
	@Override
	public EntityMetaData createMetaData()
	{
		EntityMetaData superclass2 = null; //new EntityMetaData("super0").setAbstract(true);
		//		superclass2.addAttribute("col1", ROLE_ID).setDataType(BOOL).setNillable(false); // FIXME
		metaDataService.addEntityMeta(superclass2);

		EntityMetaData superclass = null; //new EntityMetaData("super1").setExtends(superclass2).setAbstract(true);
		//		superclass.addAttribute("col2").setDataType(BOOL); // FIXME
		metaDataService.addEntityMeta(superclass);

		EntityMetaData subclass = null; //new EntityMetaData("ExtendsTest").setExtends(superclass);
		//		subclass.addAttribute("col3").setDataType(BOOL).setNillable(true).setDefaultValue("true"); // FIXME
		metaDataService.addEntityMeta(subclass);

		return subclass;
	}

	@Override
	public void populateTestEntity(Entity entity) throws Exception
	{
		entity.set("col1", false);
		entity.set("col2", true);
	}

	@Override
	public void verifyTestEntityAfterInsert(Entity entity) throws Exception
	{
		assertEquals(entity.get("col1"), false);
		assertEquals(entity.get("col2"), true);
		assertNull(entity.get("col3"));
	}

	@Override
	public void updateTestEntity(Entity entity) throws Exception
	{
		entity.set("col2", false);
	}

	@Override
	public void verifyTestEntityAfterUpdate(Entity entity) throws Exception
	{
		assertEquals(entity.get("col1"), false);
		assertEquals(entity.get("col2"), false);
		assertNull(entity.get("col3"));
	}

}
