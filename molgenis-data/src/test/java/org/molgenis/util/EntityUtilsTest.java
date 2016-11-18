package org.molgenis.util;

import org.molgenis.data.Entity;
import org.molgenis.data.EntityManager;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.DynamicEntity;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Iterator;
import java.util.List;

import static com.google.common.collect.ImmutableMap.of;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.AttributeType.*;
import static org.testng.Assert.*;

public class EntityUtilsTest
{
	@Test
	public void isEmptyNoAttributes()
	{
		EntityType entityType = when(mock(EntityType.class).getName()).thenReturn("entity").getMock();
		when(entityType.getAtomicAttributes()).thenReturn(emptyList());
		assertTrue(EntityUtils.isEmpty(new DynamicEntity(entityType)));
	}

	@Test
	public void isEmptyAttributeValuesNull()
	{
		EntityType entityType = when(mock(EntityType.class).getName()).thenReturn("entity").getMock();
		Attribute attr = when(mock(Attribute.class).getName()).thenReturn("attr").getMock();
		when(attr.getDataType()).thenReturn(STRING);
		when(entityType.getAtomicAttributes()).thenReturn(singletonList(attr));
		when(entityType.getAttribute("attr")).thenReturn(attr);
		assertTrue(EntityUtils.isEmpty(new DynamicEntity(entityType, singletonMap("attr", null))));
	}

	@Test
	public void isEmptyAttributeValuesNotNull()
	{
		EntityType entityType = when(mock(EntityType.class).getName()).thenReturn("entity").getMock();
		Attribute attr = when(mock(Attribute.class).getName()).thenReturn("attr").getMock();
		when(attr.getDataType()).thenReturn(STRING);
		when(entityType.getAtomicAttributes()).thenReturn(singletonList(attr));
		when(entityType.getAttribute("attr")).thenReturn(attr);
		assertFalse(EntityUtils.isEmpty(new DynamicEntity(entityType, of("attr", "val"))));
	}

	@Test
	public void doesExtend()
	{
		EntityType grandfather = when(mock(EntityType.class).getName()).thenReturn("grandfather").getMock();
		assertFalse(EntityUtils.doesExtend(grandfather, "grandfather"));

		EntityType father = when(mock(EntityType.class).getName()).thenReturn("father").getMock();
		when(father.getExtends()).thenReturn(grandfather);
		assertTrue(EntityUtils.doesExtend(father, "grandfather"));

		EntityType child = when(mock(EntityType.class).getName()).thenReturn("child").getMock();
		when(child.getExtends()).thenReturn(father);
		assertTrue(EntityUtils.doesExtend(child, "grandfather"));
	}

	@Test
	public void getTypedValueStringAttributeEntityManagerOneToMany()
	{
		String valueStr = "0,1,2";
		Attribute attr = mock(Attribute.class);
		EntityType refEntityType = mock(EntityType.class);
		Attribute refIdAttr = mock(Attribute.class);
		when(refIdAttr.getDataType()).thenReturn(INT);
		when(refEntityType.getIdAttribute()).thenReturn(refIdAttr);
		when(attr.getRefEntity()).thenReturn(refEntityType);
		when(attr.getDataType()).thenReturn(ONE_TO_MANY);
		EntityManager entityManager = mock(EntityManager.class);
		Entity entity0 = mock(Entity.class);
		Entity entity1 = mock(Entity.class);
		Entity entity2 = mock(Entity.class);
		when(entityManager.getReference(refEntityType, 0)).thenReturn(entity0);
		when(entityManager.getReference(refEntityType, 1)).thenReturn(entity1);
		when(entityManager.getReference(refEntityType, 2)).thenReturn(entity2);
		assertEquals(EntityUtils.getTypedValue(valueStr, attr, entityManager), newArrayList(entity0, entity1, entity2));
	}

