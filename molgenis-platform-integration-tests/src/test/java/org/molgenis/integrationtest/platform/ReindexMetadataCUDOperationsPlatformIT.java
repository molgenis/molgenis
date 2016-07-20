package org.molgenis.integrationtest.platform;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.elasticsearch.reindex.job.ReindexService;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.AttributeMetaDataMetaData;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.meta.model.EntityMetaDataMetaData;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.runas.RunAsSystemProxy;
import org.molgenis.test.data.EntityTestHarness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Created by jj on 7/18/16.
 */
public class ReindexMetadataCUDOperationsPlatformIT
{
	private static final Logger LOG = LoggerFactory.getLogger(ReindexMetadataCUDOperationsPlatformIT.class);

	public static void testReindexCreateMetaData(SearchService searchService, EntityMetaData entityMetaDataStatic,
			EntityMetaData entityMetaDataDynamic, MetaDataService metaDataService)
	{
		Query<Entity> q1 = new QueryImpl<>();
		q1.eq(EntityMetaDataMetaData.FULL_NAME, entityMetaDataStatic.getName());
		assertEquals(
				searchService.count(q1, metaDataService.getEntityMetaData(EntityMetaDataMetaData.ENTITY_META_DATA)), 1);

		Query<Entity> q2 = new QueryImpl<>();
		q2.eq(EntityMetaDataMetaData.FULL_NAME, entityMetaDataDynamic.getName());
		assertEquals(
				searchService.count(q2, metaDataService.getEntityMetaData(EntityMetaDataMetaData.ENTITY_META_DATA)), 1);
	}

	/**
	 * Test delete only for dynamic entity metadata
	 * static entity metadata cannot be deleted
	 */
	public static void testReindexDeleteMetaData(SearchService searchService, DataService dataService, EntityMetaData entityMetaDataDynamic,
			MetaDataService metaDataService, ReindexService reindexService)
	{

		// 1. verify that sys_test_TypeTestDynamic exists in mapping
		Query<Entity> q = new QueryImpl<>();
		q.eq(EntityMetaDataMetaData.FULL_NAME, entityMetaDataDynamic.getName());
		assertEquals(searchService.count(q, metaDataService.getEntityMetaData(EntityMetaDataMetaData.ENTITY_META_DATA)),
				1);

		// 2. delete sys_test_TypeTestDynamic metadata and wait on reindex
		runAsSystem(() -> {
			metaDataService.deleteEntityMeta(entityMetaDataDynamic.getName());
		});
		PlatformIT.waitForIndexToBeStable(EntityMetaDataMetaData.ENTITY_META_DATA, reindexService, LOG);
		PlatformIT.waitForWorkToBeFinished(reindexService, LOG);

		// 3. verify that mapping is removed
		assertFalse(searchService.hasMapping(entityMetaDataDynamic));

		// Reset context
		RunAsSystemProxy.runAsSystem(() -> {
			metaDataService.addEntityMeta(entityMetaDataDynamic);
		});
		PlatformIT.waitForWorkToBeFinished(reindexService, LOG);
	}

	/**
	 * Test update metadata
	 */
	public static void testReindexUpdateMetaDataUpdateAttribute(SearchService searchService, EntityMetaData entityMetaDataDynamic,
			MetaDataService metaDataService, ReindexService reindexService)
	{
		// 1. verify that sys_test_TypeTestDynamic exists in mapping
		Query<Entity> q = new QueryImpl<>();
		q.eq(EntityMetaDataMetaData.FULL_NAME, entityMetaDataDynamic.getName());
		assertEquals(searchService.count(q, metaDataService.getEntityMetaData(EntityMetaDataMetaData.ENTITY_META_DATA)),
				1);

		// 2. change dataType value of ATTR_EMAIL
		AttributeMetaData toUpdateAttribute = entityMetaDataDynamic.getAttribute(EntityTestHarness.ATTR_EMAIL);
		toUpdateAttribute.setDataType(MolgenisFieldTypes.AttributeType.STRING);
		Object toUpdateAttributeId = toUpdateAttribute.getIdValue();

		// 3. Preform update
		runAsSystem(() -> {
			metaDataService.updateEntityMeta(entityMetaDataDynamic);
		});
		PlatformIT.waitForWorkToBeFinished(reindexService, LOG);
		assertTrue(searchService.hasMapping(entityMetaDataDynamic));

		// Verify metadata changed
		Query<Entity> q2 = new QueryImpl<>();
		EntityMetaData emdActual = metaDataService.getEntityMetaData(AttributeMetaDataMetaData.ATTRIBUTE_META_DATA);
		q2.eq(AttributeMetaDataMetaData.IDENTIFIER, toUpdateAttributeId);
		q2.and();
		q2.eq(AttributeMetaDataMetaData.DATA_TYPE, MolgenisFieldTypes.STRING);
		assertEquals(searchService.count(q2, emdActual),
				1);

		// Reset context
		toUpdateAttribute.setDataType(MolgenisFieldTypes.AttributeType.EMAIL);
		runAsSystem(() -> {
			metaDataService.deleteEntityMeta(entityMetaDataDynamic.getName());
			metaDataService.addEntityMeta(entityMetaDataDynamic);
		});
		PlatformIT.waitForWorkToBeFinished(reindexService, LOG);
	}

	/**
	 * Test metadata removing an attribute
	 *
	 * FIXME
	 */
	public static void testReindexUpdateMetaDataRemoveAttribute(SearchService searchService, EntityMetaData entityMetaDataDynamic,
			MetaDataService metaDataService, ReindexService reindexService)
	{
		// 1. verify that sys_test_TypeTestDynamic exists in mapping
		Query<Entity> q = new QueryImpl<>();
		q.eq(EntityMetaDataMetaData.FULL_NAME, entityMetaDataDynamic.getName());
		assertEquals(searchService.count(q, metaDataService.getEntityMetaData(EntityMetaDataMetaData.ENTITY_META_DATA)),
				1);

		// 2. remove attribute
		AttributeMetaData toRemoveAttribute = entityMetaDataDynamic.getAttribute(EntityTestHarness.ATTR_STRING);
		Object toRemoveAttributeId = toRemoveAttribute.getIdValue();
		// entityMetaDataDynamic.removeAttribute(toRemoveAttribute);

		// 3. Preform update
		runAsSystem(() -> {
			metaDataService.deleteAttribute(entityMetaDataDynamic.getName(), toRemoveAttribute.getName());
		});
		PlatformIT.waitForWorkToBeFinished(reindexService, LOG);
		assertTrue(searchService.hasMapping(entityMetaDataDynamic));

		// 4. Verify metadata changed
		Query<Entity> q2 = new QueryImpl<>();
		EntityMetaData emdActual = metaDataService.getEntityMetaData(AttributeMetaDataMetaData.ATTRIBUTE_META_DATA);
		q2.eq(AttributeMetaDataMetaData.IDENTIFIER, toRemoveAttributeId);
		assertEquals(searchService.count(q2, emdActual),
				0);

		// Reset context
		entityMetaDataDynamic.addAttribute(toRemoveAttribute);
		runAsSystem(() -> {
			metaDataService.addAttribute(entityMetaDataDynamic.getName(), toRemoveAttribute);
		});
		PlatformIT.waitForWorkToBeFinished(reindexService, LOG);
	}
}
