package org.molgenis.integrationtest.data;

import static org.molgenis.MolgenisFieldTypes.INT;
import static org.molgenis.MolgenisFieldTypes.MREF;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import org.elasticsearch.common.collect.Iterables;
import org.molgenis.data.EditableEntityMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.DefaultEntity;
import org.molgenis.data.support.DefaultEntityMetaData;

public class AbstractMrefDatatypeTest extends AbstractDatatypeTest
{
	private DefaultEntityMetaData refEntityMetaData;
	private DefaultEntityMetaData refEntity2MetaData;

	@Override
	public EntityMetaData createMetaData()
	{
		refEntityMetaData = new DefaultEntityMetaData("StringTarget");
		refEntityMetaData.setLabelAttribute("label");
		refEntityMetaData.addAttribute("identifier").setNillable(false).setIdAttribute(true);
		refEntityMetaData.addAttribute("label");
		metaDataService.addEntityMeta(refEntityMetaData);

		refEntity2MetaData = new DefaultEntityMetaData("IntTarget");
		refEntity2MetaData.addAttribute("identifier").setDataType(INT).setNillable(false).setIdAttribute(true);
		metaDataService.addEntityMeta(refEntity2MetaData);

		EditableEntityMetaData entityMetaData = new DefaultEntityMetaData("MrefTest");
		entityMetaData.addAttribute("identifier").setNillable(false).setIdAttribute(true);
		entityMetaData.addAttribute("stringRef").setDataType(MREF).setRefEntity(refEntityMetaData).setNillable(false);
		entityMetaData.addAttribute("intRef").setDataType(MREF).setRefEntity(refEntity2MetaData).setNillable(true);

		return entityMetaData;
	}

	@Override
	public void populateTestEntity(Entity entity) throws Exception
	{
		Entity refEntity = new DefaultEntity(refEntityMetaData, dataService);
		refEntity.set("identifier", "ref");
		refEntity.set("label", "refLabel");
		dataService.add("StringTarget", refEntity);

		Entity refEntity21 = new DefaultEntity(refEntity2MetaData, dataService);
		refEntity21.set("identifier", 1);
		dataService.add("IntTarget", refEntity21);

		Entity refEntity22 = new DefaultEntity(refEntity2MetaData, dataService);
		refEntity22.set("identifier", 2);
		dataService.add("IntTarget", refEntity22);

		entity.set("identifier", "one");
		entity.set("stringRef", Arrays.asList(refEntity));
		entity.set("intRef", Arrays.asList(refEntity21, refEntity22));
	}

	@Override
	public void verifyTestEntityAfterInsert(Entity entity) throws Exception
	{
		assertEquals(entity.get("identifier"), "one");

		Iterable<Entity> stringRefs = entity.getEntities("stringRef");
		assertNotNull(stringRefs);
		assertEquals(Iterables.size(stringRefs), 1);
		Entity ref = stringRefs.iterator().next();
		assertEquals(ref.get("identifier"), "ref");
		assertEquals(ref.getLabelValue(), "refLabel");

		Iterable<Entity> intRefs = entity.getEntities("intRef");
		assertNotNull(intRefs);
		assertEquals(Iterables.size(intRefs), 2);
		Iterator<Entity> it = intRefs.iterator();
		assertEquals(it.next().get("identifier"), 1);
		assertEquals(it.next().get("identifier"), 2);
	}

	@Override
	public void updateTestEntity(Entity entity) throws Exception
	{
		entity.set("intRef", Collections.emptyList());
	}

	@Override
	public void verifyTestEntityAfterUpdate(Entity entity) throws Exception
	{
		assertEquals(entity.get("identifier"), "one");

		Iterable<Entity> stringRefs = entity.getEntities("stringRef");
		assertNotNull(stringRefs);
		assertEquals(Iterables.size(stringRefs), 1);
		Entity ref = stringRefs.iterator().next();
		assertEquals(ref.get("identifier"), "ref");
		assertEquals(ref.getLabelValue(), "refLabel");

		Iterable<Entity> intRefs = entity.getEntities("intRef");
		assertNotNull(intRefs);
		assertEquals(Iterables.size(intRefs), 0);
	}
}
