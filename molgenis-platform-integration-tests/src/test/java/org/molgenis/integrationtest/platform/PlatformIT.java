package org.molgenis.integrationtest.platform;

import org.apache.commons.io.FileUtils;
import org.molgenis.data.*;
import org.molgenis.data.cache.l2.L2Cache;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.elasticsearch.index.job.IndexService;
import org.molgenis.data.i18n.I18nUtils;
import org.molgenis.data.i18n.LanguageService;
import org.molgenis.data.i18n.model.I18nStringMetaData;
import org.molgenis.data.i18n.model.LanguageFactory;
import org.molgenis.data.i18n.model.LanguageMetaData;
import org.molgenis.data.listeners.EntityListener;
import org.molgenis.data.listeners.EntityListenersService;
import org.molgenis.data.meta.MetaDataServiceImpl;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.AttributeMetaDataMetaData;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.test.data.EntitySelfXrefTestHarness;
import org.molgenis.test.data.EntityTestHarness;
import org.molgenis.test.data.staticentity.TestEntityStatic;
import org.molgenis.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.*;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;
import static org.molgenis.data.RepositoryCapability.*;
import static org.molgenis.data.i18n.model.I18nStringMetaData.I18N_STRING;
import static org.molgenis.data.meta.model.AttributeMetaDataMetaData.ATTRIBUTE_META_DATA;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_META_DATA;
import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;
import static org.molgenis.test.data.EntityTestHarness.*;
import static org.molgenis.util.MolgenisDateFormat.getDateFormat;
import static org.molgenis.util.MolgenisDateFormat.getDateTimeFormat;
import static org.testng.Assert.*;

@ContextConfiguration(classes = { PlatformITConfig.class })
public class PlatformIT extends AbstractTestNGSpringContextTests
{
	private final Logger LOG = LoggerFactory.getLogger(PlatformIT.class);

	private EntityType entityTypeStatic;
	private EntityType refEntityTypeStatic;
	private EntityType entityTypeDynamic;
	private EntityType refEntityTypeDynamic;
	private EntityType selfXrefEntityType;

	@Autowired
	private IndexService indexService;
	@Autowired
	private EntityTestHarness testHarness;
	@Autowired
	private EntitySelfXrefTestHarness entitySelfXrefTestHarness;
	@Autowired
	private DataService dataService;
	@Autowired
	private SearchService searchService;
	@Autowired
	private MetaDataServiceImpl metaDataService;
	@Autowired
	private ConfigurableApplicationContext applicationContext;
	@Autowired
	private EntityListenersService entityListenersService;
	@Autowired
	private L2Cache l2Cache;
	@Autowired
	private LanguageFactory languageFactory;
	@Autowired
	private LanguageService languageService;
	@Autowired
	private I18nStringMetaData i18nStringMetaData;
	@Autowired
	private LanguageMetaData languageMetaData;
	@Autowired
	private EntityTypeMetadata entityTypeMetadata;
	@Autowired
	private AttributeMetaDataMetaData attributeMetaDataMetaData;

	/**
	 * Wait till the whole index is stable. Index job is done a-synchronized.
	 */
	public static void waitForWorkToBeFinished(IndexService indexService, Logger log)
	{
		try
		{
			indexService.waitForAllIndicesStable();
			log.info("All work finished");
		}
		catch (InterruptedException e)
		{
			log.warn("Interrupted while waiting for index to become stable!", e);
			fail("Interrupted while waiting for index to become stable!");
		}
	}

	/**
	 * Wait till the index is stable. Index job is executed asynchronously. This method waits for all index jobs
	 * relevant for this entity to be finished.
	 *
	 * @param entityName name of the entitiy whose index needs to be stable
	 */
	public static void waitForIndexToBeStable(String entityName, IndexService indexService, Logger log)
	{
		try
		{
			indexService.waitForIndexToBeStableIncludingReferences(entityName);
			log.info("Index for entity [{}] incl. references is stable", entityName);
		}
		catch (InterruptedException e)
		{
			log.info("Interrupted waiting for [{}] incl. references to become stable", entityName, e);
		}
	}

	@BeforeClass
	public void setUp()
	{
		refEntityTypeStatic = testHarness.createStaticRefTestEntityType();
		entityTypeStatic = testHarness.createStaticTestEntityType();
		refEntityTypeDynamic = testHarness.createDynamicRefEntityType();
		entityTypeDynamic = testHarness.createDynamicTestEntityType();

		// Create a self refer entity
		selfXrefEntityType = entitySelfXrefTestHarness.createDynamicEntityType();
		selfXrefEntityType.getAttribute(ATTR_XREF).setRefEntity(selfXrefEntityType);

		runAsSystem(() ->
		{
			metaDataService.addEntityType(refEntityTypeDynamic);
			metaDataService.addEntityType(entityTypeDynamic);
			metaDataService.addEntityType(selfXrefEntityType);
		});
		setAuthentication();
		createLanguages();
		waitForWorkToBeFinished(indexService, LOG);
	}

	private void setAuthentication()
	{
		// Permissions refEntityTypeStatic.getName()
		String writeTestRefEntityStatic = "ROLE_ENTITY_WRITE_" + refEntityTypeStatic.getName().toUpperCase();
		String readTestRefEntityStatic = "ROLE_ENTITY_READ_" + refEntityTypeStatic.getName().toUpperCase();
		String countTestRefEntityStatic = "ROLE_ENTITY_COUNT_" + refEntityTypeStatic.getName().toUpperCase();

		// Permissions entityTypeStatic.getName()
		String writeTestEntityStatic = "ROLE_ENTITY_WRITE_" + entityTypeStatic.getName().toUpperCase();
		String readTestEntityStatic = "ROLE_ENTITY_READ_" + entityTypeStatic.getName().toUpperCase();
		String countTestEntityStatic = "ROLE_ENTITY_COUNT_" + entityTypeStatic.getName().toUpperCase();

		// Permissions entityTypeDynamic.getName()
		String writeTestEntity = "ROLE_ENTITY_WRITE_" + entityTypeDynamic.getName().toUpperCase();
		String readTestEntity = "ROLE_ENTITY_READ_" + entityTypeDynamic.getName().toUpperCase();
		String countTestEntity = "ROLE_ENTITY_COUNT_" + entityTypeDynamic.getName().toUpperCase();

		// Permissions refEntityTypeDynamic.getName()
		String readTestRefEntity = "ROLE_ENTITY_READ_" + refEntityTypeDynamic.getName().toUpperCase();
		String countTestRefEntity = "ROLE_ENTITY_COUNT_" + refEntityTypeDynamic.getName().toUpperCase();

		// Permissions selfXrefEntityType.getName()
		String writeSelfXrefEntity = "ROLE_ENTITY_WRITE_" + selfXrefEntityType.getName().toUpperCase();
		String readSelfXrefEntity = "ROLE_ENTITY_READ_" + selfXrefEntityType.getName().toUpperCase();
		String countSelfXrefEntity = "ROLE_ENTITY_COUNT_" + selfXrefEntityType.getName().toUpperCase();

		// Permissions languageMetaData
		String writeLanguageMetaData = "ROLE_ENTITY_WRITE_" + languageMetaData.getName().toUpperCase();
		String readLanguageMetaData = "ROLE_ENTITY_READ_" + languageMetaData.getName().toUpperCase();
		String countLanguageMetaData = "ROLE_ENTITY_COUNT_" + languageMetaData.getName().toUpperCase();

		// Permissions attributeMetaDataMetaData
		String writeAttributeMetaDataMetaData =
				"ROLE_ENTITY_WRITE_" + attributeMetaDataMetaData.getName().toUpperCase();
		String readAttributeMetaDataMetaData = "ROLE_ENTITY_READ_" + attributeMetaDataMetaData.getName().toUpperCase();
		String countAttributeMetaDataMetaData =
				"ROLE_ENTITY_COUNT_" + attributeMetaDataMetaData.getName().toUpperCase();

		// Permissions i18nStringMetaData
		String writeI18nStringMetaData = "ROLE_ENTITY_WRITE_" + i18nStringMetaData.getName().toUpperCase();

		// EntityTypeMetadata
		String writeEntityTypeMetaData = "ROLE_ENTITY_WRITE_" + entityTypeMetadata.getName().toUpperCase();
		String readEntityTypeMetaData = "ROLE_ENTITY_READ_" + entityTypeMetadata.getName().toUpperCase();
		String countEntityTypeMetaData = "ROLE_ENTITY_COUNT_" + entityTypeMetadata.getName().toUpperCase();

		SecurityContextHolder.getContext().setAuthentication(
				new TestingAuthenticationToken("user", "user", writeTestEntity, readTestEntity, readTestRefEntity,
						countTestEntity, countTestRefEntity, writeSelfXrefEntity, readSelfXrefEntity,
						countSelfXrefEntity, writeTestEntityStatic, readTestEntityStatic, countTestEntityStatic,
						writeTestRefEntityStatic, readTestRefEntityStatic, countTestRefEntityStatic,
						writeLanguageMetaData, readLanguageMetaData, countLanguageMetaData,
						writeAttributeMetaDataMetaData, readAttributeMetaDataMetaData, countAttributeMetaDataMetaData,
						writeI18nStringMetaData, writeEntityTypeMetaData, readEntityTypeMetaData,
						countEntityTypeMetaData, "ROLE_ENTITY_READ_SYS_MD_ENTITIES",
						"ROLE_ENTITY_READ_SYS_MD_ATTRIBUTES", "ROLE_ENTITY_READ_SYS_MD_PACKAGES"));
	}

