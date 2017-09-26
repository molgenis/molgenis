package org.molgenis.data.postgresql;

import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.postgresql.identifier.EntityTypeRegistry;
import org.molgenis.data.postgresql.identifier.Identifiable;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.*;
import static org.molgenis.data.meta.AttributeType.INT;
import static org.molgenis.data.meta.AttributeType.MREF;

public class PostgreSqlRepositoryCollectionDecoratorTest
{
	private EntityType entityType;
	private RepositoryCollection repoCollection;
	private EntityTypeRegistry entityTypeRegistry;
	private PostgreSqlRepositoryCollectionDecorator repoCollectionDecorator;
	private final String entityTypeId = "entityTypeId";

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		entityType = mock(EntityType.class);
		when(entityType.getEntityType()).thenReturn(mock(EntityType.class));
		when(entityType.getId()).thenReturn(entityTypeId);
		repoCollection = mock(RepositoryCollection.class);
		entityTypeRegistry = mock(EntityTypeRegistry.class);
		repoCollectionDecorator = new PostgreSqlRepositoryCollectionDecorator(repoCollection, entityTypeRegistry);
	}

	@Test
	public void testCreateRepository()
	{
		Attribute attr = mock(Attribute.class);
		when(attr.getDataType()).thenReturn(MREF);
		when(attr.getName()).thenReturn("attr");
		when(attr.getIdentifier()).thenReturn("attrId");
		when(entityType.getAllAttributes()).thenReturn(Collections.singletonList(attr));

		repoCollectionDecorator.createRepository(entityType);
		verify(repoCollection).createRepository(entityType);
		verify(entityTypeRegistry).registerEntityType(entityType.getId(),
				singletonList(Identifiable.create("attr", "attrId")));
	}

	@Test
	public void testDeleteRepository()
	{
		Attribute attr = mock(Attribute.class);
		when(attr.getDataType()).thenReturn(MREF);
		when(attr.getName()).thenReturn("attr");
		when(attr.getIdentifier()).thenReturn("attrId");
		when(entityType.getAllAttributes()).thenReturn(Collections.singletonList(attr));

		repoCollectionDecorator.deleteRepository(entityType);
		verify(repoCollection).deleteRepository(entityType);
		verify(entityTypeRegistry).unregisterEntityType(entityType.getId(),
				singletonList(Identifiable.create("attr", "attrId")));
	}

	@Test
	public void testAddAttributeNotReferencing()
	{
		Attribute attr = mock(Attribute.class);
		when(attr.getDataType()).thenReturn(INT);
		when(entityType.getId()).thenReturn("entityTypeId");
		repoCollectionDecorator.addAttribute(entityType, attr);
		verifyNoMoreInteractions(entityTypeRegistry);
		verify(repoCollection).addAttribute(entityType, attr);
	}

	@Test
	public void testAddAttributeReferencing()
	{
		Attribute existingAttr = mock(Attribute.class);
		when(existingAttr.getDataType()).thenReturn(MREF);
		when(existingAttr.getName()).thenReturn("existing");
		when(existingAttr.getIdentifier()).thenReturn("existingId");

		Attribute newAttr = mock(Attribute.class);
		when(newAttr.getDataType()).thenReturn(MREF);
		when(newAttr.getName()).thenReturn("new");
		when(newAttr.getIdentifier()).thenReturn("newId");

		when(entityType.getAllAttributes()).thenReturn(Collections.singletonList(existingAttr));
		repoCollectionDecorator.addAttribute(entityType, newAttr);
		verify(entityTypeRegistry).registerEntityType("entityTypeId",
				asList(Identifiable.create("existing", "existingId"), Identifiable.create("new", "newId")));
		verify(repoCollection).addAttribute(entityType, newAttr);
	}


	@Test
	public void testUpdateAttribute()
	{
		Attribute attr = mock(Attribute.class);
		when(attr.getDataType()).thenReturn(MREF);
		when(attr.getName()).thenReturn("existing");
		when(attr.getIdentifier()).thenReturn("existingId");

		Attribute updatedAttr = mock(Attribute.class);
		when(updatedAttr.getDataType()).thenReturn(MREF);
		when(updatedAttr.getName()).thenReturn("existing");
		when(updatedAttr.getIdentifier()).thenReturn("existingId");
		when(entityType.getAllAttributes()).thenReturn(Collections.singletonList(attr));

		repoCollectionDecorator.updateAttribute(entityType, attr, updatedAttr);
		verify(entityTypeRegistry).registerEntityType(entityTypeId,
				Collections.singletonList(Identifiable.create("existing", "existingId")));
		verify(repoCollection).updateAttribute(entityType, attr, updatedAttr);
	}

	@Test
	public void testDeleteAttribute()
	{
		Attribute attr = mock(Attribute.class);
		when(attr.getDataType()).thenReturn(MREF);
		when(attr.getName()).thenReturn("existing");
		when(attr.getIdentifier()).thenReturn("existingId");
		when(entityType.getAllAttributes()).thenReturn(Collections.singletonList(attr));

		repoCollectionDecorator.deleteAttribute(entityType, attr);
		verify(repoCollection).deleteAttribute(entityType, attr);
		verify(entityTypeRegistry).registerEntityType(entityTypeId, emptyList());
	}
}