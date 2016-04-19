package org.molgenis.data.transaction.index;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.stream.Stream;

import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Repository;
import org.molgenis.data.transaction.index.IndexTransactionLogEntryMetaData.CudType;
import org.molgenis.data.transaction.index.IndexTransactionLogEntryMetaData.DataType;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class IndexTransactionLogRepositoryDecoratorTest
{
	private Repository decoratedRepo;
	private EntityMetaData entityMeta;
	private IndexTransactionLogService indexTransactionLogService;
	private IndexTransactionLogRepositoryDecorator indexTransactionLogRepositoryDecorator;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		decoratedRepo = mock(Repository.class);
		when(decoratedRepo.getName()).thenReturn("entity");
		entityMeta = mock(EntityMetaData.class);
		when(decoratedRepo.getEntityMetaData()).thenReturn(entityMeta);
		indexTransactionLogService = mock(IndexTransactionLogService.class);
		indexTransactionLogRepositoryDecorator = new IndexTransactionLogRepositoryDecorator(decoratedRepo,
				indexTransactionLogService);
	}

	@Test
	public void updateEntity()
	{
		Entity entity0 = mock(Entity.class);
		when(entity0.getIdValue()).thenReturn("1");
		indexTransactionLogRepositoryDecorator.update(entity0);
		verify(decoratedRepo, times(1)).update(entity0);
		verify(indexTransactionLogService).log(entityMeta, CudType.UPDATE, DataType.DATA, "1");
	}

	@Test
	public void updateStreamEntities()
	{
		Stream<Entity> entities = Stream.empty();
		indexTransactionLogRepositoryDecorator.update(entities);
		verify(decoratedRepo, times(1)).update(entities);
		verify(indexTransactionLogService).log(entityMeta, CudType.UPDATE, DataType.DATA, null);
	}

	@Test
	public void deleteEntity()
	{
		Entity entity0 = mock(Entity.class);
		when(entity0.getIdValue()).thenReturn("1");
		indexTransactionLogRepositoryDecorator.delete(entity0);
		verify(decoratedRepo, times(1)).delete(entity0);
		verify(indexTransactionLogService).log(entityMeta, CudType.DELETE, DataType.DATA, "1");
	}

	@Test
	public void deleteStreamEntities()
	{
		Stream<Entity> entities = Stream.empty();
		indexTransactionLogRepositoryDecorator.delete(entities);
		verify(decoratedRepo, times(1)).delete(entities);
		verify(indexTransactionLogService, times(1)).log(entityMeta, CudType.DELETE, DataType.DATA, null);
	}

	@Test
	public void deleteEntityById()
	{
		Entity entity0 = mock(Entity.class);
		when(entity0.getIdValue()).thenReturn("1");
		indexTransactionLogRepositoryDecorator.deleteById("1");
		verify(decoratedRepo, times(1)).deleteById("1");
		verify(indexTransactionLogService).log(entityMeta, CudType.DELETE, DataType.DATA, "1");
	}

	@Test
	public void deleteEntityByIdStream()
	{
		Stream<Object> ids = Stream.empty();
		indexTransactionLogRepositoryDecorator.deleteById(ids);
		verify(decoratedRepo, times(1)).deleteById(ids);
		verify(indexTransactionLogService, times(1)).log(entityMeta, CudType.DELETE, DataType.DATA, null);
	}

	@Test
	public void deleteAll()
	{
		indexTransactionLogRepositoryDecorator.deleteAll();
		verify(decoratedRepo, times(1)).deleteAll();
		verify(indexTransactionLogService, times(1)).log(entityMeta, CudType.DELETE, DataType.DATA, null);
	}

	@Test
	public void addEntity()
	{
		Entity entity0 = mock(Entity.class);
		when(entity0.getIdValue()).thenReturn("1");
		indexTransactionLogRepositoryDecorator.add(entity0);
		verify(decoratedRepo, times(1)).add(entity0);
		verify(indexTransactionLogService).log(entityMeta, CudType.ADD, DataType.DATA, "1");
	}

	@Test
	public void addEntitiesStream()
	{
		Stream<Entity> entities = Stream.empty();
		when(decoratedRepo.add(entities)).thenReturn(123);
		assertEquals(indexTransactionLogRepositoryDecorator.add(entities), Integer.valueOf(123));
		verify(decoratedRepo, times(1)).add(entities);
		verify(indexTransactionLogService).log(entityMeta, CudType.ADD, DataType.DATA, null);
	}

	@Test
	public void rebuildIndex()
	{
		indexTransactionLogRepositoryDecorator.rebuildIndex();
		verify(decoratedRepo, times(1)).rebuildIndex();
		verify(indexTransactionLogService).log(entityMeta, CudType.UPDATE, DataType.METADATA, null);
	}
}
