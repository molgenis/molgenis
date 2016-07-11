package org.molgenis.integrationtest.data.abstracts.model;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.support.DynamicEntity;

public class AbstractXrefDatatypeIT extends AbstractDatatypeIT
{
	private EntityMetaData refEntityMetaData;
	private EntityMetaData refEntity2MetaData;

	@Override
	public EntityMetaData createMetaData()
	{
		refEntityMetaData = null; //new EntityMetaData("StringTarget");
		//		refEntityMetaData.addAttribute("identifier", ROLE_ID).setNillable(false); // FIXME
		//		refEntityMetaData.addAttribute("label", ROLE_LABEL);
		metaDataService.addEntityMeta(refEntityMetaData);

		refEntity2MetaData = null; //new EntityMetaData("IntTarget");
		//		refEntity2MetaData.addAttribute("identifier", ROLE_ID).setDataType(INT).setNillable(false); // FIXME
		metaDataService.addEntityMeta(refEntity2MetaData);

		EntityMetaData entityMetaData = null; //new EntityMetaData("XrefTest");
		//		entityMetaData.addAttribute("identifier", ROLE_ID).setNillable(false); // FIXME
		//		entityMetaData.addAttribute("stringRef").setDataType(XREF).setRefEntity(refEntityMetaData).setNillable(false);
		//		entityMetaData.addAttribute("intRef").setDataType(XREF).setRefEntity(refEntity2MetaData);

		return entityMetaData;
	}

	@Override
	public void populateTestEntity(Entity entity) throws Exception
	{
		Entity refEntity = new DynamicEntity(refEntityMetaData);
		refEntity.set("identifier", "ref");
		refEntity.set("label", "refLabel");
		dataService.add(refEntityMetaData.getName(), refEntity);

		Entity refEntity2 = new DynamicEntity(refEntity2MetaData);
		refEntity2.set("identifier", 1);
		dataService.add(refEntity2MetaData.getName(), refEntity2);

		entity.set("identifier", "one");
		entity.set("stringRef", refEntity);
		entity.set("intRef", refEntity2);
	}

	@Override
	public void verifyTestEntityAfterInsert(Entity entity) throws Exception
	{
		assertEquals(entity.get("identifier"), "one");

		Entity refEntity = entity.getEntity("stringRef");
		assertNotNull(refEntity);
		assertEquals(refEntity.get("identifier"), "ref");
		assertEquals(refEntity.getLabelValue(), "refLabel");

		Entity refEntity2 = entity.getEntity("intRef");
		assertNotNull(refEntity2);
		assertEquals(refEntity2.get("identifier"), 1);
	}

	@Override
	public void updateTestEntity(Entity entity) throws Exception
	{
		Entity refEntity = new DynamicEntity(refEntityMetaData);
		refEntity.set("identifier", "refUpdated");
		refEntity.set("label", "refLabelUpdated");
		dataService.add(refEntityMetaData.getName(), refEntity);

		entity.set("stringRef", refEntity);
	}

	@Override
	public void verifyTestEntityAfterUpdate(Entity entity) throws Exception
	{
		assertEquals(entity.get("identifier"), "one");

		Entity refEntity = entity.getEntity("stringRef");
		assertNotNull(refEntity);
		assertEquals(refEntity.get("identifier"), "refUpdated");
		assertEquals(refEntity.getLabelValue(), "refLabelUpdated");

		Entity refEntity2 = entity.getEntity("intRef");
		assertNotNull(refEntity2);
		assertEquals(refEntity2.get("identifier"), 1);
	}

}
