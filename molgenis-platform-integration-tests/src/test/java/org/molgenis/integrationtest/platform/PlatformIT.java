package org.molgenis.integrationtest.platform;

import com.google.common.collect.Iterators;
import org.apache.commons.io.FileUtils;
import org.molgenis.data.*;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.elasticsearch.reindex.job.ReindexService;
import org.molgenis.data.listeners.EntityListener;
import org.molgenis.data.meta.MetaDataServiceImpl;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.runas.RunAsSystemProxy;
import org.molgenis.test.data.EntitySelfXrefTestHarness;
import org.molgenis.test.data.EntityTestHarness;
import org.molgenis.test.data.TestEntity;
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
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;
import static org.molgenis.data.RepositoryCapability.*;
import static org.molgenis.data.Sort.Direction.DESC;
import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;
import static org.molgenis.test.data.EntityTestHarness.*;
import static org.testng.Assert.*;
import static org.testng.Assert.assertEquals;

@ContextConfiguration(classes = { PlatformITConfig.class })
public class PlatformIT extends AbstractTestNGSpringContextTests
{
	private final Logger LOG = LoggerFactory.getLogger(PlatformIT.class);

	private EntityMetaData entityMetaData;
	private EntityMetaData refEntityMetaData;
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

	/**
	 * Wait till the whole index is stable. Reindex job is done a-synchronized.
	 */
	private void waitForWorkToBeFinished()
	{
		try
		{
			reindexService.waitForAllIndicesStable();
			LOG.info("All work finished");
		}
		catch (InterruptedException e)
		{
			LOG.warn("Interrupted while waiting for index to become stable!", e);
			fail("Interrupted while waiting for index to become stable!");
		}
	}

