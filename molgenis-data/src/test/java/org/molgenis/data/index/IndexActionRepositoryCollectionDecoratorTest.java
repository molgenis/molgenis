package org.molgenis.data.index;

import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityMetaData;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;

public class IndexActionRepositoryCollectionDecoratorTest
{
	private final static String REPOSITORY_NAME = "repo";
	private RepositoryCollection decoratedRepositoryCollection;
	private EntityMetaData entityMeta;
	private IndexActionRegisterService indexActionRegisterService;
	private IndexActionRepositoryCollectionDecorator indexActionRepositoryCollectionDecorator;

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
		indexActionRegisterService = mock(IndexActionRegisterService.class);
		indexActionRepositoryCollectionDecorator = new IndexActionRepositoryCollectionDecorator(
				decoratedRepositoryCollection, indexActionRegisterService);
	}

	@Test
	public void deleteEntityMeta()
	{
		indexActionRepositoryCollectionDecorator.deleteRepository(entityMeta);
		verify(decoratedRepositoryCollection, times(1)).deleteRepository(entityMeta);
		verify(indexActionRegisterService).register("repo", null);
	}

	@Test
	public void addAttribute()
	{
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn(REPOSITORY_NAME).getMock();
		AttributeMetaData attribute = when(mock(AttributeMetaData.class).getName()).thenReturn("attribute").getMock();
		indexActionRepositoryCollectionDecorator.addAttribute(entityMeta, attribute);
		verify(decoratedRepositoryCollection, times(1)).addAttribute(entityMeta, attribute);
		verify(indexActionRegisterService).register("repo", null);
	}

	@Test
	public void deleteAttribute()
	{
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn(REPOSITORY_NAME).getMock();
		AttributeMetaData attribute = when(mock(AttributeMetaData.class).getName()).thenReturn("attribute").getMock();
		indexActionRepositoryCollectionDecorator.deleteAttribute(entityMeta, attribute);
		verify(decoratedRepositoryCollection, times(1)).deleteAttribute(entityMeta, attribute);
		verify(indexActionRegisterService).register("repo", null);
	}

	@Test
	public void addEntityMeta()
	{
		indexActionRepositoryCollectionDecorator.createRepository(entityMeta);
		verify(decoratedRepositoryCollection, times(1)).createRepository(entityMeta);
		verify(indexActionRegisterService).register("repo", null);
	}
}
