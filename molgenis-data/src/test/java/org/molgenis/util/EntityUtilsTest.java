package org.molgenis.util;

import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityManager;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.support.DynamicEntity;
import org.testng.annotations.Test;

import static com.google.common.collect.ImmutableMap.of;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.MolgenisFieldTypes.AttributeType.*;
import static org.testng.Assert.*;

public class EntityUtilsTest
{
	@Test
	public void isEmptyNoAttributes()
	{
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		when(entityMeta.getAtomicAttributes()).thenReturn(emptyList());
		assertTrue(EntityUtils.isEmpty(new DynamicEntity(entityMeta)));
	}

	@Test
	public void isEmptyAttributeValuesNull()
	{
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		Attribute attr = when(mock(Attribute.class).getName()).thenReturn("attr").getMock();
		when(attr.getDataType()).thenReturn(STRING);
		when(entityMeta.getAtomicAttributes()).thenReturn(singletonList(attr));
		when(entityMeta.getAttribute("attr")).thenReturn(attr);
		assertTrue(EntityUtils.isEmpty(new DynamicEntity(entityMeta, singletonMap("attr", null))));
	}

	@Test
	public void isEmptyAttributeValuesNotNull()
	{
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		Attribute attr = when(mock(Attribute.class).getName()).thenReturn("attr").getMock();
		when(attr.getDataType()).thenReturn(STRING);
		when(entityMeta.getAtomicAttributes()).thenReturn(singletonList(attr));
		when(entityMeta.getAttribute("attr")).thenReturn(attr);
		assertFalse(EntityUtils.isEmpty(new DynamicEntity(entityMeta, of("attr", "val"))));
	}

	@Test
	public void doesExtend()
	{
		EntityMetaData grandfather = when(mock(EntityMetaData.class).getName()).thenReturn("grandfather").getMock();
		assertFalse(EntityUtils.doesExtend(grandfather, "grandfather"));

		EntityMetaData father = when(mock(EntityMetaData.class).getName()).thenReturn("father").getMock();
		when(father.getExtends()).thenReturn(grandfather);
		assertTrue(EntityUtils.doesExtend(father, "grandfather"));

		EntityMetaData child = when(mock(EntityMetaData.class).getName()).thenReturn("child").getMock();
		when(child.getExtends()).thenReturn(father);
		assertTrue(EntityUtils.doesExtend(child, "grandfather"));
	}

	@Test
	public void getTypedValueStringAttributeEntityManagerOneToMany()
	{
		String valueStr = "0,1,2";
		Attribute attr = mock(Attribute.class);
		EntityMetaData refEntityMeta = mock(EntityMetaData.class);
		Attribute refIdAttr = mock(Attribute.class);
		when(refIdAttr.getDataType()).thenReturn(INT);
		when(refEntityMeta.getIdAttribute()).thenReturn(refIdAttr);
		when(attr.getRefEntity()).thenReturn(refEntityMeta);
		when(attr.getDataType()).thenReturn(ONE_TO_MANY);
		EntityManager entityManager = mock(EntityManager.class);
		Entity entity0 = mock(Entity.class);
		Entity entity1 = mock(Entity.class);
		Entity entity2 = mock(Entity.class);
		when(entityManager.getReference(refEntityMeta, 0)).thenReturn(entity0);
		when(entityManager.getReference(refEntityMeta, 1)).thenReturn(entity1);
		when(entityManager.getReference(refEntityMeta, 2)).thenReturn(entity2);
		assertEquals(EntityUtils.getTypedValue(valueStr, attr, entityManager), newArrayList(entity0, entity1, entity2));
	}

	@Test
	public void getTypedValueStringAttributeEntityManagerXref()
	{
		String valueStr = "0";
		Attribute attr = mock(Attribute.class);
		EntityMetaData refEntityMeta = mock(EntityMetaData.class);
		Attribute refIdAttr = mock(Attribute.class);
		when(refIdAttr.getDataType()).thenReturn(STRING);
		when(refEntityMeta.getIdAttribute()).thenReturn(refIdAttr);
		when(attr.getRefEntity()).thenReturn(refEntityMeta);
		when(attr.getDataType()).thenReturn(XREF);
		Entity entity = mock(Entity.class);
		EntityManager entityManager = mock(EntityManager.class);
		when(entityManager.getReference(refEntityMeta, valueStr)).thenReturn(entity);
		assertEquals(EntityUtils.getTypedValue(valueStr, attr, entityManager), entity);
	}
}
