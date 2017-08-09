package org.molgenis.data.postgresql;

import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.util.AttributeCopier;
import org.molgenis.data.meta.util.EntityTypeCopier;
import org.molgenis.data.postgresql.identifier.EntityTypeRegistry;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

public class PostgreSqlRepositoryCollectionDecoratorTest
{
	private EntityType entityType;
	private EntityType updatedEntityType;
	private RepositoryCollection repoCollection;
	private EntityTypeRegistry entityTypeRegistry;
	private PostgreSqlRepositoryCollectionDecorator repoCollectionDecorator;
	private AttributeCopier attributeCopier;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		String entityTypeId = "entityTypeId";
		entityType = mock(EntityType.class);
		when(entityType.getEntityType()).thenReturn(mock(EntityType.class));
		when(entityType.getId()).thenReturn(entityTypeId);
		updatedEntityType = mock(EntityType.class);
		repoCollection = mock(RepositoryCollection.class);
		entityTypeRegistry = mock(EntityTypeRegistry.class);
		EntityTypeCopier entityTypeCopier = mock(EntityTypeCopier.class);
		when(entityTypeCopier.copy(entityType)).thenReturn(updatedEntityType);
		attributeCopier = mock(AttributeCopier.class);
		repoCollectionDecorator = new PostgreSqlRepositoryCollectionDecorator(repoCollection, entityTypeRegistry,
				entityTypeCopier, attributeCopier);
	}

	@Test
	public void testDelegate()
	{
		assertEquals(repoCollectionDecorator.delegate(), repoCollection);
	}

	@Test
	public void testCreateRepository()
	{
		repoCollectionDecorator.createRepository(entityType);
		verify(repoCollection).createRepository(entityType);
		verify(entityTypeRegistry).registerEntityType(entityType);
	}

	@Test
	public void testDeleteRepository()
	{
		repoCollectionDecorator.deleteRepository(entityType);
		verify(repoCollection).deleteRepository(entityType);
		verify(entityTypeRegistry).unregisterEntityType(entityType);
	}

	@Test
	public void testAddAttribute()
	{
		Attribute attr = mock(Attribute.class);
		when(attributeCopier.copy(attr)).thenReturn(attr);
		repoCollectionDecorator.addAttribute(entityType, attr);
		verify(entityTypeRegistry).registerEntityType(updatedEntityType);
		verify(repoCollection).addAttribute(entityType, attr);
	}

	@Test
	public void testUpdateAttribute()
	{
		String entityTypeId = "entityTypeId";
		when(entityType.getId()).thenReturn(entityTypeId);
		Attribute attr = mock(Attribute.class);

		Attribute updatedAttr = mock(Attribute.class);
		repoCollectionDecorator.updateAttribute(entityType, attr, updatedAttr);
		verify(entityTypeRegistry).registerEntityType(updatedEntityType);
		verify(repoCollection).updateAttribute(entityType, attr, updatedAttr);
	}

	@Test
	public void testDeleteAttribute()
	{
		String entityTypeId = "entityTypeId";
		when(entityType.getId()).thenReturn(entityTypeId);
		Attribute attr = mock(Attribute.class);

		repoCollectionDecorator.deleteAttribute(entityType, attr);
		verify(repoCollection).deleteAttribute(entityType, attr);
		verify(entityTypeRegistry).registerEntityType(updatedEntityType);
	}
}