package org.molgenis.integrationtest.platform;

import org.molgenis.data.*;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.elasticsearch.index.job.IndexService;
import org.molgenis.data.i18n.LanguageService;
import org.molgenis.data.i18n.model.I18nStringMetaData;
import org.molgenis.data.i18n.model.LanguageFactory;
import org.molgenis.data.i18n.model.LanguageMetadata;
import org.molgenis.data.index.IndexActionRegisterServiceImpl;
import org.molgenis.data.index.meta.IndexActionMetaData;
import org.molgenis.data.listeners.EntityListener;
import org.molgenis.data.listeners.EntityListenersService;
import org.molgenis.data.meta.IdentifierLookupService;
import org.molgenis.data.meta.MetaDataServiceImpl;
import org.molgenis.data.meta.model.*;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.test.data.EntitySelfXrefTestHarness;
import org.molgenis.test.data.EntityTestHarness;
import org.molgenis.test.data.staticentity.TestEntityStatic;
import org.molgenis.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;
import static org.molgenis.data.RepositoryCapability.*;
import static org.molgenis.data.i18n.model.I18nStringMetaData.I18N_STRING;
import static org.molgenis.data.i18n.model.LanguageMetadata.LANGUAGE;
import static org.molgenis.data.meta.model.AttributeMetadata.ATTRIBUTE_META_DATA;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;
import static org.molgenis.data.meta.model.PackageMetadata.PACKAGE;
import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;
import static org.molgenis.test.data.EntityTestHarness.*;
import static org.molgenis.util.MolgenisDateFormat.getDateFormat;
import static org.molgenis.util.MolgenisDateFormat.getDateTimeFormat;
import static org.testng.Assert.*;

@ContextConfiguration(classes = { PlatformITConfig.class })
public class PlatformIT extends AbstractTestNGSpringContextTests
{
	private final Logger LOG = LoggerFactory.getLogger(PlatformIT.class);

	private static EntityType entityTypeStatic;
	private static EntityType refEntityTypeStatic;
	private static EntityType entityTypeDynamic;
	private static EntityType refEntityTypeDynamic;
	private static EntityType selfXrefEntityType;

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
	private EntityListenersService entityListenersService;
	@Autowired
	private LanguageService languageService;
	@Autowired
	private I18nStringMetaData i18nStringMetaData;
	@Autowired
	private LanguageMetadata languageMetadata;
	@Autowired
	private EntityTypeMetadata entityTypeMetadata;
	@Autowired
	private AttributeMetadata attributeMetadata;
	@Autowired
	private LanguageFactory languageFactory;
	@Autowired
	private AttributeFactory attributeFactory;
	@Autowired
	private IndexActionRegisterServiceImpl indexActionRegisterService;
	@Autowired
	private IdentifierLookupService identifierLookupService;

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
		entityTypeDynamic = testHarness.createDynamicTestEntityType(refEntityTypeDynamic);

		// Create a self refer entity
		selfXrefEntityType = entitySelfXrefTestHarness.createDynamicEntityType();

