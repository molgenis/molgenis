package org.molgenis.integrationtest.data;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.generate;
import static java.util.stream.Stream.of;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.molgenis.data.EditableEntityMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityListener;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Fetch;
import org.molgenis.data.Package;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.meta.PackageImpl;
import org.molgenis.data.support.DefaultEntity;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;

import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;

public abstract class AbstractDataServiceTest extends AbstractDataIntegrationTest
{
	private static final String ENTITY_NAME = "test_TestEntity";
	private static final String ID = "id";
	private static final String ATTR_STR = "strAttr";
	private EditableEntityMetaData entityMetaData;

	@BeforeClass
	public void setUp()
	{
		Package p = new PackageImpl("test");
		entityMetaData = new DefaultEntityMetaData("TestEntity", p);
		entityMetaData.addAttribute(ID).setIdAttribute(true).setNillable(false).setAuto(true);
		entityMetaData.addAttribute(ATTR_STR).setNillable(true);
		metaDataService.addEntityMeta(entityMetaData);
	}

	@AfterMethod
	public void afterMethod()
	{
		dataService.deleteAll(ENTITY_NAME);
	}

	public void testAddIterable()
	{
		List<Entity> entities = create(9);
		dataService.add(ENTITY_NAME, entities);
		assertCount(9);
		assertPresent(entities);
	}

	public void testAddStream()
	{
		List<Entity> entities = create(9);
		dataService.add(ENTITY_NAME, entities.stream());
		assertCount(9);
		assertPresent(entities);
	}

