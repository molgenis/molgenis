package org.molgenis.integrationtest.platform;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityTestHarness;
import org.molgenis.data.decorator.DynamicRepositoryDecoratorRegistry;
import org.molgenis.data.decorator.meta.DecoratorConfiguration;
import org.molgenis.data.decorator.meta.DecoratorConfigurationFactory;
import org.molgenis.data.decorator.meta.DecoratorConfigurationMetadata;
import org.molgenis.data.decorator.meta.DynamicDecorator;
import org.molgenis.data.index.job.IndexJobScheduler;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.security.EntityTypePermission;
import org.molgenis.integrationtest.data.decorator.AddingRepositoryDecoratorFactory;
import org.molgenis.integrationtest.data.decorator.PostFixingRepositoryDecoratorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.molgenis.data.security.EntityTypePermission.READ;
import static org.molgenis.data.security.EntityTypePermission.WRITE;
import static org.molgenis.integrationtest.platform.PlatformIT.waitForWorkToBeFinished;
import static org.molgenis.security.core.runas.RunAsSystemAspect.runAsSystem;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

@ContextConfiguration(classes = { PlatformITConfig.class, AddingRepositoryDecoratorFactory.class,
		PostFixingRepositoryDecoratorFactory.class })
@TestExecutionListeners(listeners = WithSecurityContextTestExecutionListener.class)
@Transactional
public class DynamicDecoratorIT extends AbstractTestNGSpringContextTests
{
	private static final Logger LOG = LoggerFactory.getLogger(DynamicDecoratorIT.class);

	private static final String USERNAME = "dynamic-decorator-user";

	@Autowired
	private DataService dataService;
	@Autowired
	private IndexJobScheduler indexService;
	@Autowired
	private MetaDataService metaDataService;
	@Autowired
	private EntityTestHarness testHarness;
	@Autowired
	private DynamicRepositoryDecoratorRegistry dynamicRepositoryDecoratorRegistry;
	@Autowired
	private DecoratorConfigurationFactory decoratorConfigurationFactory;
	@Autowired
	private TestPermissionPopulator testPermissionPopulator;

	private EntityType refEntityTypeDynamic;
	private EntityType entityTypeDynamic;
	private DynamicDecorator addingDynamicDecorator;
	private DynamicDecorator postfixingDynamicDecorator;

	@BeforeClass
	public void setUp()
	{
		refEntityTypeDynamic = testHarness.createDynamicRefEntityType();
		entityTypeDynamic = testHarness.createDynamicTestEntityType(refEntityTypeDynamic);

		runAsSystem(() ->
		{
			metaDataService.addEntityType(refEntityTypeDynamic);
			metaDataService.addEntityType(entityTypeDynamic);
			List<Entity> refs = testHarness.createTestRefEntities(refEntityTypeDynamic, 1);
			List<Entity> entities = testHarness.createTestEntities(entityTypeDynamic, 1, refs)
											   .collect(Collectors.toList());
			dataService.add(refEntityTypeDynamic.getId(), refs.stream());
			dataService.add(entityTypeDynamic.getId(), entities.stream());

			waitForWorkToBeFinished(indexService, LOG);

			addingDynamicDecorator = dataService.findOneById("sys_dec_DynamicDecorator", "add", DynamicDecorator.class);
			postfixingDynamicDecorator = dataService.findOneById("sys_dec_DynamicDecorator", "postfix",
					DynamicDecorator.class);
		});
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void testRegistry()
	{
		populatePermissions();

		//check if all dynamic decorator factory beans get added to the registry
		List<String> factories = dynamicRepositoryDecoratorRegistry.getFactoryIds().collect(Collectors.toList());
		List<String> expected = Arrays.asList("add", "postfix");
		assertTrue(factories.containsAll(expected));
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void testDynamicDecorator()
	{
		populatePermissions();

		//Add decorator config
		DecoratorConfiguration decoratorConfiguration = decoratorConfigurationFactory.create("identifier");
		decoratorConfiguration.setDecorators(Arrays.asList(addingDynamicDecorator, postfixingDynamicDecorator));
		decoratorConfiguration.setEntityTypeId(entityTypeDynamic.getId());
		dataService.add(decoratorConfiguration.getEntityType().getId(), decoratorConfiguration);

		//update row
		Entity entity = dataService.findOneById(entityTypeDynamic.getId(), "0");
		dataService.update(entityTypeDynamic.getId(), entity);
		entity = dataService.findOneById(entityTypeDynamic.getId(), "0");
		assertEquals("string1_TEST", entity.getString("string_attr"));
		assertEquals(11, entity.getInt("int_attr").intValue());

		//remove second decorator
		decoratorConfiguration.setDecorators(Arrays.asList(addingDynamicDecorator));
		dataService.update(decoratorConfiguration.getEntityType().getId(), decoratorConfiguration);

		//update row again
		dataService.update(entityTypeDynamic.getId(), entity);
		entity = dataService.findOneById(entityTypeDynamic.getId(), "0");
		//stayed the same since the postfix decorator was removed
		assertEquals("string1_TEST", entity.getString("string_attr"));
		//added 1
		assertEquals(12, entity.getInt("int_attr").intValue());
	}

	@AfterClass
	public void tearDownAfterClass()
	{
		runAsSystem(() ->
		{
			dataService.deleteAll(entityTypeDynamic.getId());
			dataService.deleteAll(refEntityTypeDynamic.getId());
			dataService.deleteAll(DecoratorConfigurationMetadata.DECORATOR_CONFIGURATION);
			metaDataService.deleteEntityType(entityTypeDynamic.getId());
			metaDataService.deleteEntityType(refEntityTypeDynamic.getId());
		});
		waitForWorkToBeFinished(indexService, LOG);
	}

	private void populatePermissions()
	{
		Map<String, EntityTypePermission> entityTypePermissionMap = new HashMap<>();
		entityTypePermissionMap.put("sys_md_Package", READ);
		entityTypePermissionMap.put("sys_md_EntityType", READ);
		entityTypePermissionMap.put("sys_md_Attribute", READ);
		entityTypePermissionMap.put("sys_Language", READ);
		entityTypePermissionMap.put("sys_L10nString", READ);
		entityTypePermissionMap.put("sys_dec_DynamicDecorator", WRITE);
		entityTypePermissionMap.put("sys_dec_DecoratorConfiguration", WRITE);
		entityTypePermissionMap.put(entityTypeDynamic.getId(), WRITE);
		entityTypePermissionMap.put(refEntityTypeDynamic.getId(), READ);

		testPermissionPopulator.populate(entityTypePermissionMap);
	}
}
