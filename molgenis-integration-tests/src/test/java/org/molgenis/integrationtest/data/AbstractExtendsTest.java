package org.molgenis.integrationtest.data;

import static org.molgenis.MolgenisFieldTypes.BOOL;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import org.molgenis.data.EditableEntityMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;

public class AbstractExtendsTest extends AbstractDatatypeTest
{
	@Override
	public EntityMetaData createMetaData()
	{
		EditableEntityMetaData superclass2 = new DefaultEntityMetaData("super0").setAbstract(true);
		superclass2.addAttribute("col1").setDataType(BOOL).setNillable(false).setIdAttribute(true);
		metaDataService.addEntityMeta(superclass2);

		EditableEntityMetaData superclass = new DefaultEntityMetaData("super1").setExtends(superclass2).setAbstract(
				true);
		superclass.addAttribute("col2").setDataType(BOOL);
		metaDataService.addEntityMeta(superclass);

		EditableEntityMetaData subclass = new DefaultEntityMetaData("ExtendsTest").setExtends(superclass);
		subclass.addAttribute("col3").setDataType(BOOL).setNillable(true).setDefaultValue("true");
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
