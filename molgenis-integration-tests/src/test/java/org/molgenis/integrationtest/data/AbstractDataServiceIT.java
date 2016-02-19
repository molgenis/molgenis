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
import java.util.function.Supplier;
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

import com.google.common.collect.Iterators;
import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;

public abstract class AbstractDataServiceIT extends AbstractDataIntegrationIT
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
		entityMetaData.addAttribute(ID, ROLE_ID).setNillable(false).setAuto(true);
		entityMetaData.addAttribute(ATTR_STR).setNillable(true);
		metaDataService.addEntityMeta(entityMetaData);
	}

	@AfterMethod
	public void afterMethod()
	{
		dataService.deleteAll(ENTITY_NAME);
	}

	public void testAdd()
	{
		List<Entity> entities = create(9);
		dataService.add(ENTITY_NAME, entities.stream());
		assertCount(9);
		assertPresent(entities);
	}

	public void testEntityListener()
	{
		List<Entity> entities = create(2);
		dataService.add(ENTITY_NAME, entities.stream());

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
			dataService.update(ENTITY_NAME, entities.stream());
			assertEquals(updateCalled.get(), 1);
		}
		finally
		{
			dataService.removeEntityListener(ENTITY_NAME, listener);
			updateCalled.set(0);
			dataService.update(ENTITY_NAME, entities.stream());
			assertEquals(updateCalled.get(), 0);
		}
	}

	public void testCount()
	{
		List<Entity> entities = create(3);
		dataService.add(ENTITY_NAME, entities.stream());
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
		dataService.add(ENTITY_NAME, entities.stream());
		assertEquals(dataService.count(ENTITY_NAME, new QueryImpl()), entities.size());

		dataService.delete(ENTITY_NAME, entities.stream());
		assertEquals(dataService.count(ENTITY_NAME, new QueryImpl()), 0);
	}

	public void testDeleteAll()
	{
		List<Entity> entities = create(5);
		dataService.add(ENTITY_NAME, entities.stream());
		assertEquals(dataService.count(ENTITY_NAME, new QueryImpl()), entities.size());

		dataService.deleteAll(ENTITY_NAME);
		assertEquals(dataService.count(ENTITY_NAME, new QueryImpl()), 0);
	}

	public void testFindAllEmpty() {
		Stream<Entity> retrieved = dataService.findAll(ENTITY_NAME);
		assertEquals(retrieved.count(), 0);
	}
	
	public void testFindAll()
	{
		List<Entity> entities = create(5);
		dataService.add(ENTITY_NAME, entities.stream());
		Stream<Entity> retrieved = dataService.findAll(ENTITY_NAME);
		assertEquals(retrieved.count(), entities.size());
	}

	public void testFindAllTyped()
	{
		List<Entity> entities = create(1);
		dataService.add(ENTITY_NAME, entities.stream());
		Supplier<Stream<TestEntity>> retrieved = () -> dataService.findAll(ENTITY_NAME, TestEntity.class);
		assertEquals(retrieved.get().count(), 1);
		assertEquals(retrieved.get().iterator().next().getId(), entities.get(0).getIdValue());
	}

	public void testFindAllByIds()
	{
		List<Entity> entities = create(5);
		dataService.add(ENTITY_NAME, entities.stream());
		Stream<Object> ids = Stream.concat(entities.stream().map(Entity::getIdValue), of("bogus"));
		Stream<Entity> retrieved = dataService.findAll(ENTITY_NAME, ids);
		assertEquals(retrieved.count(), entities.size());
	}

	public void testFindAllByIdsTyped()
	{
		List<Entity> entities = create(5);
		dataService.add(ENTITY_NAME, entities.stream());

		Supplier<Stream<TestEntity>> retrieved = () -> dataService.findAll(ENTITY_NAME,
				Stream.concat(entities.stream().map(Entity::getIdValue), of("bogus")), TestEntity.class);
		assertEquals(retrieved.get().count(), entities.size());
		assertEquals(retrieved.get().iterator().next().getId(), entities.get(0).getIdValue());
	}

	public void testFindAllStreamFetch()
	{
		List<Entity> entities = create(5);
		dataService.add(ENTITY_NAME, entities.stream());
		Stream<Object> ids = concat(entities.stream().map(Entity::getIdValue), of("bogus"));
		Stream<Entity> retrieved = dataService.findAll(ENTITY_NAME, ids, new Fetch().field(ID));
		assertEquals(retrieved.count(), entities.size());
	}

	public void testFindQuery()
	{
		List<Entity> entities = create(5);
		dataService.add(ENTITY_NAME, entities.stream());
		Supplier<Stream<Entity>> found = () -> dataService.findAll(ENTITY_NAME,
				new QueryImpl().eq(ID, entities.get(0).getIdValue()));
		assertEquals(found.get().count(), 1);
		assertEquals(found.get().findFirst().get().getIdValue(), entities.get(0).getIdValue());
	}

	public void testFindQueryTyped()
	{
		List<Entity> entities = create(5);
		dataService.add(ENTITY_NAME, entities.stream());
		Supplier<Stream<TestEntity>> found = () -> dataService.findAll(ENTITY_NAME,
				new QueryImpl().eq(ID, entities.get(0).getIdValue()), TestEntity.class);
		assertEquals(found.get().count(), 1);
		assertEquals(found.get().findFirst().get().getId(), entities.get(0).getIdValue());
	}

	public void testFindOne()
	{
		List<Entity> entities = create(1);
		dataService.add(ENTITY_NAME, entities.stream());
		assertNotNull(dataService.findOne(ENTITY_NAME, entities.get(0).getIdValue()));
	}

	public void testFindOneTyped()
	{
		List<Entity> entities = create(1);
		dataService.add(ENTITY_NAME, entities.stream());
		TestEntity testEntity = dataService.findOne(ENTITY_NAME, entities.get(0).getIdValue(), TestEntity.class);
		assertNotNull(testEntity);
		assertEquals(testEntity.getId(), entities.get(0).getIdValue());
	}

	public void testFindOneFetch()
	{
		List<Entity> entities = create(1);
		dataService.add(ENTITY_NAME, entities.stream());
		assertNotNull(dataService.findOne(ENTITY_NAME, entities.get(0).getIdValue(), new Fetch().field(ID)));
	}

	public void testFindOneFetchTyped()
	{
		List<Entity> entities = create(1);
		dataService.add(ENTITY_NAME, entities.stream());
		TestEntity testEntity = dataService.findOne(ENTITY_NAME, entities.get(0).getIdValue(), new Fetch().field(ID),
				TestEntity.class);
		assertNotNull(testEntity);
		assertEquals(testEntity.getId(), entities.get(0).getIdValue());
	}

	public void testFindOneQuery()
	{
		List<Entity> entities = create(1);
		dataService.add(ENTITY_NAME, entities.stream());
		Entity entity = dataService.findOne(ENTITY_NAME, new QueryImpl().eq(ID, entities.get(0).getIdValue()));
		assertNotNull(entity);
	}

	public void testFindOneQueryTyped()
	{
		List<Entity> entities = create(1);
		dataService.add(ENTITY_NAME, entities.stream());
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
		Stream<String> names = dataService.getEntityNames();
		assertNotNull(names);
		assertTrue(names.filter(ENTITY_NAME::equals).findFirst().isPresent());
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
