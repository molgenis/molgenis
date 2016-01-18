package org.molgenis.integrationtest.data;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.Entity;
import org.molgenis.data.support.DefaultEntity;
import org.molgenis.data.support.DefaultEntityMetaData;

import com.google.common.collect.Iterables;

public abstract class AbstractAutoAttributesTest extends AbstractDataIntegrationTest
{
	public void testIt()
	{
		DefaultEntityMetaData entityMetaData = new DefaultEntityMetaData("AutoTest");
		entityMetaData.addAttribute("identifier").setIdAttribute(true).setNillable(false).setAuto(true);
		entityMetaData.addAttribute("dateAttr").setDataType(MolgenisFieldTypes.DATE).setAuto(true);
		entityMetaData.addAttribute("datetimeAttr").setDataType(MolgenisFieldTypes.DATETIME).setAuto(true);
		metaDataService.addEntityMeta(entityMetaData);

		Entity entity = new DefaultEntity(entityMetaData, dataService);
		dataService.add(entityMetaData.getName(), entity);

		Iterable<Entity> entities = dataService.findAll(entityMetaData.getName());
		assertNotNull(entities);
		assertEquals(Iterables.size(entities), 1);
		entity = entities.iterator().next();
		assertNotNull(entity.get("identifier"));
		assertNotNull(entity.getIdValue());
		assertEquals(entity.get("identifier"), entity.getIdValue());
		assertNotNull(entity.get("dateAttr"));
		assertNotNull(entity.get("datetimeAttr"));
	}
}
