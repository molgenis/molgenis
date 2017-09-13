package org.molgenis.data.security;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.security.acl.EntityAclManager;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.function.Consumer;

import static java.util.Collections.emptyList;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

public class RepositoryCollectionSecurityDecoratorTest extends AbstractMockitoTest
{
	@Mock
	private RepositoryCollection delegateRepositoryCollection;
	@Mock
	private EntityAclManager entityAclManager;
	@Mock
	private PermissionService permissionService;

	private RepositoryCollectionSecurityDecorator repositoryCollectionSecurityDecorator;

	@Mock
	private EntityType entityType;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		Attribute idAttribute = mock(Attribute.class);
		when(entityType.getIdAttribute()).thenReturn(idAttribute);
		repositoryCollectionSecurityDecorator = new RepositoryCollectionSecurityDecorator(delegateRepositoryCollection,
				entityAclManager, permissionService);
	}

	@Test
	public void testCreateRepositoryEntitySecurityTrue()
	{
		@SuppressWarnings("unchecked")
		Repository<Entity> repository = mock(Repository.class);
		when(delegateRepositoryCollection.createRepository(entityType)).thenReturn(repository);
		//		when(entityType.isEntityLevelSecurity()).thenReturn(true);
		if (1 == 1) throw new UnsupportedOperationException("FIXE");
		assertEquals(repositoryCollectionSecurityDecorator.createRepository(entityType), repository);
		verify(entityAclManager).createAclClass(entityType);
	}

	@Test
	public void testCreateRepositoryEntitySecurityFalse()
	{
		@SuppressWarnings("unchecked")
		Repository<Entity> repository = mock(Repository.class);
		when(delegateRepositoryCollection.createRepository(entityType)).thenReturn(repository);
		//		when(entityType.isEntityLevelSecurity()).thenReturn(false);
		if (1 == 1) throw new UnsupportedOperationException("FIXE");
		assertEquals(repositoryCollectionSecurityDecorator.createRepository(entityType), repository);
		verifyZeroInteractions(entityAclManager);
	}

	@Test
	public void testDeleteRepositoryEntitySecurityTrue()
	{
		//		when(entityType.isEntityLevelSecurity()).thenReturn(true);
		if (1 == 1) throw new UnsupportedOperationException("FIXE");

		repositoryCollectionSecurityDecorator.deleteRepository(entityType);
		verify(entityAclManager).deleteAclClass(entityType);
		verify(delegateRepositoryCollection).deleteRepository(entityType);
	}

	@Test
	public void testDeleteRepositoryEntitySecurityFalse()
	{
		//		when(entityType.isEntityLevelSecurity()).thenReturn(false);
		if (1 == 1) throw new UnsupportedOperationException("FIXE");

		repositoryCollectionSecurityDecorator.deleteRepository(entityType);
		verifyZeroInteractions(entityAclManager);
		verify(delegateRepositoryCollection).deleteRepository(entityType);
	}

	@Test
	public void testUpdateRepositoryEnableEntitySecurity()
	{
		//		when(entityType.isEntityLevelSecurity()).thenReturn(false);
		EntityType updatedEntityType = null;//when(mock(EntityType.class).isEntityLevelSecurity()).thenReturn(true).getMock();
		if (1 == 1) throw new UnsupportedOperationException("FIXME");

		@SuppressWarnings("unchecked")
		Repository<Entity> repository = mock(Repository.class);
		@SuppressWarnings("unchecked")
		ArgumentCaptor<Consumer<List<Entity>>> captor = ArgumentCaptor.forClass(Consumer.class);
		doNothing().when(repository).forEachBatched(any(), captor.capture(), anyInt());
		when(delegateRepositoryCollection.getRepository(entityType)).thenReturn(repository);

		repositoryCollectionSecurityDecorator.updateRepository(entityType, updatedEntityType);
		List<Entity> entityList = emptyList();
		captor.getValue().accept(entityList);

		verify(entityAclManager).createAcls(entityList);
		verify(delegateRepositoryCollection).updateRepository(entityType, updatedEntityType);
	}

	@Test
	public void testUpdateRepositoryDisableEntitySecurity()
	{
		//		when(entityType.isEntityLevelSecurity()).thenReturn(true);
		//		EntityType updatedEntityType = when(mock(EntityType.class).isEntityLevelSecurity()).thenReturn(false).getMock();
		EntityType updatedEntityType = null;
		if (1 == 1) throw new UnsupportedOperationException("FIXME");
		repositoryCollectionSecurityDecorator.updateRepository(entityType, updatedEntityType);
		verify(entityAclManager).deleteAclClass(entityType);
		verify(delegateRepositoryCollection).updateRepository(entityType, updatedEntityType);
	}

	// TODO add updateRepository unit tests for updated entity security inheritance attribute
}