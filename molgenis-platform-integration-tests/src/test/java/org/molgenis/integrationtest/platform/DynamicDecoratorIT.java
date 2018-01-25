package org.molgenis.integrationtest.platform;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityTestHarness;
import org.molgenis.data.decorator.DynamicRepositoryDecoratorRegistry;
import org.molgenis.data.decorator.meta.*;
import org.molgenis.data.index.job.IndexJobScheduler;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.integrationtest.data.decorator.AddingRepositoryDecoratorFactory;
import org.molgenis.integrationtest.data.decorator.PostFixingRepositoryDecoratorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
import static org.molgenis.data.i18n.model.L10nStringMetaData.L10N_STRING;
import static org.molgenis.data.i18n.model.LanguageMetadata.LANGUAGE;
import static org.molgenis.data.meta.model.AttributeMetadata.ATTRIBUTE_META_DATA;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;
import static org.molgenis.data.meta.model.PackageMetadata.PACKAGE;
import static org.molgenis.integrationtest.platform.PlatformIT.makeAuthorities;
import static org.molgenis.integrationtest.platform.PlatformIT.waitForWorkToBeFinished;
import static org.molgenis.security.core.runas.RunAsSystemAspect.runAsSystem;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

@ContextConfiguration(classes = { PlatformITConfig.class, AddingRepositoryDecoratorFactory.class,
		PostFixingRepositoryDecoratorFactory.class })
public class DynamicDecoratorIT extends AbstractTestNGSpringContextTests
{
	private final Logger LOG = LoggerFactory.getLogger(OneToManyIT.class);

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

		});
		setAuthentication();
		waitForWorkToBeFinished(indexService, LOG);

		addingDynamicDecorator = dataService.findOneById("sys_dec_DynamicDecorator", "add", DynamicDecorator.class);
		postfixingDynamicDecorator = dataService.findOneById("sys_dec_DynamicDecorator", "postfix",
				DynamicDecorator.class);

	}

	private void setAuthentication()
	{
		List<GrantedAuthority> authorities = newArrayList();

		authorities.addAll(makeAuthorities(ENTITY_TYPE_META_DATA, false, true, true));
		authorities.addAll(makeAuthorities(ATTRIBUTE_META_DATA, false, true, true));
		authorities.addAll(makeAuthorities(PACKAGE, false, true, true));
		authorities.addAll(makeAuthorities(LANGUAGE, false, true, true));
		authorities.addAll(makeAuthorities(L10N_STRING, false, true, true));
		authorities.addAll(makeAuthorities(entityTypeDynamic.getId(), true, true, true));
		authorities.addAll(makeAuthorities(refEntityTypeDynamic.getId(), false, true, true));
		authorities.addAll(makeAuthorities(DynamicDecoratorMetadata.DYNAMIC_DECORATOR, true, true, true));
		authorities.addAll(makeAuthorities(DecoratorConfigurationMetadata.DECORATOR_CONFIGURATION, true, true, true));

		SecurityContextHolder.getContext()
							 .setAuthentication(new TestingAuthenticationToken("user", "user", authorities));
	}

	@Test
	public void testRegistry()
	{
		//check if all dynamic decorator factory beans get added to the registry
		List<String> factories = dynamicRepositoryDecoratorRegistry.getFactoryIds().collect(Collectors.toList());
		List<String> expected = Arrays.asList("add", "postfix");
		assertTrue(factories.containsAll(expected));
	}

	@Test
	public void testDynamicDecorator()
	{
		//Add decorator config
		DecoratorConfiguration decoratorConfiguration = decoratorConfigurationFactory.create("identifier");
		decoratorConfiguration.setDecorators(Arrays.asList(addingDynamicDecorator, postfixingDynamicDecorator));
		decoratorConfiguration.setEntityTypeId(entityTypeDynamic.getId());
		dataService.add(decoratorConfiguration.getEntityType().getId(), decoratorConfiguration);

		//update row
		Entity entity = dataService.findOneById(entityTypeDynamic.getId(), "0");
		dataService.update(entityTypeDynamic.getId(), entity);
		entity = dataService.findOneById(entityTypeDynamic.getId(), "0");
		assertEquals(entity.getString("string_attr"), "string1_TEST");
		assertEquals(11, entity.getInt("int_attr").intValue());

		//remove second decorator
		decoratorConfiguration.setDecorators(Arrays.asList(addingDynamicDecorator));
		dataService.update(decoratorConfiguration.getEntityType().getId(), decoratorConfiguration);

		//update row again
		dataService.update(entityTypeDynamic.getId(), entity);
		entity = dataService.findOneById(entityTypeDynamic.getId(), "0");
		//stayed the same since the postfix decorator was removed
		assertEquals(entity.getString("string_attr"), "string1_TEST");
		//added 1
		assertEquals(12, entity.getInt("int_attr").intValue());
	}

	@AfterClass
	public void afterMethod()
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
}