	private void createLanguages()
	{
		dataService.add(LanguageMetaData.LANGUAGE, languageFactory.create("en", "English"));
		dataService.add(LanguageMetaData.LANGUAGE, languageFactory.create("nl", "Nederlands"));
	}

	@AfterClass
	public void cleanUp() throws InterruptedException
	{
		// Give asyncTransactionLog time to stop gracefully
		TimeUnit.SECONDS.sleep(1);

		applicationContext.close();
		SecurityContextHolder.getContext().setAuthentication(null);

		try
		{
			// Delete molgenis home folder
			FileUtils.deleteDirectory(new File(System.getProperty("molgenis.home")));
		}
		catch (IOException e)
		{
			LOG.error("Error removing molgenis home directory", e);
		}
		l2Cache.logStatistics();
		cleanUpLanguages();
	}

	private void cleanUpLanguages()
	{
		List<AttributeMetaData> languageAttrs = new ArrayList<>();
		for (AttributeMetaData attr : attributeMetaDataMetaData.getAttributes())
		{
			if (I18nUtils.isI18n(attr.getName()))
			{
				languageAttrs.add(attr);
			}
		}
		languageAttrs.forEach(attributeMetaDataMetaData::removeAttribute);

		languageAttrs.clear();
		for (AttributeMetaData attr : entityTypeMetadata.getAttributes())
		{
			if (I18nUtils.isI18n(attr.getName()))
			{
				languageAttrs.add(attr);
			}
		}
		languageAttrs.forEach(entityTypeMetadata::removeAttribute);
	}

	@AfterMethod
	public void afterMethod()
	{
		runAsSystem(() ->
		{
			dataService.deleteAll(entityTypeStatic.getName());
			dataService.deleteAll(refEntityTypeStatic.getName());
			dataService.deleteAll(entityTypeDynamic.getName());
			dataService.deleteAll(refEntityTypeDynamic.getName());
			dataService.deleteAll(selfXrefEntityType.getName());
		});
		waitForIndexToBeStable(entityTypeStatic.getName(), indexService, LOG);
		waitForIndexToBeStable(refEntityTypeStatic.getName(), indexService, LOG);
		waitForIndexToBeStable(entityTypeDynamic.getName(), indexService, LOG);
		waitForIndexToBeStable(refEntityTypeDynamic.getName(), indexService, LOG);
		waitForIndexToBeStable(selfXrefEntityType.getName(), indexService, LOG);
	}

	@Test
	public void testLanguageService()
	{
		assertEquals(languageService.getCurrentUserLanguageCode(), "en");
		assertEqualsNoOrder(languageService.getLanguageCodes().toArray(), new String[] { "en", "nl" });

		// NL
		assertNotNull(dataService.getEntityType(I18N_STRING).getAttribute("nl"));
		assertNotNull(dataService.getEntityType(ENTITY_META_DATA).getAttribute("label-nl"));
		assertNotNull(dataService.getEntityType(ENTITY_META_DATA).getAttribute("description-nl"));
		assertNotNull(dataService.getEntityType(ATTRIBUTE_META_DATA).getAttribute("label-nl"));
		assertNotNull(dataService.getEntityType(ATTRIBUTE_META_DATA).getAttribute("description-nl"));

		// EN
		assertNotNull(dataService.getEntityType(I18N_STRING).getAttribute("en"));
		assertNotNull(dataService.getEntityType(ENTITY_META_DATA).getAttribute("label-en"));
		assertNotNull(dataService.getEntityType(ENTITY_META_DATA).getAttribute("description-en"));
		assertNotNull(dataService.getEntityType(ATTRIBUTE_META_DATA).getAttribute("label-en"));
		assertNotNull(dataService.getEntityType(ATTRIBUTE_META_DATA).getAttribute("description-en"));

		Entity car = new DynamicEntity(i18nStringMetaData);
		car.set(I18nStringMetaData.MSGID, "car");
		car.set("en", "car");
		car.set("nl", "auto");
		dataService.add(I18nStringMetaData.I18N_STRING, car);
		assertEquals(languageService.getBundle("en").getString("car"), "car");
		assertEquals(languageService.getBundle("nl").getString("car"), "auto");

		// Test default value
		assertEquals(languageService.getBundle().getString("car"), "car");
	}

	@Test
	public void testEntityListener()
	{
		List<Entity> refEntities = testHarness.createTestRefEntities(refEntityTypeDynamic, 6);
		List<Entity> entities = testHarness.createTestEntities(entityTypeDynamic, 2, refEntities).collect(toList());
		runAsSystem(() ->
		{
			dataService.add(refEntityTypeDynamic.getName(), refEntities.stream());
			dataService.add(entityTypeDynamic.getName(), entities.stream());
			waitForIndexToBeStable(entityTypeDynamic.getName(), indexService, LOG);
		});

		AtomicInteger updateCalled = new AtomicInteger(0);
		EntityListener listener = new EntityListener()
		{
			@Override
			public Object getEntityId()
			{
				return entities.get(0).getIdValue();
			}

			@Override
			public void postUpdate(Entity entity)
			{
				updateCalled.incrementAndGet();
				assertEquals(entity.getIdValue(), entities.get(0).getIdValue());
			}
		};

		try
		{
			// Test that the listener is being called
			entityListenersService.addEntityListener(entityTypeDynamic.getName(), listener);
			dataService.update(entityTypeDynamic.getName(), entities.stream());
			assertEquals(updateCalled.get(), 1);
			waitForIndexToBeStable(entityTypeDynamic.getName(), indexService, LOG);
			assertPresent(entityTypeDynamic, entities);
		}
		finally
		{
			// Test that the listener is actually removed and not called anymore
			entityListenersService.removeEntityListener(entityTypeDynamic.getName(), listener);
			updateCalled.set(0);
			dataService.update(entityTypeDynamic.getName(), entities.stream());
			assertEquals(updateCalled.get(), 0);
			waitForIndexToBeStable(entityTypeDynamic.getName(), indexService, LOG);
			assertPresent(entityTypeDynamic, entities);
		}
	}

