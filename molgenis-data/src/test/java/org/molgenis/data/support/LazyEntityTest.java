package org.molgenis.data.support;

import com.google.common.collect.Lists;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

public class LazyEntityTest
{
	private static final String ENTITY_NAME = "entity";
	private static final String ID_ATTR_NAME = "id";

	private EntityType entityType;
	private Attribute idAttr;

	private DataService dataService;
	private Object id;
	private LazyEntity lazyEntity;
	private Entity entity;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		entityType = mock(EntityType.class);
		when(entityType.getId()).thenReturn(ENTITY_NAME);
		idAttr = mock(Attribute.class);
		when(idAttr.getName()).thenReturn(ID_ATTR_NAME);
		when(entityType.getIdAttribute()).thenReturn(idAttr);
		dataService = mock(DataService.class);
		entity = mock(Entity.class);
		when(dataService.findOneById(ENTITY_NAME, id)).thenReturn(entity);
		id = 1;
		lazyEntity = new LazyEntity(entityType, dataService, id);
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void LazyEntity()
	{
		new LazyEntity(null, null, null);
	}

	@Test
	public void get()
	{
		String attrName = "attr";
		Object value = mock(Object.class);
		when(entity.get(attrName)).thenReturn(value);
		assertEquals(value, lazyEntity.get(attrName));
		assertEquals(value, lazyEntity.get(attrName));
		verify(dataService, times(1)).findOneById(ENTITY_NAME, id);
	}

	@Test
	public void getIdAttr()
	{
		assertEquals(id, lazyEntity.get(ID_ATTR_NAME));
	}

	@Test
	public void getAttributeNames()
	{
		Entity entity = new DynamicEntity(entityType);
		Attribute attr0 = when(mock(Attribute.class).getName()).thenReturn("attr0").getMock();
		Attribute attr1 = when(mock(Attribute.class).getName()).thenReturn("attr1").getMock();
		when(entityType.getAtomicAttributes()).thenReturn(Arrays.asList(attr0, attr1));
		assertEquals(Lists.newArrayList(entity.getAttributeNames()), Arrays.asList("attr0", "attr1"));
	}

	@Test
	public void getBoolean()
	{
		String attrName = "attr";
		Boolean value = Boolean.TRUE;
		when(entity.getBoolean(attrName)).thenReturn(value);
		assertEquals(value, lazyEntity.getBoolean(attrName));
		assertEquals(value, lazyEntity.getBoolean(attrName));
		verify(dataService, times(1)).findOneById(ENTITY_NAME, id);
	}

	@Test
	public void getDouble()
	{
		String attrName = "attr";
		Double value = 0d;
		when(entity.getDouble(attrName)).thenReturn(value);
		assertEquals(value, lazyEntity.getDouble(attrName));
		assertEquals(value, lazyEntity.getDouble(attrName));
		verify(dataService, times(1)).findOneById(ENTITY_NAME, id);
	}

	@Test
	public void getEntitiesString()
	{
		String attrName = "attr";
		@SuppressWarnings("unchecked")
		Iterable<Entity> entities = mock(Iterable.class);
		when(entity.getEntities(attrName)).thenReturn(entities);
		assertEquals(entities, lazyEntity.getEntities(attrName));
		assertEquals(entities, lazyEntity.getEntities(attrName));
		verify(dataService, times(1)).findOneById(ENTITY_NAME, id);
	}

	@Test
	public void getEntitiesStringClassE()
	{
		String attrName = "attr";
		@SuppressWarnings("unchecked")
		Iterable<Entity> entities = mock(Iterable.class);
		when(entity.getEntities(attrName, Entity.class)).thenReturn(entities);
		assertEquals(entities, lazyEntity.getEntities(attrName, Entity.class));
		assertEquals(entities, lazyEntity.getEntities(attrName, Entity.class));
		verify(dataService, times(1)).findOneById(ENTITY_NAME, id);
	}

	@Test
	public void getEntityString()
	{
		String attrName = "attr";
		Entity value = mock(Entity.class);
		when(entity.getEntity(attrName)).thenReturn(value);
		assertEquals(value, lazyEntity.getEntity(attrName));
		assertEquals(value, lazyEntity.getEntity(attrName));
		verify(dataService, times(1)).findOneById(ENTITY_NAME, id);
	}

