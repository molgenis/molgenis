package org.molgenis.data.reindex;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.data.RepositoryCapability.INDEXABLE;
import static org.molgenis.data.RepositoryCapability.MANAGABLE;
import static org.testng.Assert.assertEquals;

import java.util.EnumSet;
import java.util.stream.Stream;

import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.reindex.meta.ReindexActionMetaData.CudType;
import org.molgenis.data.reindex.meta.ReindexActionMetaData.DataType;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.Sets;

public class ReindexActionRepositoryDecoratorTest
{
	private Repository<Entity> decoratedRepo;
	private EntityMetaData entityMeta;
	private ReindexActionRegisterService reindexActionRegisterService;
	private ReindexActionRepositoryDecorator reindexActionRepositoryDecorator;

	@SuppressWarnings("unchecked")
	@BeforeMethod
	public void setUpBeforeMethod()
	{
		decoratedRepo = mock(Repository.class);
		when(decoratedRepo.getName()).thenReturn("entity");
		when(decoratedRepo.getCapabilities()).thenReturn(Sets.newHashSet(RepositoryCapability.MANAGABLE));
		entityMeta = mock(EntityMetaData.class);
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
		verify(reindexActionRegisterService).register("entity", CudType.UPDATE, DataType.DATA, "1");
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
		verify(reindexActionRegisterService).register("entity", CudType.UPDATE, DataType.DATA, null);
	}

	@Test
	public void deleteEntity()
	{
		Entity entity0 = mock(Entity.class);
		when(entity0.getIdValue()).thenReturn("1");
		reindexActionRepositoryDecorator.delete(entity0);
		verify(decoratedRepo, times(1)).delete(entity0);
		verify(reindexActionRegisterService).register("entity", CudType.DELETE, DataType.DATA, "1");
	}

	@Test
	public void deleteStreamEntities()
	{
		Stream<Entity> entities = Stream.empty();
		reindexActionRepositoryDecorator.delete(entities);
		verify(decoratedRepo, times(1)).delete(entities);
		verify(reindexActionRegisterService, times(1)).register("entity", CudType.DELETE, DataType.DATA,
				null);
	}

	@Test
	public void deleteEntityById()
	{
		Entity entity0 = mock(Entity.class);
		when(entity0.getIdValue()).thenReturn("1");
		reindexActionRepositoryDecorator.deleteById("1");
		verify(decoratedRepo, times(1)).deleteById("1");
		verify(reindexActionRegisterService).register("entity", CudType.DELETE, DataType.DATA, "1");
	}

	@Test
	public void deleteEntityByIdStream()
	{
		Stream<Object> ids = Stream.empty();
		reindexActionRepositoryDecorator.deleteAll(ids);
		verify(decoratedRepo, times(1)).deleteAll(ids);
		verify(reindexActionRegisterService, times(1)).register("entity", CudType.DELETE, DataType.DATA,
				null);
	}

	@Test
	public void deleteAll()
	{
		reindexActionRepositoryDecorator.deleteAll();
		verify(decoratedRepo, times(1)).deleteAll();
		verify(reindexActionRegisterService, times(1)).register("entity", CudType.DELETE, DataType.DATA,
				null);
	}

	@Test
	public void addEntity()
	{
		Entity entity0 = mock(Entity.class);
		when(entity0.getIdValue()).thenReturn("1");
		reindexActionRepositoryDecorator.add(entity0);
		verify(decoratedRepo, times(1)).add(entity0);
		verify(reindexActionRegisterService).register("entity", CudType.CREATE, DataType.DATA, "1");
	}

	@Test
	public void addEntitiesStream()
	{
		Stream<Entity> entities = Stream.empty();
		when(decoratedRepo.add(entities)).thenReturn(123);
		assertEquals(reindexActionRepositoryDecorator.add(entities), Integer.valueOf(123));
		verify(decoratedRepo, times(1)).add(entities);
		verify(reindexActionRegisterService).register("entity", CudType.CREATE, DataType.DATA, null);
	}

	@Test
	public void rebuildIndex()
	{
		// FIXME GitHub #4809
	}
}