	@Test
	public void testAdd()
	{
		List<Entity> entities = createDynamic(2).collect(toList());
		assertEquals(searchService.count(entityTypeDynamic), 0);
		dataService.add(entityTypeDynamic.getName(), entities.stream());
		waitForIndexToBeStable(entityTypeDynamic.getName(), indexService, LOG);
		assertEquals(dataService.count(entityTypeDynamic.getName(), new QueryImpl<>()), 2);
		assertEquals(searchService.count(entityTypeDynamic), 2);
		assertPresent(entityTypeDynamic, entities);
	}

	@Test
	public void testCount()
	{
		List<Entity> entities = createDynamic(2).collect(toList());
		dataService.add(entityTypeDynamic.getName(), entities.stream());
		waitForIndexToBeStable(entityTypeDynamic.getName(), indexService, LOG);
		assertEquals(dataService.count(entityTypeDynamic.getName(), new QueryImpl<>()), 2);
		assertEquals(searchService.count(entityTypeDynamic), 2);
		assertPresent(entityTypeDynamic, entities);
	}

	@Test
	public void testDelete()
	{
		Entity entity = createDynamic(1).findFirst().get();
		dataService.add(entityTypeDynamic.getName(), entity);
		waitForIndexToBeStable(entityTypeDynamic.getName(), indexService, LOG);
		assertPresent(entityTypeDynamic, entity);

		dataService.delete(entityTypeDynamic.getName(), entity);
		waitForIndexToBeStable(entityTypeDynamic.getName(), indexService, LOG);
		assertNotPresent(entity);
	}

	@Test
	public void testDeleteById()
	{
		Entity entity = createDynamic(1).findFirst().get();
		dataService.add(entityTypeDynamic.getName(), entity);
		waitForIndexToBeStable(entityTypeDynamic.getName(), indexService, LOG);
		assertPresent(entityTypeDynamic, entity);

		dataService.deleteById(entityTypeDynamic.getName(), entity.getIdValue());
		waitForIndexToBeStable(entityTypeDynamic.getName(), indexService, LOG);
		assertNotPresent(entity);
	}

	@Test
	public void testDeleteStream()
	{
		List<Entity> entities = createDynamic(2).collect(toList());
		dataService.add(entityTypeDynamic.getName(), entities.stream());
		waitForIndexToBeStable(entityTypeDynamic.getName(), indexService, LOG);
		assertEquals(dataService.count(entityTypeDynamic.getName(), new QueryImpl<>()), entities.size());

		dataService.delete(entityTypeDynamic.getName(), entities.stream());
		waitForIndexToBeStable(entityTypeDynamic.getName(), indexService, LOG);
		assertEquals(dataService.count(entityTypeDynamic.getName(), new QueryImpl<>()), 0);
	}

	@Test
	public void testDeleteAll()
	{
		List<Entity> entities = createDynamic(5).collect(toList());
		dataService.add(entityTypeDynamic.getName(), entities.stream());
		waitForIndexToBeStable(entityTypeDynamic.getName(), indexService, LOG);
		assertEquals(dataService.count(entityTypeDynamic.getName(), new QueryImpl<>()), entities.size());

		dataService.deleteAll(entityTypeDynamic.getName());
		waitForIndexToBeStable(entityTypeDynamic.getName(), indexService, LOG);
		assertEquals(dataService.count(entityTypeDynamic.getName(), new QueryImpl<>()), 0);
	}

	@Test
	public void testFindAllEmpty()
	{
		Stream<Entity> retrieved = dataService.findAll(entityTypeDynamic.getName());
		assertEquals(retrieved.count(), 0);
	}

	@Test
	public void testFindAll()
	{
		List<Entity> entities = createDynamic(5).collect(toList());
		dataService.add(entityTypeDynamic.getName(), entities.stream());
		waitForIndexToBeStable(entityTypeDynamic.getName(), indexService, LOG);
		Stream<Entity> retrieved = dataService.findAll(entityTypeDynamic.getName());
		assertEquals(retrieved.count(), entities.size());
	}

	@Test
	public void testFindAllTyped()
	{
		List<Entity> entities = createDynamic(1).collect(toList());
		dataService.add(entityTypeDynamic.getName(), entities.stream());
		waitForIndexToBeStable(entityTypeDynamic.getName(), indexService, LOG);
		Supplier<Stream<Entity>> retrieved = () -> dataService.findAll(entityTypeDynamic.getName(), Entity.class);
		assertEquals(retrieved.get().count(), 1);
		assertEquals(retrieved.get().iterator().next().getIdValue(), entities.get(0).getIdValue());
	}

	@Test
	public void testFindAllByIds()
	{
		List<Entity> entities = createDynamic(5).collect(toList());
		dataService.add(entityTypeDynamic.getName(), entities.stream());
		waitForIndexToBeStable(entityTypeDynamic.getName(), indexService, LOG);
		Stream<Object> ids = Stream.concat(entities.stream().map(Entity::getIdValue), of("bogus"));
		Stream<Entity> retrieved = dataService.findAll(entityTypeDynamic.getName(), ids);
		assertEquals(retrieved.count(), entities.size());
	}

	@Test
	public void testFindAllByIdsTyped()
	{
		List<Entity> entities = createStatic(5).collect(toList());
		dataService.add(entityTypeStatic.getName(), entities.stream());
		waitForIndexToBeStable(entityTypeStatic.getName(), indexService, LOG);

		Supplier<Stream<TestEntityStatic>> retrieved = () -> dataService.findAll(entityTypeStatic.getName(),
				Stream.concat(entities.stream().map(Entity::getIdValue), of("bogus")), TestEntityStatic.class);
		assertEquals(retrieved.get().count(), entities.size());
		assertEquals(retrieved.get().iterator().next().getId(), entities.get(0).getIdValue());
		assertEquals(retrieved.get().iterator().next().getIdValue(), entities.get(0).getIdValue());
	}

	@Test
	public void testFindAllStreamFetch()
	{
		List<Entity> entities = createDynamic(5).collect(toList());
		dataService.add(entityTypeDynamic.getName(), entities.stream());
		waitForIndexToBeStable(entityTypeDynamic.getName(), indexService, LOG);
		Stream<Object> ids = concat(entities.stream().map(Entity::getIdValue), of("bogus"));
		Stream<Entity> retrieved = dataService
				.findAll(entityTypeDynamic.getName(), ids, new Fetch().field(ATTR_ID));
		assertEquals(retrieved.count(), entities.size());
	}

	@DataProvider(name = "findQueryOperatorEq")
	private static Object[][] findQueryOperatorEq() throws ParseException
	{
		return new Object[][] { { ATTR_ID, "1", singletonList(1) }, { ATTR_STRING, "string1", asList(0, 1, 2) },
				{ ATTR_BOOL, true, asList(0, 2) }, { ATTR_DATE, getDateFormat().parse("2012-12-21"), asList(0, 1, 2) },
				{ ATTR_DATETIME, getDateTimeFormat().parse("1985-08-12T11:12:13+0500"), asList(0, 1, 2) },
				{ ATTR_DECIMAL, 1.123, singletonList(1) },
				{ ATTR_HTML, "<html>where is my head and where is my body</html>", singletonList(1) },
				{ ATTR_HYPERLINK, "http://www.molgenis.org", asList(0, 1, 2) },
				{ ATTR_LONG, 1000000L, singletonList(1) }, { ATTR_INT, 11, singletonList(1) },
				{ ATTR_SCRIPT, "/bin/blaat/script.sh", asList(0, 1, 2) },
				{ ATTR_EMAIL, "this.is@mail.address", asList(0, 1, 2) },
				// null checks
				{ ATTR_ID, null, emptyList() }, { ATTR_STRING, null, emptyList() }, { ATTR_BOOL, null, emptyList() },
				{ ATTR_CATEGORICAL, null, emptyList() }, { ATTR_CATEGORICAL_MREF, null, emptyList() },
				{ ATTR_DATE, null, emptyList() }, { ATTR_DATETIME, null, emptyList() },
				{ ATTR_DECIMAL, null, emptyList() }, { ATTR_HTML, null, asList(0, 2) },
				{ ATTR_HYPERLINK, null, emptyList() }, { ATTR_LONG, null, emptyList() },
				{ ATTR_INT, 11, singletonList(1) }, { ATTR_SCRIPT, null, emptyList() },
				{ ATTR_EMAIL, null, emptyList() }, { ATTR_XREF, null, emptyList() }, { ATTR_MREF, null, emptyList() } };
	}