	@Test
	public void getEntityStringClassE()
	{
		String attrName = "attr";
		Entity value = mock(Entity.class);
		when(entity.getEntity(attrName, Entity.class)).thenReturn(value);
		assertEquals(value, lazyEntity.getEntity(attrName, Entity.class));
		assertEquals(value, lazyEntity.getEntity(attrName, Entity.class));
		verify(dataService, times(1)).findOneById(ENTITY_NAME, id);
	}

	@Test
	public void getEntityType()
	{
		assertEquals(entityType, lazyEntity.getEntityType());
	}

	@Test
	public void getIdValue()
	{
		assertEquals(id, lazyEntity.getIdValue());
	}

	@Test
	public void getInt()
	{
		String attrName = "attr";
		Integer value = 0;
		when(entity.getInt(attrName)).thenReturn(value);
		assertEquals(value, lazyEntity.getInt(attrName));
		assertEquals(value, lazyEntity.getInt(attrName));
		verify(dataService, times(1)).findOneById(ENTITY_NAME, id);
	}

	@Test
	public void getIntIdAttr()
	{
		assertEquals(id, lazyEntity.getInt(ID_ATTR_NAME));
	}

	@Test
	public void getLabelValue()
	{
		String value = "label";
		when(entity.getLabelValue()).thenReturn(value);
		assertEquals(value, lazyEntity.getLabelValue());
		assertEquals(value, lazyEntity.getLabelValue());
		verify(dataService, times(1)).findOneById(ENTITY_NAME, id);
	}

	@Test
	public void getLabelValueLabelAttrIsIdAttr()
	{
		when(entityType.getLabelAttribute()).thenReturn(idAttr);
		assertEquals(id.toString(), lazyEntity.getLabelValue().toString());
		verifyNoMoreInteractions(dataService);
	}

	@Test
	public void getLong()
	{
		String attrName = "attr";
		Long value = 0L;
		when(entity.getLong(attrName)).thenReturn(value);
		assertEquals(value, lazyEntity.getLong(attrName));
		assertEquals(value, lazyEntity.getLong(attrName));
		verify(dataService, times(1)).findOneById(ENTITY_NAME, id);
	}

	@Test
	public void getString()
	{
		String attrName = "attr";
		String value = "str";
		when(entity.getString(attrName)).thenReturn(value);
		assertEquals(value, lazyEntity.getString(attrName));
		assertEquals(value, lazyEntity.getString(attrName));
		verify(dataService, times(1)).findOneById(ENTITY_NAME, id);
	}

	@Test
	public void getStringIdAttr()
	{
		String strId = "1";
		when(dataService.findOneById(ENTITY_NAME, strId)).thenReturn(entity);
		lazyEntity = new LazyEntity(entityType, dataService, strId);
		assertEquals(strId, lazyEntity.getString(ID_ATTR_NAME));
	}

	@Test
	public void getLocalDate()
	{
		String attrName = "attr";
		LocalDate value = LocalDate.now();
		when(entity.getLocalDate(attrName)).thenReturn(value);
		assertEquals(value, lazyEntity.getLocalDate(attrName));
		assertEquals(value, lazyEntity.getLocalDate(attrName));
		verify(dataService, times(1)).findOneById(ENTITY_NAME, id);
	}

	@Test
	public void getInstant()
	{
		String attrName = "attr";
		Instant value = Instant.now();
		when(entity.getInstant(attrName)).thenReturn(value);
		assertEquals(value, lazyEntity.getInstant(attrName));
		assertEquals(value, lazyEntity.getInstant(attrName));
		verify(dataService, times(1)).findOneById(ENTITY_NAME, id);
	}

	@Test
	public void setStringObject()
	{
		String attrName = "attr";
		Object value = mock(Object.class);
		lazyEntity.set(attrName, value);
		lazyEntity.set(attrName, value);
		verify(dataService, times(1)).findOneById(ENTITY_NAME, id);
		verify(entity, times(2)).set(attrName, value);
	}

	@Test
	public void setEntity()
	{
		Entity value = mock(Entity.class);
		lazyEntity.set(value);
		lazyEntity.set(value);
		verify(dataService, times(1)).findOneById(ENTITY_NAME, id);
		verify(entity, times(2)).set(value);
	}
}
