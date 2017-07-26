package org.molgenis.integrationtest.platform;

import org.molgenis.data.*;
import org.molgenis.data.aggregation.AggregateQuery;
import org.molgenis.data.aggregation.AggregateResult;
import org.molgenis.data.elasticsearch.ElasticsearchService;
import org.molgenis.data.i18n.LanguageService;
import org.molgenis.data.i18n.model.*;
import org.molgenis.data.index.IndexActionRegisterServiceImpl;
import org.molgenis.data.index.job.IndexJobScheduler;
import org.molgenis.data.index.meta.IndexAction;
import org.molgenis.data.index.meta.IndexActionMetaData;
import org.molgenis.data.listeners.EntityListener;
import org.molgenis.data.listeners.EntityListenersService;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.MetaDataServiceImpl;
import org.molgenis.data.meta.model.*;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.staticentity.TestEntityStatic;
import org.molgenis.data.support.AggregateQueryImpl;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.validation.MolgenisValidationException;
import org.molgenis.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.*;

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
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;
import static org.molgenis.data.EntityTestHarness.*;
import static org.molgenis.data.RepositoryCapability.*;
import static org.molgenis.data.i18n.model.L10nStringMetaData.L10N_STRING;
import static org.molgenis.data.i18n.model.LanguageMetadata.LANGUAGE;
import static org.molgenis.data.meta.model.AttributeMetadata.ATTRIBUTE_META_DATA;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;
import static org.molgenis.data.meta.model.PackageMetadata.PACKAGE;
import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;
import static org.molgenis.util.MolgenisDateFormat.parseInstant;
import static org.molgenis.util.MolgenisDateFormat.parseLocalDate;
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
	private IndexJobScheduler indexService;
	@Autowired
	private EntityTestHarness testHarness;
	@Autowired
	private EntitySelfXrefTestHarness entitySelfXrefTestHarness;
	@Autowired
	private DataService dataService;
	@Autowired
	private ElasticsearchService searchService;
	@Autowired
	private MetaDataServiceImpl metaDataService;
	@Autowired
	private EntityListenersService entityListenersService;
	@Autowired
	private LanguageService languageService;
	@Autowired
	private L10nStringMetaData l10nStringMetaData;
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
	private L10nStringFactory l10nStringFactory;
	@Autowired
	private PackageFactory packageFactory;
	@Autowired
	private UserDetailsService userDetailsService;
	@Autowired
	private EntityTypeFactory entityTypeFactory;

	/**
	 * Wait till the whole index is stable. Index job is done a-synchronized.
	 */
	static void waitForWorkToBeFinished(IndexJobScheduler indexService, Logger log)
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
	 * @param entityType name of the entity whose index needs to be stable
	 */
	static void waitForIndexToBeStable(EntityType entityType, IndexJobScheduler indexService, Logger log)
	{
		try
		{
			indexService.waitForIndexToBeStableIncludingReferences(entityType);
			log.info("Index for entity [{}] incl. references is stable", entityType.getId());
		}
		catch (InterruptedException e)
		{
			log.info("Interrupted waiting for [{}] incl. references to become stable", entityType.getId(), e);
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

	@AfterClass
	public void tearDown()
	{
		entityTypeDynamic = dataService.getEntityType(entityTypeDynamic.getId());
		refEntityTypeDynamic = dataService.getEntityType(refEntityTypeDynamic.getId());
		selfXrefEntityType = dataService.getEntityType(selfXrefEntityType.getId());

		runAsSystem(() -> metaDataService.deleteEntityType(
				asList(refEntityTypeDynamic, entityTypeDynamic, selfXrefEntityType)));
	}

	static List<GrantedAuthority> makeAuthorities(String entityTypeId, boolean write, boolean read, boolean count)
	{
		List<GrantedAuthority> authorities = newArrayList();
		if (write) authorities.add(new SimpleGrantedAuthority("ROLE_ENTITY_WRITE_" + entityTypeId));
		if (read) authorities.add(new SimpleGrantedAuthority("ROLE_ENTITY_READ_" + entityTypeId));
		if (count) authorities.add(new SimpleGrantedAuthority("ROLE_ENTITY_COUNT_" + entityTypeId));
		return authorities;
	}

	private void setAuthentication()
	{
		List<GrantedAuthority> authorities = newArrayList();

		authorities.add(new SimpleGrantedAuthority("ROLE_ENTITY_READ_" + ENTITY_TYPE_META_DATA));
		authorities.add(new SimpleGrantedAuthority("ROLE_ENTITY_READ_" + ATTRIBUTE_META_DATA));
		authorities.add(new SimpleGrantedAuthority("ROLE_ENTITY_READ_" + PACKAGE));
		authorities.addAll(makeAuthorities(refEntityTypeStatic.getId(), true, true, true));
		authorities.addAll(makeAuthorities(entityTypeStatic.getId(), true, true, true));
		authorities.addAll(makeAuthorities(entityTypeDynamic.getId(), true, true, true));
		authorities.addAll(makeAuthorities(refEntityTypeDynamic.getId(), false, true, true));
		authorities.addAll(makeAuthorities(selfXrefEntityType.getId(), true, true, true));
		authorities.addAll(makeAuthorities(languageMetadata.getId(), true, true, true));
		authorities.addAll(makeAuthorities(attributeMetadata.getId(), true, true, true));
		authorities.addAll(makeAuthorities(l10nStringMetaData.getId(), true, false, false));
		authorities.addAll(makeAuthorities(entityTypeMetadata.getId(), true, true, true));

		SecurityContextHolder.getContext()
							 .setAuthentication(new TestingAuthenticationToken("user", "user", authorities));
	}

	@AfterMethod
	public void afterMethod()
	{
		runAsSystem(() ->
		{
			dataService.deleteAll(entityTypeStatic.getId());
			dataService.deleteAll(refEntityTypeStatic.getId());
			dataService.deleteAll(entityTypeDynamic.getId());
			dataService.deleteAll(refEntityTypeDynamic.getId());
			dataService.deleteAll(selfXrefEntityType.getId());
		});
		waitForIndexToBeStable(entityTypeStatic, indexService, LOG);
		waitForIndexToBeStable(refEntityTypeStatic, indexService, LOG);
		waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);
		waitForIndexToBeStable(refEntityTypeDynamic, indexService, LOG);
		waitForIndexToBeStable(selfXrefEntityType, indexService, LOG);
	}

	private void addDefaultLanguages()
	{
		if (dataService.count(LANGUAGE) == 0)
		{
			dataService.add(LANGUAGE,
					languageFactory.create(LanguageService.DEFAULT_LANGUAGE_CODE, LanguageService.DEFAULT_LANGUAGE_NAME,
							true));
			dataService.add(LANGUAGE,
					languageFactory.create("nl", new Locale("nl").getDisplayName(new Locale("nl")), false));
			dataService.add(LANGUAGE,
					languageFactory.create("pt", new Locale("pt").getDisplayName(new Locale("pt")), false));
			dataService.add(LANGUAGE,
					languageFactory.create("es", new Locale("es").getDisplayName(new Locale("es")), false));
			dataService.add(LANGUAGE,
					languageFactory.create("de", new Locale("de").getDisplayName(new Locale("de")), false));
			dataService.add(LANGUAGE,
					languageFactory.create("it", new Locale("it").getDisplayName(new Locale("it")), false));
			dataService.add(LANGUAGE,
					languageFactory.create("fr", new Locale("fr").getDisplayName(new Locale("fr")), false));
			dataService.add(LANGUAGE, languageFactory.create("xx", "My language", false));
		}
	}

	@Test(singleThreaded = true)
	public void testLanguageService()
	{
		assertEquals(dataService.getMeta().getEntityType(ENTITY_TYPE_META_DATA).getAttribute("labelEn").getName(),
				"labelEn");
		assertEquals(dataService.getMeta().getEntityType(ENTITY_TYPE_META_DATA).getLabelAttribute("en").getName(),
				"label");
		assertEquals(dataService.getMeta().getEntityType(ENTITY_TYPE_META_DATA).getLabelAttribute("pt").getName(),
				"label");
		assertEquals(dataService.getMeta().getEntityType(ENTITY_TYPE_META_DATA).getLabelAttribute("nl").getName(),
				"label");
		assertEquals(dataService.getMeta().getEntityType(ENTITY_TYPE_META_DATA).getLabelAttribute().getName(), "label");

		assertEquals(languageService.getCurrentUserLanguageCode(), "en");
		assertEqualsNoOrder(LanguageService.getLanguageCodes().toArray(),
				new String[] { "en", "nl", "de", "es", "it", "pt", "fr", "xx" });

		// NL
		assertNotNull(dataService.getEntityType(L10N_STRING).getAttribute("nl"));
		assertNotNull(dataService.getEntityType(ENTITY_TYPE_META_DATA).getAttribute("labelNl"));
		assertNotNull(dataService.getEntityType(ENTITY_TYPE_META_DATA).getAttribute("descriptionNl"));
		assertNotNull(dataService.getEntityType(ATTRIBUTE_META_DATA).getAttribute("labelNl"));
		assertNotNull(dataService.getEntityType(ATTRIBUTE_META_DATA).getAttribute("descriptionNl"));

		// EN
		assertNotNull(dataService.getEntityType(L10N_STRING).getAttribute("en"));
		assertNotNull(dataService.getEntityType(ENTITY_TYPE_META_DATA).getAttribute("labelEn"));
		assertNotNull(dataService.getEntityType(ENTITY_TYPE_META_DATA).getAttribute("descriptionEn"));
		assertNotNull(dataService.getEntityType(ATTRIBUTE_META_DATA).getAttribute("labelEn"));
		assertNotNull(dataService.getEntityType(ATTRIBUTE_META_DATA).getAttribute("descriptionEn"));

		L10nString car = l10nStringFactory.create();
		car.setMessageID("car");
		car.set("en", "car");
		car.set("nl", "auto");
		car.setNamespace("platform-it");
		dataService.add(L10nStringMetaData.L10N_STRING, car);
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
			dataService.add(refEntityTypeDynamic.getId(), refEntities.stream());
			dataService.add(entityTypeDynamic.getId(), entities.stream());
			waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);
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
			entityListenersService.addEntityListener(entityTypeDynamic.getId(), listener);
			dataService.update(entityTypeDynamic.getId(), entities.stream());
			assertEquals(updateCalled.get(), 1);
			waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);
			assertPresent(entityTypeDynamic, entities);
		}
		finally
		{
			// Test that the listener is actually removed and not called anymore
			entityListenersService.removeEntityListener(entityTypeDynamic.getId(), listener);
			updateCalled.set(0);
			dataService.update(entityTypeDynamic.getId(), entities.stream());
			assertEquals(updateCalled.get(), 0);
			waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);
			assertPresent(entityTypeDynamic, entities);
		}
	}

	@Test(singleThreaded = true)
	public void testAdd()
	{
		assertEquals(searchService.count(entityTypeDynamic), 0);
		List<Entity> entities = createDynamicAndAdd(2);
		assertEquals(dataService.count(entityTypeDynamic.getId(), new QueryImpl<>()), 2);
		assertEquals(searchService.count(entityTypeDynamic), 2);
		assertPresent(entityTypeDynamic, entities);
	}

	@Test(singleThreaded = true)
	public void testCount()
	{
		List<Entity> entities = createDynamicAndAdd(2);
		assertEquals(dataService.count(entityTypeDynamic.getId(), new QueryImpl<>()), 2);
		assertEquals(searchService.count(entityTypeDynamic), 2);
		assertPresent(entityTypeDynamic, entities);
	}

	@Test(singleThreaded = true)
	public void testDelete()
	{
		Entity entity = createDynamicAndAdd(1).get(0);
		assertPresent(entityTypeDynamic, entity);

		dataService.delete(entityTypeDynamic.getId(), entity);
		waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);
		assertNotPresent(entity);
	}

	@Test(singleThreaded = true)
	public void testDeleteMrefReference()
	{
		Entity entity = createDynamicAndAdd(1).get(0);
		Entity refEntity = dataService.findOneById(refEntityTypeDynamic.getId(), "2");
		assertPresent(entityTypeDynamic, entity);
		entity.set(ATTR_MREF, singletonList(refEntity));

		try
		{
			runAsSystem(() ->
			{
				dataService.update(entityTypeDynamic.getId(), entity);
				dataService.deleteById(refEntityTypeDynamic.getId(), refEntity.getIdValue());
			});
			fail("Should throw exception!");
		}
		catch (MolgenisValidationException expected)
		{
			assertEquals(expected.getMessage(),
					"Value '2' for attribute 'ref_id_attr' is referenced by entity 'TypeTestDynamic'.");
		}

		waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);
	}

	@Test(singleThreaded = true)
	public void testDeleteById()
	{
		Entity entity = createDynamicAndAdd(1).get(0);
		assertPresent(entityTypeDynamic, entity);

		dataService.deleteById(entityTypeDynamic.getId(), entity.getIdValue());
		waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);
		assertNotPresent(entity);
	}

	@Test(singleThreaded = true)
	public void testDeleteStream()
	{
		List<Entity> entities = createDynamicAndAdd(2);
		assertEquals(dataService.count(entityTypeDynamic.getId(), new QueryImpl<>()), entities.size());

		dataService.delete(entityTypeDynamic.getId(), entities.stream());
		waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);
		assertEquals(dataService.count(entityTypeDynamic.getId(), new QueryImpl<>()), 0);
	}

	@Test(singleThreaded = true)
	public void testDeleteAll()
	{
		List<Entity> entities = createDynamicAndAdd(5);
		assertEquals(dataService.count(entityTypeDynamic.getId(), new QueryImpl<>()), entities.size());

		dataService.deleteAll(entityTypeDynamic.getId());
		waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);
		assertEquals(dataService.count(entityTypeDynamic.getId(), new QueryImpl<>()), 0);
	}

	@Test(singleThreaded = true)
	public void testDeletePackage()
	{
		runAsSystem(() ->
		{
			MetaDataService metadataService = dataService.getMeta();

			Package parentPackage = packageFactory.create("parent").setLabel("parent");
			Package subPackage = packageFactory.create("parent_sub").setLabel("sub").setParent(parentPackage);
			metadataService.upsertPackages(Stream.of(parentPackage, subPackage));

			EntityType entityTypeInSubPackage = testHarness.createDynamicRefEntityType("entityInSub", subPackage);
			EntityType entityTypeInParentPackage = testHarness.createDynamicTestEntityType("entityInParent",
					parentPackage, entityTypeInSubPackage);

			metadataService.upsertEntityTypes(asList(entityTypeInSubPackage, entityTypeInParentPackage));

			List<Entity> entities = createAndAdd(entityTypeInParentPackage, entityTypeInSubPackage, 5);
			Set<Entity> refEntities = entities.stream().map(e -> e.getEntity(ATTR_XREF)).collect(toSet());
			assertPresent(entityTypeInParentPackage, entities);
			assertPresent(entityTypeInSubPackage, newArrayList(refEntities));

			dataService.deleteById(PACKAGE, "parent");
			assertNull(metadataService.getPackage("parent"));
			assertNull(metadataService.getPackage("parent_sub"));
			entities.forEach(this::assertNotPresent);
			refEntities.forEach(this::assertNotPresent);
		});
	}

	@Test(singleThreaded = true)
	public void testDeletePackageWithOneToMany()
	{
		runAsSystem(() ->
		{
			MetaDataService metadataService = dataService.getMeta();

			Package package_ = packageFactory.create("package_onetomany").setLabel("package");
			metadataService.upsertPackages(Stream.of(package_));

			EntityType refEntityType = testHarness.createDynamicRefEntityType("entityType_onetomany", package_);
			EntityType entityType = testHarness.createDynamicTestEntityType("refEntityType_onetomany", package_,
					refEntityType);

			Attribute oneToManyAttribute = attributeFactory.create("onetomany")
														   .setName("onetomany")
														   .setDataType(AttributeType.ONE_TO_MANY)
														   .setRefEntity(entityType)
														   .setMappedBy(entityType.getAttribute(ATTR_XREF));
			refEntityType.addAttribute(oneToManyAttribute);

			metadataService.upsertEntityTypes(asList(refEntityType, entityType));

			List<Entity> entities = createAndAdd(entityType, refEntityType, 5);
			Set<Entity> refEntities = entities.stream().map(e -> e.getEntity(ATTR_XREF)).collect(toSet());
			assertPresent(entityType, entities);
			assertPresent(refEntityType, newArrayList(refEntities));

			dataService.deleteById(PACKAGE, "package_onetomany");
			assertNull(metadataService.getPackage("package_onetomany"));
			assertNull(dataService.getEntityType(entityType.getId()));
			assertNull(dataService.getEntityType(refEntityType.getId()));
			entities.forEach(this::assertNotPresent);
			refEntities.forEach(this::assertNotPresent);
		});
	}

	@Test(singleThreaded = true)
	public void testReindexOnEntityTypeUpdate()
	{
		runAsSystem(() ->
		{
			EntityType entityType = testHarness.createDynamicSelfReferencingTestEntityType();
			dataService.getMeta().addEntityType(entityType);

			List<Entity> entities = testHarness.createSelfRefEntitiesWithEmptyReferences(entityType, 3)
											   .collect(toList());
			entities.get(0).set(ATTR_XREF, entities.get(1));
			entities.get(1).set(ATTR_XREF, entities.get(2));
			entities.get(2).set(ATTR_INT, 1337);

			dataService.add(entityType.getId(), entities.stream());
			waitForIndexToBeStable(entityType, indexService, LOG);

			Query<Entity> query = new QueryImpl<>().search("1337");
			Set<Object> resultIds = searchService.search(entityType, query).collect(toSet());
			assertEquals(resultIds.size(), 2);
			assertEquals(resultIds, newHashSet("1", "2"));

			entityType.setIndexingDepth(2);
			dataService.getMeta().updateEntityType(entityType);
			waitForIndexToBeStable(entityType, indexService, LOG);

			Set<Object> newResultIds = searchService.search(entityType, query).collect(toSet());
			assertEquals(newResultIds.size(), 3);
			assertEquals(newResultIds, newHashSet("0", "1", "2"));
		});
	}

	@Test(singleThreaded = true)
	public void testFindAllEmpty()
	{
		Stream<Entity> retrieved = dataService.findAll(entityTypeDynamic.getId());
		assertEquals(retrieved.count(), 0);
	}

	@Test(singleThreaded = true)
	public void testFindAll()
	{
		List<Entity> entities = createDynamicAndAdd(5);
		Stream<Entity> retrieved = dataService.findAll(entityTypeDynamic.getId());
		assertEquals(retrieved.count(), entities.size());
	}

	@Test(singleThreaded = true)
	public void testFindAllTyped()
	{
		List<Entity> entities = createDynamicAndAdd(1);
		Supplier<Stream<Entity>> retrieved = () -> dataService.findAll(entityTypeDynamic.getId(), Entity.class);
		assertEquals(retrieved.get().count(), 1);
		assertEquals(retrieved.get().iterator().next().getIdValue(), entities.get(0).getIdValue());
	}

	@Test(singleThreaded = true)
	public void testFindAllByIds()
	{
		List<Entity> entities = createDynamicAndAdd(5);
		Stream<Object> ids = Stream.concat(entities.stream().map(Entity::getIdValue), of("bogus"));
		Stream<Entity> retrieved = dataService.findAll(entityTypeDynamic.getId(), ids);
		assertEquals(retrieved.count(), entities.size());
	}

	@Test(singleThreaded = true)
	public void testFindAllByIdsTyped()
	{
		List<Entity> entities = createStaticAndAdd(5);

		Supplier<Stream<TestEntityStatic>> retrieved = () -> dataService.findAll(entityTypeStatic.getId(),
				Stream.concat(entities.stream().map(Entity::getIdValue), of("bogus")), TestEntityStatic.class);
		assertEquals(retrieved.get().count(), entities.size());
		assertEquals(retrieved.get().iterator().next().getId(), entities.get(0).getIdValue());
		assertEquals(retrieved.get().iterator().next().getIdValue(), entities.get(0).getIdValue());
	}

	@Test(singleThreaded = true)
	public void testFindAllStreamFetch()
	{
		List<Entity> entities = createDynamicAndAdd(5);
		Stream<Object> ids = concat(entities.stream().map(Entity::getIdValue), of("bogus"));
		Stream<Entity> retrieved = dataService.findAll(entityTypeDynamic.getId(), ids, new Fetch().field(ATTR_ID));
		assertEquals(retrieved.count(), entities.size());
	}

	@DataProvider(name = "findQueryOperatorEq")
	private static Object[][] findQueryOperatorEq() throws ParseException
	{
		return new Object[][] { { ATTR_ID, "1", singletonList(1) }, { ATTR_STRING, "string1", asList(0, 1, 2) },
				{ ATTR_BOOL, true, asList(0, 2) }, { ATTR_DATE, parseLocalDate("2012-12-21"), asList(0, 1, 2) },
				{ ATTR_DATETIME, parseInstant("1985-08-12T11:12:13+0500"), asList(0, 1, 2) },
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
		List<Entity> entities = createDynamicAndAdd(3);
		Supplier<Stream<Entity>> found = () -> dataService.query(entityTypeDynamic.getId())
														  .eq(attrName, value)
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

	@Test(singleThreaded = true, dataProvider = "findQueryOperatorIn")
	public void testFindQueryOperatorIn(List<String> ids, List<Integer> expectedEntityIndices)
	{
		List<Entity> entities = createDynamicAndAdd(2);
		Supplier<Stream<Entity>> found = () -> dataService.query(entityTypeDynamic.getId()).in(ATTR_ID, ids).findAll();
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
		List<Entity> entities = createDynamicAndAdd(5);
		Supplier<Stream<Entity>> found = () -> dataService.query(entityTypeDynamic.getId())
														  .lt(ATTR_INT, value)
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

	@Test(singleThreaded = true, dataProvider = "findQueryOperatorLessEqual")
	public void testFindQueryOperatorLessEqual(int value, List<Integer> expectedEntityIndices)
	{
		List<Entity> entities = createDynamicAndAdd(5);
		Supplier<Stream<Entity>> found = () -> dataService.query(entityTypeDynamic.getId())
														  .le(ATTR_INT, value)
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

	@Test(singleThreaded = true, dataProvider = "findQueryOperatorGreater")
	public void testFindQueryOperatorGreater(int value, List<Integer> expectedEntityIndices)
	{
		List<Entity> entities = createDynamicAndAdd(3);
		Supplier<Stream<Entity>> found = () -> dataService.query(entityTypeDynamic.getId())
														  .gt(ATTR_INT, value)
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

	@Test(singleThreaded = true, dataProvider = "findQueryOperatorGreaterEqual")
	public void testFindQueryOperatorGreaterEqual(int value, List<Integer> expectedEntityIndices)
	{
		List<Entity> entities = createDynamicAndAdd(3);
		Supplier<Stream<Entity>> found = () -> dataService.query(entityTypeDynamic.getId())
														  .ge(ATTR_INT, value)
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
		return new Object[][] { { 0, 9, emptyList() }, { 0, 10, singletonList(0) }, { 10, 10, singletonList(0) },
				{ 10, 11, asList(0, 1) }, { 10, 12, asList(0, 1, 2) }, { 12, 20, singletonList(2) } };
	}

	@Test(singleThreaded = true, dataProvider = "findQueryOperatorRange")
	public void testFindQueryOperatorRange(int low, int high, List<Integer> expectedEntityIndices)
	{
		List<Entity> entities = createDynamicAndAdd(3);
		Supplier<Stream<Entity>> found = () -> dataService.query(entityTypeDynamic.getId())
														  .rng(ATTR_INT, low, high)
														  .findAll();
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
		List<Entity> entities = createDynamicAndAdd(2);
		Supplier<Stream<Entity>> found = () -> dataService.query(entityTypeDynamic.getId())
														  .like(ATTR_STRING, likeStr)
														  .findAll();
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
		List<Entity> entities = createDynamicAndAdd(3);
		Supplier<Stream<Entity>> found = () -> dataService.query(entityTypeDynamic.getId())
														  .not()
														  .eq(ATTR_INT, value)
														  .findAll();
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
		createDynamicAndAdd(10000);

		Query<Entity> q1 = new QueryImpl<>().eq(EntityTestHarness.ATTR_STRING, "string1");
		q1.pageSize(1000);

		Query<Entity> q2 = new QueryImpl<>().eq(EntityTestHarness.ATTR_BOOL, true);
		q2.pageSize(500);

		Query<Entity> q3 = new QueryImpl<>().eq(ATTR_DECIMAL, 1.123);

		runAsSystem(() ->
		{
			for (int i = 0; i < 100000; i++)
			{
				dataService.findAll(entityTypeDynamic.getId(), q1);
				dataService.findAll(entityTypeDynamic.getId(), q2);
				dataService.findOne(entityTypeDynamic.getId(), q3);
			}
		});
	}

	@DataProvider(name = "findQueryOperatorAnd")
	private static Object[][] findQueryOperatorAnd()
	{
		return new Object[][] { { "string1", 10, singletonList(0) }, { "unknownString", 10, emptyList() },
				{ "string1", -1, emptyList() }, { "unknownString", -1, emptyList() } };
	}

	@Test(singleThreaded = true, dataProvider = "findQueryOperatorAnd")
	public void testFindQueryOperatorAnd(String strValue, int value, List<Integer> expectedEntityIndices)
	{
		List<Entity> entities = createDynamicAndAdd(3);
		Supplier<Stream<Entity>> found = () -> dataService.query(entityTypeDynamic.getId())
														  .eq(ATTR_STRING, strValue)
														  .and()
														  .eq(ATTR_INT, value)
														  .findAll();
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
		return new Object[][] { { "string1", 10, asList(0, 1, 2) }, { "unknownString", 10, singletonList(0) },
				{ "string1", -1, asList(0, 1, 2) }, { "unknownString", -1, emptyList() } };
	}

	@Test(singleThreaded = true, dataProvider = "findQueryOperatorOr")
	public void testFindQueryOperatorOr(String strValue, int value, List<Integer> expectedEntityIndices)
	{
		List<Entity> entities = createDynamicAndAdd(3);
		Supplier<Stream<Entity>> found = () -> dataService.query(entityTypeDynamic.getId())
														  .eq(ATTR_STRING, strValue)
														  .or()
														  .eq(ATTR_INT, value)
														  .findAll();
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
		return new Object[][] { { true, "string1", 10, asList(0, 2) }, { true, "unknownString", 10, singletonList(0) },
				{ true, "string1", -1, asList(0, 2) }, { true, "unknownString", -1, emptyList() },
				{ false, "string1", 10, singletonList(1) }, { false, "unknownString", 10, emptyList() },
				{ false, "string1", -1, singletonList(1) }, { false, "unknownString", -1, emptyList() } };
	}

	@Test(singleThreaded = true, dataProvider = "findQueryOperatorNested")
	public void testFindQueryOperatorNested(boolean boolValue, String strValue, int value,
			List<Integer> expectedEntityIndices)
	{
		List<Entity> entities = createDynamicAndAdd(3);
		Supplier<Stream<Entity>> found = () -> dataService.query(entityTypeDynamic.getId())
														  .eq(ATTR_BOOL, boolValue)
														  .and()
														  .nest()
														  .eq(ATTR_STRING, strValue)
														  .or()
														  .eq(ATTR_INT, value)
														  .unnest()
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
		return new Object[][] { { "body", singletonList(1) }, { "head", singletonList(1) },
				{ "unknownString", emptyList() } };
	}

	@Test(singleThreaded = true, dataProvider = "findQueryOperatorSearch")
	public void testFindQueryOperatorSearch(String searchStr, List<Integer> expectedEntityIndices)
	{
		List<Entity> entities = createDynamicAndAdd(2);
		Supplier<Stream<Entity>> found = () -> dataService.query(entityTypeDynamic.getId())
														  .search(ATTR_HTML, searchStr)
														  .findAll();
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
			dataService.add(refEntityTypeDynamic.getId(), testRefEntities.stream());
			dataService.add(entityTypeDynamic.getId(), testEntities.stream());
		});
		waitForIndexToBeStable(refEntityTypeDynamic, indexService, LOG);
		waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);
		Supplier<Stream<Entity>> found = () -> dataService.findAll(entityTypeDynamic.getId(),
				new QueryImpl<>().pageSize(2).offset(2).sort(new Sort(ATTR_ID, Sort.Direction.DESC)));
		List<Entity> foundAsList = found.get().collect(toList());
		assertEquals(foundAsList.size(), 2);
		assertTrue(EntityUtils.equals(foundAsList.get(0), testEntities.get(7)));
		assertTrue(EntityUtils.equals(foundAsList.get(1), testEntities.get(6)));
	}

	@Test(singleThreaded = true)
	public void testFindQueryTypedStatic()
	{
		List<Entity> entities = createStaticAndAdd(5);
		Supplier<Stream<TestEntityStatic>> found = () -> dataService.findAll(entityTypeStatic.getId(),
				new QueryImpl<TestEntityStatic>().eq(ATTR_ID, entities.get(0).getIdValue()), TestEntityStatic.class);
		assertEquals(found.get().count(), 1);
		assertEquals(found.get().findFirst().get().getId(), entities.get(0).getIdValue());
	}

	@Test(singleThreaded = true)
	public void testFindOne()
	{
		Entity entity = createDynamicAndAdd(1).get(0);
		assertNotNull(dataService.findOneById(entityTypeDynamic.getId(), entity.getIdValue()));
	}

	@Test(singleThreaded = true)
	public void testFindOneTypedStatic()
	{
		Entity entity = createStatic(1).findFirst().get();
		dataService.add(entityTypeStatic.getId(), Stream.of(entity));
		waitForIndexToBeStable(entityTypeStatic, indexService, LOG);
		TestEntityStatic testEntityStatic = dataService.findOneById(entityTypeStatic.getId(), entity.getIdValue(),
				TestEntityStatic.class);
		assertNotNull(testEntityStatic);
		assertEquals(testEntityStatic.getId(), entity.getIdValue());
	}

	@Test(singleThreaded = true)
	public void testFindOneFetch()
	{
		Entity entity = createDynamicAndAdd(1).get(0);
		assertNotNull(
				dataService.findOneById(entityTypeDynamic.getId(), entity.getIdValue(), new Fetch().field(ATTR_ID)));
	}

	@Test(singleThreaded = true)
	public void testFindOneFetchTypedStatic()
	{
		Entity entity = createStatic(1).collect(toList()).get(0);
		entity.set(ATTR_ID, "1");
		entity.set(ATTR_STRING, "string1");
		entity.set(ATTR_BOOL, true);

		dataService.add(entityTypeStatic.getId(), Stream.of(entity));
		waitForIndexToBeStable(entityTypeStatic, indexService, LOG);

		TestEntityStatic testEntityStatic = dataService.findOneById(entityTypeStatic.getId(), entity.getIdValue(),
				new Fetch().field(ATTR_ID), TestEntityStatic.class);
		assertNotNull(testEntityStatic);
		assertEquals(testEntityStatic.getIdValue(), entity.getIdValue());
	}

	@Test(singleThreaded = true)
	public void testFindOneQuery()
	{
		Entity entity = createDynamicAndAdd(1).get(0);
		entity = dataService.findOne(entityTypeDynamic.getId(), new QueryImpl<>().eq(ATTR_ID, entity.getIdValue()));
		assertNotNull(entity);
	}

	@Test(singleThreaded = true)
	public void testFindOneQueryTypedStatic()
	{
		Entity entity = createStaticAndAdd(1).get(0);
		TestEntityStatic testEntityStatic = dataService.findOne(entityTypeStatic.getId(),
				new QueryImpl<TestEntityStatic>().eq(ATTR_ID, entity.getIdValue()), TestEntityStatic.class);
		assertNotNull(testEntityStatic);
		assertEquals(testEntityStatic.getId(), entity.getIdValue());
	}

	@Test(singleThreaded = true)
	public void testGetCapabilities()
	{
		Set<RepositoryCapability> capabilities = dataService.getCapabilities(entityTypeDynamic.getId());
		assertNotNull(capabilities);
		assertTrue(capabilities.containsAll(asList(MANAGABLE, QUERYABLE, WRITABLE, VALIDATE_REFERENCE_CONSTRAINT)));
	}

	@Test(singleThreaded = true)
	public void testGetEntityType()
	{
		EntityType entityType = dataService.getEntityType(entityTypeDynamic.getId());
		assertNotNull(entityType);
		assertTrue(EntityUtils.equals(entityType, entityTypeDynamic));
	}

	@Test(singleThreaded = true)
	public void testGetEntityNames()
	{
		Stream<String> names = dataService.getEntityTypeIds();
		assertNotNull(names);
		assertTrue(names.anyMatch(entityTypeDynamic.getId()::equals));
	}

	@Test(singleThreaded = true)
	public void testGetMeta()
	{
		assertNotNull(dataService.getMeta());
	}

	@Test(singleThreaded = true)
	public void testGetKnownRepository()
	{
		Repository<Entity> repo = dataService.getRepository(entityTypeDynamic.getId());
		assertNotNull(repo);
		assertEquals(repo.getName(), entityTypeDynamic.getId());
	}

	@Test(singleThreaded = true, expectedExceptions = UnknownEntityException.class)
	public void testGetUnknownRepository()
	{
		dataService.getRepository("bogus");
	}

	@Test(singleThreaded = true)
	public void testHasRepository()
	{
		assertTrue(dataService.hasRepository(entityTypeDynamic.getId()));
		assertFalse(dataService.hasRepository("bogus"));
	}

	@Test(singleThreaded = true)
	public void testIterator()
	{
		assertNotNull(dataService.iterator());
		StreamSupport.stream(dataService.spliterator(), false).forEach(repo -> LOG.info(repo.getName()));
		Repository repo = dataService.getRepository(entityTypeDynamic.getId());

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
		assertNotNull(dataService.query(entityTypeDynamic.getId()));
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
		Entity entity = createDynamicAndAdd(1).get(0);

		entity = dataService.findOneById(entityTypeDynamic.getId(), entity.getIdValue());
		assertNotNull(entity);
		assertEquals(entity.get(ATTR_STRING), "string1");

		Query<Entity> q = new QueryImpl<>();
		q.eq(ATTR_STRING, "qwerty");
		entity.set(ATTR_STRING, "qwerty");

		assertEquals(searchService.count(entityTypeDynamic, q), 0);
		dataService.update(entityTypeDynamic.getId(), entity);
		waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);
		assertEquals(searchService.count(entityTypeDynamic, q), 1);

		assertPresent(entityTypeDynamic, entity);

		entity = dataService.findOneById(entityTypeDynamic.getId(), entity.getIdValue());
		assertNotNull(entity.get(ATTR_STRING));
		assertEquals(entity.get(ATTR_STRING), "qwerty");
	}

	@Test(singleThreaded = true)
	public void testUpdateSingleRefEntityIndexesReferencingEntities()
	{
		createDynamicAndAdd(30);

		Entity refEntity4 = dataService.findOneById(refEntityTypeDynamic.getId(), "4");

		Query<Entity> q = new QueryImpl<>().search("refstring4");

		assertEquals(searchService.count(entityTypeDynamic, q), 5);
		refEntity4.set(ATTR_REF_STRING, "qwerty");
		runAsSystem(() -> dataService.update(refEntityTypeDynamic.getId(), refEntity4));
		waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);
		assertEquals(searchService.count(entityTypeDynamic, q), 0);
		assertEquals(searchService.count(entityTypeDynamic, new QueryImpl<>().search("qwerty")), 5);
	}

	@Test(singleThreaded = true, enabled = false) //FIXME: sys_md_attributes spam
	public void testUpdateSingleRefEntityIndexesLargeAmountOfReferencingEntities()
	{
		createDynamicAndAdd(10000);

		Query<Entity> q = new QueryImpl<>().search("refstring4").or().search("refstring5");

		assertEquals(searchService.count(entityTypeDynamic, q), 3333);
		Entity refEntity4 = dataService.findOneById(refEntityTypeDynamic.getId(), "4");
		refEntity4.set(ATTR_REF_STRING, "qwerty");
		runAsSystem(() -> dataService.update(refEntityTypeDynamic.getId(), refEntity4));

		Entity refEntity5 = dataService.findOneById(refEntityTypeDynamic.getId(), "5");
		refEntity5.set(ATTR_REF_STRING, "qwerty");
		runAsSystem(() -> dataService.update(refEntityTypeDynamic.getId(), refEntity5));

		waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);
		assertEquals(searchService.count(entityTypeDynamic, q), 0);

		assertEquals(searchService.count(entityTypeDynamic, new QueryImpl<>().search("qwerty")), 3333);
	}

	@Test(singleThreaded = true)
	public void testUpdateStream()
	{
		Entity entity = createDynamicAndAdd(1).get(0);

		assertPresent(entityTypeDynamic, entity);

		entity = dataService.findOneById(entityTypeDynamic.getId(), entity.getIdValue());
		assertNotNull(entity);
		assertEquals(entity.get(ATTR_STRING), "string1");

		entity.set(ATTR_STRING, "qwerty");
		Query<Entity> q = new QueryImpl<>();
		q.eq(ATTR_STRING, "qwerty");

		assertEquals(searchService.count(entityTypeDynamic, q), 0);

		dataService.update(entityTypeDynamic.getId(), of(entity));
		waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);

		assertEquals(searchService.count(entityTypeDynamic, q), 1);

		assertPresent(entityTypeDynamic, entity);
		entity = dataService.findOneById(entityTypeDynamic.getId(), entity.getIdValue());
		assertNotNull(entity.get(ATTR_STRING));
		assertEquals(entity.get(ATTR_STRING), "qwerty");
	}

	private Stream<Entity> createStatic(int count)
	{
		return createTestEntities(entityTypeStatic, refEntityTypeStatic, count);
	}

	private List<Entity> createDynamicAndAdd(int count)
	{
		return createAndAdd(entityTypeDynamic, refEntityTypeDynamic, count);
	}

	private List<Entity> createAndAdd(EntityType entityType, EntityType refEntityType, int count)
	{
		List<Entity> entities = createTestEntities(entityType, refEntityType, count).collect(toList());
		dataService.add(entityType.getId(), entities.stream());
		waitForIndexToBeStable(entityType, indexService, LOG);
		return entities;
	}

	private List<Entity> createStaticAndAdd(int count)
	{
		return createAndAdd(entityTypeStatic, refEntityTypeStatic, count);
	}

	private Stream<Entity> createTestEntities(EntityType entityType, EntityType refEntityType, int count)
	{
		List<Entity> refEntities = testHarness.createTestRefEntities(refEntityType, 6);
		runAsSystem(() -> dataService.add(refEntityType.getId(), refEntities.stream()));
		return testHarness.createTestEntities(entityType, count, refEntities);
	}

	private void assertPresent(EntityType emd, List<Entity> entities)
	{
		entities.forEach(e -> assertPresent(emd, e));
	}

	private void assertPresent(EntityType emd, Entity entity)
	{
		// Found in PostgreSQL
		assertNotNull(dataService.findOneById(emd.getId(), entity.getIdValue()));

		// Found in index Elasticsearch
		Query<Entity> q = new QueryImpl<>();
		q.eq(emd.getIdAttribute().getName(), entity.getIdValue());
		assertEquals(searchService.count(emd, q), 1);
	}

	private void assertNotPresent(Entity entity)
	{
		// Found in PostgreSQL
		assertNull(dataService.findOneById(entityTypeDynamic.getId(), entity.getIdValue()));

		// Not found in index Elasticsearch
		Query<Entity> q = new QueryImpl<>();
		q.eq(entityTypeDynamic.getIdAttribute().getName(), entity.getIdValue());
		assertEquals(searchService.count(entityTypeDynamic, q), 0);
	}

	@Test(singleThreaded = true)
	public void testCreateSelfXref()
	{
		Entity entitySelfXref = entitySelfXrefTestHarness.createTestEntities(selfXrefEntityType, 1)
														 .collect(toList())
														 .get(0);

		//Create
		dataService.add(selfXrefEntityType.getId(), entitySelfXref);
		waitForIndexToBeStable(selfXrefEntityType, indexService, LOG);
		Entity entity = dataService.findOneById(selfXrefEntityType.getId(), entitySelfXref.getIdValue());
		assertPresent(selfXrefEntityType, entity);

		Query<Entity> q1 = new QueryImpl<>();
		q1.eq(ATTR_STRING, "attr_string_old");
		Query<Entity> q2 = new QueryImpl<>();
		q2.eq(ATTR_STRING, "attr_string_new");
		entity.set(ATTR_STRING, "attr_string_new");

		// Verify value in elasticsearch before update
		assertEquals(searchService.count(selfXrefEntityType, q1), 1);
		assertEquals(searchService.count(selfXrefEntityType, q2), 0);

		// Update
		dataService.update(selfXrefEntityType.getId(), entity);
		waitForIndexToBeStable(selfXrefEntityType, indexService, LOG);
		assertPresent(selfXrefEntityType, entity);

		// Verify value in elasticsearch after update
		assertEquals(searchService.count(selfXrefEntityType, q2), 1);
		assertEquals(searchService.count(selfXrefEntityType, q1), 0);

		// Verify value in PostgreSQL after update
		entity = dataService.findOneById(selfXrefEntityType.getId(), entity.getIdValue());
		assertNotNull(entity.get(ATTR_STRING));
		assertEquals(entity.get(ATTR_STRING), "attr_string_new");

		// Check id are equals
		assertEquals(entity.getEntity(ATTR_XREF).getIdValue(), entity.getIdValue());
	}

	@Test(singleThreaded = true)
	public void testIndexCreateMetaData()
	{
		IndexMetadataCUDOperationsPlatformIT.testIndexCreateMetaData(searchService, entityTypeStatic, entityTypeDynamic,
				metaDataService);
	}

	@Test(singleThreaded = true)
	public void testIndexDeleteMetaData()
	{
		IndexMetadataCUDOperationsPlatformIT.testIndexDeleteMetaData(searchService, dataService, entityTypeDynamic,
				metaDataService, indexService);
	}

	@Test(singleThreaded = true)
	public void testIndexUpdateMetaDataUpdateAttribute()
	{
		IndexMetadataCUDOperationsPlatformIT.testIndexUpdateMetaDataUpdateAttribute(searchService, entityTypeDynamic,
				metaDataService, indexService);
	}

	@Test(singleThreaded = true)
	public void testIndexUpdateMetaDataRemoveAttribute()
	{
		IndexMetadataCUDOperationsPlatformIT.testIndexUpdateMetaDataRemoveAttribute(entityTypeDynamic,
				EntityTestHarness.ATTR_CATEGORICAL, searchService, metaDataService, indexService);

		IndexMetadataCUDOperationsPlatformIT.testIndexUpdateMetaDataRemoveAttribute(entityTypeDynamic,
				EntityTestHarness.ATTR_BOOL, searchService, metaDataService, indexService);

		IndexMetadataCUDOperationsPlatformIT.testIndexUpdateMetaDataRemoveAttribute(entityTypeDynamic,
				EntityTestHarness.ATTR_DATE, searchService, metaDataService, indexService);

		IndexMetadataCUDOperationsPlatformIT.testIndexUpdateMetaDataRemoveAttribute(entityTypeDynamic,
				EntityTestHarness.ATTR_XREF, searchService, metaDataService, indexService);

		IndexMetadataCUDOperationsPlatformIT.testIndexUpdateMetaDataRemoveAttribute(entityTypeDynamic,
				EntityTestHarness.ATTR_DATETIME, searchService, metaDataService, indexService);

		IndexMetadataCUDOperationsPlatformIT.testIndexUpdateMetaDataRemoveAttribute(entityTypeDynamic,
				EntityTestHarness.ATTR_DECIMAL, searchService, metaDataService, indexService);

		IndexMetadataCUDOperationsPlatformIT.testIndexUpdateMetaDataRemoveAttribute(entityTypeDynamic,
				EntityTestHarness.ATTR_EMAIL, searchService, metaDataService, indexService);

		IndexMetadataCUDOperationsPlatformIT.testIndexUpdateMetaDataRemoveAttribute(entityTypeDynamic,
				EntityTestHarness.ATTR_HTML, searchService, metaDataService, indexService);

		IndexMetadataCUDOperationsPlatformIT.testIndexUpdateMetaDataRemoveAttribute(entityTypeDynamic,
				EntityTestHarness.ATTR_INT, searchService, metaDataService, indexService);

		IndexMetadataCUDOperationsPlatformIT.testIndexUpdateMetaDataRemoveAttribute(entityTypeDynamic,
				EntityTestHarness.ATTR_HYPERLINK, searchService, metaDataService, indexService);

		IndexMetadataCUDOperationsPlatformIT.testIndexUpdateMetaDataRemoveAttribute(entityTypeDynamic,
				EntityTestHarness.ATTR_COMPOUND, searchService, metaDataService, indexService);
	}

	// Derived from fix: https://github.com/molgenis/molgenis/issues/5227
	@Test(singleThreaded = true)
	public void testIndexBatchUpdate()
	{
		List<Entity> refEntities = testHarness.createTestRefEntities(refEntityTypeDynamic, 2);
		List<Entity> entities = testHarness.createTestEntities(entityTypeDynamic, 2, refEntities).collect(toList());
		runAsSystem(() ->
		{
			dataService.add(refEntityTypeDynamic.getId(), refEntities.stream());
			dataService.add(entityTypeDynamic.getId(), entities.stream());
			waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);
		});

		// test string1 from entity
		Query<Entity> q0 = new QueryImpl<>();
		q0.search("string1");
		long count0 = searchService.count(entityTypeDynamic, q0);
		assertEquals(count0, 2L);

		// test refstring1 from ref entity
		Query<Entity> q1 = new QueryImpl<>();
		q1.search("refstring0");
		long count1 = searchService.count(entityTypeDynamic, q1);
		assertEquals(count1, 1L);

		// test refstring1 from ref entity
		Query<Entity> q2 = new QueryImpl<>();
		q2.search("refstring1");
		long count2 = searchService.count(entityTypeDynamic, q2);
		assertEquals(count2, 1L);

		refEntities.get(0).set(ATTR_REF_STRING, "searchTestBatchUpdate");
		runAsSystem(() ->
		{
			dataService.update(refEntityTypeDynamic.getId(), refEntities.stream());
			waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);
		});

		// test string1 from entity
		long count3 = searchService.count(entityTypeDynamic, q0);
		assertEquals(count3, 2L);

		// test refstring1 from ref entity
		Query<Entity> q4 = new QueryImpl<>();
		q4.search("refstring0");
		long count4 = searchService.count(entityTypeDynamic, q4);
		assertEquals(count4, 0L);

		// test refstring1 from ref entity
		Query<Entity> q5 = new QueryImpl<>();
		q5.search("refstring1");
		long count5 = searchService.count(entityTypeDynamic, q5);
		assertEquals(count5, 1L);

		// test refstring1 from ref entity
		Query<Entity> q6 = new QueryImpl<>();
		q6.search("searchTestBatchUpdate");
		long count6 = searchService.count(entityTypeDynamic, q6);
		assertEquals(count6, 1L);
	}

	/**
	 * Test add and remove of a single attribute of a dynamic entity
	 */
	@Test(singleThreaded = true)
	public void addAndDeleteSingleAttribute()
	{
		final String NEW_ATTRIBUTE = "new_attribute";
		Attribute newAttr = attributeFactory.create().setName(NEW_ATTRIBUTE);
		EntityType entityType = dataService.getEntityType(entityTypeDynamic.getId());
		newAttr.setEntity(entityType);

		runAsSystem(() ->
		{
			dataService.getMeta().addAttribute(newAttr);

			List<Entity> refEntities = testHarness.createTestRefEntities(refEntityTypeDynamic, 2);
			List<Entity> entities = testHarness.createTestEntities(entityTypeDynamic, 2, refEntities).collect(toList());

			dataService.add(refEntityTypeDynamic.getId(), refEntities.stream());
			dataService.add(entityTypeDynamic.getId(), entities.stream());
			waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);

			dataService.update(entityType.getId(),
					StreamSupport.stream(dataService.findAll(entityType.getId()).spliterator(), false)
								 .peek(e -> e.set(NEW_ATTRIBUTE, "NEW_ATTRIBUTE_" + e.getIdValue())));
		});
		waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);

		// Tunnel via L3 flow
		Query<Entity> q0 = new QueryImpl<>().eq(NEW_ATTRIBUTE, "NEW_ATTRIBUTE_0")
											.or()
											.eq(NEW_ATTRIBUTE, "NEW_ATTRIBUTE_1");
		q0.pageSize(10); // L3 only caches queries with a page size
		q0.sort(new Sort().on(NEW_ATTRIBUTE));

		runAsSystem(() ->
		{
			List expected = dataService.findAll(entityTypeDynamic.getId(), q0)
									   .map(Entity::getIdValue)
									   .collect(toList());
			assertEquals(expected, asList("0", "1"));

			// Remove added attribute
			dataService.getMeta().deleteAttributeById(newAttr.getIdValue());
			waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);
		});

		// verify attribute is deleted by adding and removing it again
		runAsSystem(() ->
		{
			// Add attribute
			dataService.getMeta().addAttribute(newAttr);

			// Delete attribute
			dataService.getMeta().deleteAttributeById(newAttr.getIdValue());
			waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);
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
		EntityType entityType = dataService.getEntityType(entityTypeDynamic.getId());
		newAttr.setEntity(entityType);

		runAsSystem(() ->
		{
			dataService.getMeta().addAttributes(entityType.getId(), Stream.of(newAttr));
			waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);

			Attribute attribute = dataService.findOneById(ATTRIBUTE_META_DATA, newAttr.getIdValue(), Attribute.class);
			assertNotNull(attribute);

			// Tunnel via L3 flow
			Query<Entity> q0 = new QueryImpl<>().eq(NEW_ATTRIBUTE, "NEW_ATTRIBUTE_0")
												.or()
												.eq(NEW_ATTRIBUTE, "NEW_ATTRIBUTE_1");
			q0.pageSize(10); // L3 only caches queries with a page size
			q0.sort(new Sort().on(NEW_ATTRIBUTE));

			List actual = dataService.findAll(entityTypeDynamic.getId(), q0).map(Entity::getIdValue).collect(toList());
			assertEquals(actual, emptyList());

			// Remove added attribute
			dataService.getMeta().deleteAttributeById(newAttr.getIdValue());
			waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);
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
		EntityType entityType = dataService.getEntityType(entityTypeDynamic.getId());
		newAttr.setEntity(entityType);

		// Add attribute
		runAsSystem(() ->
		{
			dataService.getMeta().addAttribute(newAttr);

			List<Entity> refEntities = testHarness.createTestRefEntities(refEntityTypeDynamic, 2);
			List<Entity> entities = testHarness.createTestEntities(entityTypeDynamic, 2, refEntities).collect(toList());

			dataService.add(refEntityTypeDynamic.getId(), refEntities.stream());
			dataService.add(entityTypeDynamic.getId(), entities.stream());
			waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);

			dataService.update(entityType.getId(),
					StreamSupport.stream(dataService.findAll(entityType.getId()).spliterator(), false)
								 .peek(e -> e.set(NEW_ATTRIBUTE, "NEW_ATTRIBUTE_" + e.getIdValue())));
		});
		waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);

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
			waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);
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
			waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);
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
		EntityType entityType = dataService.getEntityType(entityTypeDynamic.getId());
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

			dataService.add(refEntityTypeDynamic.getId(), refEntities.stream());
			dataService.add(entityTypeDynamic.getId(), entities.stream());
			waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);

			dataService.update(entityType.getId(),
					StreamSupport.stream(dataService.findAll(entityType.getId()).spliterator(), false)
								 .peek(e -> e.set(NEW_ATTRIBUTE, "NEW_ATTRIBUTE_" + e.getIdValue())));
		});
		waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);

		// Tunnel via L3 flow
		Query<Entity> q0 = new QueryImpl<>().eq(NEW_ATTRIBUTE, "NEW_ATTRIBUTE_0")
											.or()
											.eq(NEW_ATTRIBUTE, "NEW_ATTRIBUTE_1");
		q0.pageSize(10); // L3 only caches queries with a page size
		q0.sort(new Sort().on(NEW_ATTRIBUTE));

		runAsSystem(() ->
		{
			List expected = dataService.findAll(entityTypeDynamic.getId(), q0)
									   .map(Entity::getIdValue)
									   .collect(toList());
			assertEquals(expected, Arrays.asList("0", "1"));

			// Remove added attribute
			entityType.removeAttribute(newAttr);
			dataService.update(ENTITY_TYPE_META_DATA, Stream.of(entityType));
			dataService.delete(ATTRIBUTE_META_DATA, Stream.of(newAttr));
			waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);
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
			waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);
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
			dataService.add(refEntityTypeDynamic.getId(), refEntities.stream());
			dataService.add(entityTypeDynamic.getId(), entities.stream());
			waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);

			indexActionRegisterService.register(entityTypeDynamic, "1");
			indexActionRegisterService.register(entityTypeDynamic, null);

			Query<IndexAction> q = new QueryImpl<>();
			q.eq(IndexActionMetaData.ENTITY_TYPE_ID, "sys_test_TypeTestDynamic");
			Stream<org.molgenis.data.index.meta.IndexAction> all = dataService.findAll(IndexActionMetaData.INDEX_ACTION,
					q, IndexAction.class);
			all.forEach(e -> LOG.info(e.getEntityTypeId() + "." + e.getEntityId()));
			waitForIndexToBeStable(entityTypeDynamic, indexService, LOG);
		});
	}

	@Test(singleThreaded = true)
	public void testOneDimensionalAggregateQuery()
	{
		createDynamicAndAdd(6);

		AggregateResult result = runAggregateQuery(ATTR_BOOL, null, null, new QueryImpl<>());

		AggregateResult expectedResult = new AggregateResult(asList(singletonList(3L), singletonList(3L)),
				asList(0L, 1L), emptyList());
		assertEquals(result, expectedResult);
	}

	@Test(singleThreaded = true)
	public void testTwoDimensionalAggregateQuery()
	{
		createDynamicAndAdd(6);

		AggregateResult result = runAggregateQuery(ATTR_BOOL, ATTR_ENUM, null, new QueryImpl<>());

		AggregateResult expectedResult = new AggregateResult(asList(asList(0L, 3L), asList(3L, 0L)), asList(0L, 1L),
				asList("option1", "option2"));
		assertEquals(result, expectedResult);
	}

	@Test(singleThreaded = true)
	public void testOneDimensionalDistinctAggregateQuery()
	{
		createDynamicAndAdd(6);

		AggregateResult result = runAggregateQuery(ATTR_BOOL, null, ATTR_ENUM, new QueryImpl<>());

		AggregateResult expectedResult = new AggregateResult(asList(singletonList(1L), singletonList(1L)),
				asList(0L, 1L), emptyList());
		assertEquals(result, expectedResult);
	}

	@Test(singleThreaded = true)
	public void testTwoDimensionalDistinctAggregateQuery()
	{
		createDynamicAndAdd(6);

		AggregateResult result = runAggregateQuery(ATTR_BOOL, ATTR_BOOL, ATTR_ENUM, new QueryImpl<>());

		AggregateResult expectedResult = new AggregateResult(asList(asList(1L, 0L), asList(0L, 1L)), asList(0L, 1L),
				asList(0L, 1L));
		assertEquals(result, expectedResult);
	}

	@Test(singleThreaded = true)
	public void testAggregateQueryWithQuery()
	{
		createDynamicAndAdd(6);
		Query<Entity> query = new QueryImpl<>().gt(ATTR_INT, 12); // last 3 entities

		AggregateResult result = runAggregateQuery(ATTR_BOOL, ATTR_ENUM, null, query);

		AggregateResult expectedResult = new AggregateResult(asList(asList(0L, 2L), asList(1L, 0L)), asList(0L, 1L),
				asList("option1", "option2"));
		assertEquals(result, expectedResult);
	}

	@Test(singleThreaded = true)
	public void testDistinctAggregateQueryWithQuery()
	{
		createDynamicAndAdd(6);
		Query<Entity> query = new QueryImpl<>().gt(ATTR_INT, 12); // last 3 entities

		AggregateResult result = runAggregateQuery(ATTR_BOOL, ATTR_ENUM, ATTR_ENUM, query);

		AggregateResult expectedResult = new AggregateResult(asList(asList(0L, 1L), asList(1L, 0L)), asList(0L, 1L),
				asList("option1", "option2"));
		assertEquals(result, expectedResult);
	}

	@Test(singleThreaded = true, enabled = false)
	public void testDistinctAggregateQueryManyRows()
	{
		createDynamicAndAdd(20000);
		Query<Entity> query = new QueryImpl<>().eq(ATTR_BOOL, true);

		AggregateResult result = runAggregateQuery(ATTR_BOOL, ATTR_ENUM, ATTR_ENUM, query);

		AggregateResult expectedResult = new AggregateResult(singletonList(singletonList(1L)), singletonList(1L),
				singletonList("option1"));
		assertEquals(result, expectedResult);
	}

	@Test(singleThreaded = true, enabled = false)
	public void testAggregateQueryManyRows()
	{
		createDynamicAndAdd(1000000);
		Query<Entity> query = new QueryImpl<>().eq(ATTR_BOOL, true).or().lt(ATTR_INT, 15000);

		AggregateResult result = runAggregateQuery(ATTR_BOOL, ATTR_ENUM, null, query);

		AggregateResult expectedResult = new AggregateResult(asList(asList(0L, 7495L), asList(500000L, 0L)),
				asList(0L, 1L), asList("option1", "option2"));
		assertEquals(result, expectedResult);
	}

	private AggregateResult runAggregateQuery(String attrX, String attrY, String attrDistinct, Query<Entity> query)
	{
		requireNonNull(attrX);
		requireNonNull(query);

		Attribute x = entityTypeDynamic.getAttribute(attrX);
		Attribute y = attrY != null ? entityTypeDynamic.getAttribute(attrY) : null;
		Attribute distinct = attrDistinct != null ? entityTypeDynamic.getAttribute(attrDistinct) : null;

		AggregateQuery aggregateQuery = new AggregateQueryImpl(x, y, distinct, query);
		return runAsSystem(() -> dataService.aggregate(entityTypeDynamic.getId(), aggregateQuery));
	}
}
