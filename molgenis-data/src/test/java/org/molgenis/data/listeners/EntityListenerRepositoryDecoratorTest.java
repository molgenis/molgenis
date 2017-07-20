package org.molgenis.data.listeners;

import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.molgenis.data.Entity;
import org.molgenis.data.Fetch;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.testng.Assert.assertEquals;

public class EntityListenerRepositoryDecoratorTest
{
	private Repository<Entity> decoratedRepository;
	private EntityListenerRepositoryDecorator entityListenerRepositoryDecorator;
	private EntityListenersService entityListenersService = new EntityListenersService();

	@SuppressWarnings("unchecked")
	@BeforeMethod
	public void setUpBeforeMethod()
	{
		decoratedRepository = Mockito.mock(Repository.class);
		Mockito.when(decoratedRepository.getName()).thenReturn("entityFullName");
		entityListenerRepositoryDecorator = new EntityListenerRepositoryDecorator(decoratedRepository,
				entityListenersService);
		Mockito.when(entityListenerRepositoryDecorator.getName()).thenReturn("entityFullName");
	}

	@AfterMethod
	public void afterMethod()
	{
		entityListenerRepositoryDecorator = null;
	}

	@SuppressWarnings("resource")
	@Test(expectedExceptions = NullPointerException.class)
	public void EntityListenerRepositoryDecorator()
	{
		new EntityListenerRepositoryDecorator(null, entityListenersService);
	}

	@Test
	public void delegate() throws Exception
	{
		assertEquals(entityListenerRepositoryDecorator.delegate(), decoratedRepository);
	}

	@Test
	public void testQuery() throws Exception
	{
		assertEquals(entityListenerRepositoryDecorator.query().getRepository(), entityListenerRepositoryDecorator);
	}

	@Test
	public void addStream()
	{
		Stream<Entity> entities = Stream.empty();
		Mockito.when(decoratedRepository.add(entities)).thenReturn(123);
		Assert.assertEquals(entityListenerRepositoryDecorator.add(entities), Integer.valueOf(123));
	}

	@Test
	public void deleteStream()
	{
		Stream<Entity> entities = Stream.empty();
		entityListenerRepositoryDecorator.delete(entities);
		Mockito.verify(decoratedRepository, Mockito.times(1)).delete(entities);
	}

	@SuppressWarnings("resource")
	@Test
	public void updateEntityWithListener()
	{
		@SuppressWarnings("unchecked")
		Repository<Entity> decoratedRepository = Mockito.mock(Repository.class);
		Mockito.when(decoratedRepository.getName()).thenReturn("entityFullName");
		EntityListenerRepositoryDecorator entityListenerRepositoryDecorator = new EntityListenerRepositoryDecorator(
				decoratedRepository, entityListenersService);
		EntityListener entityListener0 = Mockito.when(Mockito.mock(EntityListener.class).getEntityId())
												.thenReturn(1)
												.getMock();
		entityListenersService.addEntityListener("entityFullName", entityListener0);

		Entity entity = Mockito.when(Mockito.mock(Entity.class).getIdValue()).thenReturn(1).getMock();
		entityListenerRepositoryDecorator.update(entity);

		Mockito.verify(decoratedRepository).update(entity);
		Mockito.verify(entityListener0, Mockito.times(1)).postUpdate(entity);
	}

	@SuppressWarnings("resource")
	@Test
	public void updateEntityWithListeners()
	{
		@SuppressWarnings("unchecked")
		Repository<Entity> decoratedRepository = Mockito.mock(Repository.class);
		Mockito.when(decoratedRepository.getName()).thenReturn("entityFullName");
		EntityListenerRepositoryDecorator entityListenerRepositoryDecorator = new EntityListenerRepositoryDecorator(
				decoratedRepository, entityListenersService);
		EntityListener entityListener0 = Mockito.when(Mockito.mock(EntityListener.class).getEntityId())
												.thenReturn(1)
												.getMock();
		EntityListener entityListener1 = Mockito.when(Mockito.mock(EntityListener.class).getEntityId())
												.thenReturn(1)
												.getMock();
		entityListenersService.addEntityListener("entityFullName", entityListener0);
		entityListenersService.addEntityListener("entityFullName", entityListener1);

		Entity entity = Mockito.when(Mockito.mock(Entity.class).getIdValue()).thenReturn(1).getMock();
		entityListenerRepositoryDecorator.update(entity);

		Mockito.verify(decoratedRepository).update(entity);
		Mockito.verify(entityListener0, Mockito.times(1)).postUpdate(entity);
		Mockito.verify(entityListener1, Mockito.times(1)).postUpdate(entity);
	}

