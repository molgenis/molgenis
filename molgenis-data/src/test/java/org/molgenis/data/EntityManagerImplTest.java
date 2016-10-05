package org.molgenis.data;

import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.populate.EntityPopulator;
import org.molgenis.data.support.DynamicEntity;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static freemarker.template.utility.Collections12.singletonList;
import static org.mockito.Mockito.*;
import static org.molgenis.MolgenisFieldTypes.AttributeType.STRING;
import static org.testng.Assert.*;

public class EntityManagerImplTest
{
	private DataService dataService;
	private EntityManagerImpl entityManagerImpl;
	private EntityFactoryRegistry entityFactoryRegistry;
	private EntityPopulator entityPopulator;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		dataService = mock(DataService.class);
		entityFactoryRegistry = mock(EntityFactoryRegistry.class);
		entityPopulator = mock(EntityPopulator.class);
		entityManagerImpl = new EntityManagerImpl(dataService, entityFactoryRegistry, entityPopulator);
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void EntityManagerImpl()
	{
		new EntityManagerImpl(null, null, null);
	}

	@Test
	public void getReference()
	{
		String entityName = "entity";
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn(entityName).getMock();
		Attribute idAttr = when(mock(Attribute.class).getName()).thenReturn("id").getMock();
		Attribute lblAttr = when(mock(Attribute.class).getName()).thenReturn("label").getMock();
		when(entityMeta.getIdAttribute()).thenReturn(idAttr);
		when(entityMeta.getLabelAttribute()).thenReturn(lblAttr);

		String label = "label";
		Integer id = Integer.valueOf(0);
		Entity entity = when(mock(Entity.class).getLabelValue()).thenReturn(label).getMock();
		when(dataService.findOneById(entityName, id)).thenReturn(entity);

		Entity entityReference = entityManagerImpl.getReference(entityMeta, id);
		assertEquals(entityReference.getIdValue(), id);
		verifyNoMoreInteractions(dataService);
		assertEquals(label, entityReference.getLabelValue());
		verify(dataService, times(1)).findOneById(entityName, id);
	}

	@Test
	public void getReferences()
	{
		String entityName = "entity";
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn(entityName).getMock();
		Attribute idAttr = when(mock(Attribute.class).getName()).thenReturn("id").getMock();
		Attribute lblAttr = when(mock(Attribute.class).getName()).thenReturn("label").getMock();
		when(entityMeta.getIdAttribute()).thenReturn(idAttr);
		when(entityMeta.getLabelAttribute()).thenReturn(lblAttr);

		String label0 = "label0";
		Integer id0 = Integer.valueOf(0);
		Entity entity0 = when(mock(Entity.class).getLabelValue()).thenReturn(label0).getMock();
		when(dataService.findOneById(entityName, id0)).thenReturn(entity0);

		String label1 = "label1";
		Integer id1 = Integer.valueOf(1);
		Entity entity1 = when(mock(Entity.class).getLabelValue()).thenReturn(label1).getMock();
		when(dataService.findOneById(entityName, id1)).thenReturn(entity1);

		Iterable<Entity> entityReferences = entityManagerImpl.getReferences(entityMeta, Arrays.asList(id0, id1));
		Iterator<Entity> it = entityReferences.iterator();
		assertTrue(it.hasNext());

		Entity entityReference0 = it.next();
		assertEquals(entityReference0.getIdValue(), id0);
		verifyNoMoreInteractions(dataService);
		assertEquals(entityReference0.getLabelValue(), label0);
		verify(dataService, times(1)).findOneById(entityName, id0);

		assertTrue(it.hasNext());
		Entity entityReference1 = it.next();
		assertEquals(entityReference1.getIdValue(), id1);
		verifyNoMoreInteractions(dataService);
		assertEquals(entityReference1.getLabelValue(), label1);
		verify(dataService, times(1)).findOneById(entityName, id1);

		assertFalse(it.hasNext());
	}

	@Test
	public void resolveReferencesNoFetch()
	{
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();

		Entity entity0 = new DynamicEntity(entityMeta); // do not mock, setters will be called
		Entity entity1 = new DynamicEntity(entityMeta); // do not mock, setters will be called
		Stream<Entity> entities = Stream.of(entity0, entity1);

		Fetch fetch = null;
		assertEquals(entities, entityManagerImpl.resolveReferences(entityMeta, entities, fetch));
	}

	@Test
	public void resolveReferencesStreamNoFetch()
	{
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		Attribute labelAttr = when(mock(Attribute.class).getName()).thenReturn("labelAttr").getMock();
		when(labelAttr.getDataType()).thenReturn(STRING);
		when(entityMeta.getLabelAttribute()).thenReturn(labelAttr);
		when(entityMeta.getAtomicAttributes()).thenReturn(singletonList(labelAttr));

		Entity entity0 = new DynamicEntity(entityMeta); // do not mock, setters will be called
		Entity entity1 = new DynamicEntity(entityMeta); // do not mock, setters will be called

		Fetch fetch = null;
		Stream<Entity> entities = entityManagerImpl.resolveReferences(entityMeta, Stream.of(entity0, entity1), fetch);
		assertEquals(entities.collect(Collectors.toList()), Arrays.asList(entity0, entity1));
	}
}
