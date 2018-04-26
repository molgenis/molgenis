package org.molgenis.data.index;

import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;

public class IndexActionRepositoryCollectionDecoratorTest
{
	private final static String REPOSITORY_NAME = "repo";
	private RepositoryCollection decoratedRepositoryCollection;
	private EntityType entityType;
	private IndexActionRegisterService indexActionRegisterService;
	private IndexActionRepositoryCollectionDecorator indexActionRepositoryCollectionDecorator;

	@SuppressWarnings("unchecked")
	@BeforeMethod
	public void setUpBeforeMethod()
	{
		decoratedRepositoryCollection = mock(RepositoryCollection.class);
		Repository<Entity> repository = (Repository<Entity>) mock(Repository.class);
		when(decoratedRepositoryCollection.getRepository(REPOSITORY_NAME)).thenReturn(repository);
		entityType = mock(EntityType.class);
		when(entityType.getId()).thenReturn("repo");
		when(repository.getEntityType()).thenReturn(entityType);
		indexActionRegisterService = mock(IndexActionRegisterService.class);
		indexActionRepositoryCollectionDecorator = new IndexActionRepositoryCollectionDecorator(
				decoratedRepositoryCollection, indexActionRegisterService);
	}

	@Test
	public void deleteRepository()
	{
		indexActionRepositoryCollectionDecorator.deleteRepository(entityType);
		verify(decoratedRepositoryCollection).deleteRepository(entityType);
		verify(indexActionRegisterService).register(entityType, null);
	}

	@Test
	public void updateRepository()
	{
		EntityType entityType2 = mock(EntityType.class);
		indexActionRepositoryCollectionDecorator.updateRepository(entityType, entityType2);
		verify(decoratedRepositoryCollection).updateRepository(entityType, entityType2);
		verify(indexActionRegisterService).register(entityType, null);
	}

	@Test
	public void addAttribute()
	{
		EntityType entityType = when(mock(EntityType.class).getId()).thenReturn(REPOSITORY_NAME).getMock();
		Attribute attribute = when(mock(Attribute.class).getName()).thenReturn("attribute").getMock();
		indexActionRepositoryCollectionDecorator.addAttribute(entityType, attribute);
		verify(decoratedRepositoryCollection).addAttribute(entityType, attribute);
		verify(indexActionRegisterService).register(entityType, null);
	}

	@Test
	public void deleteAttribute()
	{
		EntityType entityType = when(mock(EntityType.class).getId()).thenReturn(REPOSITORY_NAME).getMock();
		Attribute attribute = when(mock(Attribute.class).getName()).thenReturn("attribute").getMock();
		indexActionRepositoryCollectionDecorator.deleteAttribute(entityType, attribute);
		verify(decoratedRepositoryCollection).deleteAttribute(entityType, attribute);
		verify(indexActionRegisterService).register(entityType, null);
	}

	@Test
	public void addEntityType()
	{
		indexActionRepositoryCollectionDecorator.createRepository(entityType);
		verify(decoratedRepositoryCollection).createRepository(entityType);
		verify(indexActionRegisterService).register(entityType, null);
	}
}
