package org.molgenis.data.support;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;

public class LazyEntityTest
{
	private static final String ENTITY_NAME = "entity";
	private static final String ID_ATTR_NAME = "id";

	private EntityMetaData entityMeta;
	private AttributeMetaData idAttr;
	private DataService dataService;
	private Object id;
	private LazyEntity lazyEntity;
	private Entity entity;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		entityMeta = mock(EntityMetaData.class);
		when(entityMeta.getName()).thenReturn(ENTITY_NAME);
		idAttr = mock(AttributeMetaData.class);
		when(idAttr.getName()).thenReturn(ID_ATTR_NAME);
		when(idAttr.isIdAtrribute()).thenReturn(true);
		when(entityMeta.getIdAttribute()).thenReturn(idAttr);
		dataService = mock(DataService.class);
		entity = mock(Entity.class);
		when(dataService.findOne(ENTITY_NAME, id)).thenReturn(entity);
		id = Integer.valueOf(1);
		lazyEntity = new LazyEntity(entityMeta, dataService, id);
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
		verify(dataService, times(1)).findOne(ENTITY_NAME, id);
	}

	@Test
	public void getIdAttr()
	{
		assertEquals(id, lazyEntity.get(ID_ATTR_NAME));
	}

	@Test
	public void getAttributeNames()
	{
		DefaultEntity entity = new DefaultEntity(entityMeta, dataService);
		AttributeMetaData attr0 = when(mock(AttributeMetaData.class).getName()).thenReturn("attr0").getMock();
		AttributeMetaData attr1 = when(mock(AttributeMetaData.class).getName()).thenReturn("attr1").getMock();
		when(entityMeta.getAtomicAttributes()).thenReturn(Arrays.asList(attr0, attr1));
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
		verify(dataService, times(1)).findOne(ENTITY_NAME, id);
	}

	@Test
	public void getDate()
	{
		String attrName = "attr";
		Date value = new Date(0);
		when(entity.getDate(attrName)).thenReturn(value);
		assertEquals(value, lazyEntity.getDate(attrName));
		assertEquals(value, lazyEntity.getDate(attrName));
		verify(dataService, times(1)).findOne(ENTITY_NAME, id);
	}

	@Test
	public void getDouble()
	{
		String attrName = "attr";
		Double value = Double.valueOf(0);
		when(entity.getDouble(attrName)).thenReturn(value);
		assertEquals(value, lazyEntity.getDouble(attrName));
		assertEquals(value, lazyEntity.getDouble(attrName));
		verify(dataService, times(1)).findOne(ENTITY_NAME, id);
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
		verify(dataService, times(1)).findOne(ENTITY_NAME, id);
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
		verify(dataService, times(1)).findOne(ENTITY_NAME, id);
	}

	@Test
	public void getEntityString()
	{
		String attrName = "attr";
		Entity value = mock(Entity.class);
		when(entity.getEntity(attrName)).thenReturn(value);
		assertEquals(value, lazyEntity.getEntity(attrName));
		assertEquals(value, lazyEntity.getEntity(attrName));
		verify(dataService, times(1)).findOne(ENTITY_NAME, id);
	}

	@Test
	public void getEntityStringClassE()
	{
		String attrName = "attr";
		Entity value = mock(Entity.class);
		when(entity.getEntity(attrName, Entity.class)).thenReturn(value);
		assertEquals(value, lazyEntity.getEntity(attrName, Entity.class));
		assertEquals(value, lazyEntity.getEntity(attrName, Entity.class));
		verify(dataService, times(1)).findOne(ENTITY_NAME, id);
	}

	@Test
	public void getEntityMetaData()
	{
		assertEquals(entityMeta, lazyEntity.getEntityMetaData());
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
		Integer value = Integer.valueOf(0);
		when(entity.getInt(attrName)).thenReturn(value);
		assertEquals(value, lazyEntity.getInt(attrName));
		assertEquals(value, lazyEntity.getInt(attrName));
		verify(dataService, times(1)).findOne(ENTITY_NAME, id);
	}

	@Test
	public void getIntIdAttr()
	{
		assertEquals(id, lazyEntity.getInt(ID_ATTR_NAME));
	}

	@Test
	public void getIntList()
	{
		String attrName = "attr";
		@SuppressWarnings("unchecked")
		List<Integer> value = mock(List.class);
		when(entity.getIntList(attrName)).thenReturn(value);
		assertEquals(value, lazyEntity.getIntList(attrName));
		assertEquals(value, lazyEntity.getIntList(attrName));
		verify(dataService, times(1)).findOne(ENTITY_NAME, id);
	}

	@Test
	public void getLabelValue()
	{
		String value = "label";
		when(entity.getLabelValue()).thenReturn(value);
		assertEquals(value, lazyEntity.getLabelValue());
		assertEquals(value, lazyEntity.getLabelValue());
		verify(dataService, times(1)).findOne(ENTITY_NAME, id);
	}

	@Test
	public void getLabelValueLabelAttrIsIdAttr()
	{
		when(entityMeta.getLabelAttribute()).thenReturn(idAttr);
		assertEquals(id.toString(), lazyEntity.getLabelValue());
		verifyNoMoreInteractions(dataService);
	}

	@Test
	public void getList()
	{
		String attrName = "attr";
		@SuppressWarnings("unchecked")
		List<String> value = mock(List.class);
		when(entity.getList(attrName)).thenReturn(value);
		assertEquals(value, lazyEntity.getList(attrName));
		assertEquals(value, lazyEntity.getList(attrName));
		verify(dataService, times(1)).findOne(ENTITY_NAME, id);
	}

	@Test
	public void getLong()
	{
		String attrName = "attr";
		Long value = Long.valueOf(0l);
		when(entity.getLong(attrName)).thenReturn(value);
		assertEquals(value, lazyEntity.getLong(attrName));
		assertEquals(value, lazyEntity.getLong(attrName));
		verify(dataService, times(1)).findOne(ENTITY_NAME, id);
	}

	@Test
	public void getString()
	{
		String attrName = "attr";
		String value = "str";
		when(entity.getString(attrName)).thenReturn(value);
		assertEquals(value, lazyEntity.getString(attrName));
		assertEquals(value, lazyEntity.getString(attrName));
		verify(dataService, times(1)).findOne(ENTITY_NAME, id);
	}

	@Test
	public void getStringIdAttr()
	{
		String strId = "1";
		when(dataService.findOne(ENTITY_NAME, strId)).thenReturn(entity);
		lazyEntity = new LazyEntity(entityMeta, dataService, strId);
		assertEquals(strId, lazyEntity.getString(ID_ATTR_NAME));
	}

	@Test
	public void getTimestamp()
	{
		String attrName = "attr";
		Timestamp value = new Timestamp(0);
		when(entity.getTimestamp(attrName)).thenReturn(value);
		assertEquals(value, lazyEntity.getTimestamp(attrName));
		assertEquals(value, lazyEntity.getTimestamp(attrName));
		verify(dataService, times(1)).findOne(ENTITY_NAME, id);
	}

	@Test
	public void getUtilDate()
	{
		String attrName = "attr";
		java.util.Date value = new java.util.Date();
		when(entity.getUtilDate(attrName)).thenReturn(value);
		assertEquals(value, lazyEntity.getUtilDate(attrName));
		assertEquals(value, lazyEntity.getUtilDate(attrName));
		verify(dataService, times(1)).findOne(ENTITY_NAME, id);
	}

	@Test
	public void setStringObject()
	{
		String attrName = "attr";
		Object value = mock(Object.class);
		lazyEntity.set(attrName, value);
		lazyEntity.set(attrName, value);
		verify(dataService, times(1)).findOne(ENTITY_NAME, id);
		verify(entity, times(2)).set(attrName, value);
	}

	@Test
	public void setEntity()
	{
		Entity value = mock(Entity.class);
		lazyEntity.set(value);
		lazyEntity.set(value);
		verify(dataService, times(1)).findOne(ENTITY_NAME, id);
		verify(entity, times(2)).set(value);
	}
}
