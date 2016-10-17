package org.molgenis.integrationtest.platform;

import com.google.api.client.util.Lists;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.elasticsearch.index.job.IndexService;
import org.molgenis.data.meta.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

import java.util.List;

import static org.molgenis.integrationtest.platform.PlatformIT.waitForIndexToBeStable;
import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;
import static org.testng.Assert.assertTrue;

/**
 * These tests actually remove metadata from the database, and not only from the metadata administration
 */
public class MetadataEditOperationsPlatformIT
{
	private static final Logger LOG = LoggerFactory.getLogger(MetadataEditOperationsPlatformIT.class);

	/**
	 * Test metadata Adding an attribute
	 */
	public static void testMetadataEditAddAttribute(EntityType entityTypeDynamic, DataService dataService,
			AttributeFactory attributeFactory, IndexService indexService)
	{
		// 1. verify that sys_test_TypeTestDynamic exists in the database
		Assert.assertNotNull(dataService.getEntityType(entityTypeDynamic.getName()));

		// 2. Add attribute 'Country'
		Attribute toAddAttribute = attributeFactory.create().setName("Country");
		entityTypeDynamic.addAttribute(toAddAttribute);

		// 3. Perform update
		runAsSystem(() ->
		{
			dataService.getMeta().updateEntityType(entityTypeDynamic);
		});
		waitForIndexToBeStable(entityTypeDynamic.getName(), indexService, LOG);

		// 4. Verify that the Attribute repository now contains a 'Country' attribute
		// with an xref to sys_test_TypeTestDynamic
		Repository attributeRepo = dataService.getRepository(AttributeMetadata.ATTRIBUTE_META_DATA);
		Entity actualCountryAttribute = attributeRepo.findOneById(toAddAttribute.getIdValue());

		EntityType attributeXrefToEntity = actualCountryAttribute.getEntity(AttributeMetadata.ENTITY, EntityType.class);
		Assert.assertEquals(attributeXrefToEntity.getName(), entityTypeDynamic.getName());

		// 5. Verify that the sys_test_TypeTestDynamic EntityType has a 'Country' attribute
		Repository entityTypeRepo = dataService.getRepository(EntityTypeMetadata.ENTITY_TYPE_META_DATA);
		Entity actualEntityTypeDynamic = entityTypeRepo.findOneById(entityTypeDynamic.getIdValue());

		List<String> attributes = Lists.newArrayList(actualEntityTypeDynamic.getAttributeNames());
		assertTrue(attributes.contains(actualCountryAttribute.getString(AttributeMetadata.NAME)));

		// 6. Reset context
		entityTypeDynamic.removeAttribute(toAddAttribute);
		runAsSystem(() ->
		{
			dataService.getMeta().updateEntityType(entityTypeDynamic);
		});
		waitForIndexToBeStable(entityTypeDynamic.getName(), indexService, LOG);
	}

}
