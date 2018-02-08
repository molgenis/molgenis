package org.molgenis.data;

import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.populate.EntityPopulator;
import org.molgenis.data.support.DynamicEntity;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.*;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.testng.Assert.assertEquals;

public class EntityManagerImplTest
{
	private DataService dataService;
	private EntityManagerImpl entityManagerImpl;
	private EntityFactoryRegistry entityFactoryRegistry;
	private EntityPopulator entityPopulator;
	private EntityReferenceCreator entityReferenceCreator;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		dataService = mock(DataService.class);
		entityFactoryRegistry = mock(EntityFactoryRegistry.class);
		entityPopulator = mock(EntityPopulator.class);
		entityReferenceCreator = mock(EntityReferenceCreator.class);
		entityManagerImpl = new EntityManagerImpl(dataService, entityFactoryRegistry, entityPopulator,
				entityReferenceCreator);
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void EntityManagerImpl()
	{
		new EntityManagerImpl(null, null, null, null);
	}

	@Test
	public void getReference()
	{
		EntityType entityType = mock(EntityType.class);
		Object id = mock(Object.class);
		entityManagerImpl.getReference(entityType, id);
		verify(entityReferenceCreator).getReference(entityType, id);
	}

	@Test
	public void getReferences()
	{
		EntityType entityType = mock(EntityType.class);
		Iterable<?> ids = mock(Iterable.class);
		entityManagerImpl.getReferences(entityType, ids);
		verify(entityReferenceCreator).getReferences(entityType, ids);
	}

	@Test
	public void resolveReferencesNoFetch()
	{
		EntityType entityType = when(mock(EntityType.class).getId()).thenReturn("entity").getMock();

		Entity entity0 = new DynamicEntity(entityType); // do not mock, setters will be called
		Entity entity1 = new DynamicEntity(entityType); // do not mock, setters will be called
		Stream<Entity> entities = Stream.of(entity0, entity1);

		Fetch fetch = null;
		assertEquals(entities, entityManagerImpl.resolveReferences(entityType, entities, fetch));
	}

	@Test
	public void resolveReferencesStreamNoFetch()
	{
		EntityType entityType = when(mock(EntityType.class).getId()).thenReturn("entity").getMock();
		Attribute labelAttr = when(mock(Attribute.class).getName()).thenReturn("labelAttr").getMock();
		when(labelAttr.getDataType()).thenReturn(STRING);
		when(entityType.getLabelAttribute()).thenReturn(labelAttr);
		when(entityType.getAtomicAttributes()).thenReturn(singletonList(labelAttr));

		Entity entity0 = new DynamicEntity(entityType); // do not mock, setters will be called
		Entity entity1 = new DynamicEntity(entityType); // do not mock, setters will be called

		Fetch fetch = null;
		Stream<Entity> entities = entityManagerImpl.resolveReferences(entityType, Stream.of(entity0, entity1), fetch);
		assertEquals(entities.collect(Collectors.toList()), Arrays.asList(entity0, entity1));
	}
}