	@Test
	public void getTypedValueStringAttributeEntityManagerXref()
	{
		String valueStr = "0";
		Attribute attr = mock(Attribute.class);
		EntityType refEntityType = mock(EntityType.class);
		Attribute refIdAttr = mock(Attribute.class);
		when(refIdAttr.getDataType()).thenReturn(STRING);
		when(refEntityType.getIdAttribute()).thenReturn(refIdAttr);
		when(attr.getRefEntity()).thenReturn(refEntityType);
		when(attr.getDataType()).thenReturn(XREF);
		Entity entity = mock(Entity.class);
		EntityManager entityManager = mock(EntityManager.class);
		when(entityManager.getReference(refEntityType, valueStr)).thenReturn(entity);
		assertEquals(EntityUtils.getTypedValue(valueStr, attr, entityManager), entity);
	}

	@Test(dataProvider = "attributeProvider")
	public void attributeEquals(Attribute attr, Attribute otherAttr, boolean shouldEqual)
	{
		assertEquals(EntityUtils.equals(attr, otherAttr, true), shouldEqual);
	}

	@DataProvider(name = "attributeProvider")
	public Iterator<Object[]> attributeProvider()
	{
		List<Object[]> testCases = newArrayList();

		{ // isIdAttribute equals
			Attribute attr = getMockAttr("isIdAttrTrue");
			Attribute otherAttr = getMockAttr("isIdAttrTrue");
			when(attr.isIdAttribute()).thenReturn(true);
			when(otherAttr.isIdAttribute()).thenReturn(true);

			testCases.add(new Object[]{attr, otherAttr, true});
		}

		{ // isIdAttribute not equals
			Attribute attr = getMockAttr("isIdAttrTrue");
			Attribute otherAttr = getMockAttr("isIdAttrFalse");
			when(attr.isIdAttribute()).thenReturn(true);
			when(otherAttr.isIdAttribute()).thenReturn(false);

			testCases.add(new Object[]{attr, otherAttr, false});
		}

		{ // getLookupAttributeIndex equals
			Attribute attr = getMockAttr("lookupIndex10");
			Attribute otherAttr = getMockAttr("lookupIndex10");
			when(attr.getLookupAttributeIndex()).thenReturn(10);
			when(otherAttr.getLookupAttributeIndex()).thenReturn(10);

			testCases.add(new Object[]{attr, otherAttr, true});
		}

		{ // getLookupAttributeIndex not equals
			Attribute attr = getMockAttr("lookupIndex10");
			Attribute otherAttr = getMockAttr("lookupIndex34");
			when(attr.getLookupAttributeIndex()).thenReturn(10);
			when(otherAttr.getLookupAttributeIndex()).thenReturn(34);

			testCases.add(new Object[] { attr, otherAttr, false });
		}

		{ // getLookupAttributeIndex not equals
			Attribute attr = getMockAttr("lookupIndex10");
			Attribute otherAttr = getMockAttr("lookupIndexNull");
			when(attr.getLookupAttributeIndex()).thenReturn(10);
			when(otherAttr.getLookupAttributeIndex()).thenReturn(null);

			testCases.add(new Object[] { attr, otherAttr, false });
		}

		{ // isLabelAttribute equals
			Attribute attr = getMockAttr("isLabelAttrTrue");
			Attribute otherAttr = getMockAttr("isLabelAttrTrue");
			when(attr.isLabelAttribute()).thenReturn(true);
			when(otherAttr.isLabelAttribute()).thenReturn(true);

			testCases.add(new Object[] { attr, otherAttr, true });
		}

		{ // isLabelAttribute not equals
			Attribute attr = getMockAttr("isLabelAttrTrue");
			Attribute otherAttr = getMockAttr("isLabelAttrFalse");
			when(attr.isLabelAttribute()).thenReturn(true);
			when(otherAttr.isLabelAttribute()).thenReturn(false);

			testCases.add(new Object[] { attr, otherAttr, false });
		}

		return testCases.iterator();
	}

	private Attribute getMockAttr(String toString)
	{
		Attribute attr = mock(Attribute.class);
		when(attr.toString()).thenReturn(toString);
		when(attr.getIdentifier()).thenReturn("1");
		when(attr.getName()).thenReturn("name");
		when(attr.getChildren()).thenReturn(emptyList());
		when(attr.getTags()).thenReturn(emptyList());
		return attr;
	}
}
