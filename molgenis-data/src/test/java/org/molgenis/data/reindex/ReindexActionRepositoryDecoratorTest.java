package org.molgenis.data.reindex;

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
import static org.molgenis.data.reindex.meta.ReindexActionMetaData.CudType.*;
import static org.molgenis.data.reindex.meta.ReindexActionMetaData.DataType.DATA;
import static org.testng.Assert.assertEquals;

public class ReindexActionRepositoryDecoratorTest
{
	private Repository<Entity> decoratedRepo;
	private ReindexActionRegisterService reindexActionRegisterService;
	private ReindexActionRepositoryDecorator reindexActionRepositoryDecorator;

	@SuppressWarnings("unchecked")
	@BeforeMethod
	public void setUpBeforeMethod()
	{
		decoratedRepo = mock(Repository.class);
		when(decoratedRepo.getName()).thenReturn("entity");
		when(decoratedRepo.getCapabilities()).thenReturn(singleton(MANAGABLE));
		EntityMetaData entityMeta = mock(EntityMetaData.class);
		when(decoratedRepo.getEntityMetaData()).thenReturn(entityMeta);
		reindexActionRegisterService = mock(ReindexActionRegisterService.class);
		reindexActionRepositoryDecorator = new ReindexActionRepositoryDecorator(decoratedRepo,
				reindexActionRegisterService);
	}

	@Test
	public void updateEntity()
	{
		Entity entity0 = mock(Entity.class);
		when(entity0.getIdValue()).thenReturn("1");
		reindexActionRepositoryDecorator.update(entity0);
		verify(decoratedRepo, times(1)).update(entity0);
		verify(reindexActionRegisterService).register("entity", UPDATE, DATA, "1");
	}

	@Test
	public void getCapabilities()
	{
		assertEquals(reindexActionRepositoryDecorator.getCapabilities(), EnumSet.of(INDEXABLE, MANAGABLE));
	}

	@Test
	public void updateStreamEntities()
	{
		Stream<Entity> entities = Stream.empty();
		reindexActionRepositoryDecorator.update(entities);
		verify(decoratedRepo, times(1)).update(entities);
		verify(reindexActionRegisterService).register("entity", UPDATE, DATA, null);
	}

	@Test
	public void deleteEntity()
	{
		Entity entity0 = mock(Entity.class);
		when(entity0.getIdValue()).thenReturn("1");
		reindexActionRepositoryDecorator.delete(entity0);
		verify(decoratedRepo, times(1)).delete(entity0);
		verify(reindexActionRegisterService).register("entity", DELETE, DATA, "1");
	}

	@Test
	public void deleteStreamEntities()
	{
		Stream<Entity> entities = Stream.empty();
		reindexActionRepositoryDecorator.delete(entities);
		verify(decoratedRepo, times(1)).delete(entities);
		verify(reindexActionRegisterService, times(1)).register("entity", DELETE, DATA, null);
	}

	@Test
	public void deleteEntityById()
	{
		Entity entity0 = mock(Entity.class);
		when(entity0.getIdValue()).thenReturn("1");
		reindexActionRepositoryDecorator.deleteById("1");
		verify(decoratedRepo, times(1)).deleteById("1");
		verify(reindexActionRegisterService).register("entity", DELETE, DATA, "1");
	}

	@Test
	public void deleteEntityByIdStream()
	{
		Stream<Object> ids = Stream.empty();
		reindexActionRepositoryDecorator.deleteAll(ids);
		verify(decoratedRepo, times(1)).deleteAll(ids);
		verify(reindexActionRegisterService, times(1)).register("entity", DELETE, DATA, null);
	}

	@Test
	public void deleteAll()
	{
		reindexActionRepositoryDecorator.deleteAll();
		verify(decoratedRepo, times(1)).deleteAll();
		verify(reindexActionRegisterService, times(1)).register("entity", DELETE, DATA, null);
	}

	@Test
	public void addEntity()
	{
		Entity entity0 = mock(Entity.class);
		when(entity0.getIdValue()).thenReturn("1");
		reindexActionRepositoryDecorator.add(entity0);
		verify(decoratedRepo, times(1)).add(entity0);
		verify(reindexActionRegisterService).register("entity", CREATE, DATA, "1");
	}

	@Test
	public void addEntitiesStream()
	{
		Stream<Entity> entities = Stream.empty();
		when(decoratedRepo.add(entities)).thenReturn(123);
		assertEquals(reindexActionRepositoryDecorator.add(entities), Integer.valueOf(123));
		verify(decoratedRepo, times(1)).add(entities);
		verify(reindexActionRegisterService).register("entity", CREATE, DATA, null);
	}
}
