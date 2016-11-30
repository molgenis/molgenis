package org.molgenis.util;

import org.molgenis.data.Entity;
import org.molgenis.data.EntityManager;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Tag;
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
import static org.molgenis.data.meta.AttributeType.*;
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

	@Test
	public void attributeEqualsNoIdentifierCheck()
	{
		Attribute attr = getMockAttr("attr");
		Attribute otherAttr = getMockAttr("otherAttr");
		when(attr.getIdentifier()).thenReturn("1");
		when(attr.getIdentifier()).thenReturn("2");
		assertFalse(EntityUtils.equals(attr, otherAttr, true));
	}

	@Test(dataProvider = "attributeProvider")
	public void attributeEquals(Attribute attr, Attribute otherAttr, boolean shouldEqual)
	{
		assertEquals(EntityUtils.equals(attr, otherAttr), shouldEqual);
	}

	@DataProvider(name = "attributeProvider")
	public Iterator<Object[]> attributeProvider()
	{
		List<Object[]> testCases = newArrayList();

		{ // one attr null
			testCases.add(new Object[] { getMockAttr("attr"), null, false });
		}

		{ // both attrs null
			testCases.add(new Object[] { null, null, true });
		}

		{ // children equals
			Attribute attr = getMockAttr("compound");
			Attribute otherAttr = getMockAttr("compound");
			Attribute child1 = getMockAttr();
			Attribute child2 = getMockAttr();
			Attribute child3 = getMockAttr();
			Attribute child4 = getMockAttr();
			when(child1.getIdentifier()).thenReturn("1");
			when(child2.getIdentifier()).thenReturn("2");
			when(child3.getIdentifier()).thenReturn("1");
			when(child4.getIdentifier()).thenReturn("2");
			when(attr.getChildren()).thenReturn(newArrayList(child1, child2));
			when(otherAttr.getChildren()).thenReturn(newArrayList(child3, child4));

			testCases.add(new Object[] { attr, otherAttr, true });
		}

		{ // children different order
			Attribute attr = getMockAttr("compound");
			Attribute otherAttr = getMockAttr("compound");
			Attribute child1 = getMockAttr();
			Attribute child2 = getMockAttr();
			Attribute child3 = getMockAttr();
			Attribute child4 = getMockAttr();
			when(child1.getIdentifier()).thenReturn("1");
			when(child2.getIdentifier()).thenReturn("2");
			when(child3.getIdentifier()).thenReturn("2");
			when(child4.getIdentifier()).thenReturn("1");
			when(attr.getChildren()).thenReturn(newArrayList(child1, child2));
			when(otherAttr.getChildren()).thenReturn(newArrayList(child3, child4));

			testCases.add(new Object[] { attr, otherAttr, false });
		}

		{ // children missing child
			Attribute attr = getMockAttr("compound");
			Attribute otherAttr = getMockAttr("compound");
			Attribute child1 = getMockAttr();
			Attribute child2 = getMockAttr();
			Attribute child3 = getMockAttr();
			when(child1.getIdentifier()).thenReturn("1");
			when(child2.getIdentifier()).thenReturn("2");
			when(child3.getIdentifier()).thenReturn("1");
			when(attr.getChildren()).thenReturn(newArrayList(child1, child2));
			when(otherAttr.getChildren()).thenReturn(newArrayList(child3));

			testCases.add(new Object[] { attr, otherAttr, false });
		}

		{ // refEntity one null
			Attribute attr = getMockAttr("refEntity");
			Attribute otherAttr = getMockAttr("refEntityNull");
			EntityType refEntity = mock(EntityType.class);
			when(attr.getRefEntity()).thenReturn(refEntity);

			testCases.add(new Object[] { attr, otherAttr, false });
		}

		{ // refEntity name equals
			Attribute attr = getMockAttr("refEntity1");
			Attribute otherAttr = getMockAttr("refEntity1");
			EntityType refEntity = mock(EntityType.class);
			EntityType otherRefEntity = mock(EntityType.class);
			when(refEntity.getName()).thenReturn("ref1");
			when(otherRefEntity.getName()).thenReturn("ref1");
			when(attr.getRefEntity()).thenReturn(refEntity);
			when(otherAttr.getRefEntity()).thenReturn(otherRefEntity);

			testCases.add(new Object[] { attr, otherAttr, true });
		}

		{ // refEntity name not equals
			Attribute attr = getMockAttr("refEntity1");
			Attribute otherAttr = getMockAttr("refEntity2");
			EntityType refEntity = mock(EntityType.class);
			EntityType otherRefEntity = mock(EntityType.class);
			when(refEntity.getName()).thenReturn("ref1");
			when(otherRefEntity.getName()).thenReturn("ref2");
			when(attr.getRefEntity()).thenReturn(refEntity);
			when(otherAttr.getRefEntity()).thenReturn(otherRefEntity);

			testCases.add(new Object[] { attr, otherAttr, false });
		}

		{ // tags different size
			Attribute attr = getMockAttr("1tag");
			Attribute otherAttr = getMockAttr("2tags");
			Tag tag = mock(Tag.class);
			when(attr.getTags()).thenReturn(newArrayList(tag));
			when(attr.getTags()).thenReturn(newArrayList(tag, tag));

			testCases.add(new Object[] { attr, otherAttr, false });
		}

		{ // tags different order
			Attribute attr = getMockAttr("tagA_tagB");
			Attribute otherAttr = getMockAttr("tagB_tagA");
			Tag tagA = mock(Tag.class);
			Tag tagB = mock(Tag.class);
			when(tagA.getId()).thenReturn("A");
			when(tagB.getId()).thenReturn("B");
			when(attr.getTags()).thenReturn(newArrayList(tagA, tagB));
			when(otherAttr.getTags()).thenReturn(newArrayList(tagB, tagA));

			testCases.add(new Object[] { attr, otherAttr, false });
		}

		{ // tags equals
			Attribute attr = getMockAttr("tagA_tagB");
			Attribute otherAttr = getMockAttr("tagA_tagB");
			Tag tagA = mock(Tag.class);
			Tag tagB = mock(Tag.class);
			when(tagA.getId()).thenReturn("A");
			when(tagB.getId()).thenReturn("B");
			when(attr.getTags()).thenReturn(newArrayList(tagA, tagB));
			when(otherAttr.getTags()).thenReturn(newArrayList(tagA, tagB));

			testCases.add(new Object[] { attr, otherAttr, true });
		}

		{ // name not equals
			Attribute attr = getMockAttr("nameA");
			Attribute otherAttr = getMockAttr("nameB");
			when(attr.getName()).thenReturn("A");
			when(otherAttr.getName()).thenReturn("B");

			testCases.add(new Object[] { attr, otherAttr, false });
		}

		{ // label not equals
			Attribute attr = getMockAttr("labelA");
			Attribute otherAttr = getMockAttr("labelB");
			when(attr.getLabel()).thenReturn("A");
			when(otherAttr.getLabel()).thenReturn("B");

			testCases.add(new Object[] { attr, otherAttr, false });
		}

		{ // description not equals
			Attribute attr = getMockAttr("descriptionA");
			Attribute otherAttr = getMockAttr("descriptionB");
			when(attr.getDescription()).thenReturn("A");
			when(otherAttr.getDescription()).thenReturn("B");

			testCases.add(new Object[] { attr, otherAttr, false });
		}

		{ // data type not equals
			Attribute attr = getMockAttr("typeString");
			Attribute otherAttr = getMockAttr("typeInt");
			when(attr.getDataType()).thenReturn(STRING);
			when(otherAttr.getDataType()).thenReturn(INT);

			testCases.add(new Object[] { attr, otherAttr, false });
		}

		{ // expression not equals
			Attribute attr = getMockAttr("expressionA");
			Attribute otherAttr = getMockAttr("expressionB");
			when(attr.getExpression()).thenReturn("A");
			when(otherAttr.getExpression()).thenReturn("B");

			testCases.add(new Object[] { attr, otherAttr, false });
		}

		{ // isNillable not equals
			Attribute attr = getMockAttr("isNillableTrue");
			Attribute otherAttr = getMockAttr("isNillableFalse");
			when(attr.isNillable()).thenReturn(true);
			when(otherAttr.isNillable()).thenReturn(false);

			testCases.add(new Object[] { attr, otherAttr, false });
		}

		{ // isAuto not equals
			Attribute attr = getMockAttr("isAutoTrue");
			Attribute otherAttr = getMockAttr("isAutoFalse");
			when(attr.isAuto()).thenReturn(true);
			when(otherAttr.isAuto()).thenReturn(false);

			testCases.add(new Object[] { attr, otherAttr, false });
		}

		{ // isVisible not equals
			Attribute attr = getMockAttr("isVisibleTrue");
			Attribute otherAttr = getMockAttr("isVisibleFalse");
			when(attr.isVisible()).thenReturn(true);
			when(otherAttr.isVisible()).thenReturn(false);

			testCases.add(new Object[] { attr, otherAttr, false });
		}

		{ // isAggregatable not equals
			Attribute attr = getMockAttr("isAggregatableTrue");
			Attribute otherAttr = getMockAttr("isAggregatableFalse");
			when(attr.isAggregatable()).thenReturn(true);
			when(otherAttr.isAggregatable()).thenReturn(false);

			testCases.add(new Object[] { attr, otherAttr, false });
		}

		{ // enumOptions not equals
			Attribute attr = getMockAttr("enumOptionsAB");
			Attribute otherAttr = getMockAttr("enumOptionsBC");
			when(attr.getEnumOptions()).thenReturn(newArrayList("A", "B"));
			when(attr.getEnumOptions()).thenReturn(newArrayList("B", "C"));

			testCases.add(new Object[] { attr, otherAttr, false });
		}

		{ // rangeMin not equals
			Attribute attr = getMockAttr("rangeMin3");
			Attribute otherAttr = getMockAttr("rangeMin5");
			when(attr.getRangeMin()).thenReturn(3L);
			when(attr.getRangeMin()).thenReturn(5L);

			testCases.add(new Object[] { attr, otherAttr, false });
		}

		{ // rangeMin not equals
			Attribute attr = getMockAttr("rangeMax3");
			Attribute otherAttr = getMockAttr("rangeMax5");
			when(attr.getRangeMax()).thenReturn(3L);
			when(attr.getRangeMax()).thenReturn(5L);

			testCases.add(new Object[] { attr, otherAttr, false });
		}

		{ // isReadOnly not equals
			Attribute attr = getMockAttr("isReadOnlyTrue");
			Attribute otherAttr = getMockAttr("isReadOnlyFalse");
			when(attr.isReadOnly()).thenReturn(true);
			when(otherAttr.isReadOnly()).thenReturn(false);

			testCases.add(new Object[] { attr, otherAttr, false });
		}

		{ // isUnique not equals
			Attribute attr = getMockAttr("isUniqueTrue");
			Attribute otherAttr = getMockAttr("isUniqueFalse");
			when(attr.isUnique()).thenReturn(true);
			when(otherAttr.isUnique()).thenReturn(false);

			testCases.add(new Object[] { attr, otherAttr, false });
		}

		{ // visibleExpression not equals
			Attribute attr = getMockAttr("visibleExpressionA");
			Attribute otherAttr = getMockAttr("visibleExpressionB");
			when(attr.getVisibleExpression()).thenReturn("A");
			when(attr.getVisibleExpression()).thenReturn("B");

			testCases.add(new Object[] { attr, otherAttr, false });
		}

		{ // validationExpression not equals
			Attribute attr = getMockAttr("validationExpressionA");
			Attribute otherAttr = getMockAttr("validationExpressionB");
			when(attr.getValidationExpression()).thenReturn("A");
			when(attr.getValidationExpression()).thenReturn("B");

			testCases.add(new Object[] { attr, otherAttr, false });
		}

		{ // defaultValue not equals
			Attribute attr = getMockAttr("defaultValueA");
			Attribute otherAttr = getMockAttr("defaultValueB");
			when(attr.getValidationExpression()).thenReturn("A");
			when(attr.getValidationExpression()).thenReturn("B");

			testCases.add(new Object[] { attr, otherAttr, false });
		}

		{ // isIdAttribute equals
			Attribute attr = getMockAttr("isIdAttrTrue");
			Attribute otherAttr = getMockAttr("isIdAttrTrue");
			when(attr.isIdAttribute()).thenReturn(true);
			when(otherAttr.isIdAttribute()).thenReturn(true);

			testCases.add(new Object[] { attr, otherAttr, true });
		}

		{ // isIdAttribute not equals
			Attribute attr = getMockAttr("isIdAttrTrue");
			Attribute otherAttr = getMockAttr("isIdAttrFalse");
			when(attr.isIdAttribute()).thenReturn(true);
			when(otherAttr.isIdAttribute()).thenReturn(false);

			testCases.add(new Object[] { attr, otherAttr, false });
		}

		{ // getLookupAttributeIndex equals
			Attribute attr = getMockAttr("lookupIndex10");
			Attribute otherAttr = getMockAttr("lookupIndex10");
			when(attr.getLookupAttributeIndex()).thenReturn(10);
			when(otherAttr.getLookupAttributeIndex()).thenReturn(10);

			testCases.add(new Object[] { attr, otherAttr, true });
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
		Attribute attr = getMockAttr();
		when(attr.toString()).thenReturn(toString);
		return attr;
	}

	private Attribute getMockAttr()
	{
		Attribute attr = mock(Attribute.class);
		when(attr.getChildren()).thenReturn(emptyList());
		when(attr.getTags()).thenReturn(emptyList());
		return attr;
	}
}