	@Test(dataProvider = "findQueryOperatorEq")
	public void testFindQueryOperatorEq(String attrName, Object value, List<Integer> expectedEntityIndices)
	{
		List<Entity> entities = createDynamic(3).collect(toList());
		dataService.add(entityTypeDynamic.getName(), entities.stream());
		waitForIndexToBeStable(entityTypeDynamic.getName(), indexService, LOG);
		Supplier<Stream<Entity>> found = () -> dataService.query(entityTypeDynamic.getName()).eq(attrName, value)
				.findAll();
		List<Entity> foundAsList = found.get().collect(toList());
		assertEquals(foundAsList.size(), expectedEntityIndices.size());
		for (int i = 0; i < expectedEntityIndices.size(); ++i)
		{
			assertTrue(EntityUtils.equals(foundAsList.get(i), entities.get(expectedEntityIndices.get(i))));
		}
	}

	@DataProvider(name = "findQueryOperatorIn")
	private static Object[][] findQueryOperatorIn()
	{
		return new Object[][] { { singletonList("-1"), emptyList() }, { asList("-1", "0"), singletonList(0) },
				{ asList("0", "1"), asList(0, 1) }, { asList("1", "2"), singletonList(1) } };
	}

	@Test(dataProvider = "findQueryOperatorIn")
	public void testFindQueryOperatorIn(List<String> ids, List<Integer> expectedEntityIndices)
	{
		List<Entity> entities = createDynamic(2).collect(toList());
		dataService.add(entityTypeDynamic.getName(), entities.stream());
		waitForIndexToBeStable(entityTypeDynamic.getName(), indexService, LOG);
		Supplier<Stream<Entity>> found = () -> dataService.query(entityTypeDynamic.getName()).in(ATTR_ID, ids)
				.findAll();
		List<Entity> foundAsList = found.get().collect(toList());
		assertEquals(foundAsList.size(), expectedEntityIndices.size());
		for (int i = 0; i < expectedEntityIndices.size(); ++i)
		{
			assertTrue(EntityUtils.equals(foundAsList.get(i), entities.get(expectedEntityIndices.get(i))));
		}
	}

	@DataProvider(name = "findQueryOperatorLess")
	private static Object[][] findQueryOperatorLess()
	{
		return new Object[][] { { 9, emptyList() }, { 10, emptyList() }, { 11, singletonList(0) }, { 12, asList(0, 1) },
				{ 13, asList(0, 1, 2) } };
	}

	@Test(dataProvider = "findQueryOperatorLess")
	public void testFindQueryOperatorLess(int value, List<Integer> expectedEntityIndices)
	{
		List<Entity> entities = createDynamic(5).collect(toList());
		dataService.add(entityTypeDynamic.getName(), entities.stream());
		waitForIndexToBeStable(entityTypeDynamic.getName(), indexService, LOG);
		Supplier<Stream<Entity>> found = () -> dataService.query(entityTypeDynamic.getName()).lt(ATTR_INT, value)
				.findAll();
		List<Entity> foundAsList = found.get().collect(toList());
		assertEquals(foundAsList.size(), expectedEntityIndices.size());
		for (int i = 0; i < expectedEntityIndices.size(); ++i)
		{
			assertTrue(EntityUtils.equals(foundAsList.get(i), entities.get(expectedEntityIndices.get(i))));
		}
	}

	@DataProvider(name = "findQueryOperatorLessEqual")
	private static Object[][] findQueryOperatorLessEqual()
	{
		return new Object[][] { { 9, emptyList() }, { 10, singletonList(0) }, { 11, asList(0, 1) },
				{ 12, asList(0, 1, 2) }, { 13, asList(0, 1, 2, 3) } };
	}

	@Test(dataProvider = "findQueryOperatorLessEqual")
	public void testFindQueryOperatorLessEqual(int value, List<Integer> expectedEntityIndices)
	{
		List<Entity> entities = createDynamic(5).collect(toList());
		dataService.add(entityTypeDynamic.getName(), entities.stream());
		waitForIndexToBeStable(entityTypeDynamic.getName(), indexService, LOG);
		Supplier<Stream<Entity>> found = () -> dataService.query(entityTypeDynamic.getName()).le(ATTR_INT, value)
				.findAll();
		List<Entity> foundAsList = found.get().collect(toList());
		assertEquals(foundAsList.size(), expectedEntityIndices.size());
		for (int i = 0; i < expectedEntityIndices.size(); ++i)
		{
			assertTrue(EntityUtils.equals(foundAsList.get(i), entities.get(expectedEntityIndices.get(i))));
		}
	}

	@DataProvider(name = "findQueryOperatorGreater")
	private static Object[][] findQueryOperatorGreater()
	{
		return new Object[][] { { 9, asList(0, 1, 2) }, { 10, asList(1, 2) }, { 11, singletonList(2) },
				{ 12, emptyList() } };
	}

	@Test(dataProvider = "findQueryOperatorGreater")
	public void testFindQueryOperatorGreater(int value, List<Integer> expectedEntityIndices)
	{
		List<Entity> entities = createDynamic(3).collect(toList());
		dataService.add(entityTypeDynamic.getName(), entities.stream());
		waitForIndexToBeStable(entityTypeDynamic.getName(), indexService, LOG);
		Supplier<Stream<Entity>> found = () -> dataService.query(entityTypeDynamic.getName()).gt(ATTR_INT, value)
				.findAll();
		List<Entity> foundAsList = found.get().collect(toList());
		assertEquals(foundAsList.size(), expectedEntityIndices.size());
		for (int i = 0; i < expectedEntityIndices.size(); ++i)
		{
			assertTrue(EntityUtils.equals(foundAsList.get(i), entities.get(expectedEntityIndices.get(i))));
		}
	}

	@DataProvider(name = "findQueryOperatorGreaterEqual")
	private static Object[][] findQueryOperatorGreaterEqual()
	{
		return new Object[][] { { 9, asList(0, 1, 2) }, { 10, asList(0, 1, 2) }, { 11, asList(1, 2) },
				{ 12, singletonList(2) }, { 13, emptyList() } };
	}

	@Test(dataProvider = "findQueryOperatorGreaterEqual")
	public void testFindQueryOperatorGreaterEqual(int value, List<Integer> expectedEntityIndices)
	{
		List<Entity> entities = createDynamic(3).collect(toList());
		dataService.add(entityTypeDynamic.getName(), entities.stream());
		waitForIndexToBeStable(entityTypeDynamic.getName(), indexService, LOG);
		Supplier<Stream<Entity>> found = () -> dataService.query(entityTypeDynamic.getName()).ge(ATTR_INT, value)
				.findAll();
		List<Entity> foundAsList = found.get().collect(toList());
		assertEquals(foundAsList.size(), expectedEntityIndices.size());
		for (int i = 0; i < expectedEntityIndices.size(); ++i)
		{
			assertTrue(EntityUtils.equals(foundAsList.get(i), entities.get(expectedEntityIndices.get(i))));
		}
	}

