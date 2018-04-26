package org.molgenis.data;

import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Iterator;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

public class EntityReferenceCreatorImplTest
{
	private DataService dataService;
	private EntityReferenceCreatorImpl entityManagerImpl;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		dataService = mock(DataService.class);
		EntityFactoryRegistry entityFactoryRegistry = mock(EntityFactoryRegistry.class);
		entityManagerImpl = new EntityReferenceCreatorImpl(dataService, entityFactoryRegistry);
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void EntityReferenceCreatorImpl()
	{
		new EntityReferenceCreatorImpl(null, null);
	}

	@Test
	public void getReference()
	{
		String entityTypeId = "entity";
		EntityType entityType = when(mock(EntityType.class).getId()).thenReturn(entityTypeId).getMock();
		Attribute idAttr = when(mock(Attribute.class).getName()).thenReturn("id").getMock();
		Attribute lblAttr = when(mock(Attribute.class).getName()).thenReturn("label").getMock();
		when(entityType.getIdAttribute()).thenReturn(idAttr);
		when(entityType.getLabelAttribute()).thenReturn(lblAttr);

		String label = "label";
		Integer id = 0;
		Entity entity = when(mock(Entity.class).getLabelValue()).thenReturn(label).getMock();
		when(dataService.findOneById(entityTypeId, id)).thenReturn(entity);

		Entity entityReference = entityManagerImpl.getReference(entityType, id);
		assertEquals(entityReference.getIdValue(), id);
		verifyNoMoreInteractions(dataService);
		assertEquals(label, entityReference.getLabelValue());
		verify(dataService, times(1)).findOneById(entityTypeId, id);
	}

	@Test
	public void getReferences()
	{
		String entityTypeId = "entity";
		EntityType entityType = when(mock(EntityType.class).getId()).thenReturn(entityTypeId).getMock();
		Attribute idAttr = when(mock(Attribute.class).getName()).thenReturn("id").getMock();
		Attribute lblAttr = when(mock(Attribute.class).getName()).thenReturn("label").getMock();
		when(entityType.getIdAttribute()).thenReturn(idAttr);
		when(entityType.getLabelAttribute()).thenReturn(lblAttr);

		String label0 = "label0";
		Integer id0 = 0;
		Entity entity0 = when(mock(Entity.class).getLabelValue()).thenReturn(label0).getMock();
		when(dataService.findOneById(entityTypeId, id0)).thenReturn(entity0);

		String label1 = "label1";
		Integer id1 = 1;
		Entity entity1 = when(mock(Entity.class).getLabelValue()).thenReturn(label1).getMock();
		when(dataService.findOneById(entityTypeId, id1)).thenReturn(entity1);

		Iterable<Entity> entityReferences = entityManagerImpl.getReferences(entityType, Arrays.asList(id0, id1));
		Iterator<Entity> it = entityReferences.iterator();
		assertTrue(it.hasNext());

		Entity entityReference0 = it.next();
		assertEquals(entityReference0.getIdValue(), id0);
		verifyNoMoreInteractions(dataService);
		assertEquals(entityReference0.getLabelValue(), label0);
		verify(dataService, times(1)).findOneById(entityTypeId, id0);

		assertTrue(it.hasNext());
		Entity entityReference1 = it.next();
		assertEquals(entityReference1.getIdValue(), id1);
		verifyNoMoreInteractions(dataService);
		assertEquals(entityReference1.getLabelValue(), label1);
		verify(dataService, times(1)).findOneById(entityTypeId, id1);

		assertFalse(it.hasNext());
	}
}