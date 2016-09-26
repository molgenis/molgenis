package org.molgenis.integrationtest.platform;

import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.molgenis.data.*;
import org.molgenis.data.cache.l2.L2Cache;
import org.molgenis.data.cache.l3.L3Cache;
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
import org.molgenis.data.meta.model.*;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.test.data.EntitySelfXrefTestHarness;
import org.molgenis.test.data.EntityTestHarness;
import org.molgenis.test.data.OneToManyTestHarness;
import org.molgenis.test.data.staticentity.TestEntityStatic;
import org.molgenis.test.data.staticentity.bidirectional.test1.AuthorMetaData1;
import org.molgenis.test.data.staticentity.bidirectional.test1.BookMetaData1;
import org.molgenis.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.*;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;
import static org.molgenis.data.RepositoryCapability.*;
import static org.molgenis.data.i18n.model.I18nStringMetaData.I18N_STRING;
import static org.molgenis.data.i18n.model.LanguageMetaData.LANGUAGE;
import static org.molgenis.data.meta.model.AttributeMetaDataMetaData.ATTRIBUTE_META_DATA;
import static org.molgenis.data.meta.model.EntityMetaData.AttributeCopyMode.DEEP_COPY_ATTRS;
import static org.molgenis.data.meta.model.EntityMetaDataMetaData.ENTITY_META_DATA;
import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;
import static org.molgenis.test.data.EntityTestHarness.*;
import static org.molgenis.test.data.OneToManyTestHarness.ONE_TO_MANY_CASES;
import static org.molgenis.util.MolgenisDateFormat.getDateFormat;
import static org.molgenis.util.MolgenisDateFormat.getDateTimeFormat;
import static org.testng.Assert.*;

@ContextConfiguration(classes = { PlatformITConfig.class })
public class PlatformIT extends AbstractTestNGSpringContextTests
{
	private final Logger LOG = LoggerFactory.getLogger(PlatformIT.class);

	private EntityMetaData entityMetaDataStatic;
	private EntityMetaData refEntityMetaDataStatic;
	private EntityMetaData entityMetaDataDynamic;
	private EntityMetaData refEntityMetaDataDynamic;
	private EntityMetaData selfXrefEntityMetaData;

	@Autowired
	private IndexService indexService;
	@Autowired
	private EntityTestHarness testHarness;
	@Autowired
	private EntitySelfXrefTestHarness entitySelfXrefTestHarness;
	@Autowired
	private OneToManyTestHarness oneToManyTestHarness;
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
	private LanguageService languageService;
	@Autowired
	private I18nStringMetaData i18nStringMetaData;
	@Autowired
	private LanguageMetaData languageMetaData;
	@Autowired
	private EntityMetaDataMetaData entityMetaDataMetaData;
	@Autowired
	private AttributeMetaDataMetaData attributeMetaDataMetaData;
	@Autowired
	private LanguageFactory languageFactory;
	@Autowired
	private EntityMetaDataFactory entityMetaDataFactory;
	@Autowired
	private AttributeMetaDataFactory attributeMetaDataFactory;
	@Autowired
	private EntityManager entityManager;
	@Autowired
	private L3Cache l3Cache;

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
		refEntityMetaDataStatic = testHarness.createStaticRefTestEntityMetaData();
		entityMetaDataStatic = testHarness.createStaticTestEntityMetaData();
		refEntityMetaDataDynamic = testHarness.createDynamicRefEntityMetaData();
		entityMetaDataDynamic = testHarness.createDynamicTestEntityMetaData();

		// Create a self refer entity
		selfXrefEntityMetaData = entitySelfXrefTestHarness.createDynamicEntityMetaData();
		selfXrefEntityMetaData.getAttribute(ATTR_XREF).setRefEntity(selfXrefEntityMetaData);