	@DataProvider(name = "findQueryOperatorRange")
	private static Object[][] findQueryOperatorRange()
	{
		return new Object[][] { { 0, 9, emptyList() }, { 0, 10, asList(0) }, { 10, 10, asList(0) },
				{ 10, 11, asList(0, 1) }, { 10, 12, asList(0, 1, 2) }, { 12, 20, asList(2) } };
	}

	@Test(dataProvider = "findQueryOperatorRange")
	public void testFindQueryOperatorRange(int low, int high, List<Integer> expectedEntityIndices)
	{
		List<Entity> entities = createDynamic(3).collect(toList());
		dataService.add(entityTypeDynamic.getName(), entities.stream());
		waitForIndexToBeStable(entityTypeDynamic.getName(), indexService, LOG);
		Supplier<Stream<Entity>> found = () -> dataService.query(entityTypeDynamic.getName())
				.rng(ATTR_INT, low, high).findAll();
		List<Entity> foundAsList = found.get().collect(toList());
		assertEquals(foundAsList.size(), expectedEntityIndices.size());
		for (int i = 0; i < expectedEntityIndices.size(); ++i)
		{
			assertTrue(EntityUtils.equals(foundAsList.get(i), entities.get(expectedEntityIndices.get(i))));
		}
	}

	@DataProvider(name = "findQueryOperatorLike")
	private static Object[][] findQueryOperatorLike()
	{
		return new Object[][] { { "ring", asList(0, 1) }, { "Ring", emptyList() }, { "nomatch", emptyList() } };
	}

	@Test(dataProvider = "findQueryOperatorLike")
	public void testFindQueryOperatorLike(String likeStr, List<Integer> expectedEntityIndices)
	{
		List<Entity> entities = createDynamic(2).collect(toList());
		dataService.add(entityTypeDynamic.getName(), entities.stream());
		waitForIndexToBeStable(entityTypeDynamic.getName(), indexService, LOG);
		Supplier<Stream<Entity>> found = () -> dataService.query(entityTypeDynamic.getName())
				.like(ATTR_STRING, likeStr).findAll();
		List<Entity> foundAsList = found.get().collect(toList());
		assertEquals(foundAsList.size(), expectedEntityIndices.size());
		for (int i = 0; i < expectedEntityIndices.size(); ++i)
		{
			assertTrue(EntityUtils.equals(foundAsList.get(i), entities.get(expectedEntityIndices.get(i))));
		}
	}

	@DataProvider(name = "findQueryOperatorNot")
	private static Object[][] findQueryOperatorNot()
	{
		return new Object[][] { { 9, asList(0, 1, 2) }, { 10, asList(1, 2) }, { 11, asList(0, 2) },
				{ 12, asList(0, 1) }, { 13, asList(0, 1, 2) } };
	}

	@Test(dataProvider = "findQueryOperatorNot")
	public void testFindQueryOperatorNot(int value, List<Integer> expectedEntityIndices)
	{
		List<Entity> entities = createDynamic(3).collect(toList());
		dataService.add(entityTypeDynamic.getName(), entities.stream());
		waitForIndexToBeStable(entityTypeDynamic.getName(), indexService, LOG);
		Supplier<Stream<Entity>> found = () -> dataService.query(entityTypeDynamic.getName()).not()
				.eq(ATTR_INT, value).findAll();
		List<Entity> foundAsList = found.get().collect(toList());
		assertEquals(foundAsList.size(), expectedEntityIndices.size());
		for (int i = 0; i < expectedEntityIndices.size(); ++i)
		{
			assertTrue(EntityUtils.equals(foundAsList.get(i), entities.get(expectedEntityIndices.get(i))));
		}
	}

	/**
	 * Test used as a caching benchmark
	 */
	@Test(enabled = false)
	public void cachePerformanceTest()
	{
		List<Entity> entities = createDynamic(10000).collect(toList());
		dataService.add(entityTypeDynamic.getName(), entities.stream());
		waitForIndexToBeStable(entityTypeDynamic.getName(), indexService, LOG);

		Query q1 = new QueryImpl<>().eq(EntityTestHarness.ATTR_STRING, "string1");
		q1.pageSize(1000);

		Query q2 = new QueryImpl<>().eq(EntityTestHarness.ATTR_BOOL, true);
		q2.pageSize(500);

		Query q3 = new QueryImpl<>().eq(ATTR_DECIMAL, 1.123);

		runAsSystem(() ->
		{
			for (int i = 0; i < 100000; i++)
			{
				dataService.findAll(entityTypeDynamic.getName(), q1);
				dataService.findAll(entityTypeDynamic.getName(), q2);
				dataService.findOne(entityTypeDynamic.getName(), q3);
			}
		});
	}

	@DataProvider(name = "findQueryOperatorAnd")
	private static Object[][] findQueryOperatorAnd()
	{
		return new Object[][] { { "string1", 10, asList(0) }, { "unknownString", 10, emptyList() },
				{ "string1", -1, emptyList() }, { "unknownString", -1, emptyList() } };
	}

	@Test(dataProvider = "findQueryOperatorAnd")
	public void testFindQueryOperatorAnd(String strValue, int value, List<Integer> expectedEntityIndices)
	{
		List<Entity> entities = createDynamic(3).collect(toList());
		dataService.add(entityTypeDynamic.getName(), entities.stream());
		waitForIndexToBeStable(entityTypeDynamic.getName(), indexService, LOG);
		Supplier<Stream<Entity>> found = () -> dataService.query(entityTypeDynamic.getName())
				.eq(ATTR_STRING, strValue).and().eq(ATTR_INT, value).findAll();
		List<Entity> foundAsList = found.get().collect(toList());
		assertEquals(foundAsList.size(), expectedEntityIndices.size());
		for (int i = 0; i < expectedEntityIndices.size(); ++i)
		{
			assertTrue(EntityUtils.equals(foundAsList.get(i), entities.get(expectedEntityIndices.get(i))));
		}
	}

	@DataProvider(name = "findQueryOperatorOr")
	private static Object[][] findQueryOperatorOr()
	{
		return new Object[][] { { "string1", 10, asList(0, 1, 2) }, { "unknownString", 10, asList(0) },
				{ "string1", -1, asList(0, 1, 2) }, { "unknownString", -1, emptyList() } };
	}

	@Test(dataProvider = "findQueryOperatorOr")
	public void testFindQueryOperatorOr(String strValue, int value, List<Integer> expectedEntityIndices)
	{
		List<Entity> entities = createDynamic(3).collect(toList());
		dataService.add(entityTypeDynamic.getName(), entities.stream());
		waitForIndexToBeStable(entityTypeDynamic.getName(), indexService, LOG);
		Supplier<Stream<Entity>> found = () -> dataService.query(entityTypeDynamic.getName())
				.eq(ATTR_STRING, strValue).or().eq(ATTR_INT, value).findAll();
		List<Entity> foundAsList = found.get().collect(toList());
		assertEquals(foundAsList.size(), expectedEntityIndices.size());
		for (int i = 0; i < expectedEntityIndices.size(); ++i)
		{
			assertTrue(EntityUtils.equals(foundAsList.get(i), entities.get(expectedEntityIndices.get(i))));
		}
	}

	@DataProvider(name = "findQueryOperatorNested")
	private static Object[][] findQueryOperatorNested()
	{
		return new Object[][] { { true, "string1", 10, asList(0, 2) }, { true, "unknownString", 10, asList(0) },
				{ true, "string1", -1, asList(0, 2) }, { true, "unknownString", -1, emptyList() },
				{ false, "string1", 10, singletonList(1) }, { false, "unknownString", 10, emptyList() },
				{ false, "string1", -1, asList(1) }, { false, "unknownString", -1, emptyList() } };
	}

