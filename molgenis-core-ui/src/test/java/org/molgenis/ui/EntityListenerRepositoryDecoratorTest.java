package org.molgenis.ui;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.mockito.ArgumentCaptor;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityListener;
import org.molgenis.data.Fetch;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class EntityListenerRepositoryDecoratorTest
{
	private Repository<Entity> decoratedRepository;
	private EntityListenerRepositoryDecorator entityListenerRepositoryDecorator;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		decoratedRepository = mock(Repository.class);
		entityListenerRepositoryDecorator = new EntityListenerRepositoryDecorator(decoratedRepository);
	}

	@SuppressWarnings("resource")
	@Test(expectedExceptions = NullPointerException.class)
	public void EntityListenerRepositoryDecorator()
	{
		new EntityListenerRepositoryDecorator(null);
	}

	@Test
	public void addStream()
	{
		Stream<Entity> entities = Stream.empty();
		when(decoratedRepository.add(entities)).thenReturn(Integer.valueOf(123));
		assertEquals(entityListenerRepositoryDecorator.add(entities), Integer.valueOf(123));
	}

	@Test
	public void deleteStream()
	{
		Stream<Entity> entities = Stream.empty();
		entityListenerRepositoryDecorator.delete(entities);
		verify(decoratedRepository, times(1)).delete(entities);
	}

	@SuppressWarnings("resource")
	@Test
	public void updateEntityWithListener()
	{
		Repository<Entity> decoratedRepository = mock(Repository.class);
		EntityListenerRepositoryDecorator entityListenerRepositoryDecorator = new EntityListenerRepositoryDecorator(
				decoratedRepository);
		EntityListener entityListener0 = when(mock(EntityListener.class).getEntityId()).thenReturn(Integer.valueOf(1))
				.getMock();
		entityListenerRepositoryDecorator.addEntityListener(entityListener0);

		Entity entity = when(mock(Entity.class).getIdValue()).thenReturn(Integer.valueOf(1)).getMock();
		entityListenerRepositoryDecorator.update(entity);

		verify(decoratedRepository).update(entity);
		verify(entityListener0, times(1)).postUpdate(entity);
	}

	@SuppressWarnings("resource")
	@Test
	public void updateEntityWithListeners()
	{
		Repository<Entity> decoratedRepository = mock(Repository.class);
		EntityListenerRepositoryDecorator entityListenerRepositoryDecorator = new EntityListenerRepositoryDecorator(
				decoratedRepository);
		EntityListener entityListener0 = when(mock(EntityListener.class).getEntityId()).thenReturn(Integer.valueOf(1))
				.getMock();
		EntityListener entityListener1 = when(mock(EntityListener.class).getEntityId()).thenReturn(Integer.valueOf(1))
				.getMock();
		entityListenerRepositoryDecorator.addEntityListener(entityListener0);
		entityListenerRepositoryDecorator.addEntityListener(entityListener1);

		Entity entity = when(mock(Entity.class).getIdValue()).thenReturn(Integer.valueOf(1)).getMock();
		entityListenerRepositoryDecorator.update(entity);

		verify(decoratedRepository).update(entity);
		verify(entityListener0, times(1)).postUpdate(entity);
		verify(entityListener1, times(1)).postUpdate(entity);
	}

	@SuppressWarnings("resource")
	@Test
	public void updateEntityWithoutListener()
	{
		Repository<Entity> decoratedRepository = mock(Repository.class);
		EntityListenerRepositoryDecorator entityListenerRepositoryDecorator = new EntityListenerRepositoryDecorator(
				decoratedRepository);
		EntityListener entityListener0 = when(mock(EntityListener.class).getEntityId()).thenReturn(Integer.valueOf(-1))
				.getMock();
		entityListenerRepositoryDecorator.addEntityListener(entityListener0);

		Entity entity = when(mock(Entity.class).getIdValue()).thenReturn(Integer.valueOf(1)).getMock();
		entityListenerRepositoryDecorator.update(entity);

		verify(decoratedRepository).update(entity);
		verify(entityListener0, times(0)).postUpdate(entity);
	}

	@SuppressWarnings("resource")
	@Test
	public void updateEntityNoListeners()
	{
		Repository<Entity> decoratedRepository = mock(Repository.class);
		EntityListenerRepositoryDecorator entityListenerRepositoryDecorator = new EntityListenerRepositoryDecorator(
				decoratedRepository);

		Entity entity = when(mock(Entity.class).getIdValue()).thenReturn(Integer.valueOf(1)).getMock();
		entityListenerRepositoryDecorator.update(entity);

		verify(decoratedRepository).update(entity);
	}

	@SuppressWarnings(
	{ "resource", "unchecked", "rawtypes" })
	@Test
	public void updateStreamWithListeners()
	{
		Repository<Entity> decoratedRepository = mock(Repository.class);
		EntityListenerRepositoryDecorator entityListenerRepositoryDecorator = new EntityListenerRepositoryDecorator(
				decoratedRepository);
		EntityListener entityListener0 = when(mock(EntityListener.class).getEntityId()).thenReturn(Integer.valueOf(1))
				.getMock();
		EntityListener entityListener1 = when(mock(EntityListener.class).getEntityId()).thenReturn(Integer.valueOf(2))
				.getMock();
		entityListenerRepositoryDecorator.addEntityListener(entityListener0);
		entityListenerRepositoryDecorator.addEntityListener(entityListener1);

		Entity entity0 = when(mock(Entity.class).getIdValue()).thenReturn(Integer.valueOf(1)).getMock();
		Entity entity1 = when(mock(Entity.class).getIdValue()).thenReturn(Integer.valueOf(2)).getMock();
		Stream<Entity> entities = Stream.of(entity0, entity1);
		entityListenerRepositoryDecorator.update(entities);

		ArgumentCaptor<Stream<Entity>> captor = ArgumentCaptor.forClass((Class) Stream.class);
		verify(decoratedRepository).update(captor.capture());
		assertEquals(captor.getValue().collect(Collectors.toList()), Arrays.asList(entity0, entity1));

		verify(entityListener0, times(1)).postUpdate(entity0);
		verify(entityListener1, times(1)).postUpdate(entity1);
	}

	@SuppressWarnings(
	{ "resource", "unchecked", "rawtypes" })
	@Test
	public void updateStreamWithSomeListeners()
	{
		Repository<Entity> decoratedRepository = mock(Repository.class);
		EntityListenerRepositoryDecorator entityListenerRepositoryDecorator = new EntityListenerRepositoryDecorator(
				decoratedRepository);
		EntityListener entityListener1 = when(mock(EntityListener.class).getEntityId()).thenReturn(Integer.valueOf(2))
				.getMock();
		entityListenerRepositoryDecorator.addEntityListener(entityListener1);

		Entity entity0 = when(mock(Entity.class).getIdValue()).thenReturn(Integer.valueOf(1)).getMock();
		Entity entity1 = when(mock(Entity.class).getIdValue()).thenReturn(Integer.valueOf(2)).getMock();
		Stream<Entity> entities = Stream.of(entity0, entity1);
		entityListenerRepositoryDecorator.update(entities);

		ArgumentCaptor<Stream<Entity>> captor = ArgumentCaptor.forClass((Class) Stream.class);
		verify(decoratedRepository).update(captor.capture());
		assertEquals(captor.getValue().collect(Collectors.toList()), Arrays.asList(entity0, entity1));
		verify(entityListener1, times(1)).postUpdate(entity1);
	}

	@SuppressWarnings(
	{ "resource", "unchecked", "rawtypes" })
	@Test
	public void updateStreamNoListeners()
	{
		Repository<Entity> decoratedRepository = mock(Repository.class);
		EntityListenerRepositoryDecorator entityListenerRepositoryDecorator = new EntityListenerRepositoryDecorator(
				decoratedRepository);

		Entity entity0 = when(mock(Entity.class).getIdValue()).thenReturn(Integer.valueOf(1)).getMock();
		Entity entity1 = when(mock(Entity.class).getIdValue()).thenReturn(Integer.valueOf(2)).getMock();
		Stream<Entity> entities = Stream.of(entity0, entity1);
		entityListenerRepositoryDecorator.update(entities);

		ArgumentCaptor<Stream<Entity>> captor = ArgumentCaptor.forClass((Class) Stream.class);
		verify(decoratedRepository, times(1)).update(captor.capture());
		assertEquals(captor.getValue().collect(Collectors.toList()), Arrays.asList(entity0, entity1));
	}

	@SuppressWarnings("resource")
	@Test
	public void removeEntityListener()
	{
		Repository<Entity> decoratedRepository = mock(Repository.class);
		EntityListenerRepositoryDecorator entityListenerRepositoryDecorator = new EntityListenerRepositoryDecorator(
				decoratedRepository);
		EntityListener entityListener0 = when(mock(EntityListener.class).getEntityId()).thenReturn(Integer.valueOf(1))
				.getMock();
		entityListenerRepositoryDecorator.addEntityListener(entityListener0);
		entityListenerRepositoryDecorator.removeEntityListener(entityListener0);

		Entity entity = when(mock(Entity.class).getIdValue()).thenReturn(Integer.valueOf(1)).getMock();
		entityListenerRepositoryDecorator.update(entity);

		verify(decoratedRepository).update(entity);
		verify(entityListener0, times(0)).postUpdate(entity);
	}

	@Test
	public void findAllStream()
	{
		Object id0 = "id0";
		Object id1 = "id1";
		Entity entity0 = mock(Entity.class);
		Entity entity1 = mock(Entity.class);
		Stream<Object> entityIds = Stream.of(id0, id1);
		when(decoratedRepository.findAll(entityIds)).thenReturn(Stream.of(entity0, entity1));
		Stream<Entity> expectedEntities = entityListenerRepositoryDecorator.findAll(entityIds);
		assertEquals(expectedEntities.collect(Collectors.toList()), Arrays.asList(entity0, entity1));
	}

	@Test
	public void findAllStreamFetch()
	{
		Fetch fetch = new Fetch();
		Object id0 = "id0";
		Object id1 = "id1";
		Entity entity0 = mock(Entity.class);
		Entity entity1 = mock(Entity.class);
		Stream<Object> entityIds = Stream.of(id0, id1);
		when(decoratedRepository.findAll(entityIds, fetch)).thenReturn(Stream.of(entity0, entity1));
		Stream<Entity> expectedEntities = entityListenerRepositoryDecorator.findAll(entityIds, fetch);
		assertEquals(expectedEntities.collect(Collectors.toList()), Arrays.asList(entity0, entity1));
	}

	@Test
	public void findAllAsStream()
	{
		Entity entity0 = mock(Entity.class);
		Query<Entity> query = mock(Query.class);
		when(decoratedRepository.findAll(query)).thenReturn(Stream.of(entity0));
		Stream<Entity> entities = entityListenerRepositoryDecorator.findAll(query);
		assertEquals(entities.collect(Collectors.toList()), Arrays.asList(entity0));
	}

	@Test
	public void streamFetch()
	{
		Fetch fetch = new Fetch();
		Consumer<List<Entity>> consumer = mock(Consumer.class);
		entityListenerRepositoryDecorator.forEachBatched(fetch, consumer, 543);
		verify(decoratedRepository, times(1)).forEachBatched(fetch, consumer, 543);
	}
}