	@SuppressWarnings("resource")
	@Test
	public void updateEntityWithoutListener()
	{
		@SuppressWarnings("unchecked")
		Repository<Entity> decoratedRepository = Mockito.mock(Repository.class);
		Mockito.when(decoratedRepository.getName()).thenReturn("entityFullName");
		EntityListenerRepositoryDecorator entityListenerRepositoryDecorator = new EntityListenerRepositoryDecorator(
				decoratedRepository, entityListenersService);
		EntityListener entityListener0 = Mockito.when(Mockito.mock(EntityListener.class).getEntityId())
												.thenReturn(-1)
												.getMock();
		entityListenersService.addEntityListener("entityFullName", entityListener0);

		Entity entity = Mockito.when(Mockito.mock(Entity.class).getIdValue()).thenReturn(1).getMock();
		entityListenerRepositoryDecorator.update(entity);

		Mockito.verify(decoratedRepository).update(entity);
		Mockito.verify(entityListener0, Mockito.times(0)).postUpdate(entity);
	}

	@SuppressWarnings("resource")
	@Test
	public void updateEntityNoListeners()
	{
		@SuppressWarnings("unchecked")
		Repository<Entity> decoratedRepository = Mockito.mock(Repository.class);
		Mockito.when(decoratedRepository.getName()).thenReturn("entityFullName");
		EntityListenerRepositoryDecorator entityListenerRepositoryDecorator = new EntityListenerRepositoryDecorator(
				decoratedRepository, entityListenersService);

		Entity entity = Mockito.when(Mockito.mock(Entity.class).getIdValue()).thenReturn(1).getMock();
		entityListenerRepositoryDecorator.update(entity);

		Mockito.verify(decoratedRepository).update(entity);
	}

	@SuppressWarnings({ "resource", "unchecked", "rawtypes" })
	@Test
	public void updateStreamWithListeners()
	{
		Repository<Entity> decoratedRepository = Mockito.mock(Repository.class);
		Mockito.when(decoratedRepository.getName()).thenReturn("entityFullName");
		EntityListenerRepositoryDecorator entityListenerRepositoryDecorator = new EntityListenerRepositoryDecorator(
				decoratedRepository, entityListenersService);
		EntityListener entityListener0 = Mockito.when(Mockito.mock(EntityListener.class).getEntityId())
												.thenReturn(1)
												.getMock();
		EntityListener entityListener1 = Mockito.when(Mockito.mock(EntityListener.class).getEntityId())
												.thenReturn(2)
												.getMock();
		entityListenersService.addEntityListener("entityFullName", entityListener0);
		entityListenersService.addEntityListener("entityFullName", entityListener1);

		Entity entity0 = Mockito.when(Mockito.mock(Entity.class).getIdValue()).thenReturn(1).getMock();
		Entity entity1 = Mockito.when(Mockito.mock(Entity.class).getIdValue()).thenReturn(2).getMock();
		Stream<Entity> entities = Stream.of(entity0, entity1);
		entityListenerRepositoryDecorator.update(entities);

		ArgumentCaptor<Stream<Entity>> captor = ArgumentCaptor.forClass(Stream.class);
		Mockito.verify(decoratedRepository).update(captor.capture());
		Assert.assertEquals(captor.getValue().collect(Collectors.toList()), Arrays.asList(entity0, entity1));

		Mockito.verify(entityListener0, Mockito.times(1)).postUpdate(entity0);
		Mockito.verify(entityListener1, Mockito.times(1)).postUpdate(entity1);
	}

	@SuppressWarnings({ "resource", "unchecked", "rawtypes" })
	@Test
	public void updateStreamWithSomeListeners()
	{
		Repository<Entity> decoratedRepository = Mockito.mock(Repository.class);
		Mockito.when(decoratedRepository.getName()).thenReturn("entityFullName");
		EntityListenerRepositoryDecorator entityListenerRepositoryDecorator = new EntityListenerRepositoryDecorator(
				decoratedRepository, entityListenersService);
		EntityListener entityListener1 = Mockito.when(Mockito.mock(EntityListener.class).getEntityId())
												.thenReturn(2)
												.getMock();
		entityListenersService.addEntityListener("entityFullName", entityListener1);

		Entity entity0 = Mockito.when(Mockito.mock(Entity.class).getIdValue()).thenReturn(1).getMock();
		Entity entity1 = Mockito.when(Mockito.mock(Entity.class).getIdValue()).thenReturn(2).getMock();
		Stream<Entity> entities = Stream.of(entity0, entity1);
		entityListenerRepositoryDecorator.update(entities);

		ArgumentCaptor<Stream<Entity>> captor = ArgumentCaptor.forClass(Stream.class);
		Mockito.verify(decoratedRepository).update(captor.capture());
		Assert.assertEquals(captor.getValue().collect(Collectors.toList()), Arrays.asList(entity0, entity1));
		Mockito.verify(entityListener1, Mockito.times(1)).postUpdate(entity1);
	}