	@Test(dataProvider = "findQueryOperatorNested")
	public void testFindQueryOperatorNested(boolean boolValue, String strValue, int value,
			List<Integer> expectedEntityIndices)
	{
		List<Entity> entities = createDynamic(3).collect(toList());
		dataService.add(entityTypeDynamic.getName(), entities.stream());
		waitForIndexToBeStable(entityTypeDynamic.getName(), indexService, LOG);
		Supplier<Stream<Entity>> found = () -> dataService.query(entityTypeDynamic.getName())
				.eq(ATTR_BOOL, boolValue).and().nest().eq(ATTR_STRING, strValue).or().eq(ATTR_INT, value).unnest()
				.findAll();
		List<Entity> foundAsList = found.get().collect(toList());
		assertEquals(foundAsList.size(), expectedEntityIndices.size());
		for (int i = 0; i < expectedEntityIndices.size(); ++i)
		{
			assertTrue(EntityUtils.equals(foundAsList.get(i), entities.get(expectedEntityIndices.get(i))));
		}
	}

	@DataProvider(name = "findQueryOperatorSearch")
	private static Object[][] findQueryOperatorSearch()
	{
		return new Object[][] { { "body", asList(1) }, { "head", asList(1) }, { "unknownString", emptyList() } };
	}

	@Test(dataProvider = "findQueryOperatorSearch")
	public void testFindQueryOperatorSearch(String searchStr, List<Integer> expectedEntityIndices)
	{
		List<Entity> entities = createDynamic(2).collect(toList());
		dataService.add(entityTypeDynamic.getName(), entities.stream());
		waitForIndexToBeStable(entityTypeDynamic.getName(), indexService, LOG);
		Supplier<Stream<Entity>> found = () -> dataService.query(entityTypeDynamic.getName())
				.search(ATTR_HTML, searchStr).findAll();
		List<Entity> foundAsList = found.get().collect(toList());
		assertEquals(foundAsList.size(), expectedEntityIndices.size());
		for (int i = 0; i < expectedEntityIndices.size(); ++i)
		{
			assertTrue(EntityUtils.equals(foundAsList.get(i), entities.get(expectedEntityIndices.get(i))));
		}
	}

	@Test
	public void testFindQueryLimit2_Offset2_sortOnInt()
	{
		List<Entity> testRefEntities = testHarness.createTestRefEntities(refEntityTypeDynamic, 6);
		List<Entity> testEntities = testHarness.createTestEntities(entityTypeDynamic, 10, testRefEntities)
				.collect(toList());
		runAsSystem(() ->
		{
			dataService.add(refEntityTypeDynamic.getName(), testRefEntities.stream());
			dataService.add(entityTypeDynamic.getName(), testEntities.stream());
		});
		waitForIndexToBeStable(refEntityTypeDynamic.getName(), indexService, LOG);
		waitForIndexToBeStable(entityTypeDynamic.getName(), indexService, LOG);
		Supplier<Stream<Entity>> found = () -> dataService.findAll(entityTypeDynamic.getName(),
				new QueryImpl<>().pageSize(2).offset(2).sort(new Sort(ATTR_ID, Sort.Direction.DESC)));
		List<Entity> foundAsList = found.get().collect(toList());
		assertEquals(foundAsList.size(), 2);
		assertTrue(EntityUtils.equals(foundAsList.get(0), testEntities.get(7)));
		assertTrue(EntityUtils.equals(foundAsList.get(1), testEntities.get(6)));
	}

	@Test
	public void testFindQueryTypedStatic()
	{
		List<Entity> entities = createStatic(5).collect(toList());
		dataService.add(entityTypeStatic.getName(), entities.stream());
		waitForIndexToBeStable(entityTypeStatic.getName(), indexService, LOG);
		Supplier<Stream<TestEntityStatic>> found = () -> dataService.findAll(entityTypeStatic.getName(),
				new QueryImpl<TestEntityStatic>().eq(ATTR_ID, entities.get(0).getIdValue()), TestEntityStatic.class);
		assertEquals(found.get().count(), 1);
		assertEquals(found.get().findFirst().get().getId(), entities.get(0).getIdValue());
	}

	@Test
	public void testFindOne()
	{
		Entity entity = createDynamic(1).findFirst().get();
		dataService.add(entityTypeDynamic.getName(), Stream.of(entity));
		waitForIndexToBeStable(entityTypeDynamic.getName(), indexService, LOG);
		assertNotNull(dataService.findOneById(entityTypeDynamic.getName(), entity.getIdValue()));
	}

	@Test
	public void testFindOneTypedStatic()
	{
		Entity entity = createStatic(1).findFirst().get();
		dataService.add(entityTypeStatic.getName(), Stream.of(entity));
		waitForIndexToBeStable(entityTypeStatic.getName(), indexService, LOG);
		TestEntityStatic testEntityStatic = dataService
				.findOneById(entityTypeStatic.getName(), entity.getIdValue(), TestEntityStatic.class);
		assertNotNull(testEntityStatic);
		assertEquals(testEntityStatic.getId(), entity.getIdValue());
	}

	@Test
	public void testFindOneFetch()
	{
		Entity entity = createDynamic(1).findFirst().get();
		dataService.add(entityTypeDynamic.getName(), Stream.of(entity));
		waitForIndexToBeStable(entityTypeDynamic.getName(), indexService, LOG);
		assertNotNull(dataService
				.findOneById(entityTypeDynamic.getName(), entity.getIdValue(), new Fetch().field(ATTR_ID)));
	}

	@Test
	public void testFindOneFetchTypedStatic()
	{
		TestEntityStatic entity = new TestEntityStatic(entityTypeStatic);
		entity.set(ATTR_ID, "1");
		entity.set(ATTR_STRING, "string1");
		entity.set(ATTR_BOOL, true);

		dataService.add(entityTypeStatic.getName(), Stream.of(entity));
		waitForIndexToBeStable(entityTypeStatic.getName(), indexService, LOG);
		TestEntityStatic testEntityStatic = dataService
				.findOneById(entityTypeStatic.getName(), entity.getIdValue(), new Fetch().field(ATTR_ID),
						TestEntityStatic.class);
		assertNotNull(testEntityStatic);
		assertEquals(testEntityStatic.getIdValue(), entity.getIdValue());
	}

	@Test
	public void testFindOneQuery()
	{
		Entity entity = createDynamic(1).findFirst().get();
		dataService.add(entityTypeDynamic.getName(), entity);
		waitForIndexToBeStable(entityTypeDynamic.getName(), indexService, LOG);
		entity = dataService
				.findOne(entityTypeDynamic.getName(), new QueryImpl<>().eq(ATTR_ID, entity.getIdValue()));
		assertNotNull(entity);
	}

	@Test
	public void testFindOneQueryTypedStatic()
	{
		Entity entity = createStatic(1).findFirst().get();
		dataService.add(entityTypeStatic.getName(), entity);
		waitForIndexToBeStable(entityTypeStatic.getName(), indexService, LOG);
		TestEntityStatic testEntityStatic = dataService.findOne(entityTypeStatic.getName(),
				new QueryImpl<TestEntityStatic>().eq(ATTR_ID, entity.getIdValue()), TestEntityStatic.class);
		assertNotNull(testEntityStatic);
		assertEquals(testEntityStatic.getId(), entity.getIdValue());
	}

	@Test
	public void testGetCapabilities()
	{
		Set<RepositoryCapability> capabilities = dataService.getCapabilities(entityTypeDynamic.getName());
		assertNotNull(capabilities);
		assertTrue(capabilities.containsAll(asList(MANAGABLE, QUERYABLE, WRITABLE, VALIDATE_REFERENCE_CONSTRAINT)));
	}

	@Test
	public void testGetEntityType()
	{
		EntityType entityType = dataService.getEntityType(entityTypeDynamic.getName());
		assertNotNull(entityType);
		assertTrue(EntityUtils.equals(entityType, entityTypeDynamic));
	}

	@Test
	public void testGetEntityNames()
	{
		Stream<String> names = dataService.getEntityNames();
		assertNotNull(names);
		assertTrue(names.filter(entityTypeDynamic.getName()::equals).findFirst().isPresent());
	}

