package org.molgenis.data.index;

import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.model.EntityMetaData;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.EnumSet;
import java.util.stream.Stream;

import static java.util.Collections.singleton;
import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static org.molgenis.data.RepositoryCapability.INDEXABLE;
import static org.molgenis.data.RepositoryCapability.MANAGABLE;
import static org.testng.Assert.assertEquals;

public class IndexActionRepositoryDecoratorTest
{
	private Repository<Entity> decoratedRepo;
	private IndexActionRegisterService indexActionRegisterService;
	private IndexActionRepositoryDecorator indexActionRepositoryDecorator;

	@SuppressWarnings("unchecked")
	@BeforeMethod
	public void setUpBeforeMethod()
	{
		decoratedRepo = mock(Repository.class);
		when(decoratedRepo.getName()).thenReturn("entity");
		when(decoratedRepo.getCapabilities()).thenReturn(singleton(MANAGABLE));
		EntityMetaData entityMeta = mock(EntityMetaData.class);
		when(decoratedRepo.getEntityMetaData()).thenReturn(entityMeta);
		indexActionRegisterService = mock(IndexActionRegisterService.class);
		indexActionRepositoryDecorator = new IndexActionRepositoryDecorator(decoratedRepo, indexActionRegisterService);
	}

	@Test
	public void updateEntity()
	{
		Entity entity0 = mock(Entity.class);
		when(entity0.getIdValue()).thenReturn("1");
		indexActionRepositoryDecorator.update(entity0);
		verify(decoratedRepo, times(1)).update(entity0);
		verify(indexActionRegisterService).register("entity", "1");
	}

	@Test
	public void getCapabilities()
	{
		assertEquals(indexActionRepositoryDecorator.getCapabilities(), EnumSet.of(INDEXABLE, MANAGABLE));
	}

	@Test
	public void updateStreamEntities()
	{
		Stream<Entity> entities = Stream.empty();
		indexActionRepositoryDecorator.update(entities);
		verify(decoratedRepo, times(1)).update(entities);
		verify(indexActionRegisterService).register("entity", null);
	}

	@Test
	public void deleteEntity()
	{
		Entity entity0 = mock(Entity.class);
		when(entity0.getIdValue()).thenReturn("1");
		indexActionRepositoryDecorator.delete(entity0);
		verify(decoratedRepo, times(1)).delete(entity0);
		verify(indexActionRegisterService).register("entity", "1");
	}

	@Test
	public void deleteStreamEntities()
	{
		Stream<Entity> entities = Stream.empty();
		indexActionRepositoryDecorator.delete(entities);
		verify(decoratedRepo, times(1)).delete(entities);
		verify(indexActionRegisterService, times(1)).register("entity", null);
	}

	@Test
	public void deleteEntityById()
	{
		Entity entity0 = mock(Entity.class);
		when(entity0.getIdValue()).thenReturn("1");
		indexActionRepositoryDecorator.deleteById("1");
		verify(decoratedRepo, times(1)).deleteById("1");
		verify(indexActionRegisterService).register("entity", "1");
	}

	@Test
	public void deleteEntityByIdStream()
	{
		Stream<Object> ids = Stream.empty();
		indexActionRepositoryDecorator.deleteAll(ids);
		verify(decoratedRepo, times(1)).deleteAll(ids);
		verify(indexActionRegisterService, times(1)).register("entity", null);
	}

	@Test
	public void deleteAll()
	{
		indexActionRepositoryDecorator.deleteAll();
		verify(decoratedRepo, times(1)).deleteAll();
		verify(indexActionRegisterService, times(1)).register("entity", null);
	}

	@Test
	public void addEntity()
	{
		Entity entity0 = mock(Entity.class);
		when(entity0.getIdValue()).thenReturn("1");
		indexActionRepositoryDecorator.add(entity0);
		verify(decoratedRepo, times(1)).add(entity0);
		verify(indexActionRegisterService).register("entity", "1");
	}

	@Test
	public void addEntitiesStream()
	{
		Stream<Entity> entities = Stream.empty();
		when(decoratedRepo.add(entities)).thenReturn(123);
		assertEquals(indexActionRepositoryDecorator.add(entities), Integer.valueOf(123));
		verify(decoratedRepo, times(1)).add(entities);
		verify(indexActionRegisterService).register("entity", null);
	}
}
