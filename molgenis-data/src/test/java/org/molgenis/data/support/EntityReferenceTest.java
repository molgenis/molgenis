package org.molgenis.data.support;

import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

public class EntityReferenceTest
{
	private static final String ID_ATTR_NAME = "idAttr";
	private static final String LABEL_ATTR_NAME = "labelAttr";

	private EntityType entityType;
	private Attribute idAttribute;
	private EntityReference entityReference;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		entityType = mock(EntityType.class);
		when(entityType.getAttributeNames()).thenReturn(asList(ID_ATTR_NAME, LABEL_ATTR_NAME));

		idAttribute = when(mock(Attribute.class).getName()).thenReturn(ID_ATTR_NAME).getMock();
		when(idAttribute.getDataType()).thenReturn(AttributeType.STRING);
		Attribute labelAttribute = when(mock(Attribute.class).getName()).thenReturn(LABEL_ATTR_NAME).getMock();
		when(labelAttribute.getDataType()).thenReturn(AttributeType.STRING);
		when(entityType.getIdAttribute()).thenReturn(idAttribute);
		when(entityType.getLabelAttribute()).thenReturn(labelAttribute);
		when(entityType.getAtomicAttributes()).thenReturn(asList(idAttribute, labelAttribute));
		entityReference = new EntityReference(entityType, "entityId");
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void testEntityReference()
	{
		new EntityReference(null, null);
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void testEntityReferenceInvalidIdTrue()
	{
		new EntityReference(entityType, true);
	}

	@Test
	public void testGetEntityType()
	{
		assertEquals(entityReference.getEntityType(), entityType);
	}

	@Test
	public void testGetAttributeNames()
	{
		assertEquals(newArrayList(entityReference.getAttributeNames()), asList(ID_ATTR_NAME, LABEL_ATTR_NAME));
	}

	@Test
	public void testGetIdValue()
	{
		assertEquals(entityReference.getIdValue(), "entityId");
	}

	@Test
	public void testSetIdValue()
	{
		entityReference.setIdValue("newEntityId");
		assertEquals(entityReference.getIdValue(), "newEntityId");
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void testSetIdValueStringWrongType()
	{
		entityReference.setIdValue(123);
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void testSetIdValueIntegerWrongType()
	{
		when(idAttribute.getDataType()).thenReturn(AttributeType.INT);
		entityReference.setIdValue(34359738368L);
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void testSetIdValueLongWrongType()
	{
		when(idAttribute.getDataType()).thenReturn(AttributeType.LONG);
		entityReference.setIdValue("123");
	}

	@Test(expectedExceptions = UnsupportedOperationException.class)
	public void testGetLabelValue()
	{
		Attribute labelAttribute = when(mock(Attribute.class).getName()).thenReturn(LABEL_ATTR_NAME).getMock();
		when(entityType.getLabelAttribute()).thenReturn(labelAttribute);
		entityReference.getLabelValue();
	}

	@Test
	public void testGetLabelValueLabelAttributeIsIdAttribute()
	{
		when(entityType.getLabelAttribute()).thenReturn(idAttribute);
		assertEquals(entityReference.getLabelValue(), "entityId");
	}

	@Test(expectedExceptions = UnsupportedOperationException.class)
	public void testGet()
	{
		entityReference.get(LABEL_ATTR_NAME);
	}

	@Test
	public void testGetIdAttribute()
	{
		assertEquals(entityReference.get(ID_ATTR_NAME), "entityId");
	}

	@Test(expectedExceptions = UnsupportedOperationException.class)
	public void testGetString()
	{
		entityReference.getString(LABEL_ATTR_NAME);
	}

	@Test
	public void testGetStringIdAttribute()
	{
		assertEquals(entityReference.get(ID_ATTR_NAME), "entityId");
	}

	@Test(expectedExceptions = UnsupportedOperationException.class)
	public void testGetInt()
	{
		entityReference.getInt("someAttr");
	}

	@Test
	public void testGetIntIdAttribute()
	{
		when(idAttribute.getDataType()).thenReturn(AttributeType.INT);
		EntityReference entityReference = new EntityReference(entityType, 123);
		assertEquals(entityReference.getInt(ID_ATTR_NAME), Integer.valueOf(123));
	}

	@Test(expectedExceptions = UnsupportedOperationException.class)
	public void testGetLong()
	{
		entityReference.getLong("someAttr");
	}

	@Test
	public void testGetLongIdAttribute()
	{
		when(idAttribute.getDataType()).thenReturn(AttributeType.LONG);
		EntityReference entityReference = new EntityReference(entityType, 123L);
		assertEquals(entityReference.getLong(ID_ATTR_NAME), Long.valueOf(123L));
	}

	@Test(expectedExceptions = UnsupportedOperationException.class)
	public void testGetBoolean()
	{
		entityReference.getBoolean("someAttr");
	}

	@Test(expectedExceptions = UnsupportedOperationException.class)
	public void testGetDouble()
	{
		entityReference.getDouble("someAttr");
	}

	@Test(expectedExceptions = UnsupportedOperationException.class)
	public void testGetInstant()
	{
		entityReference.getInstant("someAttr");
	}

	@Test(expectedExceptions = UnsupportedOperationException.class)
	public void testGetLocalDate()
	{
		entityReference.getLocalDate("someAttr");
	}

	@Test(expectedExceptions = UnsupportedOperationException.class)
	public void testGetEntity()
	{
		entityReference.getEntity("someAttr");
	}

	@Test(expectedExceptions = UnsupportedOperationException.class)
	public void testGetEntityClass()
	{
		entityReference.getEntity("someAttr", Entity.class);
	}

	@Test(expectedExceptions = UnsupportedOperationException.class)
	public void testGetEntities()
	{
		entityReference.getEntities("someAttr");
	}

	@Test(expectedExceptions = UnsupportedOperationException.class)
	public void testGetEntitiesClass()
	{
		entityReference.getEntities("someAttr", Entity.class);
	}

	@Test(expectedExceptions = UnsupportedOperationException.class)
	public void testSet()
	{
		entityReference.set("someAttr", "value");
	}

	@Test
	public void testSetIdAttribute()
	{
		entityReference.set(ID_ATTR_NAME, "newEntityId");
		assertEquals(entityReference.getIdValue(), "newEntityId");
	}

	@Test(expectedExceptions = UnsupportedOperationException.class)
	public void testSetEntity()
	{
		Entity entity = mock(Entity.class);
		when(entity.get(ID_ATTR_NAME)).thenReturn("newEntityId");
		when(entity.getAttributeNames()).thenReturn(asList(ID_ATTR_NAME, LABEL_ATTR_NAME)).getMock();
		entityReference.set(entity);
	}

	@Test
	public void testSetEntityIdAttribute()
	{
		Entity entity = mock(Entity.class);
		when(entity.getAttributeNames()).thenReturn(singletonList(ID_ATTR_NAME)).getMock();
		when(entity.get(ID_ATTR_NAME)).thenReturn("newEntityId");
		entityReference.set(entity);
		assertEquals(entityReference.getIdValue(), "newEntityId");
	}
}