	@Test
	public void testGetMeta()
	{
		assertNotNull(dataService.getMeta());
	}

	@Test()
	public void testGetKnownRepository()
	{
		Repository<Entity> repo = dataService.getRepository(entityTypeDynamic.getName());
		assertNotNull(repo);
		assertEquals(repo.getName(), entityTypeDynamic.getName());
	}

	@Test(expectedExceptions = UnknownEntityException.class)
	public void testGetUnknownRepository()
	{
		dataService.getRepository("bogus");
	}

	@Test
	public void testHasRepository()
	{
		assertTrue(dataService.hasRepository(entityTypeDynamic.getName()));
		assertFalse(dataService.hasRepository("bogus"));
	}

	@Test
	public void testIterator()
	{
		assertNotNull(dataService.iterator());
		StreamSupport.stream(dataService.spliterator(), false).forEach(repo -> LOG.info(repo.getName()));
		Repository repo = dataService.getRepository(entityTypeDynamic.getName());

		/*
			Repository equals is not implemented. The repository from dataService
			and from the dataService.getRepository are not the same instances.
		*/
		assertTrue(StreamSupport.stream(dataService.spliterator(), false)
				.anyMatch(e -> repo.getName().equals(e.getName())));
	}

	@Test
	public void testQuery()
	{
		assertNotNull(dataService.query(entityTypeDynamic.getName()));
		try
		{
			dataService.query("bogus");
			fail("Should have thrown UnknownEntityException");
		}
		catch (UnknownEntityException e)
		{
			// Expected
		}
	}

	@Test
	public void testUpdate()
	{
		Entity entity = createDynamic(1).findFirst().get();
		dataService.add(entityTypeDynamic.getName(), entity);
		waitForIndexToBeStable(entityTypeDynamic.getName(), indexService, LOG);

		entity = dataService.findOneById(entityTypeDynamic.getName(), entity.getIdValue());
		assertNotNull(entity);
		assertEquals(entity.get(ATTR_STRING), "string1");

		Query<Entity> q = new QueryImpl<>();
		q.eq(ATTR_STRING, "qwerty");
		entity.set(ATTR_STRING, "qwerty");

		assertEquals(searchService.count(q, entityTypeDynamic), 0);
		dataService.update(entityTypeDynamic.getName(), entity);
		waitForIndexToBeStable(entityTypeDynamic.getName(), indexService, LOG);
		assertEquals(searchService.count(q, entityTypeDynamic), 1);

		assertPresent(entityTypeDynamic, entity);

		entity = dataService.findOneById(entityTypeDynamic.getName(), entity.getIdValue());
		assertNotNull(entity.get(ATTR_STRING));
		assertEquals(entity.get(ATTR_STRING), "qwerty");
	}

	@Test
	public void testUpdateSingleRefEntityIndexesReferencingEntities()
	{
		dataService.add(entityTypeDynamic.getName(), createDynamic(30));
		waitForIndexToBeStable(entityTypeDynamic.getName(), indexService, LOG);

		Entity refEntity4 = dataService.findOneById(refEntityTypeDynamic.getName(), "4");

		Query<Entity> q = new QueryImpl<>().search("refstring4");

		assertEquals(searchService.count(q, entityTypeDynamic), 5);
		refEntity4.set(ATTR_REF_STRING, "qwerty");
		runAsSystem(() -> dataService.update(refEntityTypeDynamic.getName(), refEntity4));
		waitForIndexToBeStable(entityTypeDynamic.getName(), indexService, LOG);
		assertEquals(searchService.count(q, entityTypeDynamic), 0);
		assertEquals(searchService.count(new QueryImpl<>().search("qwerty"), entityTypeDynamic), 5);
	}

	@Test(enabled = false) //FIXME: sys_md_attributes spam
	public void testUpdateSingleRefEntityIndexesLargeAmountOfReferencingEntities()
	{
		dataService.add(entityTypeDynamic.getName(), createDynamic(10000));
		waitForIndexToBeStable(entityTypeDynamic.getName(), indexService, LOG);

		Query<Entity> q = new QueryImpl<>().search("refstring4").or().search("refstring5");

		assertEquals(searchService.count(q, entityTypeDynamic), 3333);
		Entity refEntity4 = dataService.findOneById(refEntityTypeDynamic.getName(), "4");
		refEntity4.set(ATTR_REF_STRING, "qwerty");
		runAsSystem(() -> dataService.update(refEntityTypeDynamic.getName(), refEntity4));

		Entity refEntity5 = dataService.findOneById(refEntityTypeDynamic.getName(), "5");
		refEntity5.set(ATTR_REF_STRING, "qwerty");
		runAsSystem(() -> dataService.update(refEntityTypeDynamic.getName(), refEntity5));

		waitForIndexToBeStable(entityTypeDynamic.getName(), indexService, LOG);
		assertEquals(searchService.count(q, entityTypeDynamic), 0);

		assertEquals(searchService.count(new QueryImpl<>().search("qwerty"), entityTypeDynamic), 3333);
	}

	@Test
	public void testUpdateStream()
	{
		Entity entity = createDynamic(1).findFirst().get();

		dataService.add(entityTypeDynamic.getName(), entity);
		waitForIndexToBeStable(entityTypeDynamic.getName(), indexService, LOG);
		assertPresent(entityTypeDynamic, entity);

		entity = dataService.findOneById(entityTypeDynamic.getName(), entity.getIdValue());
		assertNotNull(entity);
		assertEquals(entity.get(ATTR_STRING), "string1");

		entity.set(ATTR_STRING, "qwerty");
		Query<Entity> q = new QueryImpl<>();
		q.eq(ATTR_STRING, "qwerty");

		assertEquals(searchService.count(q, entityTypeDynamic), 0);

		dataService.update(entityTypeDynamic.getName(), of(entity));
		waitForIndexToBeStable(entityTypeDynamic.getName(), indexService, LOG);

		assertEquals(searchService.count(q, entityTypeDynamic), 1);

		assertPresent(entityTypeDynamic, entity);
		entity = dataService.findOneById(entityTypeDynamic.getName(), entity.getIdValue());
		assertNotNull(entity.get(ATTR_STRING));
		assertEquals(entity.get(ATTR_STRING), "qwerty");
	}

	private Stream<Entity> createDynamic(int count)
	{
		List<Entity> refEntities = testHarness.createTestRefEntities(refEntityTypeDynamic, 6);
		runAsSystem(() -> dataService.add(refEntityTypeDynamic.getName(), refEntities.stream()));
		return testHarness.createTestEntities(entityTypeDynamic, count, refEntities);
	}

	private Stream<Entity> createStatic(int count)
	{
		List<Entity> refEntities = testHarness.createTestRefEntities(refEntityTypeStatic, 6);
		runAsSystem(() -> dataService.add(refEntityTypeStatic.getName(), refEntities.stream()));
		return testHarness.createTestEntities(entityTypeStatic, count, refEntities);
	}

	private void assertPresent(EntityType emd, List<Entity> entities)
	{
		entities.forEach(e -> assertPresent(emd, e));
	}

	private void assertPresent(EntityType emd, Entity entity)
	{
		// Found in PostgreSQL
		assertNotNull(dataService.findOneById(emd.getName(), entity.getIdValue()));

		// Found in index Elasticsearch
		Query<Entity> q = new QueryImpl<>();
		q.eq(emd.getIdAttribute().getName(), entity.getIdValue());
		assertEquals(searchService.count(q, emd), 1);
	}

	private void assertNotPresent(Entity entity)
	{
		// Found in PostgreSQL
		assertNull(dataService.findOneById(entityTypeDynamic.getName(), entity.getIdValue()));

		// Not found in index Elasticsearch
		Query<Entity> q = new QueryImpl<>();
		q.eq(entityTypeDynamic.getIdAttribute().getName(), entity.getIdValue());
		assertEquals(searchService.count(q, entityTypeDynamic), 0);
	}