		runAsSystem(() ->
		{
			addDefaultLanguages();
			metaDataService.addEntityType(refEntityTypeDynamic);
			metaDataService.addEntityType(entityTypeDynamic);
			metaDataService.addEntityType(selfXrefEntityType);
			entitySelfXrefTestHarness.addSelfReference(selfXrefEntityType);
			metaDataService.updateEntityType(selfXrefEntityType);
		});
		setAuthentication();
		waitForWorkToBeFinished(indexService, LOG);
	}

	static List<GrantedAuthority> makeAuthorities(String entityName, boolean write, boolean read, boolean count, IdentifierLookupService identifierLookupService)
	{
		List<GrantedAuthority> authorities = newArrayList();
		String entityId = identifierLookupService.getEntityTypeId(entityName);
		if (write) authorities.add(new SimpleGrantedAuthority("ROLE_ENTITY_WRITE_" + entityId));
		if (read) authorities.add(new SimpleGrantedAuthority("ROLE_ENTITY_READ_" + entityId));
		if (count) authorities.add(new SimpleGrantedAuthority("ROLE_ENTITY_COUNT_" + entityId));
		return authorities;
	}

	private void setAuthentication()
	{
		List<GrantedAuthority> authorities = newArrayList();

		authorities.add(new SimpleGrantedAuthority("ROLE_ENTITY_READ_" + ENTITY_TYPE_META_DATA));
		authorities.add(new SimpleGrantedAuthority("ROLE_ENTITY_READ_" + ATTRIBUTE_META_DATA));
		authorities.add(new SimpleGrantedAuthority("ROLE_ENTITY_READ_" + PACKAGE));
		authorities.addAll(makeAuthorities(refEntityTypeStatic.getFullyQualifiedName(), true, true, true, identifierLookupService));
		authorities.addAll(makeAuthorities(entityTypeStatic.getFullyQualifiedName(), true, true, true, identifierLookupService));
		authorities.addAll(makeAuthorities(entityTypeDynamic.getFullyQualifiedName(), true, true, true, identifierLookupService));
		authorities.addAll(makeAuthorities(refEntityTypeDynamic.getFullyQualifiedName(), false, true, true, identifierLookupService));
		authorities.addAll(makeAuthorities(selfXrefEntityType.getFullyQualifiedName(), true, true, true, identifierLookupService));
		authorities.addAll(makeAuthorities(languageMetadata.getFullyQualifiedName(), true, true, true, identifierLookupService));
		authorities.addAll(makeAuthorities(attributeMetadata.getFullyQualifiedName(), true, true, true, identifierLookupService));
		authorities.addAll(makeAuthorities(i18nStringMetaData.getFullyQualifiedName(), true, false, false, identifierLookupService));
		authorities.addAll(makeAuthorities(entityTypeMetadata.getFullyQualifiedName(), true, true, true, identifierLookupService));

		SecurityContextHolder.getContext()
				.setAuthentication(new TestingAuthenticationToken("user", "user", authorities));
	}

	@AfterMethod
	public void afterMethod()
	{
		runAsSystem(() ->
		{
			dataService.deleteAll(entityTypeStatic.getFullyQualifiedName());
			dataService.deleteAll(refEntityTypeStatic.getFullyQualifiedName());
			dataService.deleteAll(entityTypeDynamic.getFullyQualifiedName());
			dataService.deleteAll(refEntityTypeDynamic.getFullyQualifiedName());
			dataService.deleteAll(selfXrefEntityType.getFullyQualifiedName());
		});
		waitForIndexToBeStable(entityTypeStatic.getFullyQualifiedName(), indexService, LOG);
		waitForIndexToBeStable(refEntityTypeStatic.getFullyQualifiedName(), indexService, LOG);
		waitForIndexToBeStable(entityTypeDynamic.getFullyQualifiedName(), indexService, LOG);
		waitForIndexToBeStable(refEntityTypeDynamic.getFullyQualifiedName(), indexService, LOG);
		waitForIndexToBeStable(selfXrefEntityType.getFullyQualifiedName(), indexService, LOG);
	}

	private void addDefaultLanguages()
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
	}

	@Test(singleThreaded = true)
	public void testLanguageService()
	{
		assertEquals(dataService.getMeta().getEntityType(ENTITY_TYPE_META_DATA).getAttribute("labelEn").getName(),
				"labelEn");
		assertEquals(dataService.getMeta().getEntityType(ENTITY_TYPE_META_DATA).getLabelAttribute("en").getName(),
				"name");
		assertEquals(dataService.getMeta().getEntityType(ENTITY_TYPE_META_DATA).getLabelAttribute("pt").getName(),
				"name");
		assertEquals(dataService.getMeta().getEntityType(ENTITY_TYPE_META_DATA).getLabelAttribute("nl").getName(),
				"name");
		assertEquals(dataService.getMeta().getEntityType(ENTITY_TYPE_META_DATA).getLabelAttribute().getName(),
				"name");

		assertEquals(languageService.getCurrentUserLanguageCode(), "en");
		assertEqualsNoOrder(languageService.getLanguageCodes().toArray(),
				new String[] { "en", "nl", "de", "es", "it", "pt", "fr", "xx" });

		// NL
		assertNotNull(dataService.getEntityType(I18N_STRING).getAttribute("nl"));
		assertNotNull(dataService.getEntityType(ENTITY_TYPE_META_DATA).getAttribute("labelNl"));
		assertNotNull(dataService.getEntityType(ENTITY_TYPE_META_DATA).getAttribute("descriptionNl"));
		assertNotNull(dataService.getEntityType(ATTRIBUTE_META_DATA).getAttribute("labelNl"));
		assertNotNull(dataService.getEntityType(ATTRIBUTE_META_DATA).getAttribute("descriptionNl"));

		// EN
		assertNotNull(dataService.getEntityType(I18N_STRING).getAttribute("en"));
		assertNotNull(dataService.getEntityType(ENTITY_TYPE_META_DATA).getAttribute("labelEn"));
		assertNotNull(dataService.getEntityType(ENTITY_TYPE_META_DATA).getAttribute("descriptionEn"));
		assertNotNull(dataService.getEntityType(ATTRIBUTE_META_DATA).getAttribute("labelEn"));
		assertNotNull(dataService.getEntityType(ATTRIBUTE_META_DATA).getAttribute("descriptionEn"));

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

	@Test(singleThreaded = true)
	public void testEntityListener()
	{
		List<Entity> refEntities = testHarness.createTestRefEntities(refEntityTypeDynamic, 6);
		List<Entity> entities = testHarness.createTestEntities(entityTypeDynamic, 2, refEntities).collect(toList());
		runAsSystem(() ->
		{
			dataService.add(refEntityTypeDynamic.getFullyQualifiedName(), refEntities.stream());
			dataService.add(entityTypeDynamic.getFullyQualifiedName(), entities.stream());
			waitForIndexToBeStable(entityTypeDynamic.getFullyQualifiedName(), indexService, LOG);
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
			entityListenersService.addEntityListener(entityTypeDynamic.getFullyQualifiedName(), listener);
			dataService.update(entityTypeDynamic.getFullyQualifiedName(), entities.stream());
			assertEquals(updateCalled.get(), 1);
			waitForIndexToBeStable(entityTypeDynamic.getFullyQualifiedName(), indexService, LOG);
			assertPresent(entityTypeDynamic, entities);
		}
		finally
		{
			// Test that the listener is actually removed and not called anymore
			entityListenersService.removeEntityListener(entityTypeDynamic.getFullyQualifiedName(), listener);
			updateCalled.set(0);
			dataService.update(entityTypeDynamic.getFullyQualifiedName(), entities.stream());
			assertEquals(updateCalled.get(), 0);
			waitForIndexToBeStable(entityTypeDynamic.getFullyQualifiedName(), indexService, LOG);
			assertPresent(entityTypeDynamic, entities);
		}
	}

	@Test(singleThreaded = true)
	public void testAdd()
	{
		List<Entity> entities = createDynamic(2).collect(toList());
		assertEquals(searchService.count(entityTypeDynamic), 0);
		dataService.add(entityTypeDynamic.getFullyQualifiedName(), entities.stream());
		waitForIndexToBeStable(entityTypeDynamic.getFullyQualifiedName(), indexService, LOG);
		assertEquals(dataService.count(entityTypeDynamic.getFullyQualifiedName(), new QueryImpl<>()), 2);
		assertEquals(searchService.count(entityTypeDynamic), 2);
		assertPresent(entityTypeDynamic, entities);
	}

	@Test(singleThreaded = true)
	public void testCount()
	{
		List<Entity> entities = createDynamic(2).collect(toList());
		dataService.add(entityTypeDynamic.getFullyQualifiedName(), entities.stream());
		waitForIndexToBeStable(entityTypeDynamic.getFullyQualifiedName(), indexService, LOG);
		assertEquals(dataService.count(entityTypeDynamic.getFullyQualifiedName(), new QueryImpl<>()), 2);
		assertEquals(searchService.count(entityTypeDynamic), 2);
		assertPresent(entityTypeDynamic, entities);
	}

	@Test(singleThreaded = true)
	public void testDelete()
	{
		Entity entity = createDynamic(1).findFirst().get();
		dataService.add(entityTypeDynamic.getFullyQualifiedName(), entity);
		waitForIndexToBeStable(entityTypeDynamic.getFullyQualifiedName(), indexService, LOG);
		assertPresent(entityTypeDynamic, entity);

		dataService.delete(entityTypeDynamic.getFullyQualifiedName(), entity);
		waitForIndexToBeStable(entityTypeDynamic.getFullyQualifiedName(), indexService, LOG);
		assertNotPresent(entity);
	}

	@Test(singleThreaded = true)
	public void testDeleteById()
	{
		Entity entity = createDynamic(1).findFirst().get();
		dataService.add(entityTypeDynamic.getFullyQualifiedName(), entity);
		waitForIndexToBeStable(entityTypeDynamic.getFullyQualifiedName(), indexService, LOG);
		assertPresent(entityTypeDynamic, entity);

		dataService.deleteById(entityTypeDynamic.getFullyQualifiedName(), entity.getIdValue());
		waitForIndexToBeStable(entityTypeDynamic.getFullyQualifiedName(), indexService, LOG);
		assertNotPresent(entity);
	}

	@Test(singleThreaded = true)
	public void testDeleteStream()
	{
		List<Entity> entities = createDynamic(2).collect(toList());
		dataService.add(entityTypeDynamic.getFullyQualifiedName(), entities.stream());
		waitForIndexToBeStable(entityTypeDynamic.getFullyQualifiedName(), indexService, LOG);
		assertEquals(dataService.count(entityTypeDynamic.getFullyQualifiedName(), new QueryImpl<>()), entities.size());

		dataService.delete(entityTypeDynamic.getFullyQualifiedName(), entities.stream());
		waitForIndexToBeStable(entityTypeDynamic.getFullyQualifiedName(), indexService, LOG);
		assertEquals(dataService.count(entityTypeDynamic.getFullyQualifiedName(), new QueryImpl<>()), 0);
	}

	@Test(singleThreaded = true)
	public void testDeleteAll()
	{
		List<Entity> entities = createDynamic(5).collect(toList());
		dataService.add(entityTypeDynamic.getFullyQualifiedName(), entities.stream());
		waitForIndexToBeStable(entityTypeDynamic.getFullyQualifiedName(), indexService, LOG);
		assertEquals(dataService.count(entityTypeDynamic.getFullyQualifiedName(), new QueryImpl<>()), entities.size());

		dataService.deleteAll(entityTypeDynamic.getFullyQualifiedName());
		waitForIndexToBeStable(entityTypeDynamic.getFullyQualifiedName(), indexService, LOG);
		assertEquals(dataService.count(entityTypeDynamic.getFullyQualifiedName(), new QueryImpl<>()), 0);
	}

	@Test(singleThreaded = true)
	public void testFindAllEmpty()
	{
		Stream<Entity> retrieved = dataService.findAll(entityTypeDynamic.getFullyQualifiedName());
		assertEquals(retrieved.count(), 0);
	}

	@Test(singleThreaded = true)
	public void testFindAll()
	{
		List<Entity> entities = createDynamic(5).collect(toList());
		dataService.add(entityTypeDynamic.getFullyQualifiedName(), entities.stream());
		waitForIndexToBeStable(entityTypeDynamic.getFullyQualifiedName(), indexService, LOG);
		Stream<Entity> retrieved = dataService.findAll(entityTypeDynamic.getFullyQualifiedName());
		assertEquals(retrieved.count(), entities.size());
	}

	@Test(singleThreaded = true)
	public void testFindAllTyped()
	{
		List<Entity> entities = createDynamic(1).collect(toList());
		dataService.add(entityTypeDynamic.getFullyQualifiedName(), entities.stream());
		waitForIndexToBeStable(entityTypeDynamic.getFullyQualifiedName(), indexService, LOG);
		Supplier<Stream<Entity>> retrieved = () -> dataService
				.findAll(entityTypeDynamic.getFullyQualifiedName(), Entity.class);
		assertEquals(retrieved.get().count(), 1);
		assertEquals(retrieved.get().iterator().next().getIdValue(), entities.get(0).getIdValue());
	}

	@Test(singleThreaded = true)
	public void testFindAllByIds()
	{
		List<Entity> entities = createDynamic(5).collect(toList());
		dataService.add(entityTypeDynamic.getFullyQualifiedName(), entities.stream());
		waitForIndexToBeStable(entityTypeDynamic.getFullyQualifiedName(), indexService, LOG);
		Stream<Object> ids = Stream.concat(entities.stream().map(Entity::getIdValue), of("bogus"));
		Stream<Entity> retrieved = dataService.findAll(entityTypeDynamic.getFullyQualifiedName(), ids);
		assertEquals(retrieved.count(), entities.size());
	}

	@Test(singleThreaded = true)
	public void testFindAllByIdsTyped()
	{
		List<Entity> entities = createStatic(5).collect(toList());
		dataService.add(entityTypeStatic.getFullyQualifiedName(), entities.stream());
		waitForIndexToBeStable(entityTypeStatic.getFullyQualifiedName(), indexService, LOG);

		Supplier<Stream<TestEntityStatic>> retrieved = () -> dataService
				.findAll(entityTypeStatic.getFullyQualifiedName(),
						Stream.concat(entities.stream().map(Entity::getIdValue), of("bogus")), TestEntityStatic.class);
		assertEquals(retrieved.get().count(), entities.size());
		assertEquals(retrieved.get().iterator().next().getId(), entities.get(0).getIdValue());
		assertEquals(retrieved.get().iterator().next().getIdValue(), entities.get(0).getIdValue());
	}

	@Test(singleThreaded = true)
	public void testFindAllStreamFetch()
	{
		List<Entity> entities = createDynamic(5).collect(toList());
		dataService.add(entityTypeDynamic.getFullyQualifiedName(), entities.stream());
		waitForIndexToBeStable(entityTypeDynamic.getFullyQualifiedName(), indexService, LOG);
		Stream<Object> ids = concat(entities.stream().map(Entity::getIdValue), of("bogus"));
		Stream<Entity> retrieved = dataService
				.findAll(entityTypeDynamic.getFullyQualifiedName(), ids, new Fetch().field(ATTR_ID));
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

	@Test(singleThreaded = true, dataProvider = "findQueryOperatorEq")
	public void testFindQueryOperatorEq(String attrName, Object value, List<Integer> expectedEntityIndices)
	{
		List<Entity> entities = createDynamic(3).collect(toList());
		dataService.add(entityTypeDynamic.getFullyQualifiedName(), entities.stream());
		waitForIndexToBeStable(entityTypeDynamic.getFullyQualifiedName(), indexService, LOG);
		Supplier<Stream<Entity>> found = () -> dataService.query(entityTypeDynamic.getFullyQualifiedName())
				.eq(attrName, value).findAll();
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

	@Test(singleThreaded = true, dataProvider = "findQueryOperatorIn")
	public void testFindQueryOperatorIn(List<String> ids, List<Integer> expectedEntityIndices)
	{
		List<Entity> entities = createDynamic(2).collect(toList());
		dataService.add(entityTypeDynamic.getFullyQualifiedName(), entities.stream());
		waitForIndexToBeStable(entityTypeDynamic.getFullyQualifiedName(), indexService, LOG);
		Supplier<Stream<Entity>> found = () -> dataService.query(entityTypeDynamic.getFullyQualifiedName())
				.in(ATTR_ID, ids).findAll();
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

	@Test(singleThreaded = true, dataProvider = "findQueryOperatorLess")
	public void testFindQueryOperatorLess(int value, List<Integer> expectedEntityIndices)
	{
		List<Entity> entities = createDynamic(5).collect(toList());
		dataService.add(entityTypeDynamic.getFullyQualifiedName(), entities.stream());
		waitForIndexToBeStable(entityTypeDynamic.getFullyQualifiedName(), indexService, LOG);
		Supplier<Stream<Entity>> found = () -> dataService.query(entityTypeDynamic.getFullyQualifiedName())
				.lt(ATTR_INT, value).findAll();
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

	@Test(singleThreaded = true, dataProvider = "findQueryOperatorLessEqual")
	public void testFindQueryOperatorLessEqual(int value, List<Integer> expectedEntityIndices)
	{
		List<Entity> entities = createDynamic(5).collect(toList());
		dataService.add(entityTypeDynamic.getFullyQualifiedName(), entities.stream());
		waitForIndexToBeStable(entityTypeDynamic.getFullyQualifiedName(), indexService, LOG);
		Supplier<Stream<Entity>> found = () -> dataService.query(entityTypeDynamic.getFullyQualifiedName())
				.le(ATTR_INT, value).findAll();
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

	@Test(singleThreaded = true, dataProvider = "findQueryOperatorGreater")
	public void testFindQueryOperatorGreater(int value, List<Integer> expectedEntityIndices)
	{
		List<Entity> entities = createDynamic(3).collect(toList());
		dataService.add(entityTypeDynamic.getFullyQualifiedName(), entities.stream());
		waitForIndexToBeStable(entityTypeDynamic.getFullyQualifiedName(), indexService, LOG);
		Supplier<Stream<Entity>> found = () -> dataService.query(entityTypeDynamic.getFullyQualifiedName())
				.gt(ATTR_INT, value).findAll();
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

	@Test(singleThreaded = true, dataProvider = "findQueryOperatorGreaterEqual")
	public void testFindQueryOperatorGreaterEqual(int value, List<Integer> expectedEntityIndices)
	{
		List<Entity> entities = createDynamic(3).collect(toList());
		dataService.add(entityTypeDynamic.getFullyQualifiedName(), entities.stream());
		waitForIndexToBeStable(entityTypeDynamic.getFullyQualifiedName(), indexService, LOG);
		Supplier<Stream<Entity>> found = () -> dataService.query(entityTypeDynamic.getFullyQualifiedName())
				.ge(ATTR_INT, value).findAll();
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

	@Test(singleThreaded = true, dataProvider = "findQueryOperatorRange")
	public void testFindQueryOperatorRange(int low, int high, List<Integer> expectedEntityIndices)
	{
		List<Entity> entities = createDynamic(3).collect(toList());
		dataService.add(entityTypeDynamic.getFullyQualifiedName(), entities.stream());
		waitForIndexToBeStable(entityTypeDynamic.getFullyQualifiedName(), indexService, LOG);
		Supplier<Stream<Entity>> found = () -> dataService.query(entityTypeDynamic.getFullyQualifiedName())
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

	@Test(singleThreaded = true, dataProvider = "findQueryOperatorLike")
	public void testFindQueryOperatorLike(String likeStr, List<Integer> expectedEntityIndices)
	{
		List<Entity> entities = createDynamic(2).collect(toList());
		dataService.add(entityTypeDynamic.getFullyQualifiedName(), entities.stream());
		waitForIndexToBeStable(entityTypeDynamic.getFullyQualifiedName(), indexService, LOG);
		Supplier<Stream<Entity>> found = () -> dataService.query(entityTypeDynamic.getFullyQualifiedName())
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

	@Test(singleThreaded = true, dataProvider = "findQueryOperatorNot")
	public void testFindQueryOperatorNot(int value, List<Integer> expectedEntityIndices)
	{
		List<Entity> entities = createDynamic(3).collect(toList());
		dataService.add(entityTypeDynamic.getFullyQualifiedName(), entities.stream());
		waitForIndexToBeStable(entityTypeDynamic.getFullyQualifiedName(), indexService, LOG);
		Supplier<Stream<Entity>> found = () -> dataService.query(entityTypeDynamic.getFullyQualifiedName()).not()
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
	@Test(singleThreaded = true, enabled = false)
	public void cachePerformanceTest()
	{
		List<Entity> entities = createDynamic(10000).collect(toList());
		dataService.add(entityTypeDynamic.getFullyQualifiedName(), entities.stream());
		waitForIndexToBeStable(entityTypeDynamic.getFullyQualifiedName(), indexService, LOG);

		Query q1 = new QueryImpl<>().eq(EntityTestHarness.ATTR_STRING, "string1");
		q1.pageSize(1000);

		Query q2 = new QueryImpl<>().eq(EntityTestHarness.ATTR_BOOL, true);
		q2.pageSize(500);

		Query q3 = new QueryImpl<>().eq(ATTR_DECIMAL, 1.123);

		runAsSystem(() ->
		{
			for (int i = 0; i < 100000; i++)
			{
				dataService.findAll(entityTypeDynamic.getFullyQualifiedName(), q1);
				dataService.findAll(entityTypeDynamic.getFullyQualifiedName(), q2);
				dataService.findOne(entityTypeDynamic.getFullyQualifiedName(), q3);
			}
		});
	}

	@DataProvider(name = "findQueryOperatorAnd")
	private static Object[][] findQueryOperatorAnd()
	{
		return new Object[][] { { "string1", 10, asList(0) }, { "unknownString", 10, emptyList() },
				{ "string1", -1, emptyList() }, { "unknownString", -1, emptyList() } };
	}

	@Test(singleThreaded = true, dataProvider = "findQueryOperatorAnd")
	public void testFindQueryOperatorAnd(String strValue, int value, List<Integer> expectedEntityIndices)
	{
		List<Entity> entities = createDynamic(3).collect(toList());
		dataService.add(entityTypeDynamic.getFullyQualifiedName(), entities.stream());
		waitForIndexToBeStable(entityTypeDynamic.getFullyQualifiedName(), indexService, LOG);
		Supplier<Stream<Entity>> found = () -> dataService.query(entityTypeDynamic.getFullyQualifiedName())
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

	@Test(singleThreaded = true, dataProvider = "findQueryOperatorOr")
	public void testFindQueryOperatorOr(String strValue, int value, List<Integer> expectedEntityIndices)
	{
		List<Entity> entities = createDynamic(3).collect(toList());
		dataService.add(entityTypeDynamic.getFullyQualifiedName(), entities.stream());
		waitForIndexToBeStable(entityTypeDynamic.getFullyQualifiedName(), indexService, LOG);
		Supplier<Stream<Entity>> found = () -> dataService.query(entityTypeDynamic.getFullyQualifiedName())
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

	@Test(singleThreaded = true, dataProvider = "findQueryOperatorNested")
	public void testFindQueryOperatorNested(boolean boolValue, String strValue, int value,
			List<Integer> expectedEntityIndices)
	{
		List<Entity> entities = createDynamic(3).collect(toList());
		dataService.add(entityTypeDynamic.getFullyQualifiedName(), entities.stream());
		waitForIndexToBeStable(entityTypeDynamic.getFullyQualifiedName(), indexService, LOG);
		Supplier<Stream<Entity>> found = () -> dataService.query(entityTypeDynamic.getFullyQualifiedName())
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

	@Test(singleThreaded = true, dataProvider = "findQueryOperatorSearch")
	public void testFindQueryOperatorSearch(String searchStr, List<Integer> expectedEntityIndices)
	{
		List<Entity> entities = createDynamic(2).collect(toList());
		dataService.add(entityTypeDynamic.getFullyQualifiedName(), entities.stream());
		waitForIndexToBeStable(entityTypeDynamic.getFullyQualifiedName(), indexService, LOG);
		Supplier<Stream<Entity>> found = () -> dataService.query(entityTypeDynamic.getFullyQualifiedName())
				.search(ATTR_HTML, searchStr).findAll();
		List<Entity> foundAsList = found.get().collect(toList());
		assertEquals(foundAsList.size(), expectedEntityIndices.size());
		for (int i = 0; i < expectedEntityIndices.size(); ++i)
		{
			assertTrue(EntityUtils.equals(foundAsList.get(i), entities.get(expectedEntityIndices.get(i))));
		}
	}

	@Test(singleThreaded = true)
	public void testFindQueryLimit2_Offset2_sortOnInt()
	{
		List<Entity> testRefEntities = testHarness.createTestRefEntities(refEntityTypeDynamic, 6);
		List<Entity> testEntities = testHarness.createTestEntities(entityTypeDynamic, 10, testRefEntities)
				.collect(toList());
		runAsSystem(() ->
		{
			dataService.add(refEntityTypeDynamic.getFullyQualifiedName(), testRefEntities.stream());
			dataService.add(entityTypeDynamic.getFullyQualifiedName(), testEntities.stream());
		});
		waitForIndexToBeStable(refEntityTypeDynamic.getFullyQualifiedName(), indexService, LOG);
		waitForIndexToBeStable(entityTypeDynamic.getFullyQualifiedName(), indexService, LOG);
		Supplier<Stream<Entity>> found = () -> dataService.findAll(entityTypeDynamic.getFullyQualifiedName(),
				new QueryImpl<>().pageSize(2).offset(2).sort(new Sort(ATTR_ID, Sort.Direction.DESC)));
		List<Entity> foundAsList = found.get().collect(toList());
		assertEquals(foundAsList.size(), 2);
		assertTrue(EntityUtils.equals(foundAsList.get(0), testEntities.get(7)));
		assertTrue(EntityUtils.equals(foundAsList.get(1), testEntities.get(6)));
	}

	@Test(singleThreaded = true)
	public void testFindQueryTypedStatic()
	{
		List<Entity> entities = createStatic(5).collect(toList());
		dataService.add(entityTypeStatic.getFullyQualifiedName(), entities.stream());
		waitForIndexToBeStable(entityTypeStatic.getFullyQualifiedName(), indexService, LOG);
		Supplier<Stream<TestEntityStatic>> found = () -> dataService.findAll(entityTypeStatic.getFullyQualifiedName(),
				new QueryImpl<TestEntityStatic>().eq(ATTR_ID, entities.get(0).getIdValue()), TestEntityStatic.class);
		assertEquals(found.get().count(), 1);
		assertEquals(found.get().findFirst().get().getId(), entities.get(0).getIdValue());
	}

	@Test(singleThreaded = true)
	public void testFindOne()
	{
		Entity entity = createDynamic(1).findFirst().get();
		dataService.add(entityTypeDynamic.getFullyQualifiedName(), Stream.of(entity));
		waitForIndexToBeStable(entityTypeDynamic.getFullyQualifiedName(), indexService, LOG);
		assertNotNull(dataService.findOneById(entityTypeDynamic.getFullyQualifiedName(), entity.getIdValue()));
	}

	@Test(singleThreaded = true)
	public void testFindOneTypedStatic()
	{
		Entity entity = createStatic(1).findFirst().get();
		dataService.add(entityTypeStatic.getFullyQualifiedName(), Stream.of(entity));
		waitForIndexToBeStable(entityTypeStatic.getFullyQualifiedName(), indexService, LOG);
		TestEntityStatic testEntityStatic = dataService
				.findOneById(entityTypeStatic.getFullyQualifiedName(), entity.getIdValue(), TestEntityStatic.class);
		assertNotNull(testEntityStatic);
		assertEquals(testEntityStatic.getId(), entity.getIdValue());
	}

	@Test(singleThreaded = true)
	public void testFindOneFetch()
	{
		Entity entity = createDynamic(1).findFirst().get();
		dataService.add(entityTypeDynamic.getFullyQualifiedName(), Stream.of(entity));
		waitForIndexToBeStable(entityTypeDynamic.getFullyQualifiedName(), indexService, LOG);
		assertNotNull(dataService.findOneById(entityTypeDynamic.getFullyQualifiedName(), entity.getIdValue(),
				new Fetch().field(ATTR_ID)));
	}

	@Test(singleThreaded = true)
	public void testFindOneFetchTypedStatic()
	{
		TestEntityStatic entity = new TestEntityStatic(entityTypeStatic);
		entity.set(ATTR_ID, "1");
		entity.set(ATTR_STRING, "string1");
		entity.set(ATTR_BOOL, true);

		dataService.add(entityTypeStatic.getFullyQualifiedName(), Stream.of(entity));
		waitForIndexToBeStable(entityTypeStatic.getFullyQualifiedName(), indexService, LOG);
		TestEntityStatic testEntityStatic = dataService
				.findOneById(entityTypeStatic.getFullyQualifiedName(), entity.getIdValue(), new Fetch().field(ATTR_ID),
						TestEntityStatic.class);
		assertNotNull(testEntityStatic);
		assertEquals(testEntityStatic.getIdValue(), entity.getIdValue());
	}

	@Test(singleThreaded = true)
	public void testFindOneQuery()
	{
		Entity entity = createDynamic(1).findFirst().get();
		dataService.add(entityTypeDynamic.getFullyQualifiedName(), entity);
		waitForIndexToBeStable(entityTypeDynamic.getFullyQualifiedName(), indexService, LOG);
		entity = dataService
				.findOne(entityTypeDynamic.getFullyQualifiedName(), new QueryImpl<>().eq(ATTR_ID, entity.getIdValue()));
		assertNotNull(entity);
	}

	@Test(singleThreaded = true)
	public void testFindOneQueryTypedStatic()
	{
		Entity entity = createStatic(1).findFirst().get();
		dataService.add(entityTypeStatic.getFullyQualifiedName(), entity);
		waitForIndexToBeStable(entityTypeStatic.getFullyQualifiedName(), indexService, LOG);
		TestEntityStatic testEntityStatic = dataService.findOne(entityTypeStatic.getFullyQualifiedName(),
				new QueryImpl<TestEntityStatic>().eq(ATTR_ID, entity.getIdValue()), TestEntityStatic.class);
		assertNotNull(testEntityStatic);
		assertEquals(testEntityStatic.getId(), entity.getIdValue());
	}

	@Test(singleThreaded = true)
	public void testGetCapabilities()
	{
		Set<RepositoryCapability> capabilities = dataService.getCapabilities(entityTypeDynamic.getFullyQualifiedName());
		assertNotNull(capabilities);
		assertTrue(capabilities.containsAll(asList(MANAGABLE, QUERYABLE, WRITABLE, VALIDATE_REFERENCE_CONSTRAINT)));
	}

	@Test(singleThreaded = true)
	public void testGetEntityType()
	{
		EntityType entityType = dataService.getEntityType(entityTypeDynamic.getFullyQualifiedName());
		assertNotNull(entityType);
		assertTrue(EntityUtils.equals(entityType, entityTypeDynamic));
	}

	@Test(singleThreaded = true)
	public void testGetEntityNames()
	{
		Stream<String> names = dataService.getEntityNames();
		assertNotNull(names);
		assertTrue(names.filter(entityTypeDynamic.getFullyQualifiedName()::equals).findFirst().isPresent());
	}

	@Test(singleThreaded = true)
	public void testGetMeta()
	{
		assertNotNull(dataService.getMeta());
	}

	@Test(singleThreaded = true)
	public void testGetKnownRepository()
	{
		Repository<Entity> repo = dataService.getRepository(entityTypeDynamic.getFullyQualifiedName());
		assertNotNull(repo);
		assertEquals(repo.getName(), entityTypeDynamic.getFullyQualifiedName());
	}

	@Test(singleThreaded = true, expectedExceptions = UnknownEntityException.class)
	public void testGetUnknownRepository()
	{
		dataService.getRepository("bogus");
	}

	@Test(singleThreaded = true)
	public void testHasRepository()
	{
		assertTrue(dataService.hasRepository(entityTypeDynamic.getFullyQualifiedName()));
		assertFalse(dataService.hasRepository("bogus"));
	}

	@Test(singleThreaded = true)
	public void testIterator()
	{
		assertNotNull(dataService.iterator());
		StreamSupport.stream(dataService.spliterator(), false).forEach(repo -> LOG.info(repo.getName()));
		Repository repo = dataService.getRepository(entityTypeDynamic.getFullyQualifiedName());

		/*
			Repository equals is not implemented. The repository from dataService
			and from the dataService.getRepository are not the same instances.
		*/
		assertTrue(StreamSupport.stream(dataService.spliterator(), false)
				.anyMatch(e -> repo.getName().equals(e.getName())));
	}

	@Test(singleThreaded = true)
	public void testQuery()
	{
		assertNotNull(dataService.query(entityTypeDynamic.getFullyQualifiedName()));
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

	@Test(singleThreaded = true)
	public void testUpdate()
	{
		Entity entity = createDynamic(1).findFirst().get();
		dataService.add(entityTypeDynamic.getFullyQualifiedName(), entity);
		waitForIndexToBeStable(entityTypeDynamic.getFullyQualifiedName(), indexService, LOG);

		entity = dataService.findOneById(entityTypeDynamic.getFullyQualifiedName(), entity.getIdValue());
		assertNotNull(entity);
		assertEquals(entity.get(ATTR_STRING), "string1");

		Query<Entity> q = new QueryImpl<>();
		q.eq(ATTR_STRING, "qwerty");
		entity.set(ATTR_STRING, "qwerty");

		assertEquals(searchService.count(q, entityTypeDynamic), 0);
		dataService.update(entityTypeDynamic.getFullyQualifiedName(), entity);
		waitForIndexToBeStable(entityTypeDynamic.getFullyQualifiedName(), indexService, LOG);
		assertEquals(searchService.count(q, entityTypeDynamic), 1);

		assertPresent(entityTypeDynamic, entity);

		entity = dataService.findOneById(entityTypeDynamic.getFullyQualifiedName(), entity.getIdValue());
		assertNotNull(entity.get(ATTR_STRING));
		assertEquals(entity.get(ATTR_STRING), "qwerty");
	}

	@Test(singleThreaded = true)
	public void testUpdateSingleRefEntityIndexesReferencingEntities()
	{
		dataService.add(entityTypeDynamic.getFullyQualifiedName(), createDynamic(30));
		waitForIndexToBeStable(entityTypeDynamic.getFullyQualifiedName(), indexService, LOG);

		Entity refEntity4 = dataService.findOneById(refEntityTypeDynamic.getFullyQualifiedName(), "4");

		Query<Entity> q = new QueryImpl<>().search("refstring4");

		assertEquals(searchService.count(q, entityTypeDynamic), 5);
		refEntity4.set(ATTR_REF_STRING, "qwerty");
		runAsSystem(() -> dataService.update(refEntityTypeDynamic.getFullyQualifiedName(), refEntity4));
		waitForIndexToBeStable(entityTypeDynamic.getFullyQualifiedName(), indexService, LOG);
		assertEquals(searchService.count(q, entityTypeDynamic), 0);
		assertEquals(searchService.count(new QueryImpl<>().search("qwerty"), entityTypeDynamic), 5);
	}

	@Test(singleThreaded = true, enabled = false) //FIXME: sys_md_attributes spam
	public void testUpdateSingleRefEntityIndexesLargeAmountOfReferencingEntities()
	{
		dataService.add(entityTypeDynamic.getFullyQualifiedName(), createDynamic(10000));
		waitForIndexToBeStable(entityTypeDynamic.getFullyQualifiedName(), indexService, LOG);

		Query<Entity> q = new QueryImpl<>().search("refstring4").or().search("refstring5");

		assertEquals(searchService.count(q, entityTypeDynamic), 3333);
		Entity refEntity4 = dataService.findOneById(refEntityTypeDynamic.getFullyQualifiedName(), "4");
		refEntity4.set(ATTR_REF_STRING, "qwerty");
		runAsSystem(() -> dataService.update(refEntityTypeDynamic.getFullyQualifiedName(), refEntity4));

		Entity refEntity5 = dataService.findOneById(refEntityTypeDynamic.getFullyQualifiedName(), "5");
		refEntity5.set(ATTR_REF_STRING, "qwerty");
		runAsSystem(() -> dataService.update(refEntityTypeDynamic.getFullyQualifiedName(), refEntity5));

		waitForIndexToBeStable(entityTypeDynamic.getFullyQualifiedName(), indexService, LOG);
		assertEquals(searchService.count(q, entityTypeDynamic), 0);

		assertEquals(searchService.count(new QueryImpl<>().search("qwerty"), entityTypeDynamic), 3333);
	}

	@Test(singleThreaded = true)
	public void testUpdateStream()
	{
		Entity entity = createDynamic(1).findFirst().get();

		dataService.add(entityTypeDynamic.getFullyQualifiedName(), entity);
		waitForIndexToBeStable(entityTypeDynamic.getFullyQualifiedName(), indexService, LOG);
		assertPresent(entityTypeDynamic, entity);

		entity = dataService.findOneById(entityTypeDynamic.getFullyQualifiedName(), entity.getIdValue());
		assertNotNull(entity);
		assertEquals(entity.get(ATTR_STRING), "string1");

		entity.set(ATTR_STRING, "qwerty");
		Query<Entity> q = new QueryImpl<>();
		q.eq(ATTR_STRING, "qwerty");

		assertEquals(searchService.count(q, entityTypeDynamic), 0);

		dataService.update(entityTypeDynamic.getFullyQualifiedName(), of(entity));
		waitForIndexToBeStable(entityTypeDynamic.getFullyQualifiedName(), indexService, LOG);

		assertEquals(searchService.count(q, entityTypeDynamic), 1);

		assertPresent(entityTypeDynamic, entity);
		entity = dataService.findOneById(entityTypeDynamic.getFullyQualifiedName(), entity.getIdValue());
		assertNotNull(entity.get(ATTR_STRING));
		assertEquals(entity.get(ATTR_STRING), "qwerty");
	}

	private Stream<Entity> createDynamic(int count)
	{
		List<Entity> refEntities = testHarness.createTestRefEntities(refEntityTypeDynamic, 6);
		runAsSystem(() -> dataService.add(refEntityTypeDynamic.getFullyQualifiedName(), refEntities.stream()));
		return testHarness.createTestEntities(entityTypeDynamic, count, refEntities);
	}

	private Stream<Entity> createStatic(int count)
	{
		List<Entity> refEntities = testHarness.createTestRefEntities(refEntityTypeStatic, 6);
		runAsSystem(() -> dataService.add(refEntityTypeStatic.getFullyQualifiedName(), refEntities.stream()));
		return testHarness.createTestEntities(entityTypeStatic, count, refEntities);
	}

	private void assertPresent(EntityType emd, List<Entity> entities)
	{
		entities.forEach(e -> assertPresent(emd, e));
	}

	private void assertPresent(EntityType emd, Entity entity)
	{
		// Found in PostgreSQL
		assertNotNull(dataService.findOneById(emd.getFullyQualifiedName(), entity.getIdValue()));

		// Found in index Elasticsearch
		Query<Entity> q = new QueryImpl<>();
		q.eq(emd.getIdAttribute().getName(), entity.getIdValue());
		assertEquals(searchService.count(q, emd), 1);
	}

	private void assertNotPresent(Entity entity)
	{
		// Found in PostgreSQL
		assertNull(dataService.findOneById(entityTypeDynamic.getFullyQualifiedName(), entity.getIdValue()));

		// Not found in index Elasticsearch
		Query<Entity> q = new QueryImpl<>();
		q.eq(entityTypeDynamic.getIdAttribute().getName(), entity.getIdValue());
		assertEquals(searchService.count(q, entityTypeDynamic), 0);
	}

	@Test(singleThreaded = true)
	public void testCreateSelfXref()
	{
		Entity entitySelfXref = entitySelfXrefTestHarness.createTestEntities(selfXrefEntityType, 1).collect(toList())
				.get(0);

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

	@Test(singleThreaded = true)
	public void testIndexCreateMetaData()
	{
		IndexMetadataCUDOperationsPlatformIT
				.testIndexCreateMetaData(searchService, entityTypeStatic, entityTypeDynamic, metaDataService);
	}

	@Test(singleThreaded = true)
	public void testIndexDeleteMetaData()
	{
		IndexMetadataCUDOperationsPlatformIT
				.testIndexDeleteMetaData(searchService, dataService, entityTypeDynamic, metaDataService, indexService);
	}

	@Test(singleThreaded = true)
	public void testIndexUpdateMetaDataUpdateAttribute()
	{
		IndexMetadataCUDOperationsPlatformIT
				.testIndexUpdateMetaDataUpdateAttribute(searchService, entityTypeDynamic, metaDataService,
						indexService);
	}

	@Test(singleThreaded = true)
	public void testIndexUpdateMetaDataRemoveAttribute()
	{
		IndexMetadataCUDOperationsPlatformIT
				.testIndexUpdateMetaDataRemoveAttribute(entityTypeDynamic, EntityTestHarness.ATTR_CATEGORICAL,
						searchService, metaDataService, indexService);

		IndexMetadataCUDOperationsPlatformIT
				.testIndexUpdateMetaDataRemoveAttribute(entityTypeDynamic, EntityTestHarness.ATTR_BOOL, searchService,
						metaDataService, indexService);

		IndexMetadataCUDOperationsPlatformIT
				.testIndexUpdateMetaDataRemoveAttribute(entityTypeDynamic, EntityTestHarness.ATTR_DATE, searchService,
						metaDataService, indexService);

		IndexMetadataCUDOperationsPlatformIT
				.testIndexUpdateMetaDataRemoveAttribute(entityTypeDynamic, EntityTestHarness.ATTR_XREF, searchService,
						metaDataService, indexService);

		IndexMetadataCUDOperationsPlatformIT
				.testIndexUpdateMetaDataRemoveAttribute(entityTypeDynamic, EntityTestHarness.ATTR_DATETIME,
						searchService, metaDataService, indexService);

		IndexMetadataCUDOperationsPlatformIT
				.testIndexUpdateMetaDataRemoveAttribute(entityTypeDynamic, EntityTestHarness.ATTR_DECIMAL,
						searchService, metaDataService, indexService);

		IndexMetadataCUDOperationsPlatformIT
				.testIndexUpdateMetaDataRemoveAttribute(entityTypeDynamic, EntityTestHarness.ATTR_EMAIL, searchService,
						metaDataService, indexService);

		IndexMetadataCUDOperationsPlatformIT
				.testIndexUpdateMetaDataRemoveAttribute(entityTypeDynamic, EntityTestHarness.ATTR_HTML, searchService,
						metaDataService, indexService);

		IndexMetadataCUDOperationsPlatformIT
				.testIndexUpdateMetaDataRemoveAttribute(entityTypeDynamic, EntityTestHarness.ATTR_INT, searchService,
						metaDataService, indexService);

		IndexMetadataCUDOperationsPlatformIT
				.testIndexUpdateMetaDataRemoveAttribute(entityTypeDynamic, EntityTestHarness.ATTR_HYPERLINK,
						searchService, metaDataService, indexService);

		IndexMetadataCUDOperationsPlatformIT
				.testIndexUpdateMetaDataRemoveAttribute(entityTypeDynamic, EntityTestHarness.ATTR_COMPOUND,
						searchService, metaDataService, indexService);
	}

	// Derived from fix: https://github.com/molgenis/molgenis/issues/5227
	@Test(singleThreaded = true)
	public void testIndexBatchUpdate()
	{
		List<Entity> refEntities = testHarness.createTestRefEntities(refEntityTypeDynamic, 2);
		List<Entity> entities = testHarness.createTestEntities(entityTypeDynamic, 2, refEntities).collect(toList());
		runAsSystem(() ->
		{
			dataService.add(refEntityTypeDynamic.getFullyQualifiedName(), refEntities.stream());
			dataService.add(entityTypeDynamic.getFullyQualifiedName(), entities.stream());
			waitForIndexToBeStable(entityTypeDynamic.getFullyQualifiedName(), indexService, LOG);
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
			dataService.update(refEntityTypeDynamic.getFullyQualifiedName(), refEntities.stream());
			waitForIndexToBeStable(entityTypeDynamic.getFullyQualifiedName(), indexService, LOG);
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

	/**
	 * Test add and remove of a single attribute of a dynamic entity
	 */
	@Test(singleThreaded = true)
	public void addAndDeleteSingleAttribute()
	{
		final String NEW_ATTRIBUTE = "new_attribute";
		Attribute newAttr = attributeFactory.create().setName(NEW_ATTRIBUTE);
		EntityType entityType = dataService.getEntityType(entityTypeDynamic.getFullyQualifiedName());
		newAttr.setEntity(entityType);

		runAsSystem(() ->
		{
			dataService.getMeta().addAttribute(newAttr);

			List<Entity> refEntities = testHarness.createTestRefEntities(refEntityTypeDynamic, 2);
			List<Entity> entities = testHarness.createTestEntities(entityTypeDynamic, 2, refEntities).collect(toList());

			dataService.add(refEntityTypeDynamic.getFullyQualifiedName(), refEntities.stream());
			dataService.add(entityTypeDynamic.getFullyQualifiedName(), entities.stream());
			waitForIndexToBeStable(entityTypeDynamic.getFullyQualifiedName(), indexService, LOG);

			dataService.update(entityType.getFullyQualifiedName(),
					StreamSupport.stream(dataService.findAll(entityType.getFullyQualifiedName()).spliterator(), false)
							.peek(e -> e.set(NEW_ATTRIBUTE, "NEW_ATTRIBUTE_" + e.getIdValue())));
		});
		waitForIndexToBeStable(entityTypeDynamic.getFullyQualifiedName(), indexService, LOG);

		// Tunnel via L3 flow
		Query<Entity> q0 = new QueryImpl<>().eq(NEW_ATTRIBUTE, "NEW_ATTRIBUTE_0").or()
				.eq(NEW_ATTRIBUTE, "NEW_ATTRIBUTE_1");
		q0.pageSize(10); // L3 only caches queries with a page size
		q0.sort(new Sort().on(NEW_ATTRIBUTE));

		runAsSystem(() ->
		{
			List expected = dataService.findAll(entityTypeDynamic.getFullyQualifiedName(), q0).map(Entity::getIdValue)
					.collect(toList());
			assertEquals(expected, Arrays.asList("0", "1"));

			// Remove added attribute
			dataService.getMeta().deleteAttributeById(newAttr.getIdValue());
			waitForIndexToBeStable(entityTypeDynamic.getFullyQualifiedName(), indexService, LOG);
		});

		// verify attribute is deleted by adding and removing it again
		runAsSystem(() ->
		{
			// Add attribute
			dataService.getMeta().addAttribute(newAttr);

			// Delete attribute
			dataService.getMeta().deleteAttributeById(newAttr.getIdValue());
			waitForIndexToBeStable(entityTypeDynamic.getFullyQualifiedName(), indexService, LOG);
		});
	}

	/**
	 * Test add stream attribute of a dynamic entity
	 */
	@Test(singleThreaded = true)
	public void addStreamAttribute()
	{
		final String NEW_ATTRIBUTE = "new_attribute";
		Attribute newAttr = attributeFactory.create().setName(NEW_ATTRIBUTE);
		EntityType entityType = dataService.getEntityType(entityTypeDynamic.getFullyQualifiedName());
		newAttr.setEntity(entityType);

		runAsSystem(() ->
		{
			dataService.getMeta().addAttributes(entityType.getFullyQualifiedName(), Stream.of(newAttr));
			waitForIndexToBeStable(entityTypeDynamic.getFullyQualifiedName(), indexService, LOG);

			Attribute attribute = dataService.findOneById(ATTRIBUTE_META_DATA, newAttr.getIdValue(), Attribute.class);
			assertNotNull(attribute);

			// Tunnel via L3 flow
			Query<Entity> q0 = new QueryImpl<>().eq(NEW_ATTRIBUTE, "NEW_ATTRIBUTE_0").or()
					.eq(NEW_ATTRIBUTE, "NEW_ATTRIBUTE_1");
			q0.pageSize(10); // L3 only caches queries with a page size
			q0.sort(new Sort().on(NEW_ATTRIBUTE));

			List expected = dataService.findAll(entityTypeDynamic.getFullyQualifiedName(), q0).map(Entity::getIdValue)
					.collect(toList());
			assertEquals(expected, Arrays.asList());

			// Remove added attribute
			dataService.getMeta().deleteAttributeById(newAttr.getIdValue());
			waitForIndexToBeStable(entityTypeDynamic.getFullyQualifiedName(), indexService, LOG);
		});
	}

	/**
	 * Test update of a single attribute of a dynamic entity
	 */
	@Test(singleThreaded = true)
	public void updateAttribute()
	{
		final String NEW_ATTRIBUTE = "new_attribute";
		Attribute newAttr = attributeFactory.create().setName(NEW_ATTRIBUTE);
		EntityType entityType = dataService.getEntityType(entityTypeDynamic.getFullyQualifiedName());
		newAttr.setEntity(entityType);

		// Add attribute
		runAsSystem(() ->
		{
			dataService.getMeta().addAttribute(newAttr);

			List<Entity> refEntities = testHarness.createTestRefEntities(refEntityTypeDynamic, 2);
			List<Entity> entities = testHarness.createTestEntities(entityTypeDynamic, 2, refEntities).collect(toList());

			dataService.add(refEntityTypeDynamic.getFullyQualifiedName(), refEntities.stream());
			dataService.add(entityTypeDynamic.getFullyQualifiedName(), entities.stream());
			waitForIndexToBeStable(entityTypeDynamic.getFullyQualifiedName(), indexService, LOG);

			dataService.update(entityType.getFullyQualifiedName(),
					StreamSupport.stream(dataService.findAll(entityType.getFullyQualifiedName()).spliterator(), false)
							.peek(e -> e.set(NEW_ATTRIBUTE, "NEW_ATTRIBUTE_" + e.getIdValue())));
		});
		waitForIndexToBeStable(entityTypeDynamic.getFullyQualifiedName(), indexService, LOG);

		// Verify old values
		assertNotEquals(newAttr.getSequenceNumber(), 0);
		assertFalse(newAttr.isReadOnly());
		assertFalse(newAttr.isUnique());
		assertNotEquals(newAttr.getLabel(), "test");
		assertNotEquals(newAttr.getDescription(), "test");
		assertTrue(newAttr.isNillable());

		// New values
		newAttr.setSequenceNumber(0);
		newAttr.setReadOnly(true);
		newAttr.setUnique(true);
		newAttr.setLabel("test");
		newAttr.setNillable(false);
		newAttr.setDescription("test");

		// Update attribute
		runAsSystem(() ->
		{
			// Update added attribute
			dataService.update(ATTRIBUTE_META_DATA, newAttr);
			waitForIndexToBeStable(entityTypeDynamic.getFullyQualifiedName(), indexService, LOG);
		});

		Attribute attr = dataService.findOneById(ATTRIBUTE_META_DATA, newAttr.getIdValue(), Attribute.class);
		assertEquals(attr.getSequenceNumber(), Integer.valueOf(0));
		assertTrue(attr.isReadOnly());
		assertTrue(attr.isUnique());
		assertEquals(attr.getLabel(), "test");
		assertEquals(attr.getDescription(), "test");

		// Delete attribute
		runAsSystem(() ->
		{
			// Remove added attribute
			dataService.getMeta().deleteAttributeById(newAttr.getIdValue());
			waitForIndexToBeStable(entityTypeDynamic.getFullyQualifiedName(), indexService, LOG);
		});
	}

	/*
	 * Test add and remove of a single attribute of a dynamic entity
	 */
	@Test(singleThreaded = true)
	public void addAndDeleteSingleAttributeStream()
	{
		final String NEW_ATTRIBUTE = "new_attribute";
		Attribute newAttr = attributeFactory.create().setName(NEW_ATTRIBUTE);
		EntityType entityType = dataService.getEntityType(entityTypeDynamic.getFullyQualifiedName());
		newAttr.setEntity(entityType);
		newAttr.setSequenceNumber(2);
		entityType.addAttribute(newAttr);

		assertEquals(newAttr.getSequenceNumber(), Integer.valueOf(2)); // Test if sequence number is 2

		runAsSystem(() ->
		{
			dataService.update(ENTITY_TYPE_META_DATA, Stream.of(entityType)); // Adds the column to the table
			dataService.add(ATTRIBUTE_META_DATA, Stream.of(newAttr));

			List<Entity> refEntities = testHarness.createTestRefEntities(refEntityTypeDynamic, 2);
			List<Entity> entities = testHarness.createTestEntities(entityTypeDynamic, 2, refEntities).collect(toList());

			dataService.add(refEntityTypeDynamic.getFullyQualifiedName(), refEntities.stream());
			dataService.add(entityTypeDynamic.getFullyQualifiedName(), entities.stream());
			waitForIndexToBeStable(entityTypeDynamic.getFullyQualifiedName(), indexService, LOG);

			dataService.update(entityType.getFullyQualifiedName(),
					StreamSupport.stream(dataService.findAll(entityType.getFullyQualifiedName()).spliterator(), false)
							.peek(e -> e.set(NEW_ATTRIBUTE, "NEW_ATTRIBUTE_" + e.getIdValue())));
		});
		waitForIndexToBeStable(entityTypeDynamic.getFullyQualifiedName(), indexService, LOG);

		// Tunnel via L3 flow
		Query<Entity> q0 = new QueryImpl<>().eq(NEW_ATTRIBUTE, "NEW_ATTRIBUTE_0").or()
				.eq(NEW_ATTRIBUTE, "NEW_ATTRIBUTE_1");
		q0.pageSize(10); // L3 only caches queries with a page size
		q0.sort(new Sort().on(NEW_ATTRIBUTE));

		runAsSystem(() ->
		{
			List expected = dataService.findAll(entityTypeDynamic.getFullyQualifiedName(), q0).map(Entity::getIdValue)
					.collect(toList());
			assertEquals(expected, Arrays.asList("0", "1"));

			// Remove added attribute
			entityType.removeAttribute(newAttr);
			dataService.update(ENTITY_TYPE_META_DATA, Stream.of(entityType));
			dataService.delete(ATTRIBUTE_META_DATA, Stream.of(newAttr));
			waitForIndexToBeStable(entityTypeDynamic.getFullyQualifiedName(), indexService, LOG);
		});

		// verify attribute is deleted by adding and removing it again
		runAsSystem(() ->
		{
			entityType.addAttribute(newAttr);
			dataService.update(ENTITY_TYPE_META_DATA, Stream.of(entityType));
			dataService.add(ATTRIBUTE_META_DATA, Stream.of(newAttr));

			// Remove added attribute
			entityType.removeAttribute(newAttr);
			dataService.update(ENTITY_TYPE_META_DATA, Stream.of(entityType));
			dataService.delete(ATTRIBUTE_META_DATA, Stream.of(newAttr));
			waitForIndexToBeStable(entityTypeDynamic.getFullyQualifiedName(), indexService, LOG);
		});
	}

	@Test(singleThreaded = true)
	@Transactional
	public void storeIndexActions()
	{
		List<Entity> refEntities = testHarness.createTestRefEntities(refEntityTypeDynamic, 2);
		List<Entity> entities = testHarness.createTestEntities(entityTypeDynamic, 2, refEntities).collect(toList());
		runAsSystem(() ->
		{
			dataService.add(refEntityTypeDynamic.getFullyQualifiedName(), refEntities.stream());
			dataService.add(entityTypeDynamic.getFullyQualifiedName(), entities.stream());
			waitForIndexToBeStable(entityTypeDynamic.getFullyQualifiedName(), indexService, LOG);

			indexActionRegisterService.register(entityTypeDynamic, "1");
			indexActionRegisterService.register(entityTypeDynamic, null);

			Query q = new QueryImpl();
			q.eq(IndexActionMetaData.ENTITY_FULL_NAME, "sys_test_TypeTestDynamic");
			Stream<org.molgenis.data.index.meta.IndexAction> all = dataService
					.findAll(IndexActionMetaData.INDEX_ACTION, q);
			all.forEach(e ->
			{
				LOG.info(e.getEntityFullName() + "." + e.getEntityId());
			});
			waitForIndexToBeStable(entityTypeDynamic.getFullyQualifiedName(), indexService, LOG);
		});
	}
}
