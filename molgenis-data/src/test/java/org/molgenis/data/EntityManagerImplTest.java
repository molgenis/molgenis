package org.molgenis.data;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Iterator;

import org.molgenis.data.support.DefaultEntity;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class EntityManagerImplTest
{
	private DataService dataService;
	private EntityManagerImpl entityManagerImpl;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		dataService = mock(DataService.class);
		entityManagerImpl = new EntityManagerImpl(dataService);
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void EntityManagerImpl()
	{
		new EntityManagerImpl(null);
	}

	@Test
	public void getReference()
	{
		String entityName = "entity";
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn(entityName).getMock();
		AttributeMetaData idAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("id").getMock();
		AttributeMetaData lblAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("label").getMock();
		when(entityMeta.getIdAttribute()).thenReturn(idAttr);
		when(entityMeta.getLabelAttribute()).thenReturn(lblAttr);

		String label = "label";
		Integer id = Integer.valueOf(0);
		Entity entity = when(mock(Entity.class).getLabelValue()).thenReturn(label).getMock();
		when(dataService.findOne(entityName, id)).thenReturn(entity);

		Entity entityReference = entityManagerImpl.getReference(entityMeta, id);
		assertEquals(entityReference.getIdValue(), id);
		verifyNoMoreInteractions(dataService);
		assertEquals(label, entityReference.getLabelValue());
		verify(dataService, times(1)).findOne(entityName, id);
	}

	@Test
	public void getReferences()
	{
		String entityName = "entity";
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn(entityName).getMock();
		AttributeMetaData idAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("id").getMock();
		AttributeMetaData lblAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("label").getMock();
		when(entityMeta.getIdAttribute()).thenReturn(idAttr);
		when(entityMeta.getLabelAttribute()).thenReturn(lblAttr);

		String label0 = "label0";
		Integer id0 = Integer.valueOf(0);
		Entity entity0 = when(mock(Entity.class).getLabelValue()).thenReturn(label0).getMock();
		when(dataService.findOne(entityName, id0)).thenReturn(entity0);

		String label1 = "label1";
		Integer id1 = Integer.valueOf(1);
		Entity entity1 = when(mock(Entity.class).getLabelValue()).thenReturn(label1).getMock();
		when(dataService.findOne(entityName, id1)).thenReturn(entity1);

		Iterable<Entity> entityReferences = entityManagerImpl.getReferences(entityMeta, Arrays.asList(id0, id1));
		Iterator<Entity> it = entityReferences.iterator();
		assertTrue(it.hasNext());

		Entity entityReference0 = it.next();
		assertEquals(entityReference0.getIdValue(), id0);
		verifyNoMoreInteractions(dataService);
		assertEquals(entityReference0.getLabelValue(), label0);
		verify(dataService, times(1)).findOne(entityName, id0);

		assertTrue(it.hasNext());
		Entity entityReference1 = it.next();
		assertEquals(entityReference1.getIdValue(), id1);
		verifyNoMoreInteractions(dataService);
		assertEquals(entityReference1.getLabelValue(), label1);
		verify(dataService, times(1)).findOne(entityName, id1);

		assertFalse(it.hasNext());
	}

	@Test
	public void resolveReferencesNoFetch()
	{
		EntityMetaData entityMeta = mock(EntityMetaData.class);

		Entity entity0 = new DefaultEntity(entityMeta, dataService); // do not mock, setters will be called
		Entity entity1 = new DefaultEntity(entityMeta, dataService); // do not mock, setters will be called
		Iterable<Entity> entities = Arrays.asList(entity0, entity1);

		Fetch fetch = null;
		assertEquals(entities, entityManagerImpl.resolveReferences(entityMeta, entities, fetch));
	}
}