	@SuppressWarnings({ "resource", "unchecked", "rawtypes" })
	@Test
	public void updateStreamNoListeners()
	{
		Repository<Entity> decoratedRepository = Mockito.mock(Repository.class);
		Mockito.when(decoratedRepository.getName()).thenReturn("entityFullName");
		EntityListenerRepositoryDecorator entityListenerRepositoryDecorator = new EntityListenerRepositoryDecorator(
				decoratedRepository, entityListenersService);

		Entity entity0 = Mockito.when(Mockito.mock(Entity.class).getIdValue()).thenReturn(1).getMock();
		Entity entity1 = Mockito.when(Mockito.mock(Entity.class).getIdValue()).thenReturn(2).getMock();
		Stream<Entity> entities = Stream.of(entity0, entity1);
		entityListenerRepositoryDecorator.update(entities);

		ArgumentCaptor<Stream<Entity>> captor = ArgumentCaptor.forClass(Stream.class);
		Mockito.verify(decoratedRepository, Mockito.times(1)).update(captor.capture());
		Assert.assertEquals(captor.getValue().collect(Collectors.toList()), Arrays.asList(entity0, entity1));
	}

	@SuppressWarnings("resource")
	@Test
	public void removeEntityListener()
	{
		@SuppressWarnings("unchecked")
		Repository<Entity> decoratedRepository = Mockito.mock(Repository.class);
		Mockito.when(decoratedRepository.getName()).thenReturn("entityFullName");
		EntityListenerRepositoryDecorator entityListenerRepositoryDecorator = new EntityListenerRepositoryDecorator(
				decoratedRepository, entityListenersService);
		EntityListener entityListener0 = Mockito.when(Mockito.mock(EntityListener.class).getEntityId())
												.thenReturn(1)
												.getMock();
		entityListenersService.addEntityListener("entityFullName", entityListener0);
		entityListenersService.removeEntityListener("entityFullName", entityListener0);

		Entity entity = Mockito.when(Mockito.mock(Entity.class).getIdValue()).thenReturn(1).getMock();
		entityListenerRepositoryDecorator.update(entity);

		Mockito.verify(decoratedRepository).update(entity);
		Mockito.verify(entityListener0, Mockito.times(0)).postUpdate(entity);
	}

	@Test
	public void findAllStream()
	{
		Object id0 = "id0";
		Object id1 = "id1";
		Entity entity0 = Mockito.mock(Entity.class);
		Entity entity1 = Mockito.mock(Entity.class);
		Stream<Object> entityIds = Stream.of(id0, id1);
		Mockito.when(decoratedRepository.findAll(entityIds)).thenReturn(Stream.of(entity0, entity1));
		Stream<Entity> expectedEntities = entityListenerRepositoryDecorator.findAll(entityIds);
		Assert.assertEquals(expectedEntities.collect(Collectors.toList()), Arrays.asList(entity0, entity1));
	}

	@Test
	public void findAllStreamFetch()
	{
		Fetch fetch = new Fetch();
		Object id0 = "id0";
		Object id1 = "id1";
		Entity entity0 = Mockito.mock(Entity.class);
		Entity entity1 = Mockito.mock(Entity.class);
		Stream<Object> entityIds = Stream.of(id0, id1);
		Mockito.when(decoratedRepository.findAll(entityIds, fetch)).thenReturn(Stream.of(entity0, entity1));
		Stream<Entity> expectedEntities = entityListenerRepositoryDecorator.findAll(entityIds, fetch);
		Assert.assertEquals(expectedEntities.collect(Collectors.toList()), Arrays.asList(entity0, entity1));
	}

	@Test
	public void findAllAsStream()
	{
		Entity entity0 = Mockito.mock(Entity.class);
		@SuppressWarnings("unchecked")
		Query<Entity> query = Mockito.mock(Query.class);
		Mockito.when(decoratedRepository.findAll(query)).thenReturn(Stream.of(entity0));
		Stream<Entity> entities = entityListenerRepositoryDecorator.findAll(query);
		Assert.assertEquals(entities.collect(Collectors.toList()), Arrays.asList(entity0));
	}

	@Test
	public void streamFetch()
	{
		Fetch fetch = new Fetch();
		@SuppressWarnings("unchecked")
		Consumer<List<Entity>> consumer = Mockito.mock(Consumer.class);
		entityListenerRepositoryDecorator.forEachBatched(fetch, consumer, 543);
		Mockito.verify(decoratedRepository, Mockito.times(1)).forEachBatched(fetch, consumer, 543);
	}
}
