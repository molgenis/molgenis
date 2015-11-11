package org.molgenis.ui;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.molgenis.data.Entity;
import org.molgenis.data.EntityListener;
import org.molgenis.data.Repository;
import org.testng.annotations.Test;

public class EntityListenerRepositoryDecoratorTest
{
	@SuppressWarnings("resource")
	@Test(expectedExceptions = NullPointerException.class)
	public void EntityListenerRepositoryDecorator()
	{
		new EntityListenerRepositoryDecorator(null);
	}

	@SuppressWarnings("resource")
	@Test
	public void updateEntityWithListener()
	{
		Repository decoratedRepository = mock(Repository.class);
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
	public void updateEntityWithoutListener()
	{
		Repository decoratedRepository = mock(Repository.class);
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
		Repository decoratedRepository = mock(Repository.class);
		EntityListenerRepositoryDecorator entityListenerRepositoryDecorator = new EntityListenerRepositoryDecorator(
				decoratedRepository);

		Entity entity = when(mock(Entity.class).getIdValue()).thenReturn(Integer.valueOf(1)).getMock();
		entityListenerRepositoryDecorator.update(entity);

		verify(decoratedRepository).update(entity);
	}

	@SuppressWarnings("resource")
	@Test
	public void updateIterableextendsEntityWithListeners()
	{
		Repository decoratedRepository = mock(Repository.class);
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
		List<Entity> entities = Arrays.asList(entity0, entity1);
		entityListenerRepositoryDecorator.update(entities);

		verify(decoratedRepository).update(entities);
		verify(entityListener0, times(1)).postUpdate(entity0);
		verify(entityListener1, times(1)).postUpdate(entity1);
	}

	@SuppressWarnings("resource")
	@Test
	public void updateIterableextendsEntityWithSomeListeners()
	{
		Repository decoratedRepository = mock(Repository.class);
		EntityListenerRepositoryDecorator entityListenerRepositoryDecorator = new EntityListenerRepositoryDecorator(
				decoratedRepository);
		EntityListener entityListener1 = when(mock(EntityListener.class).getEntityId()).thenReturn(Integer.valueOf(2))
				.getMock();
		entityListenerRepositoryDecorator.addEntityListener(entityListener1);

		Entity entity0 = when(mock(Entity.class).getIdValue()).thenReturn(Integer.valueOf(1)).getMock();
		Entity entity1 = when(mock(Entity.class).getIdValue()).thenReturn(Integer.valueOf(2)).getMock();
		List<Entity> entities = Arrays.asList(entity0, entity1);
		entityListenerRepositoryDecorator.update(entities);

		verify(decoratedRepository).update(entities);
		verify(entityListener1, times(1)).postUpdate(entity1);
	}

	@SuppressWarnings("resource")
	@Test
	public void updateIterableextendsEntityNoListeners()
	{
		Repository decoratedRepository = mock(Repository.class);
		EntityListenerRepositoryDecorator entityListenerRepositoryDecorator = new EntityListenerRepositoryDecorator(
				decoratedRepository);

		Entity entity0 = when(mock(Entity.class).getIdValue()).thenReturn(Integer.valueOf(1)).getMock();
		Entity entity1 = when(mock(Entity.class).getIdValue()).thenReturn(Integer.valueOf(2)).getMock();
		List<Entity> entities = Arrays.asList(entity0, entity1);
		entityListenerRepositoryDecorator.update(entities);

		verify(decoratedRepository).update(entities);
	}
	
	@SuppressWarnings("resource")
	@Test
	public void removeEntityListener()
	{
		Repository decoratedRepository = mock(Repository.class);
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
}
