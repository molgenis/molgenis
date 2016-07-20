package org.molgenis.integrationtest.platform;

import org.apache.commons.io.FileUtils;
import org.molgenis.data.*;
import org.molgenis.data.Entity;
import org.molgenis.data.cache.l2.L2Cache;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.elasticsearch.reindex.job.ReindexService;
import org.molgenis.data.listeners.EntityListener;
import org.molgenis.data.listeners.EntityListenersService;
import org.molgenis.data.meta.MetaDataServiceImpl;
import org.molgenis.data.meta.model.EntityMetaData;

import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.runas.RunAsSystemProxy;
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
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Arrays.asList;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;
import static org.molgenis.data.RepositoryCapability.*;
import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;
import static org.molgenis.test.data.EntityTestHarness.*;
import static org.testng.Assert.*;
import static org.testng.Assert.assertEquals;

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
	private ReindexService reindexService;
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

	/**
	 * Wait till the whole index is stable. Reindex job is done a-synchronized.
	 */
	public static void waitForWorkToBeFinished(ReindexService reindexService, Logger log)
	{
		try
		{
			reindexService.waitForAllIndicesStable();
			log.info("All work finished");
		}
		catch (InterruptedException e)
		{
			log.warn("Interrupted while waiting for index to become stable!", e);
			fail("Interrupted while waiting for index to become stable!");
		}
	}

	/**
	 * Wait till the index is stable. Reindex job is executed asynchronously. This method waits for all reindex jobs
	 * relevant for this entity to be finished.
	 *
	 * @param entityName name of the entitiy whose index needs to be stable
	 */
	public static void waitForIndexToBeStable(String entityName, ReindexService reindexService, Logger log)
	{
		try
		{
			reindexService.waitForIndexToBeStableIncludingReferences(entityName);
			log.info("Index for entity [{}] incl. references is stable", entityName);
		}
		catch (InterruptedException e)
		{
			log.info("Interrupted waiting for [{}] incl. references to become stable", entityName, e);
		}
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

		RunAsSystemProxy.runAsSystem(() -> {
			metaDataService.addEntityMeta(refEntityMetaDataDynamic);
			metaDataService.addEntityMeta(entityMetaDataDynamic);
			metaDataService.addEntityMeta(selfXrefEntityMetaData);
		});
		waitForWorkToBeFinished(reindexService, LOG);
		setAuthentication();
	}

	private void setAuthentication()
	{
		// Permissions refEntityMetaDataStatic.getName()
		String writeTestRefEntityStatic = "ROLE_ENTITY_WRITE_" + refEntityMetaDataStatic.getName().toUpperCase();
		String readTestRefEntityStatic = "ROLE_ENTITY_READ_" + refEntityMetaDataStatic.getName().toUpperCase();
		String countTestRefEntityStatic = "ROLE_ENTITY_COUNT_" + refEntityMetaDataStatic.getName().toUpperCase();

		// Permissions entityMetaDataStatic.getName()
		String writeTestEntityStatic = "ROLE_ENTITY_WRITE_" + entityMetaDataStatic.getName().toUpperCase();
		String readTestEntityStatic = "ROLE_ENTITY_READ_" + entityMetaDataStatic.getName().toUpperCase();
		String countTestEntityStatic = "ROLE_ENTITY_COUNT_" + entityMetaDataStatic.getName().toUpperCase();

		// Permissions entityMetaDataDynamic.getName()
		String writeTestEntity = "ROLE_ENTITY_WRITE_" + entityMetaDataDynamic.getName().toUpperCase();
		String readTestEntity = "ROLE_ENTITY_READ_" + entityMetaDataDynamic.getName().toUpperCase();
		String countTestEntity = "ROLE_ENTITY_COUNT_" + entityMetaDataDynamic.getName().toUpperCase();

		// Permissions refEntityMetaDataDynamic.getName()
		String readTestRefEntity = "ROLE_ENTITY_READ_" + refEntityMetaDataDynamic.getName().toUpperCase();
		String countTestRefEntity = "ROLE_ENTITY_COUNT_" + refEntityMetaDataDynamic.getName().toUpperCase();

		// Permissions selfXrefEntityMetaData.getName()
		String writeSelfXrefEntity = "ROLE_ENTITY_WRITE_" + selfXrefEntityMetaData.getName().toUpperCase();
		String readSelfXrefEntity = "ROLE_ENTITY_READ_" + selfXrefEntityMetaData.getName().toUpperCase();
		String countSelfXrefEntity = "ROLE_ENTITY_COUNT_" + selfXrefEntityMetaData.getName().toUpperCase();

		// Permissions selfXrefEntityMetaData.getName()
		String writeEntitiesEntity = "ROLE_ENTITY_WRITE_" + "sys_md_entities";

		SecurityContextHolder.getContext().setAuthentication(
				new TestingAuthenticationToken("user", "user", writeTestEntity, readTestEntity, readTestRefEntity,
						countTestEntity, countTestRefEntity, writeSelfXrefEntity, readSelfXrefEntity,
						countSelfXrefEntity, writeTestEntityStatic, readTestEntityStatic, countTestEntityStatic,
						writeTestRefEntityStatic, readTestRefEntityStatic, countTestRefEntityStatic,
						writeEntitiesEntity, "ROLE_ENTITY_READ_SYS_MD_ENTITIES", "ROLE_ENTITY_READ_SYS_MD_ATTRIBUTES",
						"ROLE_ENTITY_READ_SYS_MD_PACKAGES"));
	}

	@AfterMethod
	public void afterMethod()
	{
		runAsSystem(() -> {
			dataService.deleteAll(entityMetaDataStatic.getName());
			dataService.deleteAll(refEntityMetaDataStatic.getName());
			dataService.deleteAll(entityMetaDataDynamic.getName());
			dataService.deleteAll(refEntityMetaDataDynamic.getName());
			dataService.deleteAll(selfXrefEntityMetaData.getName());
		});
		waitForIndexToBeStable(entityMetaDataStatic.getName(), reindexService, LOG);
		waitForIndexToBeStable(refEntityMetaDataStatic.getName(), reindexService, LOG);
		waitForIndexToBeStable(entityMetaDataDynamic.getName(), reindexService, LOG);
		waitForIndexToBeStable(refEntityMetaDataDynamic.getName(), reindexService, LOG);
		waitForIndexToBeStable(selfXrefEntityMetaData.getName(), reindexService, LOG);
	}

	@Test
	public void testEntityListener()
	{
		List<Entity> refEntities = testHarness.createTestRefEntities(refEntityMetaDataDynamic, 6);
		List<Entity> entities = testHarness.createTestEntities(entityMetaDataDynamic, 2, refEntities)
				.collect(Collectors.toList());
		runAsSystem(() -> {
			dataService.add(refEntityMetaDataDynamic.getName(), refEntities.stream());
			dataService.add(entityMetaDataDynamic.getName(), entities.stream());
			waitForIndexToBeStable(entityMetaDataDynamic.getName(), reindexService, LOG);
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
			waitForIndexToBeStable(entityMetaDataDynamic.getName(), reindexService, LOG);
			assertPresent(entityMetaDataDynamic, entities);
		}
		finally
		{
			// Test that the listener is actually removed and not called anymore
			entityListenersService.removeEntityListener(entityMetaDataDynamic.getName(), listener);
			updateCalled.set(0);
			dataService.update(entityMetaDataDynamic.getName(), entities.stream());
			assertEquals(updateCalled.get(), 0);
			waitForIndexToBeStable(entityMetaDataDynamic.getName(), reindexService, LOG);
			assertPresent(entityMetaDataDynamic, entities);
		}
	}

	@Test
	public void testAdd()
	{
		List<Entity> entities = createDynamic(2).collect(Collectors.toList());
		assertEquals(searchService.count(entityMetaDataDynamic), 0);
		dataService.add(entityMetaDataDynamic.getName(), entities.stream());
		waitForIndexToBeStable(entityMetaDataDynamic.getName(), reindexService, LOG);
		assertEquals(dataService.count(entityMetaDataDynamic.getName(), new QueryImpl<>()), 2);
		assertEquals(searchService.count(entityMetaDataDynamic), 2);
		assertPresent(entityMetaDataDynamic, entities);
	}

	@Test
	public void testCount()
	{
		List<Entity> entities = createDynamic(2).collect(Collectors.toList());
		dataService.add(entityMetaDataDynamic.getName(), entities.stream());
		waitForIndexToBeStable(entityMetaDataDynamic.getName(), reindexService, LOG);
		assertEquals(dataService.count(entityMetaDataDynamic.getName(), new QueryImpl<>()), 2);
		assertEquals(searchService.count(entityMetaDataDynamic), 2);
		assertPresent(entityMetaDataDynamic, entities);
	}

	@Test
	public void testDelete()
	{
		Entity entity = createDynamic(1).findFirst().get();
		dataService.add(entityMetaDataDynamic.getName(), entity);
		waitForIndexToBeStable(entityMetaDataDynamic.getName(), reindexService, LOG);
		assertPresent(entityMetaDataDynamic, entity);

		dataService.delete(entityMetaDataDynamic.getName(), entity);
		waitForIndexToBeStable(entityMetaDataDynamic.getName(), reindexService, LOG);
		assertNotPresent(entity);
	}

	@Test
	public void testDeleteById()
	{
		Entity entity = createDynamic(1).findFirst().get();
		dataService.add(entityMetaDataDynamic.getName(), entity);
		waitForIndexToBeStable(entityMetaDataDynamic.getName(), reindexService, LOG);
		assertPresent(entityMetaDataDynamic, entity);

		dataService.deleteById(entityMetaDataDynamic.getName(), entity.getIdValue());
		waitForIndexToBeStable(entityMetaDataDynamic.getName(), reindexService, LOG);
		assertNotPresent(entity);
	}

	@Test
	public void testDeleteStream()
	{
		List<Entity> entities = createDynamic(2).collect(Collectors.toList());
		dataService.add(entityMetaDataDynamic.getName(), entities.stream());
		waitForIndexToBeStable(entityMetaDataDynamic.getName(), reindexService, LOG);
		assertEquals(dataService.count(entityMetaDataDynamic.getName(), new QueryImpl<>()), entities.size());

		dataService.delete(entityMetaDataDynamic.getName(), entities.stream());
		waitForIndexToBeStable(entityMetaDataDynamic.getName(), reindexService, LOG);
		assertEquals(dataService.count(entityMetaDataDynamic.getName(), new QueryImpl<>()), 0);
	}

	@Test
	public void testDeleteAll()
	{
		List<Entity> entities = createDynamic(5).collect(Collectors.toList());
		dataService.add(entityMetaDataDynamic.getName(), entities.stream());
		waitForIndexToBeStable(entityMetaDataDynamic.getName(), reindexService, LOG);
		assertEquals(dataService.count(entityMetaDataDynamic.getName(), new QueryImpl<>()), entities.size());

		dataService.deleteAll(entityMetaDataDynamic.getName());
		waitForIndexToBeStable(entityMetaDataDynamic.getName(), reindexService, LOG);
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
		List<Entity> entities = createDynamic(5).collect(Collectors.toList());
		dataService.add(entityMetaDataDynamic.getName(), entities.stream());
		waitForIndexToBeStable(entityMetaDataDynamic.getName(), reindexService, LOG);
		Stream<Entity> retrieved = dataService.findAll(entityMetaDataDynamic.getName());
		assertEquals(retrieved.count(), entities.size());
	}

	@Test
	public void testFindAllTyped()
	{
		List<Entity> entities = createDynamic(1).collect(Collectors.toList());
		dataService.add(entityMetaDataDynamic.getName(), entities.stream());
		waitForIndexToBeStable(entityMetaDataDynamic.getName(), reindexService, LOG);
		Supplier<Stream<Entity>> retrieved = () -> dataService.findAll(entityMetaDataDynamic.getName(), Entity.class);
		assertEquals(retrieved.get().count(), 1);
		assertEquals(retrieved.get().iterator().next().getIdValue(), entities.get(0).getIdValue());
	}

	@Test
	public void testFindAllByIds()
	{
		List<Entity> entities = createDynamic(5).collect(Collectors.toList());
		dataService.add(entityMetaDataDynamic.getName(), entities.stream());
		waitForIndexToBeStable(entityMetaDataDynamic.getName(), reindexService, LOG);
		Stream<Object> ids = Stream.concat(entities.stream().map(Entity::getIdValue), of("bogus"));
		Stream<Entity> retrieved = dataService.findAll(entityMetaDataDynamic.getName(), ids);
		assertEquals(retrieved.count(), entities.size());
	}

	@Test
	public void testFindAllByIdsTyped()
	{
		List<Entity> entities = createStatic(5).collect(Collectors.toList());
		dataService.add(entityMetaDataStatic.getName(), entities.stream());
		waitForIndexToBeStable(entityMetaDataStatic.getName(), reindexService, LOG);

		Supplier<Stream<TestEntityStatic>> retrieved = () -> dataService.findAll(entityMetaDataStatic.getName(),
				Stream.concat(entities.stream().map(Entity::getIdValue), of("bogus")), TestEntityStatic.class);
		assertEquals(retrieved.get().count(), entities.size());
		assertEquals(retrieved.get().iterator().next().getId(), entities.get(0).getIdValue());
		assertEquals(retrieved.get().iterator().next().getIdValue(), entities.get(0).getIdValue());
	}

	@Test
	public void testFindAllStreamFetch()
	{
		List<Entity> entities = createDynamic(5).collect(Collectors.toList());
		dataService.add(entityMetaDataDynamic.getName(), entities.stream());
		waitForIndexToBeStable(entityMetaDataDynamic.getName(), reindexService, LOG);
		Stream<Object> ids = concat(entities.stream().map(Entity::getIdValue), of("bogus"));
		Stream<Entity> retrieved = dataService
				.findAll(entityMetaDataDynamic.getName(), ids, new Fetch().field(ATTR_ID));
		assertEquals(retrieved.count(), entities.size());
	}

	@Test
	public void testFindQuery()
	{
		List<Entity> entities = createDynamic(5).collect(Collectors.toList());
		dataService.add(entityMetaDataDynamic.getName(), entities.stream());
		waitForIndexToBeStable(entityMetaDataDynamic.getName(), reindexService, LOG);
		Supplier<Stream<Entity>> found = () -> dataService
				.findAll(entityMetaDataDynamic.getName(), new QueryImpl<>().eq(ATTR_ID, entities.get(0).getIdValue()));
		assertEquals(found.get().count(), 1);
		assertEquals(found.get().findFirst().get().getIdValue(), entities.get(0).getIdValue());
	}

	@Test
	public void testFindQueryLimit2_Offset2_sortOnInt()
	{
		List<Entity> testRefEntities = testHarness.createTestRefEntities(refEntityMetaDataDynamic, 6);
		List<Entity> testEntities = testHarness.createTestEntities(entityMetaDataDynamic, 10, testRefEntities)
				.collect(Collectors.toList());
		runAsSystem(() -> {
			dataService.add(refEntityMetaDataDynamic.getName(), testRefEntities.stream());
			dataService.add(entityMetaDataDynamic.getName(), testEntities.stream());
		});
		waitForIndexToBeStable(refEntityMetaDataDynamic.getName(), reindexService, LOG);
		waitForIndexToBeStable(entityMetaDataDynamic.getName(), reindexService, LOG);
		Supplier<Stream<Entity>> found = () -> dataService.findAll(entityMetaDataDynamic.getName(),
				new QueryImpl<>().pageSize(2).offset(2).sort(new Sort(ATTR_ID, Sort.Direction.DESC)));
		List<Entity> foundAsList = found.get().collect(Collectors.toList());
		assertEquals(foundAsList.size(), 2);
		assertTrue(EntityUtils.equals(foundAsList.get(0), testEntities.get(7)));
		assertTrue(EntityUtils.equals(foundAsList.get(1), testEntities.get(6)));
	}

	@Test
	public void testFindQueryTypedStatic()
	{
		List<Entity> entities = createStatic(5).collect(Collectors.toList());
		dataService.add(entityMetaDataStatic.getName(), entities.stream());
		waitForIndexToBeStable(entityMetaDataStatic.getName(), reindexService, LOG);
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
		waitForIndexToBeStable(entityMetaDataDynamic.getName(), reindexService, LOG);
		assertNotNull(dataService.findOneById(entityMetaDataDynamic.getName(), entity.getIdValue()));
	}

	@Test
	public void testFindOneTypedStatic()
	{
		Entity entity = createStatic(1).findFirst().get();
		dataService.add(entityMetaDataStatic.getName(), Stream.of(entity));
		waitForIndexToBeStable(entityMetaDataStatic.getName(), reindexService, LOG);
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
		waitForIndexToBeStable(entityMetaDataDynamic.getName(), reindexService, LOG);
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
		waitForIndexToBeStable(entityMetaDataStatic.getName(), reindexService, LOG);
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
		waitForIndexToBeStable(entityMetaDataDynamic.getName(), reindexService, LOG);
		entity = dataService
				.findOne(entityMetaDataDynamic.getName(), new QueryImpl<>().eq(ATTR_ID, entity.getIdValue()));
		assertNotNull(entity);
	}

	@Test
	public void testFindOneQueryTypedStatic()
	{
		Entity entity = createStatic(1).findFirst().get();
		dataService.add(entityMetaDataStatic.getName(), entity);
		waitForIndexToBeStable(entityMetaDataStatic.getName(), reindexService, LOG);
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
		waitForIndexToBeStable(entityMetaDataDynamic.getName(), reindexService, LOG);

		entity = dataService.findOneById(entityMetaDataDynamic.getName(), entity.getIdValue());
		assertNotNull(entity);
		assertEquals(entity.get(ATTR_STRING), "string1");

		Query<Entity> q = new QueryImpl<>();
		q.eq(ATTR_STRING, "qwerty");
		entity.set(ATTR_STRING, "qwerty");

		assertEquals(searchService.count(q, entityMetaDataDynamic), 0);
		dataService.update(entityMetaDataDynamic.getName(), entity);
		waitForIndexToBeStable(entityMetaDataDynamic.getName(), reindexService, LOG);
		assertEquals(searchService.count(q, entityMetaDataDynamic), 1);

		assertPresent(entityMetaDataDynamic, entity);

		entity = dataService.findOneById(entityMetaDataDynamic.getName(), entity.getIdValue());
		assertNotNull(entity.get(ATTR_STRING));
		assertEquals(entity.get(ATTR_STRING), "qwerty");
	}

	@Test
	public void testUpdateSingleRefEntityReindexesReferencingEntities()
	{
		dataService.add(entityMetaDataDynamic.getName(), createDynamic(30));
		waitForIndexToBeStable(entityMetaDataDynamic.getName(), reindexService, LOG);

		Entity refEntity4 = dataService.findOneById(refEntityMetaDataDynamic.getName(), "4");

		Query<Entity> q = new QueryImpl<>().search("refstring4");

		assertEquals(searchService.count(q, entityMetaDataDynamic), 5);
		refEntity4.set(ATTR_REF_STRING, "qwerty");
		runAsSystem(() -> dataService.update(refEntityMetaDataDynamic.getName(), refEntity4));
		waitForIndexToBeStable(entityMetaDataDynamic.getName(), reindexService, LOG);
		assertEquals(searchService.count(q, entityMetaDataDynamic), 0);
		assertEquals(searchService.count(new QueryImpl<>().search("qwerty"), entityMetaDataDynamic), 5);
	}

	@Test(enabled = false) //FIXME: sys_md_attributes spam
	public void testUpdateSingleRefEntityReindexesLargeAmountOfReferencingEntities()
	{
		dataService.add(entityMetaDataDynamic.getName(), createDynamic(10000));
		waitForIndexToBeStable(entityMetaDataDynamic.getName(), reindexService, LOG);

		Query<Entity> q = new QueryImpl<>().search("refstring4").or().search("refstring5");

		assertEquals(searchService.count(q, entityMetaDataDynamic), 3333);
		Entity refEntity4 = dataService.findOneById(refEntityMetaDataDynamic.getName(), "4");
		refEntity4.set(ATTR_REF_STRING, "qwerty");
		runAsSystem(() -> dataService.update(refEntityMetaDataDynamic.getName(), refEntity4));

		Entity refEntity5 = dataService.findOneById(refEntityMetaDataDynamic.getName(), "5");
		refEntity5.set(ATTR_REF_STRING, "qwerty");
		runAsSystem(() -> dataService.update(refEntityMetaDataDynamic.getName(), refEntity5));

		waitForIndexToBeStable(entityMetaDataDynamic.getName(), reindexService, LOG);
		assertEquals(searchService.count(q, entityMetaDataDynamic), 0);

		assertEquals(searchService.count(new QueryImpl<>().search("qwerty"), entityMetaDataDynamic), 3333);
	}

	@Test
	public void testUpdateStream()
	{
		Entity entity = createDynamic(1).findFirst().get();

		dataService.add(entityMetaDataDynamic.getName(), entity);
		waitForIndexToBeStable(entityMetaDataDynamic.getName(), reindexService, LOG);
		assertPresent(entityMetaDataDynamic, entity);

		entity = dataService.findOneById(entityMetaDataDynamic.getName(), entity.getIdValue());
		assertNotNull(entity);
		assertEquals(entity.get(ATTR_STRING), "string1");

		entity.set(ATTR_STRING, "qwerty");
		Query<Entity> q = new QueryImpl<>();
		q.eq(ATTR_STRING, "qwerty");

		assertEquals(searchService.count(q, entityMetaDataDynamic), 0);

		dataService.update(entityMetaDataDynamic.getName(), of(entity));
		waitForIndexToBeStable(entityMetaDataDynamic.getName(), reindexService, LOG);

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
				.collect(Collectors.toList()).get(0);

		//Create
		dataService.add(selfXrefEntityMetaData.getName(), entitySelfXref);
		waitForIndexToBeStable(selfXrefEntityMetaData.getName(), reindexService, LOG);
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
		waitForIndexToBeStable(selfXrefEntityMetaData.getName(), reindexService, LOG);
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
	public void testReindexCreateMetaData()
	{
		ReindexMetadataCUDOperationsPlatformIT
				.testReindexCreateMetaData(searchService, entityMetaDataStatic, entityMetaDataDynamic, metaDataService);
	}

	@Test
	public void testReindexDeleteMetaData()
	{
		ReindexMetadataCUDOperationsPlatformIT
				.testReindexDeleteMetaData(searchService, dataService, entityMetaDataDynamic, metaDataService,
						reindexService);
	}

	@Test
	public void testReindexUpdateMetaDataUpdateAttribute()
	{
		ReindexMetadataCUDOperationsPlatformIT
				.testReindexUpdateMetaDataUpdateAttribute(searchService, entityMetaDataDynamic, metaDataService,
						reindexService);
	}

	@Test
	public void testReindexUpdateMetaDataRemoveAttribute()
	{
		ReindexMetadataCUDOperationsPlatformIT
				.testReindexUpdateMetaDataRemoveAttribute(entityMetaDataDynamic, EntityTestHarness.ATTR_CATEGORICAL,
						searchService, metaDataService, reindexService);

		ReindexMetadataCUDOperationsPlatformIT
				.testReindexUpdateMetaDataRemoveAttribute(entityMetaDataDynamic, EntityTestHarness.ATTR_BOOL,
						searchService, metaDataService, reindexService);

		ReindexMetadataCUDOperationsPlatformIT
				.testReindexUpdateMetaDataRemoveAttribute(entityMetaDataDynamic, EntityTestHarness.ATTR_DATE,
						searchService, metaDataService, reindexService);

		ReindexMetadataCUDOperationsPlatformIT
				.testReindexUpdateMetaDataRemoveAttribute(entityMetaDataDynamic, EntityTestHarness.ATTR_XREF,
						searchService, metaDataService, reindexService);

		ReindexMetadataCUDOperationsPlatformIT
				.testReindexUpdateMetaDataRemoveAttribute(entityMetaDataDynamic, EntityTestHarness.ATTR_DATETIME,
						searchService, metaDataService, reindexService);

		ReindexMetadataCUDOperationsPlatformIT
				.testReindexUpdateMetaDataRemoveAttribute(entityMetaDataDynamic, EntityTestHarness.ATTR_DECIMAL,
						searchService, metaDataService, reindexService);

		ReindexMetadataCUDOperationsPlatformIT
				.testReindexUpdateMetaDataRemoveAttribute(entityMetaDataDynamic, EntityTestHarness.ATTR_EMAIL,
						searchService, metaDataService, reindexService);

		ReindexMetadataCUDOperationsPlatformIT
				.testReindexUpdateMetaDataRemoveAttribute(entityMetaDataDynamic, EntityTestHarness.ATTR_HTML,
						searchService, metaDataService, reindexService);

		ReindexMetadataCUDOperationsPlatformIT
				.testReindexUpdateMetaDataRemoveAttribute(entityMetaDataDynamic, EntityTestHarness.ATTR_INT,
						searchService, metaDataService, reindexService);

		ReindexMetadataCUDOperationsPlatformIT
				.testReindexUpdateMetaDataRemoveAttribute(entityMetaDataDynamic, EntityTestHarness.ATTR_HYPERLINK,
						searchService, metaDataService, reindexService);
	}
}