	/**
	 * Wait till the index is stable. Reindex job is executed asynchronously. This method waits for all reindex jobs
	 * relevant for this entity to be finished.
	 *
	 * @param entityName name of the entitiy whose index needs to be stable
	 */
	private void waitForIndexToBeStable(String entityName)
	{
		try
		{
			reindexService.waitForIndexToBeStableIncludingReferences(entityName);
			LOG.info("Index for entity [{}] incl. references is stable", entityName);
		}
		catch (InterruptedException e)
		{
			LOG.info("Interrupted waiting for [{}] incl. references to become stable", entityName, e);
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
	}

	@BeforeClass
	public void setUp()
	{
		refEntityMetaData = testHarness.createDynamicRefEntityMetaData();
		entityMetaData = testHarness.createDynamicTestEntityMetaData();

		// Create a self refer entity
		selfXrefEntityMetaData = entitySelfXrefTestHarness.createDynamicEntityMetaData();
		selfXrefEntityMetaData.getAttribute(ATTR_XREF).setRefEntity(selfXrefEntityMetaData);

		RunAsSystemProxy.runAsSystem(() -> {
			metaDataService.addEntityMeta(refEntityMetaData);
			metaDataService.addEntityMeta(entityMetaData);
			metaDataService.addEntityMeta(selfXrefEntityMetaData);
		});
		this.waitForWorkToBeFinished();
		setAuthentication();
	}

	private void setAuthentication()
	{
		// Permissions entityMetaData.getName()
		String writeTestEntity = "ROLE_ENTITY_WRITE_" + entityMetaData.getName().toUpperCase();
		String readTestEntity = "ROLE_ENTITY_READ_" + entityMetaData.getName().toUpperCase();
		String countTestEntity = "ROLE_ENTITY_COUNT_" + entityMetaData.getName().toUpperCase();

		// Permissions refEntityMetaData.getName()
		String readTestRefEntity = "ROLE_ENTITY_READ_" + refEntityMetaData.getName().toUpperCase();
		String countTestRefEntity = "ROLE_ENTITY_COUNT_" + refEntityMetaData.getName().toUpperCase();

		// Permissions selfXrefEntityMetaData.getName()
		String writeSelfXrefEntity = "ROLE_ENTITY_WRITE_" + selfXrefEntityMetaData.getName().toUpperCase();
		String readSelfXrefEntity = "ROLE_ENTITY_READ_" + selfXrefEntityMetaData.getName().toUpperCase();
		String countSelfXrefEntity = "ROLE_ENTITY_COUNT_" + selfXrefEntityMetaData.getName().toUpperCase();

		SecurityContextHolder.getContext().setAuthentication(
				new TestingAuthenticationToken("user", "user", writeTestEntity, readTestEntity, readTestRefEntity,
						countTestEntity, countTestRefEntity, writeSelfXrefEntity, readSelfXrefEntity,
						countSelfXrefEntity, "ROLE_ENTITY_READ_SYS_MD_ENTITIES",
						"ROLE_ENTITY_READ_SYS_MD_ATTRIBUTES", "ROLE_ENTITY_READ_SYS_MD_PACKAGES"));
	}

	@AfterMethod
	public void afterMethod()
	{
		runAsSystem(() -> {
			dataService.deleteAll(entityMetaData.getName());
			dataService.deleteAll(refEntityMetaData.getName());
			dataService.deleteAll(refEntityMetaData.getName());
		});
		waitForIndexToBeStable(entityMetaData.getName());
	}

	@Test
	public void testEntityListener()
	{
		List<Entity> refEntities = testHarness.createTestRefEntities(refEntityMetaData, 6);
		List<Entity> entities = testHarness.createTestEntities(entityMetaData, 2, refEntities)
				.collect(Collectors.toList());
		runAsSystem(() -> {
			dataService.add(refEntityMetaData.getName(), refEntities.stream());
			dataService.add(entityMetaData.getName(), entities.stream());
			waitForIndexToBeStable(entityMetaData.getName());
		});

		AtomicInteger updateCalled = new AtomicInteger(0);
		EntityListener listener = new EntityListener()
		{
			@Override
			public Object getEntityId(){
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
			dataService.addEntityListener(entityMetaData.getName(), listener);
			dataService.update(entityMetaData.getName(), entities.stream());
			assertEquals(updateCalled.get(), 1);
			waitForIndexToBeStable(entityMetaData.getName());
			assertPresent(entityMetaData, entities);
		}
		finally
		{
			// Test that the listener is actually removed and not called anymore
			dataService.removeEntityListener(entityMetaData.getName(), listener);
			updateCalled.set(0);
			dataService.update(entityMetaData.getName(), entities.stream());
			assertEquals(updateCalled.get(), 0);
			waitForIndexToBeStable(entityMetaData.getName());
			assertPresent(entityMetaData, entities);
		}
	}

	@Test
	public void testAdd()
	{
		List<Entity> entities = create(2).collect(Collectors.toList());
		assertEquals(searchService.count(entityMetaData), 0);
		dataService.add(entityMetaData.getName(), entities.stream());
		waitForIndexToBeStable(entityMetaData.getName());
		assertEquals(dataService.count(entityMetaData.getName(), new QueryImpl<>()), 2);
		assertEquals(searchService.count(entityMetaData), 2);
		assertPresent(entityMetaData, entities);
	}

	@Test
	public void testCount()
	{
		List<Entity> entities = create(2).collect(Collectors.toList());
		dataService.add(entityMetaData.getName(), entities.stream());
		waitForIndexToBeStable(entityMetaData.getName());
		assertEquals(dataService.count(entityMetaData.getName(), new QueryImpl<>()), 2);
		assertEquals(searchService.count(entityMetaData), 2);
		assertPresent(entityMetaData, entities);
	}

	@Test
	public void testDelete()
	{
		Entity entity = create(1).findFirst().get();
		dataService.add(entityMetaData.getName(), entity);
		waitForIndexToBeStable(entityMetaData.getName());
		assertPresent(entityMetaData, entity);

		dataService.delete(entityMetaData.getName(), entity);
		waitForIndexToBeStable(entityMetaData.getName());
		assertNotPresent(entity);
	}

	@Test
	public void testDeleteById()
	{
		Entity entity = create(1).findFirst().get();
		dataService.add(entityMetaData.getName(), entity);
		waitForIndexToBeStable(entityMetaData.getName());
		assertPresent(entityMetaData, entity);

		dataService.deleteById(entityMetaData.getName(), entity.getIdValue());
		waitForIndexToBeStable(entityMetaData.getName());
		assertNotPresent(entity);
	}

	@Test
	public void testDeleteStream()
	{
		List<Entity> entities = create(2).collect(Collectors.toList());
		dataService.add(entityMetaData.getName(), entities.stream());
		waitForIndexToBeStable(entityMetaData.getName());
		assertEquals(dataService.count(entityMetaData.getName(), new QueryImpl<>()), entities.size());

		dataService.delete(entityMetaData.getName(), entities.stream());
		waitForIndexToBeStable(entityMetaData.getName());
		assertEquals(dataService.count(entityMetaData.getName(), new QueryImpl<>()), 0);
	}

	@Test
	public void testDeleteAll()
	{
		List<Entity> entities = create(5).collect(Collectors.toList());
		dataService.add(entityMetaData.getName(), entities.stream());
		waitForIndexToBeStable(entityMetaData.getName());
		assertEquals(dataService.count(entityMetaData.getName(), new QueryImpl<>()), entities.size());

		dataService.deleteAll(entityMetaData.getName());
		waitForIndexToBeStable(entityMetaData.getName());
		assertEquals(dataService.count(entityMetaData.getName(), new QueryImpl<>()), 0);
	}

	@Test
	public void testFindAllEmpty()
	{
		Stream<Entity> retrieved = dataService.findAll(entityMetaData.getName());
		assertEquals(retrieved.count(), 0);
	}

	@Test
	public void testFindAll()
	{
		List<Entity> entities = create(5).collect(Collectors.toList());
		dataService.add(entityMetaData.getName(), entities.stream());
		waitForIndexToBeStable(entityMetaData.getName());
		Stream<Entity> retrieved = dataService.findAll(entityMetaData.getName());
		assertEquals(retrieved.count(), entities.size());
	}

	@Test
	public void testFindAllTyped()
	{
		List<Entity> entities = create(1).collect(Collectors.toList());
		dataService.add(entityMetaData.getName(), entities.stream());
		waitForIndexToBeStable(entityMetaData.getName());
		Supplier<Stream<TestEntity>> retrieved = () -> dataService.findAll(entityMetaData.getName(), TestEntity.class);
		assertEquals(retrieved.get().count(), 1);
		assertEquals(retrieved.get().iterator().next().getId(), entities.get(0).getIdValue());
	}

	@Test
	public void testFindAllByIds()
	{
		List<Entity> entities = create(5).collect(Collectors.toList());
		dataService.add(entityMetaData.getName(), entities.stream());
		waitForIndexToBeStable(entityMetaData.getName());
		Stream<Object> ids = Stream.concat(entities.stream().map(Entity::getIdValue), of("bogus"));
		Stream<Entity> retrieved = dataService.findAll(entityMetaData.getName(), ids);
		assertEquals(retrieved.count(), entities.size());
	}

	@Test
	public void testFindAllByIdsTyped()
	{
		List<Entity> entities = create(5).collect(Collectors.toList());
		dataService.add(entityMetaData.getName(), entities.stream());
		waitForIndexToBeStable(entityMetaData.getName());

		Supplier<Stream<TestEntity>> retrieved = () -> dataService.findAll(entityMetaData.getName(),
				Stream.concat(entities.stream().map(Entity::getIdValue), of("bogus")), TestEntity.class);
		assertEquals(retrieved.get().count(), entities.size());
		assertEquals(retrieved.get().iterator().next().getId(), entities.get(0).getIdValue());
	}

	@Test
	public void testFindAllStreamFetch()
	{
		List<Entity> entities = create(5).collect(Collectors.toList());
		dataService.add(entityMetaData.getName(), entities.stream());
		waitForIndexToBeStable(entityMetaData.getName());
		Stream<Object> ids = concat(entities.stream().map(Entity::getIdValue), of("bogus"));
		Stream<Entity> retrieved = dataService.findAll(entityMetaData.getName(), ids, new Fetch().field(ATTR_ID));
		assertEquals(retrieved.count(), entities.size());
	}

	@Test
	public void testFindQuery()
	{
		List<Entity> entities = create(5).collect(Collectors.toList());
		dataService.add(entityMetaData.getName(), entities.stream());
		waitForIndexToBeStable(entityMetaData.getName());
		Supplier<Stream<Entity>> found = () -> dataService
				.findAll(entityMetaData.getName(), new QueryImpl<>().eq(ATTR_ID, entities.get(0).getIdValue()));
		assertEquals(found.get().count(), 1);
		assertEquals(found.get().findFirst().get().getIdValue(), entities.get(0).getIdValue());
	}

	@Test
	public void testFindQueryLimit2_Offset2_sortOnInt()
	{
		List<Entity> testRefEntities = testHarness.createTestRefEntities(refEntityMetaData, 6);
		List<Entity> testEntities = testHarness.createTestEntities(entityMetaData, 10, testRefEntities)
				.collect(Collectors.toList());
		runAsSystem(() -> {
			dataService.add(refEntityMetaData.getName(), testRefEntities.stream());
			dataService.add(entityMetaData.getName(), testEntities.stream());
		});
		waitForIndexToBeStable(refEntityMetaData.getName());
		waitForIndexToBeStable(entityMetaData.getName());
		Supplier<Stream<Entity>> found = () -> dataService.findAll(entityMetaData.getName(),
				new QueryImpl<>().pageSize(2).offset(2).sort(new Sort(ATTR_ID, DESC)));
		assertEquals(found.get().count(), 2);
		assertEquals(found.get().collect(toList()), Arrays.asList(testEntities.get(7), testEntities.get(6)));
	}

	@Test
	public void testFindQueryTyped()
	{
		List<Entity> entities = create(5).collect(Collectors.toList());
		dataService.add(entityMetaData.getName(), entities.stream());
		waitForIndexToBeStable(entityMetaData.getName());
		Supplier<Stream<TestEntity>> found = () -> dataService.findAll(entityMetaData.getName(),
				new QueryImpl<TestEntity>().eq(ATTR_ID, entities.get(0).getIdValue()), TestEntity.class);
		assertEquals(found.get().count(), 1);
		assertEquals(found.get().findFirst().get().getId(), entities.get(0).getIdValue());
	}

	@Test
	public void testFindOne()
	{
		Entity entity = create(1).findFirst().get();
		dataService.add(entityMetaData.getName(), Stream.of(entity));
		waitForIndexToBeStable(entityMetaData.getName());
		assertNotNull(dataService.findOneById(entityMetaData.getName(), entity.getIdValue()));
	}

	@Test
	public void testFindOneTyped()
	{
		Entity entity = create(1).findFirst().get();
		dataService.add(entityMetaData.getName(), Stream.of(entity));
		waitForIndexToBeStable(entityMetaData.getName());
		TestEntity testEntity = dataService
				.findOneById(entityMetaData.getName(), entity.getIdValue(), TestEntity.class);
		assertNotNull(testEntity);
		assertEquals(testEntity.getId(), entity.getIdValue());
	}

	@Test
	public void testFindOneFetch()
	{
		Entity entity = create(1).findFirst().get();
		dataService.add(entityMetaData.getName(), Stream.of(entity));
		waitForIndexToBeStable(entityMetaData.getName());
		assertNotNull(
				dataService.findOneById(entityMetaData.getName(), entity.getIdValue(), new Fetch().field(ATTR_ID)));
	}

	@Test
	public void testFindOneFetchTyped()
	{
		Entity entity = create(1).findFirst().get();
		dataService.add(entityMetaData.getName(), Stream.of(entity));
		waitForIndexToBeStable(entityMetaData.getName());
		TestEntity testEntity = dataService
				.findOneById(entityMetaData.getName(), entity.getIdValue(), new Fetch().field(ATTR_ID),
						TestEntity.class);
		assertNotNull(testEntity);
		assertEquals(testEntity.getId(), entity.getIdValue());
	}

	@Test
	public void testFindOneQuery()
	{
		Entity entity = create(1).findFirst().get();
		dataService.add(entityMetaData.getName(), entity);
		waitForIndexToBeStable(entityMetaData.getName());
		entity = dataService.findOne(entityMetaData.getName(), new QueryImpl<>().eq(ATTR_ID, entity.getIdValue()));
		assertNotNull(entity);
	}

	@Test
	public void testFindOneQueryTyped()
	{
		Entity entity = create(1).findFirst().get();
		dataService.add(entityMetaData.getName(), entity);
		waitForIndexToBeStable(entityMetaData.getName());
		TestEntity testEntity = dataService
				.findOne(entityMetaData.getName(), new QueryImpl<TestEntity>().eq(ATTR_ID, entity.getIdValue()),
						TestEntity.class);
		assertNotNull(testEntity);
		assertEquals(testEntity.getId(), entity.getIdValue());
	}

	@Test
	public void testGetCapabilities()
	{
		Set<RepositoryCapability> capabilities = dataService.getCapabilities(entityMetaData.getName());
		assertNotNull(capabilities);
		assertTrue(capabilities.containsAll(asList(MANAGABLE, QUERYABLE, WRITABLE, VALIDATE_REFERENCE_CONSTRAINT)));
	}

	@Test
	public void testGetEntityMetaData()
	{
		EntityMetaData emd = dataService.getEntityMetaData(entityMetaData.getName());
		assertNotNull(emd);
		assertEquals(emd, entityMetaData);
	}

	@Test
	public void testGetEntityNames()
	{
		Stream<String> names = dataService.getEntityNames();
		assertNotNull(names);
		assertTrue(names.filter(entityMetaData.getName()::equals).findFirst().isPresent());
	}

	@Test
	public void testGetMeta()
	{
		assertNotNull(dataService.getMeta());
	}

	@Test()
	public void testGetKnownRepository()
	{
		Repository<Entity> repo = dataService.getRepository(entityMetaData.getName());
		assertNotNull(repo);
		assertEquals(repo.getName(), entityMetaData.getName());
	}

	@Test(expectedExceptions = UnknownEntityException.class)
	public void testGetUnknownRepository()
	{
		dataService.getRepository("bogus");
	}

	@Test
	public void testHasRepository()
	{
		assertTrue(dataService.hasRepository(entityMetaData.getName()));
		assertFalse(dataService.hasRepository("bogus"));
	}

	@Test
	public void testIterator()
	{
		assertNotNull(dataService.iterator());
		assertTrue(Iterators.contains(dataService.iterator(), dataService.getRepository(entityMetaData.getName())));
	}

	@Test
	public void testQuery()
	{
		assertNotNull(dataService.query(entityMetaData.getName()));
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
		Entity entity = create(1).findFirst().get();
		dataService.add(entityMetaData.getName(), entity);
		waitForIndexToBeStable(entityMetaData.getName());

		entity = dataService.findOneById(entityMetaData.getName(), entity.getIdValue());
		assertNotNull(entity);
		assertEquals(entity.get(ATTR_STRING), "string1");

		Query<Entity> q = new QueryImpl<>();
		q.eq(ATTR_STRING, "qwerty");
		entity.set(ATTR_STRING, "qwerty");

		assertEquals(searchService.count(q, entityMetaData), 0);
		dataService.update(entityMetaData.getName(), entity);
		waitForIndexToBeStable(entityMetaData.getName());
		assertEquals(searchService.count(q, entityMetaData), 1);

		assertPresent(entityMetaData, entity);

		entity = dataService.findOneById(entityMetaData.getName(), entity.getIdValue());
		assertNotNull(entity.get(ATTR_STRING));
		assertEquals(entity.get(ATTR_STRING), "qwerty");
	}

	@Test
	public void testUpdateSingleRefEntityReindexesReferencingEntities()
	{
		dataService.add(entityMetaData.getName(), create(30));
		waitForIndexToBeStable(entityMetaData.getName());

		Entity refEntity4 = dataService.findOneById(refEntityMetaData.getName(), "4");

		Query<Entity> q = new QueryImpl<>().search("refstring4");

		assertEquals(searchService.count(q, entityMetaData), 5);
		refEntity4.set(ATTR_REF_STRING, "qwerty");
		runAsSystem(() -> dataService.update(refEntityMetaData.getName(), refEntity4));
		waitForIndexToBeStable(entityMetaData.getName());
		assertEquals(searchService.count(q, entityMetaData), 0);
		assertEquals(searchService.count(new QueryImpl<>().search("qwerty"), entityMetaData), 5);
	}

	@Test(enabled = false) //FIXME: sys_md_attributes spam
	public void testUpdateSingleRefEntityReindexesLargeAmountOfReferencingEntities()
	{
		dataService.add(entityMetaData.getName(), create(10000));
		waitForIndexToBeStable(entityMetaData.getName());

		Query<Entity> q = new QueryImpl<>().search("refstring4").or().search("refstring5");

		assertEquals(searchService.count(q, entityMetaData), 3333);
		Entity refEntity4 = dataService.findOneById(refEntityMetaData.getName(), "4");
		refEntity4.set(ATTR_REF_STRING, "qwerty");
		runAsSystem(() -> dataService.update(refEntityMetaData.getName(), refEntity4));

		Entity refEntity5 = dataService.findOneById(refEntityMetaData.getName(), "5");
		refEntity5.set(ATTR_REF_STRING, "qwerty");
		runAsSystem(() -> dataService.update(refEntityMetaData.getName(), refEntity5));

		waitForIndexToBeStable(entityMetaData.getName());
		assertEquals(searchService.count(q, entityMetaData), 0);

		assertEquals(searchService.count(new QueryImpl<>().search("qwerty"), entityMetaData), 3333);
	}

	@Test
	public void testUpdateStream()
	{
		Entity entity = create(1).findFirst().get();

		dataService.add(entityMetaData.getName(), entity);
		waitForIndexToBeStable(entityMetaData.getName());
		assertPresent(entityMetaData, entity);

		entity = dataService.findOneById(entityMetaData.getName(), entity.getIdValue());
		assertNotNull(entity);
		assertEquals(entity.get(ATTR_STRING), "string1");

		entity.set(ATTR_STRING, "qwerty");
		Query<Entity> q = new QueryImpl<>();
		q.eq(ATTR_STRING, "qwerty");

		assertEquals(searchService.count(q, entityMetaData), 0);

		dataService.update(entityMetaData.getName(), of(entity));
		waitForIndexToBeStable(entityMetaData.getName());

		assertEquals(searchService.count(q, entityMetaData), 1);

		assertPresent(entityMetaData, entity);
		entity = dataService.findOneById(entityMetaData.getName(), entity.getIdValue());
		assertNotNull(entity.get(ATTR_STRING));
		assertEquals(entity.get(ATTR_STRING), "qwerty");
	}

	private Stream<Entity> create(int count)
	{
		List<Entity> refEntities = testHarness.createTestRefEntities(refEntityMetaData, 6);
		runAsSystem(() -> dataService.add(refEntityMetaData.getName(), refEntities.stream()));
		return testHarness.createTestEntities(entityMetaData, count, refEntities);
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
		assertNull(dataService.findOneById(entityMetaData.getName(), entity.getIdValue()));

		// Not found in index Elasticsearch
		Query<Entity> q = new QueryImpl<>();
		q.eq(entityMetaData.getIdAttribute().getName(), entity.getIdValue());
		assertEquals(searchService.count(q, entityMetaData), 0);
	}

	@Test
	public void testCreateSelfXref()
	{
		Entity entitySelfXref = entitySelfXrefTestHarness.createTestEntities(selfXrefEntityMetaData, 1).collect(Collectors.toList()).get(0);

		//Create
		dataService.add(selfXrefEntityMetaData.getName(), entitySelfXref);
		waitForIndexToBeStable(selfXrefEntityMetaData.getName());
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
		waitForIndexToBeStable(selfXrefEntityMetaData.getName());
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
}
