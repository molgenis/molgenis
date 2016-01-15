package org.molgenis.integrationtest.data;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.generate;
import static java.util.stream.Stream.of;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.molgenis.data.EditableEntityMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityListener;
import org.molgenis.data.Fetch;
import org.molgenis.data.Package;
import org.molgenis.data.meta.PackageImpl;
import org.molgenis.data.support.DefaultEntity;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;

import com.google.common.collect.Iterables;

public abstract class AbstractDataServiceTest extends AbstractDataIntegrationTest
{
	private static final String ENTITY_NAME = "test_TestEntity";
	private static final String ID = "id";
	private EditableEntityMetaData entityMetaData;

	@BeforeClass
	public void setUp()
	{
		Package p = new PackageImpl("test");
		entityMetaData = new DefaultEntityMetaData("TestEntity", p);
		entityMetaData.addAttribute(ID).setIdAttribute(true).setNillable(false).setAuto(true);
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
		entities.stream().forEach(this::assertPresent);
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