	public void testEntityListener()
	{
		List<Entity> entities = create(2);
		dataService.add(ENTITY_NAME, entities);

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
			dataService.addEntityListener(ENTITY_NAME, listener);
			dataService.update(ENTITY_NAME, entities);
			assertEquals(updateCalled.get(), 1);
		}
		finally
		{
			dataService.removeEntityListener(ENTITY_NAME, listener);
			updateCalled.set(0);
			dataService.update(ENTITY_NAME, entities);
			assertEquals(updateCalled.get(), 0);
		}
	}

	public void testCount()
	{
		List<Entity> entities = create(3);
		dataService.add(ENTITY_NAME, entities);
		assertEquals(dataService.count(ENTITY_NAME, new QueryImpl()), 3);
		assertEquals(dataService.count(ENTITY_NAME, new QueryImpl().eq(ID, entities.get(0).getIdValue())), 1);
	}

	public void testDelete()
	{
		Entity entity = create();
		dataService.add(ENTITY_NAME, entity);
		assertPresent(entity);

		dataService.delete(ENTITY_NAME, entity);
		assertNull(dataService.findOne(ENTITY_NAME, entity.getIdValue()));
	}

	public void testDeleteIterable()
	{
		List<Entity> entities = create(5);
		dataService.add(ENTITY_NAME, entities);
		assertEquals(dataService.count(ENTITY_NAME, new QueryImpl()), entities.size());

		dataService.delete(ENTITY_NAME, entities);
		assertEquals(dataService.count(ENTITY_NAME, new QueryImpl()), 0);
	}

	public void testDeleteById()
	{
		Entity entity = create();
		dataService.add(ENTITY_NAME, entity);
		assertPresent(entity);

		dataService.delete(ENTITY_NAME, entity.getIdValue());
		assertNull(dataService.findOne(ENTITY_NAME, entity.getIdValue()));
	}

	public void testDeleteStream()
	{
		List<Entity> entities = create(5);
		dataService.add(ENTITY_NAME, entities);
		assertEquals(dataService.count(ENTITY_NAME, new QueryImpl()), entities.size());

		dataService.delete(ENTITY_NAME, entities.stream());
		assertEquals(dataService.count(ENTITY_NAME, new QueryImpl()), 0);
	}

	public void testDeleteAll()
	{
		List<Entity> entities = create(5);
		dataService.add(ENTITY_NAME, entities);
		assertEquals(dataService.count(ENTITY_NAME, new QueryImpl()), entities.size());

		dataService.deleteAll(ENTITY_NAME);
		assertEquals(dataService.count(ENTITY_NAME, new QueryImpl()), 0);
	}

	public void testFindAll()
	{
		List<Entity> entities = create(5);
		dataService.add(ENTITY_NAME, entities);
		Iterable<Entity> retrieved = dataService.findAll(ENTITY_NAME);
		assertEquals(Iterables.size(retrieved), entities.size());
	}

	public void testFindAllTyped()
	{
		List<Entity> entities = create(1);
		dataService.add(ENTITY_NAME, entities);
		Iterable<TestEntity> retrieved = dataService.findAll(ENTITY_NAME, TestEntity.class);
		assertEquals(Iterables.size(retrieved), 1);
		assertEquals(retrieved.iterator().next().getId(), entities.get(0).getIdValue());
	}

	public void testFindAllIterable()
	{
		List<Entity> entities = create(5);
		dataService.add(ENTITY_NAME, entities);
		List<Object> ids = entities.stream().map(Entity::getIdValue).collect(toList());
		ids.add("bogus");
		Iterable<Entity> retrieved = dataService.findAll(ENTITY_NAME, ids);
		assertEquals(Iterables.size(retrieved), entities.size());
	}

	public void testFindAllIterableTyped()
	{
		List<Entity> entities = create(5);
		dataService.add(ENTITY_NAME, entities);
		List<Object> ids = entities.stream().map(Entity::getIdValue).collect(toList());
		ids.add("bogus");
		Iterable<TestEntity> retrieved = dataService.findAll(ENTITY_NAME, ids, TestEntity.class);
		assertEquals(Iterables.size(retrieved), entities.size());
		assertEquals(retrieved.iterator().next().getId(), entities.get(0).getIdValue());
	}

	public void testFindAllIterableFetch()
	{
		List<Entity> entities = create(5);
		dataService.add(ENTITY_NAME, entities);
		List<Object> ids = entities.stream().map(Entity::getIdValue).collect(toList());
		ids.add("bogus");
		Iterable<Entity> retrieved = dataService.findAll(ENTITY_NAME, ids, new Fetch().field(ID));
		assertEquals(Iterables.size(retrieved), entities.size());
	}

	public void testFindAllStream()
	{
		List<Entity> entities = create(5);
		dataService.add(ENTITY_NAME, entities);
		Stream<Object> ids = concat(entities.stream().map(Entity::getIdValue), of("bogus"));
		Stream<Entity> retrieved = dataService.findAll(ENTITY_NAME, ids);
		assertEquals(retrieved.count(), entities.size());
	}

	public void testFindAllTypedStream()
	{
		List<Entity> entities = create(5);
		dataService.add(ENTITY_NAME, entities);
		Stream<Object> ids = concat(entities.stream().map(Entity::getIdValue), of("bogus"));
		Stream<TestEntity> retrieved = dataService.findAll(ENTITY_NAME, ids, TestEntity.class);
		assertEquals(retrieved.count(), entities.size());

		ids = concat(entities.stream().map(Entity::getIdValue), of("bogus"));
		retrieved = dataService.findAll(ENTITY_NAME, ids, TestEntity.class);
		assertEquals(retrieved.iterator().next().getId(), entities.get(0).getIdValue());
	}

	public void testFindAllStreamFetch()
	{
		List<Entity> entities = create(5);
		dataService.add(ENTITY_NAME, entities);
		Stream<Object> ids = concat(entities.stream().map(Entity::getIdValue), of("bogus"));
		Stream<Entity> retrieved = dataService.findAll(ENTITY_NAME, ids, new Fetch().field(ID));
		assertEquals(retrieved.count(), entities.size());
	}

	public void testFindAllTypedFetchStream()
	{
		List<Entity> entities = create(5);
		dataService.add(ENTITY_NAME, entities);
		Stream<Object> ids = concat(entities.stream().map(Entity::getIdValue), of("bogus"));
		Stream<TestEntity> retrieved = dataService.findAll(ENTITY_NAME, ids, TestEntity.class);
		assertEquals(retrieved.count(), entities.size());

		ids = concat(entities.stream().map(Entity::getIdValue), of("bogus"));
		retrieved = dataService.findAll(ENTITY_NAME, ids, new Fetch().field(ID), TestEntity.class);
		assertEquals(retrieved.iterator().next().getId(), entities.get(0).getIdValue());
	}

	public void testFindAllAsStream()
	{
		List<Entity> entities = create(5);
		dataService.add(ENTITY_NAME, entities);
		assertEquals(dataService.findAllAsStream(ENTITY_NAME).count(), entities.size());
	}

	public void testFindAllAsStreamTyped()
	{
		List<Entity> entities = create(5);
		dataService.add(ENTITY_NAME, entities);
		assertEquals(dataService.findAllAsStream(ENTITY_NAME, TestEntity.class).count(), entities.size());
		assertEquals(dataService.findAllAsStream(ENTITY_NAME, TestEntity.class).findFirst().get().getId(), entities
				.get(0).getIdValue());
	}

	public void testFindAsStreamQuery()
	{
		List<Entity> entities = create(5);
		dataService.add(ENTITY_NAME, entities);
		Stream<Entity> found = dataService.findAllAsStream(ENTITY_NAME,
				new QueryImpl().eq(ID, entities.get(0).getIdValue()));
		assertEquals(found.count(), 1);

		found = dataService.findAllAsStream(ENTITY_NAME, new QueryImpl().eq(ID, entities.get(0).getIdValue()));
		assertEquals(found.findFirst().get().getIdValue(), entities.get(0).getIdValue());
	}

	public void testFindAsStreamQueryTyped()
	{
		List<Entity> entities = create(5);
		dataService.add(ENTITY_NAME, entities);
		Stream<TestEntity> found = dataService.findAllAsStream(ENTITY_NAME,
				new QueryImpl().eq(ID, entities.get(0).getIdValue()), TestEntity.class);
		assertEquals(found.count(), 1);

		found = dataService.findAllAsStream(ENTITY_NAME, new QueryImpl().eq(ID, entities.get(0).getIdValue()),
				TestEntity.class);
		assertEquals(found.findFirst().get().getId(), entities.get(0).getIdValue());
	}

	public void testFindOne()
	{
		List<Entity> entities = create(1);
		dataService.add(ENTITY_NAME, entities);
		assertNotNull(dataService.findOne(ENTITY_NAME, entities.get(0).getIdValue()));
	}

	public void testFindOneTyped()
	{
		List<Entity> entities = create(1);
		dataService.add(ENTITY_NAME, entities);
		TestEntity testEntity = dataService.findOne(ENTITY_NAME, entities.get(0).getIdValue(), TestEntity.class);
		assertNotNull(testEntity);
		assertEquals(testEntity.getId(), entities.get(0).getIdValue());
	}

	public void testFindOneFetch()
	{
		List<Entity> entities = create(1);
		dataService.add(ENTITY_NAME, entities);
		assertNotNull(dataService.findOne(ENTITY_NAME, entities.get(0).getIdValue(), new Fetch().field(ID)));
	}

	public void testFindOneFetchTyped()
	{
		List<Entity> entities = create(1);
		dataService.add(ENTITY_NAME, entities);
		TestEntity testEntity = dataService.findOne(ENTITY_NAME, entities.get(0).getIdValue(), new Fetch().field(ID),
				TestEntity.class);
		assertNotNull(testEntity);
		assertEquals(testEntity.getId(), entities.get(0).getIdValue());
	}

	public void testFindOneQuery()
	{
		List<Entity> entities = create(1);
		dataService.add(ENTITY_NAME, entities);
		Entity entity = dataService.findOne(ENTITY_NAME, new QueryImpl().eq(ID, entities.get(0).getIdValue()));
		assertNotNull(entity);
	}

	public void testFindOneQueryTyped()
	{
		List<Entity> entities = create(1);
		dataService.add(ENTITY_NAME, entities);
		TestEntity entity = dataService.findOne(ENTITY_NAME, new QueryImpl().eq(ID, entities.get(0).getIdValue()),
				TestEntity.class);
		assertNotNull(entity);
		assertEquals(entity.getId(), entities.get(0).getIdValue());
	}

	public void testGetCapabilities()
	{
		Set<RepositoryCapability> capabilities = dataService.getCapabilities(ENTITY_NAME);
		assertNotNull(capabilities);
		assertEquals(capabilities.size(), 5);
	}

	public void testGetEntityMetaData()
	{
		EntityMetaData emd = dataService.getEntityMetaData(ENTITY_NAME);
		assertNotNull(emd);
		assertEquals(emd, entityMetaData);
	}

	public void testGetEntityNames()
	{
		Iterable<String> names = dataService.getEntityNames();
		assertNotNull(names);
		assertTrue(Iterables.contains(names, ENTITY_NAME));
	}

	public void testGetMeta()
	{
		assertNotNull(dataService.getMeta());
	}

	public void testGetRepository()
	{
		Repository repo = dataService.getRepository(ENTITY_NAME);
		assertNotNull(repo);
		assertEquals(repo.getName(), ENTITY_NAME);

		try
		{
			dataService.getRepository("bogus");
			fail("Should have thrown UnknownEntityException");
		}
		catch (UnknownEntityException e)
		{
			// Expected
		}
	}

	public void testHasRepository()
	{
		assertTrue(dataService.hasRepository(ENTITY_NAME));
		assertFalse(dataService.hasRepository("bogus"));
	}

	public void testIterator()
	{
		assertNotNull(dataService.iterator());
		assertTrue(Iterators.contains(dataService.iterator(), dataService.getRepository(ENTITY_NAME)));
	}

	public void testQuery()
	{
		assertNotNull(dataService.query(ENTITY_NAME));
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

	public void testUpdate()
	{
		Entity entity = create(1).get(0);
		dataService.add(ENTITY_NAME, entity);
		entity = dataService.findOne(ENTITY_NAME, entity.getIdValue());
		assertNotNull(entity);
		assertNull(entity.get(ATTR_STR));

		entity.set(ATTR_STR, "qwerty");
		dataService.update(ENTITY_NAME, entity);
		entity = dataService.findOne(ENTITY_NAME, entity.getIdValue());
		assertNotNull(entity.get(ATTR_STR));
		assertEquals(entity.get(ATTR_STR), "qwerty");
	}

	public void testUpdateStream()
	{
		Entity entity = create(1).get(0);
		dataService.add(ENTITY_NAME, entity);
		entity = dataService.findOne(ENTITY_NAME, entity.getIdValue());
		assertNotNull(entity);
		assertNull(entity.get(ATTR_STR));

		entity.set(ATTR_STR, "qwerty");
		dataService.update(ENTITY_NAME, of(entity));
		entity = dataService.findOne(ENTITY_NAME, entity.getIdValue());
		assertNotNull(entity.get(ATTR_STR));
		assertEquals(entity.get(ATTR_STR), "qwerty");
	}

	private List<Entity> create(int count)
	{
		return generate(() -> create()).limit(count).collect(toList());
	}

	private Entity create()
	{
		return new DefaultEntity(entityMetaData, dataService);
	}

	private void assertPresent(List<Entity> entities)
	{
		entities.forEach(this::assertPresent);
	}

	private void assertPresent(Entity entity)
	{
		assertNotNull(dataService.findOne(entityMetaData.getName(), entity.getIdValue()));
	}

	private void assertCount(int count)
	{
		assertEquals(dataService.count(ENTITY_NAME, new QueryImpl()), count);
	}

	public static class TestEntity extends MapEntity
	{
		private static final long serialVersionUID = 1L;

		public String getId()
		{
			return getString(ID);
		}

		public void setId(String id)
		{
			set(ID, id);
		}
	}
}
