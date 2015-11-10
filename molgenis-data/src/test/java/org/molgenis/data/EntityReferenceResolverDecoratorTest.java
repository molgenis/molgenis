package org.molgenis.data;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;

import org.molgenis.data.support.QueryImpl;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class EntityReferenceResolverDecoratorTest
{
	private Repository decoratedRepo;
	private EntityMetaData entityMeta;
	private EntityManager entityManager;
	private EntityReferenceResolverDecorator entityReferenceResolverDecorator;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		decoratedRepo = mock(Repository.class);
		entityMeta = mock(EntityMetaData.class);
		when(decoratedRepo.getEntityMetaData()).thenReturn(entityMeta);
		entityManager = mock(EntityManager.class);
		entityReferenceResolverDecorator = new EntityReferenceResolverDecorator(decoratedRepo, entityManager);
	}

	@SuppressWarnings("resource")
	@Test(expectedExceptions = NullPointerException.class)
	public void EntityReferenceResolverDecorator()
	{
		new EntityReferenceResolverDecorator(null, null);
	}

	@Test
	public void addEntity()
	{
		Entity entity = mock(Entity.class);
		entityReferenceResolverDecorator.add(entity);
		verify(decoratedRepo, times(1)).add(entity);
		verifyZeroInteractions(entityManager);
	}

	@Test
	public void addIterableextendsEntity()
	{
		@SuppressWarnings("unchecked")
		Iterable<Entity> entities = mock(Iterable.class);
		entityReferenceResolverDecorator.add(entities);
		verify(decoratedRepo, times(1)).add(entities);
		verifyZeroInteractions(entityManager);
	}

	@Test
	public void aggregate()
	{
		AggregateQuery aggregateQuery = mock(AggregateQuery.class);
		entityReferenceResolverDecorator.aggregate(aggregateQuery);
		verify(decoratedRepo, times(1)).aggregate(aggregateQuery);
		verifyZeroInteractions(entityManager);
	}

	@Test
	public void clearCache()
	{
		entityReferenceResolverDecorator.clearCache();
		verify(decoratedRepo, times(1)).clearCache();
		verifyZeroInteractions(entityManager);
	}

	@Test
	public void close() throws IOException
	{
		entityReferenceResolverDecorator.close();
		verify(decoratedRepo, times(1)).close();
		verifyZeroInteractions(entityManager);
	}

	@Test
	public void count()
	{
		entityReferenceResolverDecorator.count();
		verify(decoratedRepo, times(1)).count();
		verifyZeroInteractions(entityManager);
	}

	@Test
	public void countQuery()
	{
		Query q = mock(Query.class);
		entityReferenceResolverDecorator.count(q);
		verify(decoratedRepo, times(1)).count(q);
		verifyZeroInteractions(entityManager);
	}

	@Test
	public void create()
	{
		entityReferenceResolverDecorator.create();
		verify(decoratedRepo, times(1)).create();
		verifyZeroInteractions(entityManager);
	}

	@Test
	public void deleteEntity()
	{
		Entity entity = mock(Entity.class);
		entityReferenceResolverDecorator.delete(entity);
		verify(decoratedRepo, times(1)).delete(entity);
		verifyZeroInteractions(entityManager);
	}

	@Test
	public void deleteIterableextendsEntity()
	{
		@SuppressWarnings("unchecked")
		Iterable<Entity> entities = mock(Iterable.class);
		entityReferenceResolverDecorator.delete(entities);
		verify(decoratedRepo, times(1)).delete(entities);
		verifyZeroInteractions(entityManager);
	}

	@Test
	public void deleteAll()
	{
		entityReferenceResolverDecorator.deleteAll();
		verify(decoratedRepo, times(1)).deleteAll();
		verifyZeroInteractions(entityManager);
	}

	@Test
	public void deleteByIdObject()
	{
		Object id = mock(Object.class);
		entityReferenceResolverDecorator.deleteById(id);
		verify(decoratedRepo, times(1)).deleteById(id);
		verifyZeroInteractions(entityManager);
	}

	@Test
	public void deleteByIdIterableObject()
	{
		@SuppressWarnings("unchecked")
		Iterable<Object> ids = mock(Iterable.class);
		entityReferenceResolverDecorator.deleteById(ids);
		verify(decoratedRepo, times(1)).deleteById(ids);
		verifyZeroInteractions(entityManager);
	}

	@Test
	public void drop()
	{
		entityReferenceResolverDecorator.drop();
		verify(decoratedRepo, times(1)).drop();
		verifyZeroInteractions(entityManager);
	}

	@Test
	public void findAllQueryFetch()
	{
		Fetch fetch = new Fetch();
		Query q = mock(Query.class);
		when(q.getFetch()).thenReturn(fetch);
		@SuppressWarnings("unchecked")
		Iterable<Entity> entities = mock(Iterable.class);
		when(decoratedRepo.findAll(q)).thenReturn(entities);
		entityReferenceResolverDecorator.findAll(q);
		verify(decoratedRepo, times(1)).findAll(q);
		verify(entityManager).resolveReferences(entityMeta, entities, fetch);
	}

	@Test
	public void findAllQueryNoFetch()
	{
		Query q = mock(Query.class);
		@SuppressWarnings("unchecked")
		Iterable<Entity> entities = mock(Iterable.class);
		when(decoratedRepo.findAll(q)).thenReturn(entities);
		entityReferenceResolverDecorator.findAll(q);
		verify(decoratedRepo, times(1)).findAll(q);
		verify(entityManager).resolveReferences(entityMeta, entities, null);
	}

	@Test
	public void findAllIterableObject()
	{
		@SuppressWarnings("unchecked")
		Iterable<Object> ids = mock(Iterable.class);
		@SuppressWarnings("unchecked")
		Iterable<Entity> entities = mock(Iterable.class);
		when(decoratedRepo.findAll(ids)).thenReturn(entities);
		entityReferenceResolverDecorator.findAll(ids);
		verify(decoratedRepo, times(1)).findAll(ids);
		verify(entityManager).resolveReferences(entityMeta, entities, null);
	}

	@Test
	public void findAllIterableObjectFetch()
	{
		Fetch fetch = new Fetch();
		@SuppressWarnings("unchecked")
		Iterable<Object> ids = mock(Iterable.class);
		@SuppressWarnings("unchecked")
		Iterable<Entity> entities = mock(Iterable.class);
		when(decoratedRepo.findAll(ids, fetch)).thenReturn(entities);
		entityReferenceResolverDecorator.findAll(ids, fetch);
		verify(decoratedRepo, times(1)).findAll(ids, fetch);
		verify(entityManager).resolveReferences(entityMeta, entities, fetch);
	}

	@Test
	public void findAllIterableObjectFetchNull()
	{
		@SuppressWarnings("unchecked")
		Iterable<Object> ids = mock(Iterable.class);
		@SuppressWarnings("unchecked")
		Iterable<Entity> entities = mock(Iterable.class);
		when(decoratedRepo.findAll(ids, null)).thenReturn(entities);
		entityReferenceResolverDecorator.findAll(ids, null);
		verify(decoratedRepo, times(1)).findAll(ids, null);
		verify(entityManager).resolveReferences(entityMeta, entities, null);
	}

	@Test
	public void findOneQueryFetchEntity()
	{
		Fetch fetch = new Fetch();
		Query q = mock(Query.class);
		when(q.getFetch()).thenReturn(fetch);
		Entity entity = mock(Entity.class);
		when(decoratedRepo.findOne(q)).thenReturn(entity);
		entityReferenceResolverDecorator.findOne(q);
		verify(decoratedRepo, times(1)).findOne(q);
		verify(entityManager).resolveReferences(entityMeta, entity, fetch);
	}

	@Test
	public void findOneQueryFetchEntityNull()
	{
		Fetch fetch = new Fetch();
		Query q = mock(Query.class);
		when(q.getFetch()).thenReturn(fetch);
		entityReferenceResolverDecorator.findOne(q);
		verify(decoratedRepo, times(1)).findOne(q);
		verifyZeroInteractions(entityManager);
	}

	@Test
	public void findOneQueryNoFetch()
	{
		Query q = mock(Query.class);
		entityReferenceResolverDecorator.findOne(q);
		verify(decoratedRepo, times(1)).findOne(q);
		verifyZeroInteractions(entityManager);
	}

	@Test
	public void findOneObject()
	{
		Object id = mock(Object.class);
		entityReferenceResolverDecorator.findOne(id);
		verify(decoratedRepo, times(1)).findOne(id);
		verifyZeroInteractions(entityManager);
	}

	@Test
	public void findOneObjectFetch()
	{
		Object id = Integer.valueOf(1);
		Fetch fetch = new Fetch();
		Entity entity = mock(Entity.class);
		when(decoratedRepo.findOne(id, fetch)).thenReturn(entity);
		entityReferenceResolverDecorator.findOne(id, fetch);
		verify(decoratedRepo, times(1)).findOne(id, fetch);
		verify(entityManager).resolveReferences(entityMeta, entity, fetch);
	}

	@Test
	public void findOneObjectFetchNull()
	{
		Object id = Integer.valueOf(1);
		entityReferenceResolverDecorator.findOne(id, null);
		verify(decoratedRepo, times(1)).findOne(id, null);
		verifyZeroInteractions(entityManager);
	}

	@Test
	public void flush()
	{
		entityReferenceResolverDecorator.flush();
		verify(decoratedRepo, times(1)).flush();
		verifyZeroInteractions(entityManager);
	}

	@Test
	public void getCapabilities()
	{
		entityReferenceResolverDecorator.getCapabilities();
		verify(decoratedRepo, times(1)).getCapabilities();
		verifyZeroInteractions(entityManager);
	}

	@Test
	public void getEntityMetaData()
	{
		entityReferenceResolverDecorator.getEntityMetaData();
		verify(decoratedRepo, times(1)).getEntityMetaData();
		verifyZeroInteractions(entityManager);
	}

	@Test
	public void getName()
	{
		entityReferenceResolverDecorator.getName();
		verify(decoratedRepo, times(1)).getName();
		verifyZeroInteractions(entityManager);
	}

	@Test
	public void iterator()
	{
		QueryImpl q = new QueryImpl();
		Iterable<Entity> entities = Arrays.asList(mock(Entity.class));
		when(decoratedRepo.findAll(q)).thenReturn(entities);
		when(entityManager.resolveReferences(entityMeta, entities, null)).thenReturn(entities);
		entityReferenceResolverDecorator.iterator();
		verify(decoratedRepo, times(1)).findAll(q);
		verify(entityManager).resolveReferences(entityMeta, entities, null);
	}

	@Test
	public void query()
	{
		entityReferenceResolverDecorator.query();
		verify(decoratedRepo, times(1)).query();
		verifyZeroInteractions(entityManager);
	}

	@Test
	public void rebuildIndex()
	{
		entityReferenceResolverDecorator.rebuildIndex();
		verify(decoratedRepo, times(1)).rebuildIndex();
		verifyZeroInteractions(entityManager);
	}

	@Test
	public void updateEntity()
	{
		Entity entity = mock(Entity.class);
		entityReferenceResolverDecorator.update(entity);
		verify(decoratedRepo, times(1)).update(entity);
		verifyZeroInteractions(entityManager);
	}

	@Test
	public void updateIterableextendsEntity()
	{
		@SuppressWarnings("unchecked")
		Iterable<Entity> entities = mock(Iterable.class);
		entityReferenceResolverDecorator.update(entities);
		verify(decoratedRepo, times(1)).update(entities);
		verifyZeroInteractions(entityManager);
	}
}
