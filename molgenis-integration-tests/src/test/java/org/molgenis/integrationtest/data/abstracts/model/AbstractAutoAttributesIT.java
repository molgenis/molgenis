package org.molgenis.integrationtest.data.abstracts.model;

import org.molgenis.integrationtest.data.abstracts.AbstractDataIntegrationIT;


public abstract class AbstractAutoAttributesIT extends AbstractDataIntegrationIT
{
	public void testIt()
	{
		//		EntityMetaData entityMetaData = new EntityMetaDataImpl("AutoTest");
		//		entityMetaData.addAttribute("identifier", ROLE_ID).setNillable(false).setAuto(true);
		//		entityMetaData.addAttribute("dateAttr").setDataType(MolgenisFieldTypes.DATE).setAuto(true);
		//		entityMetaData.addAttribute("datetimeAttr").setDataType(MolgenisFieldTypes.DATETIME).setAuto(true);
		//		metaDataService.addEntityMeta(entityMetaData);
		//
		//		Entity entity = new DefaultEntity(entityMetaData, dataService);
		//		dataService.add(entityMetaData.getName(), entity);
		//
		//		Supplier<Stream<Entity>> entities = () -> dataService.findAll(entityMetaData.getName());
		//		assertNotNull(entities.get());
		//		assertEquals(entities.get().count(), 1);
		//		entity = entities.get().iterator().next();
		//		assertNotNull(entity.get("identifier"));
		//		assertNotNull(entity.getIdValue());
		//		assertEquals(entity.get("identifier"), entity.getIdValue());
		//		assertNotNull(entity.get("dateAttr"));
		//		assertNotNull(entity.get("datetimeAttr"));
	}
}