	@Test
	public void testCreateSelfXref()
	{
		Entity entitySelfXref = entitySelfXrefTestHarness.createTestEntities(selfXrefEntityType, 1)
				.collect(toList()).get(0);

		//Create
		dataService.add(selfXrefEntityType.getName(), entitySelfXref);
		waitForIndexToBeStable(selfXrefEntityType.getName(), indexService, LOG);
		Entity entity = dataService.findOneById(selfXrefEntityType.getName(), entitySelfXref.getIdValue());
		assertPresent(selfXrefEntityType, entity);

		Query<Entity> q1 = new QueryImpl<>();
		q1.eq(ATTR_STRING, "attr_string_old");
		Query<Entity> q2 = new QueryImpl<>();
		q2.eq(ATTR_STRING, "attr_string_new");
		entity.set(ATTR_STRING, "attr_string_new");

		// Verify value in elasticsearch before update
		assertEquals(searchService.count(q1, selfXrefEntityType), 1);
		assertEquals(searchService.count(q2, selfXrefEntityType), 0);

		// Update
		dataService.update(selfXrefEntityType.getName(), entity);
		waitForIndexToBeStable(selfXrefEntityType.getName(), indexService, LOG);
		assertPresent(selfXrefEntityType, entity);

		// Verify value in elasticsearch after update
		assertEquals(searchService.count(q2, selfXrefEntityType), 1);
		assertEquals(searchService.count(q1, selfXrefEntityType), 0);

		// Verify value in PostgreSQL after update
		entity = dataService.findOneById(selfXrefEntityType.getName(), entity.getIdValue());
		assertNotNull(entity.get(ATTR_STRING));
		assertEquals(entity.get(ATTR_STRING), "attr_string_new");

		// Check id are equals
		assertEquals(entity.getEntity(ATTR_XREF).getIdValue(), entity.getIdValue());
	}

	@Test
	public void testIndexCreateMetaData()
	{
		IndexMetadataCUDOperationsPlatformIT
				.testIndexCreateMetaData(searchService, entityTypeStatic, entityTypeDynamic, metaDataService);
	}

	@Test
	public void testIndexDeleteMetaData()
	{
		IndexMetadataCUDOperationsPlatformIT
				.testIndexDeleteMetaData(searchService, dataService, entityTypeDynamic, metaDataService,
						indexService);
	}

	@Test
	public void testIndexUpdateMetaDataUpdateAttribute()
	{
		IndexMetadataCUDOperationsPlatformIT
				.testIndexUpdateMetaDataUpdateAttribute(searchService, entityTypeDynamic, metaDataService,
						indexService);
	}

	@Test
	public void testIndexUpdateMetaDataRemoveAttribute()
	{
		IndexMetadataCUDOperationsPlatformIT
				.testIndexUpdateMetaDataRemoveAttribute(entityTypeDynamic, EntityTestHarness.ATTR_CATEGORICAL,
						searchService, metaDataService, indexService);

		IndexMetadataCUDOperationsPlatformIT
				.testIndexUpdateMetaDataRemoveAttribute(entityTypeDynamic, EntityTestHarness.ATTR_BOOL,
						searchService, metaDataService, indexService);

		IndexMetadataCUDOperationsPlatformIT
				.testIndexUpdateMetaDataRemoveAttribute(entityTypeDynamic, EntityTestHarness.ATTR_DATE,
						searchService, metaDataService, indexService);

		IndexMetadataCUDOperationsPlatformIT
				.testIndexUpdateMetaDataRemoveAttribute(entityTypeDynamic, EntityTestHarness.ATTR_XREF,
						searchService, metaDataService, indexService);

		IndexMetadataCUDOperationsPlatformIT
				.testIndexUpdateMetaDataRemoveAttribute(entityTypeDynamic, EntityTestHarness.ATTR_DATETIME,
						searchService, metaDataService, indexService);

		IndexMetadataCUDOperationsPlatformIT
				.testIndexUpdateMetaDataRemoveAttribute(entityTypeDynamic, EntityTestHarness.ATTR_DECIMAL,
						searchService, metaDataService, indexService);

		IndexMetadataCUDOperationsPlatformIT
				.testIndexUpdateMetaDataRemoveAttribute(entityTypeDynamic, EntityTestHarness.ATTR_EMAIL,
						searchService, metaDataService, indexService);

		IndexMetadataCUDOperationsPlatformIT
				.testIndexUpdateMetaDataRemoveAttribute(entityTypeDynamic, EntityTestHarness.ATTR_HTML,
						searchService, metaDataService, indexService);

		IndexMetadataCUDOperationsPlatformIT
				.testIndexUpdateMetaDataRemoveAttribute(entityTypeDynamic, EntityTestHarness.ATTR_INT,
						searchService, metaDataService, indexService);

		IndexMetadataCUDOperationsPlatformIT
				.testIndexUpdateMetaDataRemoveAttribute(entityTypeDynamic, EntityTestHarness.ATTR_HYPERLINK,
						searchService, metaDataService, indexService);
	}

	// Derived from fix: https://github.com/molgenis/molgenis/issues/5227
	@Test
	public void testIndexBatchUpdate()
	{
		List<Entity> refEntities = testHarness.createTestRefEntities(refEntityTypeDynamic, 2);
		List<Entity> entities = testHarness.createTestEntities(entityTypeDynamic, 2, refEntities).collect(toList());
		runAsSystem(() ->
		{
			dataService.add(refEntityTypeDynamic.getName(), refEntities.stream());
			dataService.add(entityTypeDynamic.getName(), entities.stream());
			waitForIndexToBeStable(entityTypeDynamic.getName(), indexService, LOG);
		});

		// test string1 from entity
		Query<Entity> q0 = new QueryImpl<>();
		q0.search("string1");
		Stream<Entity> result0 = searchService.searchAsStream(q0, entityTypeDynamic);
		assertEquals(result0.count(), 2);

		// test refstring1 from ref entity
		Query<Entity> q1 = new QueryImpl<>();
		q1.search("refstring0");
		Stream<Entity> result1 = searchService.searchAsStream(q1, entityTypeDynamic);
		assertEquals(result1.count(), 1);

		// test refstring1 from ref entity
		Query<Entity> q2 = new QueryImpl<>();
		q2.search("refstring1");
		Stream<Entity> result2 = searchService.searchAsStream(q2, entityTypeDynamic);
		assertEquals(result2.count(), 1);

		refEntities.get(0).set(ATTR_REF_STRING, "searchTestBatchUpdate");
		runAsSystem(() ->
		{
			dataService.update(refEntityTypeDynamic.getName(), refEntities.stream());
			waitForIndexToBeStable(entityTypeDynamic.getName(), indexService, LOG);
		});

		// test string1 from entity
		Stream<Entity> result3 = searchService.searchAsStream(q0, entityTypeDynamic);
		assertEquals(result3.count(), 2);

		// test refstring1 from ref entity
		Query<Entity> q4 = new QueryImpl<>();
		q4.search("refstring0");
		Stream<Entity> result4 = searchService.searchAsStream(q4, entityTypeDynamic);
		assertEquals(result4.count(), 0);

		// test refstring1 from ref entity
		Query<Entity> q5 = new QueryImpl<>();
		q5.search("refstring1");
		Stream<Entity> result5 = searchService.searchAsStream(q5, entityTypeDynamic);
		assertEquals(result5.count(), 1);

		// test refstring1 from ref entity
		Query<Entity> q6 = new QueryImpl<>();
		q6.search("searchTestBatchUpdate");
		Stream<Entity> result6 = searchService.searchAsStream(q6, entityTypeDynamic);
		assertEquals(result6.count(), 1);
	}
}

