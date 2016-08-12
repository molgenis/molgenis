package org.molgenis.data.reindex;

import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.reindex.meta.ReindexActionMetaData.CudType;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;
import static org.molgenis.data.reindex.meta.ReindexActionMetaData.CudType.CREATE;
import static org.molgenis.data.reindex.meta.ReindexActionMetaData.CudType.UPDATE;
import static org.molgenis.data.reindex.meta.ReindexActionMetaData.DataType.METADATA;

public class ReindexActionRepositoryCollectionDecoratorTest
{
	private final static String REPOSITORY_NAME = "repo";
	private RepositoryCollection decoratedRepositoryCollection;
	private EntityMetaData entityMeta;
	private ReindexActionRegisterService reindexActionRegisterService;
	private ReindexActionRepositoryCollectionDecorator reindexActionRepositoryCollectionDecorator;

	@SuppressWarnings("unchecked")
	@BeforeMethod
	public void setUpBeforeMethod()
	{
		decoratedRepositoryCollection = mock(RepositoryCollection.class);
		Repository<Entity> repository = (Repository<Entity>) mock(Repository.class);
		when(decoratedRepositoryCollection.getRepository(REPOSITORY_NAME)).thenReturn(repository);
		entityMeta = mock(EntityMetaData.class);
		when(entityMeta.getName()).thenReturn("repo");
		when(repository.getEntityMetaData()).thenReturn(entityMeta);
		reindexActionRegisterService = mock(ReindexActionRegisterService.class);
		reindexActionRepositoryCollectionDecorator = new ReindexActionRepositoryCollectionDecorator(
				decoratedRepositoryCollection, reindexActionRegisterService);
	}

	@Test
	public void deleteEntityMeta()
	{
		reindexActionRepositoryCollectionDecorator.deleteRepository(entityMeta);
		verify(decoratedRepositoryCollection, times(1)).deleteRepository(entityMeta);
		verify(reindexActionRegisterService).register("repo", CudType.DELETE, METADATA, null);
	}

	@Test
	public void addAttribute()
	{
		AttributeMetaData attribute = when(mock(AttributeMetaData.class).getName()).thenReturn("attribute").getMock();
		reindexActionRepositoryCollectionDecorator.addAttribute(REPOSITORY_NAME, attribute);
		verify(decoratedRepositoryCollection, times(1)).addAttribute(REPOSITORY_NAME, attribute);
		verify(reindexActionRegisterService).register("repo", UPDATE, METADATA, null);
	}

	@Test
	public void deleteAttribute()
	{
		reindexActionRepositoryCollectionDecorator.deleteAttribute(REPOSITORY_NAME, "attribute");
		verify(decoratedRepositoryCollection, times(1)).deleteAttribute(REPOSITORY_NAME, "attribute");
		verify(reindexActionRegisterService).register("repo", UPDATE, METADATA, null);
	}

	@Test
	public void addEntityMeta()
	{
		reindexActionRepositoryCollectionDecorator.createRepository(entityMeta);
		verify(decoratedRepositoryCollection, times(1)).createRepository(entityMeta);
		verify(reindexActionRegisterService).register("repo", CREATE, METADATA, null);
	}
}
