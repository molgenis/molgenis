package org.molgenis.integrationtest.platform;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.elasticsearch.index.job.IndexService;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeMetadata;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.runas.RunAsSystemProxy;
import org.molgenis.test.data.EntityTestHarness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.molgenis.data.meta.AttributeType.*;
import static org.molgenis.integrationtest.platform.PlatformIT.waitForWorkToBeFinished;
import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;
import static org.testng.Assert.*;

public class IndexMetadataCUDOperationsPlatformIT
{
	private static final Logger LOG = LoggerFactory.getLogger(IndexMetadataCUDOperationsPlatformIT.class);

	public static void testIndexCreateMetaData(SearchService searchService, EntityType entityTypeStatic,
			EntityType entityTypeDynamic, MetaDataService metaDataService)
	{
		Query<Entity> q1 = new QueryImpl<>();
		q1.eq(EntityTypeMetadata.FULL_NAME, entityTypeStatic.getName());
		assertEquals(searchService.count(q1, metaDataService.getEntityType(EntityTypeMetadata.ENTITY_TYPE_META_DATA)),
				1);

		Query<Entity> q2 = new QueryImpl<>();
		q2.eq(EntityTypeMetadata.FULL_NAME, entityTypeDynamic.getName());
		assertEquals(searchService.count(q2, metaDataService.getEntityType(EntityTypeMetadata.ENTITY_TYPE_META_DATA)),
				1);
	}

	/**
	 * Test delete only for dynamic entity metadata
	 * static entity metadata cannot be deleted
	 */
	public static void testIndexDeleteMetaData(SearchService searchService, DataService dataService,
			EntityType entityTypeDynamic, MetaDataService metaDataService, IndexService indexService)
	{

		// 1. verify that sys_test_TypeTestDynamic exists in mapping
		Query<Entity> q = new QueryImpl<>();
		q.eq(EntityTypeMetadata.FULL_NAME, entityTypeDynamic.getName());
		assertEquals(searchService.count(q, metaDataService.getEntityType(EntityTypeMetadata.ENTITY_TYPE_META_DATA)),
				1);

		// 2. delete sys_test_TypeTestDynamic metadata and wait on index
		runAsSystem(() ->
		{
			dataService.getMeta().deleteEntityType(entityTypeDynamic.getName());
		});
		PlatformIT.waitForIndexToBeStable(EntityTypeMetadata.ENTITY_TYPE_META_DATA, indexService, LOG);
		waitForWorkToBeFinished(indexService, LOG);

		// 3. verify that mapping is removed
		assertFalse(searchService.hasMapping(entityTypeDynamic));

		// Reset context
		RunAsSystemProxy.runAsSystem(() ->
		{
			metaDataService.addEntityType(entityTypeDynamic);
		});
		waitForWorkToBeFinished(indexService, LOG);
	}

	/**
	 * Test metadata Updating an attribute
	 */
	public static void testIndexUpdateMetaDataUpdateAttribute(SearchService searchService, EntityType entityTypeDynamic,
			MetaDataService metaDataService, IndexService indexService)
	{
		// 1. verify that sys_test_TypeTestDynamic exists in mapping
		Query<Entity> q = new QueryImpl<>();
		q.eq(EntityTypeMetadata.FULL_NAME, entityTypeDynamic.getName());
		assertEquals(searchService.count(q, metaDataService.getEntityType(EntityTypeMetadata.ENTITY_TYPE_META_DATA)),
				1);

		// 2. change dataType value of ATTR_EMAIL
		Attribute toUpdateAttribute = entityTypeDynamic.getAttribute(EntityTestHarness.ATTR_EMAIL);
		toUpdateAttribute.setDataType(STRING);
		Object toUpdateAttributeId = toUpdateAttribute.getIdValue();

		// 3. Preform update
		runAsSystem(() ->
		{
			metaDataService.updateEntityType(entityTypeDynamic);
		});
		waitForWorkToBeFinished(indexService, LOG);
		assertTrue(searchService.hasMapping(entityTypeDynamic));

		// Verify metadata changed
		Query<Entity> q2 = new QueryImpl<>();
		EntityType emdActual = metaDataService.getEntityType(AttributeMetadata.ATTRIBUTE_META_DATA);
		q2.eq(AttributeMetadata.ID, toUpdateAttributeId);
		q2.and();
		q2.eq(AttributeMetadata.TYPE, getValueString(STRING));
		assertEquals(searchService.count(q2, emdActual), 1);

		// Reset context
		toUpdateAttribute.setDataType(EMAIL);
		runAsSystem(() ->
		{
			metaDataService.deleteEntityType(entityTypeDynamic.getName());
			metaDataService.addEntityType(entityTypeDynamic);
		});
		waitForWorkToBeFinished(indexService, LOG);
	}

	/**
	 * Test metadata removing an attribute
	 */
	public static void testIndexUpdateMetaDataRemoveAttribute(EntityType emd, String attributeName,
			SearchService searchService, MetaDataService metaDataService, IndexService indexService)
	{
		// 1. verify that sys_test_TypeTestDynamic exists in mapping
		Query<Entity> q = new QueryImpl<>();
		q.eq(EntityTypeMetadata.FULL_NAME, emd.getName());
		assertEquals(searchService.count(q, metaDataService.getEntityType(EntityTypeMetadata.ENTITY_TYPE_META_DATA)),
				1);

		// 2. remove attribute
		Attribute toRemoveAttribute = emd.getAttribute(attributeName);
		emd.removeAttribute(toRemoveAttribute);

		// 3. Preform update
		runAsSystem(() ->
		{
			metaDataService.updateEntityType(emd);
		});
		waitForWorkToBeFinished(indexService, LOG);
		assertTrue(searchService.hasMapping(emd));

		// 4. Verify metadata changed
		Query<Entity> q2 = new QueryImpl<>();
		EntityType emdActual = metaDataService.getEntityType(AttributeMetadata.ATTRIBUTE_META_DATA);
		q2.eq(AttributeMetadata.ID, toRemoveAttribute.getIdValue());
		assertEquals(searchService.count(q2, emdActual), 0);

		// Reset context
		emd.addAttribute(toRemoveAttribute);
		runAsSystem(() ->
		{
			metaDataService.updateEntityType(emd);
		});
		waitForWorkToBeFinished(indexService, LOG);
	}
}