		runAsSystem(() ->
		{
			metaDataService.addEntityMeta(refEntityMetaDataDynamic);
			metaDataService.addEntityMeta(entityMetaDataDynamic);
			metaDataService.addEntityMeta(selfXrefEntityMetaData);
		});
		setAuthentication();
		waitForWorkToBeFinished(indexService, LOG);
	}

	private List<GrantedAuthority> makeAuthorities(String entityName, boolean write, boolean read, boolean count)
	{
		List<GrantedAuthority> authorities = newArrayList();
		if (write) authorities.add(new SimpleGrantedAuthority("ROLE_ENTITY_WRITE_" + entityName.toUpperCase()));
		if (read) authorities.add(new SimpleGrantedAuthority("ROLE_ENTITY_READ_" + entityName.toUpperCase()));
		if (count) authorities.add(new SimpleGrantedAuthority("ROLE_ENTITY_COUNT_" + entityName.toUpperCase()));
		return authorities;
	}

	private void setAuthentication()
	{
		List<GrantedAuthority> authorities = newArrayList();

		authorities.add(new SimpleGrantedAuthority("ROLE_ENTITY_READ_SYS_MD_ENTITIES"));
		authorities.add(new SimpleGrantedAuthority("ROLE_ENTITY_READ_SYS_MD_ATTRIBUTES"));
		authorities.add(new SimpleGrantedAuthority("ROLE_ENTITY_READ_SYS_MD_PACKAGES"));
		authorities.addAll(makeAuthorities(refEntityMetaDataStatic.getName(), true, true, true));
		authorities.addAll(makeAuthorities(entityMetaDataStatic.getName(), true, true, true));
		authorities.addAll(makeAuthorities(entityMetaDataDynamic.getName(), true, true, true));
		authorities.addAll(makeAuthorities(refEntityMetaDataDynamic.getName(), false, true, true));
		authorities.addAll(makeAuthorities(selfXrefEntityMetaData.getName(), true, true, true));
		authorities.addAll(makeAuthorities(languageMetaData.getName(), true, true, true));
		authorities.addAll(makeAuthorities(attributeMetaDataMetaData.getName(), true, true, true));
		authorities.addAll(makeAuthorities(i18nStringMetaData.getName(), true, false, false));
		authorities.addAll(makeAuthorities(entityMetaDataMetaData.getName(), true, true, true));

		for (int i = 1; i <= ONE_TO_MANY_CASES; i++)
		{
			authorities.addAll(makeAuthorities("sys_Author" + i, true, true, true));
			authorities.addAll(makeAuthorities("sys_Book" + i, true, true, true));
		}

		SecurityContextHolder.getContext()
				.setAuthentication(new TestingAuthenticationToken("user", "user", authorities));
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
		for (AttributeMetaData attr : entityMetaDataMetaData.getAttributes())
		{
			if (I18nUtils.isI18n(attr.getName()))
			{
				languageAttrs.add(attr);
			}
		}
		languageAttrs.forEach(entityMetaDataMetaData::removeAttribute);
	}

	@AfterMethod
	public void afterMethod()
	{
		runAsSystem(() ->
		{
			dataService.deleteAll(entityMetaDataStatic.getName());
			dataService.deleteAll(refEntityMetaDataStatic.getName());
			dataService.deleteAll(entityMetaDataDynamic.getName());
			dataService.deleteAll(refEntityMetaDataDynamic.getName());
			dataService.deleteAll(selfXrefEntityMetaData.getName());

			deleteAuthorsThenBooks(1);
			deleteBooksThenAuthors(1);
			deleteAuthorsThenBooks(3);
			deleteAuthorsThenBooks(4);
			deleteAuthorsThenBooks(5);
			deleteAuthorsThenBooks(6);
		});
		waitForIndexToBeStable(entityMetaDataStatic.getName(), indexService, LOG);
		waitForIndexToBeStable(refEntityMetaDataStatic.getName(), indexService, LOG);
		waitForIndexToBeStable(entityMetaDataDynamic.getName(), indexService, LOG);
		waitForIndexToBeStable(refEntityMetaDataDynamic.getName(), indexService, LOG);
		waitForIndexToBeStable(selfXrefEntityMetaData.getName(), indexService, LOG);
	}

	@Test
	public void testLanguageService()
	{
		dataService.add(LANGUAGE, languageFactory
				.create(LanguageService.DEFAULT_LANGUAGE_CODE, LanguageService.DEFAULT_LANGUAGE_NAME, true));
		dataService
				.add(LANGUAGE, languageFactory.create("nl", new Locale("nl").getDisplayName(new Locale("nl")), false));
		dataService
				.add(LANGUAGE, languageFactory.create("pt", new Locale("pt").getDisplayName(new Locale("pt")), false));
		dataService
				.add(LANGUAGE, languageFactory.create("es", new Locale("es").getDisplayName(new Locale("es")), false));
		dataService
				.add(LANGUAGE, languageFactory.create("de", new Locale("de").getDisplayName(new Locale("de")), false));
		dataService
				.add(LANGUAGE, languageFactory.create("it", new Locale("it").getDisplayName(new Locale("it")), false));
		dataService
				.add(LANGUAGE, languageFactory.create("fr", new Locale("fr").getDisplayName(new Locale("fr")), false));
		dataService.add(LANGUAGE, languageFactory.create("xx", "My language", false));

		assertEquals(dataService.getMeta().getEntityMetaData(ENTITY_META_DATA).getAttribute("label-en").getName(),
				"label-en");
		assertEquals(dataService.getMeta().getEntityMetaData(ENTITY_META_DATA).getLabelAttribute("en").getName(),
				"simpleName");
		assertEquals(dataService.getMeta().getEntityMetaData(ENTITY_META_DATA).getLabelAttribute("pt").getName(),
				"simpleName");
		assertEquals(dataService.getMeta().getEntityMetaData(ENTITY_META_DATA).getLabelAttribute("nl").getName(),
				"simpleName");
		assertEquals(dataService.getMeta().getEntityMetaData(ENTITY_META_DATA).getLabelAttribute().getName(),
				"simpleName");

		assertEquals(languageService.getCurrentUserLanguageCode(), "en");
		assertEqualsNoOrder(languageService.getLanguageCodes().toArray(),
				new String[] { "en", "nl", "de", "es", "it", "pt", "fr", "xx" });

		// NL
		assertNotNull(dataService.getEntityMetaData(I18N_STRING).getAttribute("nl"));
		assertNotNull(dataService.getEntityMetaData(ENTITY_META_DATA).getAttribute("label-nl"));
		assertNotNull(dataService.getEntityMetaData(ENTITY_META_DATA).getAttribute("description-nl"));
		assertNotNull(dataService.getEntityMetaData(ATTRIBUTE_META_DATA).getAttribute("label-nl"));
		assertNotNull(dataService.getEntityMetaData(ATTRIBUTE_META_DATA).getAttribute("description-nl"));

		// EN
		assertNotNull(dataService.getEntityMetaData(I18N_STRING).getAttribute("en"));
		assertNotNull(dataService.getEntityMetaData(ENTITY_META_DATA).getAttribute("label-en"));
		assertNotNull(dataService.getEntityMetaData(ENTITY_META_DATA).getAttribute("description-en"));
		assertNotNull(dataService.getEntityMetaData(ATTRIBUTE_META_DATA).getAttribute("label-en"));
		assertNotNull(dataService.getEntityMetaData(ATTRIBUTE_META_DATA).getAttribute("description-en"));

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
		List<Entity> refEntities = testHarness.createTestRefEntities(refEntityMetaDataDynamic, 6);
		List<Entity> entities = testHarness.createTestEntities(entityMetaDataDynamic, 2, refEntities).collect(toList());
		runAsSystem(() ->
		{
			dataService.add(refEntityMetaDataDynamic.getName(), refEntities.stream());
			dataService.add(entityMetaDataDynamic.getName(), entities.stream());
			waitForIndexToBeStable(entityMetaDataDynamic.getName(), indexService, LOG);
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
			entityListenersService.addEntityListener(entityMetaDataDynamic.getName(), listener);
			dataService.update(entityMetaDataDynamic.getName(), entities.stream());
			assertEquals(updateCalled.get(), 1);
			waitForIndexToBeStable(entityMetaDataDynamic.getName(), indexService, LOG);
			assertPresent(entityMetaDataDynamic, entities);
		}
		finally
		{
			// Test that the listener is actually removed and not called anymore
			entityListenersService.removeEntityListener(entityMetaDataDynamic.getName(), listener);
			updateCalled.set(0);
			dataService.update(entityMetaDataDynamic.getName(), entities.stream());
			assertEquals(updateCalled.get(), 0);
			waitForIndexToBeStable(entityMetaDataDynamic.getName(), indexService, LOG);
			assertPresent(entityMetaDataDynamic, entities);
		}
	}

	@Test
	public void testAdd()
	{
		List<Entity> entities = createDynamic(2).collect(toList());
		assertEquals(searchService.count(entityMetaDataDynamic), 0);
		dataService.add(entityMetaDataDynamic.getName(), entities.stream());
		waitForIndexToBeStable(entityMetaDataDynamic.getName(), indexService, LOG);
		assertEquals(dataService.count(entityMetaDataDynamic.getName(), new QueryImpl<>()), 2);
		assertEquals(searchService.count(entityMetaDataDynamic), 2);
		assertPresent(entityMetaDataDynamic, entities);
	}

	@Test
	public void testCount()
	{
		List<Entity> entities = createDynamic(2).collect(toList());
		dataService.add(entityMetaDataDynamic.getName(), entities.stream());
		waitForIndexToBeStable(entityMetaDataDynamic.getName(), indexService, LOG);
		assertEquals(dataService.count(entityMetaDataDynamic.getName(), new QueryImpl<>()), 2);
		assertEquals(searchService.count(entityMetaDataDynamic), 2);
		assertPresent(entityMetaDataDynamic, entities);
	}

	@Test
	public void testDelete()
	{
		Entity entity = createDynamic(1).findFirst().get();
		dataService.add(entityMetaDataDynamic.getName(), entity);
		waitForIndexToBeStable(entityMetaDataDynamic.getName(), indexService, LOG);
		assertPresent(entityMetaDataDynamic, entity);

		dataService.delete(entityMetaDataDynamic.getName(), entity);
		waitForIndexToBeStable(entityMetaDataDynamic.getName(), indexService, LOG);
		assertNotPresent(entity);
	}

	@Test
	public void testDeleteById()
	{
		Entity entity = createDynamic(1).findFirst().get();
		dataService.add(entityMetaDataDynamic.getName(), entity);
		waitForIndexToBeStable(entityMetaDataDynamic.getName(), indexService, LOG);
		assertPresent(entityMetaDataDynamic, entity);

		dataService.deleteById(entityMetaDataDynamic.getName(), entity.getIdValue());
		waitForIndexToBeStable(entityMetaDataDynamic.getName(), indexService, LOG);
		assertNotPresent(entity);
	}

	@Test
	public void testDeleteStream()
	{
		List<Entity> entities = createDynamic(2).collect(toList());
		dataService.add(entityMetaDataDynamic.getName(), entities.stream());
		waitForIndexToBeStable(entityMetaDataDynamic.getName(), indexService, LOG);
		assertEquals(dataService.count(entityMetaDataDynamic.getName(), new QueryImpl<>()), entities.size());

		dataService.delete(entityMetaDataDynamic.getName(), entities.stream());
		waitForIndexToBeStable(entityMetaDataDynamic.getName(), indexService, LOG);
		assertEquals(dataService.count(entityMetaDataDynamic.getName(), new QueryImpl<>()), 0);
	}

	@Test
	public void testDeleteAll()
	{
		List<Entity> entities = createDynamic(5).collect(toList());
		dataService.add(entityMetaDataDynamic.getName(), entities.stream());
		waitForIndexToBeStable(entityMetaDataDynamic.getName(), indexService, LOG);
		assertEquals(dataService.count(entityMetaDataDynamic.getName(), new QueryImpl<>()), entities.size());

		dataService.deleteAll(entityMetaDataDynamic.getName());
		waitForIndexToBeStable(entityMetaDataDynamic.getName(), indexService, LOG);
		assertEquals(dataService.count(entityMetaDataDynamic.getName(), new QueryImpl<>()), 0);
	}

	@Test
	public void testFindAllEmpty()
	{
		Stream<Entity> retrieved = dataService.findAll(entityMetaDataDynamic.getName());
		assertEquals(retrieved.count(), 0);
	}

	@Test
	public void testFindAll()
	{
		List<Entity> entities = createDynamic(5).collect(toList());
		dataService.add(entityMetaDataDynamic.getName(), entities.stream());
		waitForIndexToBeStable(entityMetaDataDynamic.getName(), indexService, LOG);
		Stream<Entity> retrieved = dataService.findAll(entityMetaDataDynamic.getName());
		assertEquals(retrieved.count(), entities.size());
	}

	@Test
	public void testFindAllTyped()
	{
		List<Entity> entities = createDynamic(1).collect(toList());
		dataService.add(entityMetaDataDynamic.getName(), entities.stream());
		waitForIndexToBeStable(entityMetaDataDynamic.getName(), indexService, LOG);
		Supplier<Stream<Entity>> retrieved = () -> dataService.findAll(entityMetaDataDynamic.getName(), Entity.class);
		assertEquals(retrieved.get().count(), 1);
		assertEquals(retrieved.get().iterator().next().getIdValue(), entities.get(0).getIdValue());
	}

	@Test
	public void testFindAllByIds()
	{
		List<Entity> entities = createDynamic(5).collect(toList());
		dataService.add(entityMetaDataDynamic.getName(), entities.stream());
		waitForIndexToBeStable(entityMetaDataDynamic.getName(), indexService, LOG);
		Stream<Object> ids = Stream.concat(entities.stream().map(Entity::getIdValue), of("bogus"));
		Stream<Entity> retrieved = dataService.findAll(entityMetaDataDynamic.getName(), ids);
		assertEquals(retrieved.count(), entities.size());
	}

	@Test
	public void testFindAllByIdsTyped()
	{
		List<Entity> entities = createStatic(5).collect(toList());
		dataService.add(entityMetaDataStatic.getName(), entities.stream());
		waitForIndexToBeStable(entityMetaDataStatic.getName(), indexService, LOG);

		Supplier<Stream<TestEntityStatic>> retrieved = () -> dataService.findAll(entityMetaDataStatic.getName(),
				Stream.concat(entities.stream().map(Entity::getIdValue), of("bogus")), TestEntityStatic.class);
		assertEquals(retrieved.get().count(), entities.size());
		assertEquals(retrieved.get().iterator().next().getId(), entities.get(0).getIdValue());
		assertEquals(retrieved.get().iterator().next().getIdValue(), entities.get(0).getIdValue());
	}

	@Test
	public void testFindAllStreamFetch()
	{
		List<Entity> entities = createDynamic(5).collect(toList());
		dataService.add(entityMetaDataDynamic.getName(), entities.stream());
		waitForIndexToBeStable(entityMetaDataDynamic.getName(), indexService, LOG);
		Stream<Object> ids = concat(entities.stream().map(Entity::getIdValue), of("bogus"));
		Stream<Entity> retrieved = dataService
				.findAll(entityMetaDataDynamic.getName(), ids, new Fetch().field(ATTR_ID));
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
		dataService.add(entityMetaDataDynamic.getName(), entities.stream());
		waitForIndexToBeStable(entityMetaDataDynamic.getName(), indexService, LOG);
		Supplier<Stream<Entity>> found = () -> dataService.query(entityMetaDataDynamic.getName()).eq(attrName, value)
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
		dataService.add(entityMetaDataDynamic.getName(), entities.stream());
		waitForIndexToBeStable(entityMetaDataDynamic.getName(), indexService, LOG);
		Supplier<Stream<Entity>> found = () -> dataService.query(entityMetaDataDynamic.getName()).in(ATTR_ID, ids)
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
		dataService.add(entityMetaDataDynamic.getName(), entities.stream());
		waitForIndexToBeStable(entityMetaDataDynamic.getName(), indexService, LOG);
		Supplier<Stream<Entity>> found = () -> dataService.query(entityMetaDataDynamic.getName()).lt(ATTR_INT, value)
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
		dataService.add(entityMetaDataDynamic.getName(), entities.stream());
		waitForIndexToBeStable(entityMetaDataDynamic.getName(), indexService, LOG);
		Supplier<Stream<Entity>> found = () -> dataService.query(entityMetaDataDynamic.getName()).le(ATTR_INT, value)
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
		dataService.add(entityMetaDataDynamic.getName(), entities.stream());
		waitForIndexToBeStable(entityMetaDataDynamic.getName(), indexService, LOG);
		Supplier<Stream<Entity>> found = () -> dataService.query(entityMetaDataDynamic.getName()).gt(ATTR_INT, value)
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
		dataService.add(entityMetaDataDynamic.getName(), entities.stream());
		waitForIndexToBeStable(entityMetaDataDynamic.getName(), indexService, LOG);
		Supplier<Stream<Entity>> found = () -> dataService.query(entityMetaDataDynamic.getName()).ge(ATTR_INT, value)
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
		dataService.add(entityMetaDataDynamic.getName(), entities.stream());
		waitForIndexToBeStable(entityMetaDataDynamic.getName(), indexService, LOG);
		Supplier<Stream<Entity>> found = () -> dataService.query(entityMetaDataDynamic.getName())
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
		dataService.add(entityMetaDataDynamic.getName(), entities.stream());
		waitForIndexToBeStable(entityMetaDataDynamic.getName(), indexService, LOG);
		Supplier<Stream<Entity>> found = () -> dataService.query(entityMetaDataDynamic.getName())
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
		dataService.add(entityMetaDataDynamic.getName(), entities.stream());
		waitForIndexToBeStable(entityMetaDataDynamic.getName(), indexService, LOG);
		Supplier<Stream<Entity>> found = () -> dataService.query(entityMetaDataDynamic.getName()).not()
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
		dataService.add(entityMetaDataDynamic.getName(), entities.stream());
		waitForIndexToBeStable(entityMetaDataDynamic.getName(), indexService, LOG);

		Query q1 = new QueryImpl<>().eq(EntityTestHarness.ATTR_STRING, "string1");
		q1.pageSize(1000);

		Query q2 = new QueryImpl<>().eq(EntityTestHarness.ATTR_BOOL, true);
		q2.pageSize(500);

		Query q3 = new QueryImpl<>().eq(ATTR_DECIMAL, 1.123);

		runAsSystem(() ->
		{
			for (int i = 0; i < 100000; i++)
			{
				dataService.findAll(entityMetaDataDynamic.getName(), q1);
				dataService.findAll(entityMetaDataDynamic.getName(), q2);
				dataService.findOne(entityMetaDataDynamic.getName(), q3);
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
		dataService.add(entityMetaDataDynamic.getName(), entities.stream());
		waitForIndexToBeStable(entityMetaDataDynamic.getName(), indexService, LOG);
		Supplier<Stream<Entity>> found = () -> dataService.query(entityMetaDataDynamic.getName())
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
		dataService.add(entityMetaDataDynamic.getName(), entities.stream());
		waitForIndexToBeStable(entityMetaDataDynamic.getName(), indexService, LOG);
		Supplier<Stream<Entity>> found = () -> dataService.query(entityMetaDataDynamic.getName())
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
		dataService.add(entityMetaDataDynamic.getName(), entities.stream());
		waitForIndexToBeStable(entityMetaDataDynamic.getName(), indexService, LOG);
		Supplier<Stream<Entity>> found = () -> dataService.query(entityMetaDataDynamic.getName())
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
		dataService.add(entityMetaDataDynamic.getName(), entities.stream());
		waitForIndexToBeStable(entityMetaDataDynamic.getName(), indexService, LOG);
		Supplier<Stream<Entity>> found = () -> dataService.query(entityMetaDataDynamic.getName())
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
		List<Entity> testRefEntities = testHarness.createTestRefEntities(refEntityMetaDataDynamic, 6);
		List<Entity> testEntities = testHarness.createTestEntities(entityMetaDataDynamic, 10, testRefEntities)
				.collect(toList());
		runAsSystem(() ->
		{
			dataService.add(refEntityMetaDataDynamic.getName(), testRefEntities.stream());
			dataService.add(entityMetaDataDynamic.getName(), testEntities.stream());
		});
		waitForIndexToBeStable(refEntityMetaDataDynamic.getName(), indexService, LOG);
		waitForIndexToBeStable(entityMetaDataDynamic.getName(), indexService, LOG);
		Supplier<Stream<Entity>> found = () -> dataService.findAll(entityMetaDataDynamic.getName(),
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
		dataService.add(entityMetaDataStatic.getName(), entities.stream());
		waitForIndexToBeStable(entityMetaDataStatic.getName(), indexService, LOG);
		Supplier<Stream<TestEntityStatic>> found = () -> dataService.findAll(entityMetaDataStatic.getName(),
				new QueryImpl<TestEntityStatic>().eq(ATTR_ID, entities.get(0).getIdValue()), TestEntityStatic.class);
		assertEquals(found.get().count(), 1);
		assertEquals(found.get().findFirst().get().getId(), entities.get(0).getIdValue());
	}

	@Test
	public void testFindOne()
	{
		Entity entity = createDynamic(1).findFirst().get();
		dataService.add(entityMetaDataDynamic.getName(), Stream.of(entity));
		waitForIndexToBeStable(entityMetaDataDynamic.getName(), indexService, LOG);
		assertNotNull(dataService.findOneById(entityMetaDataDynamic.getName(), entity.getIdValue()));
	}

	@Test
	public void testFindOneTypedStatic()
	{
		Entity entity = createStatic(1).findFirst().get();
		dataService.add(entityMetaDataStatic.getName(), Stream.of(entity));
		waitForIndexToBeStable(entityMetaDataStatic.getName(), indexService, LOG);
		TestEntityStatic testEntityStatic = dataService
				.findOneById(entityMetaDataStatic.getName(), entity.getIdValue(), TestEntityStatic.class);
		assertNotNull(testEntityStatic);
		assertEquals(testEntityStatic.getId(), entity.getIdValue());
	}

	@Test
	public void testFindOneFetch()
	{
		Entity entity = createDynamic(1).findFirst().get();
		dataService.add(entityMetaDataDynamic.getName(), Stream.of(entity));
		waitForIndexToBeStable(entityMetaDataDynamic.getName(), indexService, LOG);
		assertNotNull(dataService
				.findOneById(entityMetaDataDynamic.getName(), entity.getIdValue(), new Fetch().field(ATTR_ID)));
	}

	@Test
	public void testFindOneFetchTypedStatic()
	{
		TestEntityStatic entity = new TestEntityStatic(entityMetaDataStatic);
		entity.set(ATTR_ID, "1");
		entity.set(ATTR_STRING, "string1");
		entity.set(ATTR_BOOL, true);

		dataService.add(entityMetaDataStatic.getName(), Stream.of(entity));
		waitForIndexToBeStable(entityMetaDataStatic.getName(), indexService, LOG);
		TestEntityStatic testEntityStatic = dataService
				.findOneById(entityMetaDataStatic.getName(), entity.getIdValue(), new Fetch().field(ATTR_ID),
						TestEntityStatic.class);
		assertNotNull(testEntityStatic);
		assertEquals(testEntityStatic.getIdValue(), entity.getIdValue());
	}

	@Test
	public void testFindOneQuery()
	{
		Entity entity = createDynamic(1).findFirst().get();
		dataService.add(entityMetaDataDynamic.getName(), entity);
		waitForIndexToBeStable(entityMetaDataDynamic.getName(), indexService, LOG);
		entity = dataService
				.findOne(entityMetaDataDynamic.getName(), new QueryImpl<>().eq(ATTR_ID, entity.getIdValue()));
		assertNotNull(entity);
	}

	@Test
	public void testFindOneQueryTypedStatic()
	{
		Entity entity = createStatic(1).findFirst().get();
		dataService.add(entityMetaDataStatic.getName(), entity);
		waitForIndexToBeStable(entityMetaDataStatic.getName(), indexService, LOG);
		TestEntityStatic testEntityStatic = dataService.findOne(entityMetaDataStatic.getName(),
				new QueryImpl<TestEntityStatic>().eq(ATTR_ID, entity.getIdValue()), TestEntityStatic.class);
		assertNotNull(testEntityStatic);
		assertEquals(testEntityStatic.getId(), entity.getIdValue());
	}

	@Test
	public void testGetCapabilities()
	{
		Set<RepositoryCapability> capabilities = dataService.getCapabilities(entityMetaDataDynamic.getName());
		assertNotNull(capabilities);
		assertTrue(capabilities.containsAll(asList(MANAGABLE, QUERYABLE, WRITABLE, VALIDATE_REFERENCE_CONSTRAINT)));
	}

	@Test
	public void testGetEntityMetaData()
	{
		EntityMetaData emd = dataService.getEntityMetaData(entityMetaDataDynamic.getName());
		assertNotNull(emd);
		assertTrue(EntityUtils.equals(emd, entityMetaDataDynamic));
	}

	@Test
	public void testGetEntityNames()
	{
		Stream<String> names = dataService.getEntityNames();
		assertNotNull(names);
		assertTrue(names.filter(entityMetaDataDynamic.getName()::equals).findFirst().isPresent());
	}

	@Test
	public void testGetMeta()
	{
		assertNotNull(dataService.getMeta());
	}

	@Test()
	public void testGetKnownRepository()
	{
		Repository<Entity> repo = dataService.getRepository(entityMetaDataDynamic.getName());
		assertNotNull(repo);
		assertEquals(repo.getName(), entityMetaDataDynamic.getName());
	}

	@Test(expectedExceptions = UnknownEntityException.class)
	public void testGetUnknownRepository()
	{
		dataService.getRepository("bogus");
	}

	@Test
	public void testHasRepository()
	{
		assertTrue(dataService.hasRepository(entityMetaDataDynamic.getName()));
		assertFalse(dataService.hasRepository("bogus"));
	}

	@Test
	public void testIterator()
	{
		assertNotNull(dataService.iterator());
		StreamSupport.stream(dataService.spliterator(), false).forEach(repo -> LOG.info(repo.getName()));
		Repository repo = dataService.getRepository(entityMetaDataDynamic.getName());

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
		assertNotNull(dataService.query(entityMetaDataDynamic.getName()));
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
		dataService.add(entityMetaDataDynamic.getName(), entity);
		waitForIndexToBeStable(entityMetaDataDynamic.getName(), indexService, LOG);

		entity = dataService.findOneById(entityMetaDataDynamic.getName(), entity.getIdValue());
		assertNotNull(entity);
		assertEquals(entity.get(ATTR_STRING), "string1");

		Query<Entity> q = new QueryImpl<>();
		q.eq(ATTR_STRING, "qwerty");
		entity.set(ATTR_STRING, "qwerty");

		assertEquals(searchService.count(q, entityMetaDataDynamic), 0);
		dataService.update(entityMetaDataDynamic.getName(), entity);
		waitForIndexToBeStable(entityMetaDataDynamic.getName(), indexService, LOG);
		assertEquals(searchService.count(q, entityMetaDataDynamic), 1);

		assertPresent(entityMetaDataDynamic, entity);

		entity = dataService.findOneById(entityMetaDataDynamic.getName(), entity.getIdValue());
		assertNotNull(entity.get(ATTR_STRING));
		assertEquals(entity.get(ATTR_STRING), "qwerty");
	}

	@Test
	public void testUpdateSingleRefEntityIndexesReferencingEntities()
	{
		dataService.add(entityMetaDataDynamic.getName(), createDynamic(30));
		waitForIndexToBeStable(entityMetaDataDynamic.getName(), indexService, LOG);

		Entity refEntity4 = dataService.findOneById(refEntityMetaDataDynamic.getName(), "4");

		Query<Entity> q = new QueryImpl<>().search("refstring4");

		assertEquals(searchService.count(q, entityMetaDataDynamic), 5);
		refEntity4.set(ATTR_REF_STRING, "qwerty");
		runAsSystem(() -> dataService.update(refEntityMetaDataDynamic.getName(), refEntity4));
		waitForIndexToBeStable(entityMetaDataDynamic.getName(), indexService, LOG);
		assertEquals(searchService.count(q, entityMetaDataDynamic), 0);
		assertEquals(searchService.count(new QueryImpl<>().search("qwerty"), entityMetaDataDynamic), 5);
	}

	@Test(enabled = false) //FIXME: sys_md_attributes spam
	public void testUpdateSingleRefEntityIndexesLargeAmountOfReferencingEntities()
	{
		dataService.add(entityMetaDataDynamic.getName(), createDynamic(10000));
		waitForIndexToBeStable(entityMetaDataDynamic.getName(), indexService, LOG);

		Query<Entity> q = new QueryImpl<>().search("refstring4").or().search("refstring5");

		assertEquals(searchService.count(q, entityMetaDataDynamic), 3333);
		Entity refEntity4 = dataService.findOneById(refEntityMetaDataDynamic.getName(), "4");
		refEntity4.set(ATTR_REF_STRING, "qwerty");
		runAsSystem(() -> dataService.update(refEntityMetaDataDynamic.getName(), refEntity4));

		Entity refEntity5 = dataService.findOneById(refEntityMetaDataDynamic.getName(), "5");
		refEntity5.set(ATTR_REF_STRING, "qwerty");
		runAsSystem(() -> dataService.update(refEntityMetaDataDynamic.getName(), refEntity5));

		waitForIndexToBeStable(entityMetaDataDynamic.getName(), indexService, LOG);
		assertEquals(searchService.count(q, entityMetaDataDynamic), 0);

		assertEquals(searchService.count(new QueryImpl<>().search("qwerty"), entityMetaDataDynamic), 3333);
	}

	@Test
	public void testUpdateStream()
	{
		Entity entity = createDynamic(1).findFirst().get();

		dataService.add(entityMetaDataDynamic.getName(), entity);
		waitForIndexToBeStable(entityMetaDataDynamic.getName(), indexService, LOG);
		assertPresent(entityMetaDataDynamic, entity);

		entity = dataService.findOneById(entityMetaDataDynamic.getName(), entity.getIdValue());
		assertNotNull(entity);
		assertEquals(entity.get(ATTR_STRING), "string1");

		entity.set(ATTR_STRING, "qwerty");
		Query<Entity> q = new QueryImpl<>();
		q.eq(ATTR_STRING, "qwerty");

		assertEquals(searchService.count(q, entityMetaDataDynamic), 0);

		dataService.update(entityMetaDataDynamic.getName(), of(entity));
		waitForIndexToBeStable(entityMetaDataDynamic.getName(), indexService, LOG);

		assertEquals(searchService.count(q, entityMetaDataDynamic), 1);

		assertPresent(entityMetaDataDynamic, entity);
		entity = dataService.findOneById(entityMetaDataDynamic.getName(), entity.getIdValue());
		assertNotNull(entity.get(ATTR_STRING));
		assertEquals(entity.get(ATTR_STRING), "qwerty");
	}

	private Stream<Entity> createDynamic(int count)
	{
		List<Entity> refEntities = testHarness.createTestRefEntities(refEntityMetaDataDynamic, 6);
		runAsSystem(() -> dataService.add(refEntityMetaDataDynamic.getName(), refEntities.stream()));
		return testHarness.createTestEntities(entityMetaDataDynamic, count, refEntities);
	}

	private Stream<Entity> createStatic(int count)
	{
		List<Entity> refEntities = testHarness.createTestRefEntities(refEntityMetaDataStatic, 6);
		runAsSystem(() -> dataService.add(refEntityMetaDataStatic.getName(), refEntities.stream()));
		return testHarness.createTestEntities(entityMetaDataStatic, count, refEntities);
	}

	private void assertPresent(EntityMetaData emd, List<Entity> entities)
	{
		entities.forEach(e -> assertPresent(emd, e));
	}

	private void assertPresent(EntityMetaData emd, Entity entity)
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
		assertNull(dataService.findOneById(entityMetaDataDynamic.getName(), entity.getIdValue()));

		// Not found in index Elasticsearch
		Query<Entity> q = new QueryImpl<>();
		q.eq(entityMetaDataDynamic.getIdAttribute().getName(), entity.getIdValue());
		assertEquals(searchService.count(q, entityMetaDataDynamic), 0);
	}

	@Test
	public void testCreateSelfXref()
	{
		Entity entitySelfXref = entitySelfXrefTestHarness.createTestEntities(selfXrefEntityMetaData, 1)
				.collect(toList()).get(0);

		//Create
		dataService.add(selfXrefEntityMetaData.getName(), entitySelfXref);
		waitForIndexToBeStable(selfXrefEntityMetaData.getName(), indexService, LOG);
		Entity entity = dataService.findOneById(selfXrefEntityMetaData.getName(), entitySelfXref.getIdValue());
		assertPresent(selfXrefEntityMetaData, entity);

		Query<Entity> q1 = new QueryImpl<>();
		q1.eq(ATTR_STRING, "attr_string_old");
		Query<Entity> q2 = new QueryImpl<>();
		q2.eq(ATTR_STRING, "attr_string_new");
		entity.set(ATTR_STRING, "attr_string_new");

		// Verify value in elasticsearch before update
		assertEquals(searchService.count(q1, selfXrefEntityMetaData), 1);
		assertEquals(searchService.count(q2, selfXrefEntityMetaData), 0);

		// Update
		dataService.update(selfXrefEntityMetaData.getName(), entity);
		waitForIndexToBeStable(selfXrefEntityMetaData.getName(), indexService, LOG);
		assertPresent(selfXrefEntityMetaData, entity);

		// Verify value in elasticsearch after update
		assertEquals(searchService.count(q2, selfXrefEntityMetaData), 1);
		assertEquals(searchService.count(q1, selfXrefEntityMetaData), 0);

		// Verify value in PostgreSQL after update
		entity = dataService.findOneById(selfXrefEntityMetaData.getName(), entity.getIdValue());
		assertNotNull(entity.get(ATTR_STRING));
		assertEquals(entity.get(ATTR_STRING), "attr_string_new");

		// Check id are equals
		assertEquals(entity.getEntity(ATTR_XREF).getIdValue(), entity.getIdValue());
	}

	@Test
	public void testIndexCreateMetaData()
	{
		IndexMetadataCUDOperationsPlatformIT
				.testIndexCreateMetaData(searchService, entityMetaDataStatic, entityMetaDataDynamic, metaDataService);
	}

	@Test
	public void testIndexDeleteMetaData()
	{
		IndexMetadataCUDOperationsPlatformIT
				.testIndexDeleteMetaData(searchService, dataService, entityMetaDataDynamic, metaDataService,
						indexService);
	}

	@Test
	public void testIndexUpdateMetaDataUpdateAttribute()
	{
		IndexMetadataCUDOperationsPlatformIT
				.testIndexUpdateMetaDataUpdateAttribute(searchService, entityMetaDataDynamic, metaDataService,
						indexService);
	}

	@Test
	public void testIndexUpdateMetaDataRemoveAttribute()
	{
		IndexMetadataCUDOperationsPlatformIT
				.testIndexUpdateMetaDataRemoveAttribute(entityMetaDataDynamic, EntityTestHarness.ATTR_CATEGORICAL,
						searchService, metaDataService, indexService);

		IndexMetadataCUDOperationsPlatformIT
				.testIndexUpdateMetaDataRemoveAttribute(entityMetaDataDynamic, EntityTestHarness.ATTR_BOOL,
						searchService, metaDataService, indexService);

		IndexMetadataCUDOperationsPlatformIT
				.testIndexUpdateMetaDataRemoveAttribute(entityMetaDataDynamic, EntityTestHarness.ATTR_DATE,
						searchService, metaDataService, indexService);

		IndexMetadataCUDOperationsPlatformIT
				.testIndexUpdateMetaDataRemoveAttribute(entityMetaDataDynamic, EntityTestHarness.ATTR_XREF,
						searchService, metaDataService, indexService);

		IndexMetadataCUDOperationsPlatformIT
				.testIndexUpdateMetaDataRemoveAttribute(entityMetaDataDynamic, EntityTestHarness.ATTR_DATETIME,
						searchService, metaDataService, indexService);

		IndexMetadataCUDOperationsPlatformIT
				.testIndexUpdateMetaDataRemoveAttribute(entityMetaDataDynamic, EntityTestHarness.ATTR_DECIMAL,
						searchService, metaDataService, indexService);

		IndexMetadataCUDOperationsPlatformIT
				.testIndexUpdateMetaDataRemoveAttribute(entityMetaDataDynamic, EntityTestHarness.ATTR_EMAIL,
						searchService, metaDataService, indexService);

		IndexMetadataCUDOperationsPlatformIT
				.testIndexUpdateMetaDataRemoveAttribute(entityMetaDataDynamic, EntityTestHarness.ATTR_HTML,
						searchService, metaDataService, indexService);

		IndexMetadataCUDOperationsPlatformIT
				.testIndexUpdateMetaDataRemoveAttribute(entityMetaDataDynamic, EntityTestHarness.ATTR_INT,
						searchService, metaDataService, indexService);

		IndexMetadataCUDOperationsPlatformIT
				.testIndexUpdateMetaDataRemoveAttribute(entityMetaDataDynamic, EntityTestHarness.ATTR_HYPERLINK,
						searchService, metaDataService, indexService);
	}

	// Derived from fix: https://github.com/molgenis/molgenis/issues/5227
	@Test
	public void testIndexBatchUpdate()
	{
		List<Entity> refEntities = testHarness.createTestRefEntities(refEntityMetaDataDynamic, 2);
		List<Entity> entities = testHarness.createTestEntities(entityMetaDataDynamic, 2, refEntities).collect(toList());
		runAsSystem(() ->
		{
			dataService.add(refEntityMetaDataDynamic.getName(), refEntities.stream());
			dataService.add(entityMetaDataDynamic.getName(), entities.stream());
			waitForIndexToBeStable(entityMetaDataDynamic.getName(), indexService, LOG);
		});

		// test string1 from entity
		Query<Entity> q0 = new QueryImpl<>();
		q0.search("string1");
		Stream<Entity> result0 = searchService.searchAsStream(q0, entityMetaDataDynamic);
		assertEquals(result0.count(), 2);

		// test refstring1 from ref entity
		Query<Entity> q1 = new QueryImpl<>();
		q1.search("refstring0");
		Stream<Entity> result1 = searchService.searchAsStream(q1, entityMetaDataDynamic);
		assertEquals(result1.count(), 1);

		// test refstring1 from ref entity
		Query<Entity> q2 = new QueryImpl<>();
		q2.search("refstring1");
		Stream<Entity> result2 = searchService.searchAsStream(q2, entityMetaDataDynamic);
		assertEquals(result2.count(), 1);

		refEntities.get(0).set(ATTR_REF_STRING, "searchTestBatchUpdate");
		runAsSystem(() ->
		{
			dataService.update(refEntityMetaDataDynamic.getName(), refEntities.stream());
			waitForIndexToBeStable(entityMetaDataDynamic.getName(), indexService, LOG);
		});

		// test string1 from entity
		Stream<Entity> result3 = searchService.searchAsStream(q0, entityMetaDataDynamic);
		assertEquals(result3.count(), 2);

		// test refstring1 from ref entity
		Query<Entity> q4 = new QueryImpl<>();
		q4.search("refstring0");
		Stream<Entity> result4 = searchService.searchAsStream(q4, entityMetaDataDynamic);
		assertEquals(result4.count(), 0);

		// test refstring1 from ref entity
		Query<Entity> q5 = new QueryImpl<>();
		q5.search("refstring1");
		Stream<Entity> result5 = searchService.searchAsStream(q5, entityMetaDataDynamic);
		assertEquals(result5.count(), 1);

		// test refstring1 from ref entity
		Query<Entity> q6 = new QueryImpl<>();
		q6.search("searchTestBatchUpdate");
		Stream<Entity> result6 = searchService.searchAsStream(q6, entityMetaDataDynamic);
		assertEquals(result6.count(), 1);
	}

	@Test
	public void testOneToManyInsert()
	{
		for (int i = 1; i <= 6; i++)
		{
			OneToManyTestHarness.AuthorsAndBooks authorsAndBooks = importAuthorsAndBooks(i);

			if (authorsAndBooks == null) continue; // skip "disabled" test cases //FIXME

			String book = "sys_Book" + i;
			assertEquals(dataService.findOneById(book, "book1").getEntity("author").getIdValue(), "author1");
			assertEquals(dataService.findOneById(book, "book2").getEntity("author").getIdValue(), "author2");
			assertEquals(dataService.findOneById(book, "book3").getEntity("author").getIdValue(), "author3");

			String author = "sys_Author" + i;
			assertEquals(dataService.findOneById(author, "author1").getEntities("books").iterator().next().getIdValue(),
					"book1");
			assertEquals(dataService.findOneById(author, "author2").getEntities("books").iterator().next().getIdValue(),
					"book2");
			assertEquals(dataService.findOneById(author, "author3").getEntities("books").iterator().next().getIdValue(),
					"book3");
		}
	}

	private void importBooksThenAuthors(OneToManyTestHarness.AuthorsAndBooks authorsAndBooks)
	{
		runAsSystem(() ->
		{
			dataService.add(authorsAndBooks.getBookMetaData().getName(), authorsAndBooks.getBooks().stream());
			dataService.add(authorsAndBooks.getAuthorMetaData().getName(), authorsAndBooks.getAuthors().stream());
			waitForIndexToBeStable(authorsAndBooks.getAuthorMetaData().getName(), indexService, LOG);
			waitForIndexToBeStable(authorsAndBooks.getBookMetaData().getName(), indexService, LOG);
		});
	}

	private void importAuthorsThenBooks(OneToManyTestHarness.AuthorsAndBooks authorsAndBooks)
	{
		runAsSystem(() ->
		{
			dataService.add(authorsAndBooks.getAuthorMetaData().getName(), authorsAndBooks.getAuthors().stream());
			dataService.add(authorsAndBooks.getBookMetaData().getName(), authorsAndBooks.getBooks().stream());
			waitForIndexToBeStable(authorsAndBooks.getAuthorMetaData().getName(), indexService, LOG);
			waitForIndexToBeStable(authorsAndBooks.getBookMetaData().getName(), indexService, LOG);
		});
	}

	private void deleteBooksThenAuthors(int testCase)
	{
		dataService.deleteAll("sys_Book" + testCase);
		dataService.deleteAll("sys_Author" + testCase);
	}

	private void deleteAuthorsThenBooks(int testCase)
	{
		dataService.deleteAll("sys_Author" + testCase);
		dataService.deleteAll("sys_Book" + testCase);
	}

	private OneToManyTestHarness.AuthorsAndBooks importAuthorsAndBooks(int testCase)
	{
		OneToManyTestHarness.AuthorsAndBooks authorsAndBooks;
		switch (testCase)
		{
			case 1:
				authorsAndBooks = oneToManyTestHarness.createEntities1();
				// case 1: books/authors both nillable, order of import not important
				importBooksThenAuthors(authorsAndBooks);
				return authorsAndBooks;
			case 2:
				authorsAndBooks = oneToManyTestHarness.createEntities2();
				// case 2: book.author required so add Author entities first
				importAuthorsThenBooks(authorsAndBooks);
				return authorsAndBooks;
			case 3:
				authorsAndBooks = oneToManyTestHarness.createEntities3();
				// case 3: author.books required so add Book entities first
				importBooksThenAuthors(authorsAndBooks);
				return authorsAndBooks;
			case 4:
				// FIXME can't import when both sides of onetomany are required
				// case 4: books/authors both required: impossible?
				return null;
			case 5:
				authorsAndBooks = oneToManyTestHarness.createEntities5();
				importBooksThenAuthors(authorsAndBooks);
				return authorsAndBooks;
			case 6:
				authorsAndBooks = oneToManyTestHarness.createEntities6();
				importBooksThenAuthors(authorsAndBooks);
				return authorsAndBooks;
			default:
				return null;
		}
	}

	@Test (enabled = false)
	public void testOneToManyCaching()
	{
		//TODO
		// For all test cases
		// Retrieve/update (with and without queries) some books and authors. Request again and verify they contain correct data
	}

	@Test
	@Transactional
	public void testL1OneToManySingleEntityUpdate()
	{
		OneToManyTestHarness.AuthorsAndBooks authorsAndBooks = importAuthorsAndBooks(1);
		try
		{
			Entity book1 = dataService.findOneById(authorsAndBooks.getBookMetaData().getName(), "book1");
			Entity author1 = dataService.findOneById(authorsAndBooks.getAuthorMetaData().getName(), "author1");
			Entity author2 = dataService.findOneById(authorsAndBooks.getAuthorMetaData().getName(), "author2");

			book1.set(BookMetaData1.AUTHOR, author2);
			dataService.update(book1.getEntityMetaData().getName(), book1);

			Entity author1RetrievedAgain = dataService
					.findOneById(authorsAndBooks.getAuthorMetaData().getName(), author1.getIdValue());
			assertEquals(Collections.emptyList(),
					Lists.newArrayList(author1RetrievedAgain.getEntities(AuthorMetaData1.ATTR_BOOKS)));

			Entity author2Retrieved = dataService
					.findOneById(authorsAndBooks.getAuthorMetaData().getName(), author2.getIdValue());
			Iterable<Entity> author2Books = author2Retrieved.getEntities(AuthorMetaData1.ATTR_BOOKS);
			List<Object> retrievedAuthor2BookIds = StreamSupport.stream(author2Books.spliterator(), false)
					.map(Entity::getIdValue).collect(toList());
			assertEquals(retrievedAuthor2BookIds, newArrayList("book2", "book1"));
		}
		finally
		{
			dataService.deleteAll(authorsAndBooks.getBookMetaData().getName());
			dataService.deleteAll(authorsAndBooks.getAuthorMetaData().getName());
		}
	}

	@Test
	@Transactional
	public void testL1OneToManyStreamingEntityUpdate()
	{
		OneToManyTestHarness.AuthorsAndBooks authorsAndBooks = importAuthorsAndBooks(1);
		try
		{
			Entity book1 = dataService.findOneById(authorsAndBooks.getBookMetaData().getName(), "book1");
			Entity author1 = dataService.findOneById(authorsAndBooks.getAuthorMetaData().getName(), "author1");
			Entity author2 = dataService.findOneById(authorsAndBooks.getAuthorMetaData().getName(), "author2");

			book1.set(BookMetaData1.AUTHOR, author2);
			dataService.update(book1.getEntityMetaData().getName(), Stream.of(book1));

			Entity author1RetrievedAgain = dataService
					.findOneById(authorsAndBooks.getAuthorMetaData().getName(), author1.getIdValue());
			assertEquals(Collections.emptyList(),
					Lists.newArrayList(author1RetrievedAgain.getEntities(AuthorMetaData1.ATTR_BOOKS)));

			Entity author2Retrieved = dataService
					.findOneById(authorsAndBooks.getAuthorMetaData().getName(), author2.getIdValue());
			Iterable<Entity> author2Books = author2Retrieved.getEntities(AuthorMetaData1.ATTR_BOOKS);
			List<Object> retrievedAuthor2BookIds = StreamSupport.stream(author2Books.spliterator(), false)
					.map(Entity::getIdValue).collect(toList());
			assertEquals(retrievedAuthor2BookIds, newArrayList("book2", "book1"));
		}
		finally
		{
			dataService.deleteAll(authorsAndBooks.getBookMetaData().getName());
			dataService.deleteAll(authorsAndBooks.getAuthorMetaData().getName());
		}
	}

	@Test
	@Transactional
	public void testL1OneToManyEntitySingleEntityDelete()
	{
		OneToManyTestHarness.AuthorsAndBooks authorsAndBooks = importAuthorsAndBooks(1);
		Entity book1 = dataService.findOneById(authorsAndBooks.getBookMetaData().getName(), "book1");
		Entity author1 = dataService.findOneById(authorsAndBooks.getAuthorMetaData().getName(), "author1");

		dataService.delete(book1.getEntityMetaData().getName(), book1);

		Entity author1RetrievedAgain = dataService
				.findOneById(authorsAndBooks.getAuthorMetaData().getName(), author1.getIdValue());
		assertEquals(Collections.emptyList(),
				Lists.newArrayList(author1RetrievedAgain.getEntities(AuthorMetaData1.ATTR_BOOKS)));

		dataService.deleteAll(authorsAndBooks.getBookMetaData().getName());
		dataService.deleteAll(authorsAndBooks.getAuthorMetaData().getName());
	}

	@Test
	@Transactional
	public void testL1OneToManyEntityStreamingEntityDelete()
	{
		OneToManyTestHarness.AuthorsAndBooks authorsAndBooks = importAuthorsAndBooks(1);
		try
		{
			Entity book1 = dataService.findOneById(authorsAndBooks.getBookMetaData().getName(), "book1");
			Entity author1 = dataService.findOneById(authorsAndBooks.getAuthorMetaData().getName(), "author1");

			dataService.delete(book1.getEntityMetaData().getName(), Stream.of(book1));

			Entity author1RetrievedAgain = dataService
					.findOneById(authorsAndBooks.getAuthorMetaData().getName(), author1.getIdValue());
			assertEquals(Collections.emptyList(),
					Lists.newArrayList(author1RetrievedAgain.getEntities(AuthorMetaData1.ATTR_BOOKS)));
		}
		finally
		{
			dataService.deleteAll(authorsAndBooks.getBookMetaData().getName());
			dataService.deleteAll(authorsAndBooks.getAuthorMetaData().getName());
		}
	}

	@Test (enabled = false)
	public void testOneToManyOrdering()
	{
		OneToManyTestHarness.AuthorsAndBooks authorsAndBooks = importAuthorsAndBooks(5); // book.author required

		//TODO
		// test case 1, 5, 6
		// Verify order is correct
		// Update book name, verify books attribute in author has new and correct ordering
	}

	@Test (enabled = false, expectedExceptions = MolgenisDataException.class)
	public void testOneToManyAuthorRequiredSetNull()
	{
		// FIXME book.author can be set to null
		OneToManyTestHarness.AuthorsAndBooks authorsAndBooks = importAuthorsAndBooks(2); // book.author required
		Entity author = authorsAndBooks.getAuthors().get(0);
		author.set("books", null);
		dataService.update(authorsAndBooks.getAuthorMetaData().getName(), author);

		Entity book = authorsAndBooks.getBooks().get(0);
		book.set("author", null);
		dataService.update(authorsAndBooks.getBookMetaData().getName(), book);
	}

	@Test
	public void testOneToManyAuthorRequiredUpdateValue()
	{
		importAuthorsAndBooks(2); // book.author required
		Entity book = dataService.findOneById("sys_Book2", "book1");
		book.set("author", dataService.findOneById("sys_Author2", "author2"));
		dataService.update("sys_Book2", book);

		Entity updatedBook = dataService.findOneById("sys_Book2", "book1");
		assertEquals(updatedBook.getEntity("author").getIdValue(), "author2");

		// FIXME which order do we expect? (change set to list when decided)
		Entity updatedAuthor1 = dataService.findOneById("sys_Author2", "author1");
		assertEquals(StreamSupport.stream(updatedAuthor1.getEntities("books").spliterator(), false).map(Entity::getIdValue).collect(toSet()), newHashSet());

		// FIXME which order do we expect? (change set to list when decided)
		Entity updatedAuthor2 = dataService.findOneById("sys_Author2", "author2");
		assertEquals(StreamSupport.stream(updatedAuthor2.getEntities("books").spliterator(), false).map(Entity::getIdValue).collect(toSet()), newHashSet("book2", "book1"));
	}

	@Test
	public void testOneToManyBookRequiredSetNull(){
		// TODO
	}

	@Test
	public void testOneToManyBookRequiredUpdateValue(){
		// TODO
	}

	@Test
	public void l3CacheTest()
	{
		String COUNTRY = "Country";
		runAsSystem(() ->
		{
			EntityMetaData emd = EntityMetaData
					.newInstance(dataService.getEntityMetaData(entityMetaDataDynamic.getName()), DEEP_COPY_ATTRS);
			emd.addAttribute(attributeMetaDataFactory.create().setName(COUNTRY));
			dataService.getMeta().updateEntityMeta(emd);

			List<Entity> refEntities = testHarness.createTestRefEntities(refEntityMetaDataDynamic, 2);
			List<Entity> entities = testHarness.createTestEntities(entityMetaDataDynamic, 2, refEntities)
					.collect(toList());

			dataService.add(refEntityMetaDataDynamic.getName(), refEntities.stream());
			dataService.add(entityMetaDataDynamic.getName(), entities.stream());
			waitForIndexToBeStable(entityMetaDataDynamic.getName(), indexService, LOG);

			dataService.update(emd.getName(),
					StreamSupport.stream(dataService.findAll(emd.getName()).spliterator(), false).filter(e ->
					{
						e.set(COUNTRY, "NL" + e.getIdValue());
						return true;
					}));
		});
		waitForIndexToBeStable(entityMetaDataDynamic.getName(), indexService, LOG);

		Query<Entity> q0 = new QueryImpl<>().eq(COUNTRY, "NL0").or().eq(COUNTRY, "NL1");
		q0.pageSize(10); // The only reason to be cached l3, important!
		q0.sort(new Sort().on(COUNTRY));

		Repository repoQ0 = dataService.getRepository(entityMetaDataDynamic.getName());
		runAsSystem(() ->
		{
			List expected = dataService.findAll(repoQ0.getName(), q0).map(e -> e.getIdValue()).collect(toList());
			assertEquals(expected, Arrays.asList("0", "1"));
		});
	}
}

