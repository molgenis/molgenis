package org.molgenis.data.transaction.index;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.ManageableRepositoryCollection;
import org.molgenis.data.Repository;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.transaction.index.IndexTransactionLogEntryMetaData.CudType;
import org.molgenis.data.transaction.index.IndexTransactionLogEntryMetaData.DataType;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class IndexTransactionLogRepositoryCollectionDecoratorTest
{
	private final static String REPOSITORY_NAME = "repo";
	private ManageableRepositoryCollection decoratedRepositoryCollection;
	private Repository<Entity> repository;
	private EntityMetaData entityMeta;
	private IndexTransactionLogService indexTransactionLogService;
	private IndexTransactionLogRepositoryCollectionDecorator indexTransactionLogRepositoryCollectionDecorator;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		decoratedRepositoryCollection = mock(ManageableRepositoryCollection.class);
		Repository<Entity> repository = (Repository<Entity>) mock(Repository.class);
		when(decoratedRepositoryCollection.getRepository(REPOSITORY_NAME)).thenReturn(repository);
		entityMeta = mock(EntityMetaData.class);
		when(repository.getEntityMetaData()).thenReturn(entityMeta);
		indexTransactionLogService = mock(IndexTransactionLogService.class);
		indexTransactionLogRepositoryCollectionDecorator = new IndexTransactionLogRepositoryCollectionDecorator(
				decoratedRepositoryCollection,
				indexTransactionLogService);
	}

	@Test
	public void deleteEntityMeta()
	{
		indexTransactionLogRepositoryCollectionDecorator.deleteEntityMeta(REPOSITORY_NAME);
		verify(decoratedRepositoryCollection, times(1)).deleteEntityMeta(REPOSITORY_NAME);
		verify(indexTransactionLogService).log(entityMeta, CudType.DELETE, DataType.METADATA, null);
	}

	@Test
	public void addAttribute()
	{
		DefaultAttributeMetaData attribute = new DefaultAttributeMetaData("attribute");
		indexTransactionLogRepositoryCollectionDecorator.addAttribute(REPOSITORY_NAME, attribute);
		verify(decoratedRepositoryCollection, times(1)).addAttribute(REPOSITORY_NAME, attribute);
		verify(indexTransactionLogService).log(entityMeta, CudType.UPDATE, DataType.METADATA, null);
	}

	@Test
	public void deleteAttribute()
	{
		indexTransactionLogRepositoryCollectionDecorator.deleteAttribute(REPOSITORY_NAME, "attribute");
		verify(decoratedRepositoryCollection, times(1)).deleteAttribute(REPOSITORY_NAME, "attribute");
		verify(indexTransactionLogService).log(entityMeta, CudType.UPDATE, DataType.METADATA, null);
	}

	@Test
	public void addAttributeSync()
	{
		DefaultAttributeMetaData attribute = mock(DefaultAttributeMetaData.class);
		indexTransactionLogRepositoryCollectionDecorator.addAttributeSync(REPOSITORY_NAME, attribute);
		verify(decoratedRepositoryCollection, times(1)).addAttribute(REPOSITORY_NAME, attribute);
		verify(indexTransactionLogService).log(entityMeta, CudType.UPDATE, DataType.METADATA, null);
	}

	@Test
	public void addEntityMeta()
	{
		indexTransactionLogRepositoryCollectionDecorator.addEntityMeta(entityMeta);
		verify(decoratedRepositoryCollection, times(1)).addEntityMeta(entityMeta);
		verify(indexTransactionLogService).log(entityMeta, CudType.ADD, DataType.METADATA, null);
	}
}
