package org.molgenis.data.util;

import org.molgenis.data.Entity;
import org.molgenis.data.EntityManager;
import org.molgenis.data.Sort;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.model.Tag;
import org.molgenis.data.support.DynamicEntity;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumSet;
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
	@DataProvider(name = "testEqualsEntityTypeProvider")
	public static Iterator<Object[]> testEqualsEntityTypeProvider()
	{
		EntityType entityType = createEqualsEntityType();

		List<Object[]> dataList = new ArrayList<>();
		dataList.add(new Object[] { entityType, entityType, true });

		EntityType otherIdEntityType = createEqualsEntityType();
		when(otherIdEntityType.getId()).thenReturn("otherId");
		dataList.add(new Object[] { entityType, otherIdEntityType, false });

		EntityType otherLabelEntityType = createEqualsEntityType();
		when(otherLabelEntityType.getLabel()).thenReturn("otherLabel");
		dataList.add(new Object[] { entityType, otherLabelEntityType, false });

		EntityType otherAbstractEntityType = createEqualsEntityType();
		when(otherAbstractEntityType.isAbstract()).thenReturn(false);
		dataList.add(new Object[] { entityType, otherAbstractEntityType, false });

		EntityType otherBackendEntityType = createEqualsEntityType();
		when(otherBackendEntityType.getBackend()).thenReturn("otherBackend");
		dataList.add(new Object[] { entityType, otherBackendEntityType, false });

		EntityType otherPackageEntityType = createEqualsEntityType();
		when(otherPackageEntityType.getPackage()).thenReturn(mock(Package.class));
		dataList.add(new Object[] { entityType, otherPackageEntityType, false });

		EntityType otherDescriptionEntityType = createEqualsEntityType();
		when(otherDescriptionEntityType.getDescription()).thenReturn("otherDescription");
		dataList.add(new Object[] { entityType, otherDescriptionEntityType, false });

		EntityType otherAttributesEntityType = createEqualsEntityType();
		when(otherAttributesEntityType.getOwnAllAttributes()).thenReturn(singletonList(mock(Attribute.class)));
		dataList.add(new Object[] { entityType, otherAttributesEntityType, false });

		EntityType otherTagsEntityType = createEqualsEntityType();
		when(otherTagsEntityType.getTags()).thenReturn(singletonList(mock(Tag.class)));
		dataList.add(new Object[] { entityType, otherTagsEntityType, false });

		EntityType otherExtendsEntityType = createEqualsEntityType();
		when(otherExtendsEntityType.getExtends()).thenReturn(mock(EntityType.class));
		dataList.add(new Object[] { entityType, otherExtendsEntityType, false });

		EntityType otherIndexingDepthEntityType = createEqualsEntityType();
		when(otherIndexingDepthEntityType.getIndexingDepth()).thenReturn(3);
		dataList.add(new Object[] { entityType, otherExtendsEntityType, false });

		return dataList.iterator();
	}

	private static EntityType createEqualsEntityType()
	{
		EntityType entityType = mock(EntityType.class);
		when(entityType.toString()).thenReturn("entity");
		when(entityType.getId()).thenReturn("id");
		when(entityType.getLabel()).thenReturn("label");
		when(entityType.isAbstract()).thenReturn(true);
		when(entityType.getBackend()).thenReturn("backend");
		when(entityType.getPackage()).thenReturn(null);
		when(entityType.getDescription()).thenReturn(null);
		when(entityType.getOwnAllAttributes()).thenReturn(emptyList());
		when(entityType.getOwnLookupAttributes()).thenReturn(emptyList());
		when(entityType.getTags()).thenReturn(emptyList());
		when(entityType.getExtends()).thenReturn(null);
		when(entityType.getIndexingDepth()).thenReturn(1);
		return entityType;
	}

	@Test(dataProvider = "testEqualsEntityTypeProvider")
	public void testEqualsEntityType(EntityType entityType, EntityType otherEntityType, boolean equals) throws Exception
	{
		assertEquals(EntityUtils.equals(entityType, otherEntityType), equals);
	}

	@Test
	public void isEmptyNoAttributes()
	{
		EntityType entityType = when(mock(EntityType.class).getId()).thenReturn("entity").getMock();
		when(entityType.getAtomicAttributes()).thenReturn(emptyList());
		assertTrue(EntityUtils.isEmpty(new DynamicEntity(entityType)));
	}

	@Test
	public void isEmptyAttributeValuesNull()
	{
		EntityType entityType = when(mock(EntityType.class).getId()).thenReturn("entity").getMock();
		Attribute attr = when(mock(Attribute.class).getName()).thenReturn("attr").getMock();
		when(attr.getDataType()).thenReturn(STRING);
		when(entityType.getAtomicAttributes()).thenReturn(singletonList(attr));
		when(entityType.getAttribute("attr")).thenReturn(attr);
		assertTrue(EntityUtils.isEmpty(new DynamicEntity(entityType, singletonMap("attr", null))));
	}

	@Test
	public void isEmptyAttributeValuesNotNull()
	{
		EntityType entityType = when(mock(EntityType.class).getId()).thenReturn("entity").getMock();
		Attribute attr = when(mock(Attribute.class).getName()).thenReturn("attr").getMock();
		when(attr.getDataType()).thenReturn(STRING);
		when(entityType.getAtomicAttributes()).thenReturn(singletonList(attr));
		when(entityType.getAttribute("attr")).thenReturn(attr);
		assertFalse(EntityUtils.isEmpty(new DynamicEntity(entityType, of("attr", "val"))));
	}

	@Test
	public void doesExtend()
	{
		EntityType grandfather = when(mock(EntityType.class).getId()).thenReturn("grandfather").getMock();
		assertFalse(EntityUtils.doesExtend(grandfather, "grandfather"));

		EntityType father = when(mock(EntityType.class).getId()).thenReturn("father").getMock();
		when(father.getExtends()).thenReturn(grandfather);
		assertTrue(EntityUtils.doesExtend(father, "grandfather"));

		EntityType child = when(mock(EntityType.class).getId()).thenReturn("child").getMock();
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

	@Test(dataProvider = "testEqualsAttributeProvider")
	public void testEqualsAttribute(Attribute attr, Attribute otherAttr, boolean shouldEqual)
	{
		assertEquals(EntityUtils.equals(attr, otherAttr), shouldEqual);
	}

	@DataProvider(name = "testEqualsAttributeProvider")
	public Iterator<Object[]> testEqualsAttributeProvider()
	{
		List<Object[]> testCases = newArrayList();

		{ // one attr null
			testCases.add(new Object[] { getMockAttr("attr"), null, false });
		}

		{ // both attrs null
			testCases.add(new Object[] { null, null, true });
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
			when(refEntity.getId()).thenReturn("ref1");
			when(otherRefEntity.getId()).thenReturn("ref1");
			when(attr.getRefEntity()).thenReturn(refEntity);
			when(otherAttr.getRefEntity()).thenReturn(otherRefEntity);

			testCases.add(new Object[] { attr, otherAttr, true });
		}

		{ // refEntity name not equals
			Attribute attr = getMockAttr("refEntity1");
			Attribute otherAttr = getMockAttr("refEntity2");
			EntityType refEntity = mock(EntityType.class);
			EntityType otherRefEntity = mock(EntityType.class);
			when(refEntity.getId()).thenReturn("ref1");
			when(otherRefEntity.getId()).thenReturn("ref2");
			when(attr.getRefEntity()).thenReturn(refEntity);
			when(otherAttr.getRefEntity()).thenReturn(otherRefEntity);

			testCases.add(new Object[] { attr, otherAttr, false });
		}

		{ // tags different size
			Attribute attr = getMockAttr("1tag");
			Attribute otherAttr = getMockAttr("2tags");
			Tag tag = mock(Tag.class);
			when(attr.getTags()).thenReturn(newArrayList(tag));
			when(otherAttr.getTags()).thenReturn(newArrayList(tag, tag));

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
			when(otherAttr.getEnumOptions()).thenReturn(newArrayList("B", "C"));

			testCases.add(new Object[] { attr, otherAttr, false });
		}

		{ // rangeMin not equals
			Attribute attr = getMockAttr("rangeMin3");
			Attribute otherAttr = getMockAttr("rangeMin5");
			when(attr.getRangeMin()).thenReturn(3L);
			when(otherAttr.getRangeMin()).thenReturn(5L);

			testCases.add(new Object[] { attr, otherAttr, false });
		}

		{ // rangeMin not equals
			Attribute attr = getMockAttr("rangeMax3");
			Attribute otherAttr = getMockAttr("rangeMax5");
			when(attr.getRangeMax()).thenReturn(3L);
			when(otherAttr.getRangeMax()).thenReturn(5L);

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
			when(otherAttr.getVisibleExpression()).thenReturn("B");

			testCases.add(new Object[] { attr, otherAttr, false });
		}

		{ // validationExpression not equals
			Attribute attr = getMockAttr("validationExpressionA");
			Attribute otherAttr = getMockAttr("validationExpressionB");
			when(attr.getValidationExpression()).thenReturn("A");
			when(otherAttr.getValidationExpression()).thenReturn("B");

			testCases.add(new Object[] { attr, otherAttr, false });
		}

		{ // defaultValue not equals
			Attribute attr = getMockAttr("defaultValueA");
			Attribute otherAttr = getMockAttr("defaultValueB");
			when(attr.getValidationExpression()).thenReturn("A");
			when(otherAttr.getValidationExpression()).thenReturn("B");

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

		{ // sequence number equals
			Attribute attr = getMockAttr("sequenceNumber0");
			Attribute otherAttr = getMockAttr("sequenceNumber0");
			when(attr.getSequenceNumber()).thenReturn(0);
			when(otherAttr.getSequenceNumber()).thenReturn(0);

			testCases.add(new Object[] { attr, otherAttr, true });
		}

		{ // sequence number not equals
			Attribute attr = getMockAttr("sequenceNumber0");
			Attribute otherAttr = getMockAttr("sequenceNumber1");
			when(attr.getSequenceNumber()).thenReturn(0);
			when(otherAttr.getSequenceNumber()).thenReturn(1);

			testCases.add(new Object[] { attr, otherAttr, false });
		}

		{ // order by equals
			Attribute attr = getMockAttr("orderBySort");
			Attribute otherAttr = getMockAttr("orderBySort");
			Sort sort = mock(Sort.class);
			when(attr.getOrderBy()).thenReturn(sort);
			when(otherAttr.getOrderBy()).thenReturn(sort);

			testCases.add(new Object[] { attr, otherAttr, true });
		}

		{ // order by not equals
			Attribute attr = getMockAttr("orderBySort");
			Attribute otherAttr = getMockAttr("orderByOtherSort");
			Sort sort = mock(Sort.class);
			Sort otherSort = mock(Sort.class);
			when(attr.getOrderBy()).thenReturn(sort);
			when(otherAttr.getOrderBy()).thenReturn(otherSort);

			testCases.add(new Object[] { attr, otherAttr, false });
		}

		{ // mapped by equals
			Attribute attr = getMockAttr("mappedByAttribute");
			Attribute otherAttr = getMockAttr("mappedByAttribute");
			Attribute mappedByAttr = getMockAttr();
			when(attr.getMappedBy()).thenReturn(mappedByAttr);
			when(otherAttr.getMappedBy()).thenReturn(mappedByAttr);

			testCases.add(new Object[] { attr, otherAttr, true });
		}

		{ // mapped by not equals
			Attribute attr = getMockAttr("mappedByAttribute");
			Attribute otherAttr = getMockAttr("mappedByOtherAttribute");
			Attribute mappedByAttr = getMockAttr();
			when(mappedByAttr.getIdentifier()).thenReturn("attrId0");
			when(attr.getMappedBy()).thenReturn(mappedByAttr);
			Attribute mappedByOtherAttr = getMockAttr();
			when(mappedByOtherAttr.getIdentifier()).thenReturn("attrId1");
			when(otherAttr.getMappedBy()).thenReturn(mappedByOtherAttr);

			testCases.add(new Object[] { attr, otherAttr, false });
		}

		{ // parent equals
			Attribute attr = getMockAttr("parentAttribute");
			Attribute otherAttr = getMockAttr("parentAttribute");
			Attribute parentAttr = getMockAttr();
			when(attr.getParent()).thenReturn(parentAttr);
			when(otherAttr.getParent()).thenReturn(parentAttr);

			testCases.add(new Object[] { attr, otherAttr, true });
		}

		{ // parent not equals
			Attribute attr = getMockAttr("parentAttribute");
			Attribute otherAttr = getMockAttr("parentOtherAttribute");
			Attribute parentAttr = getMockAttr();
			when(attr.getParent()).thenReturn(parentAttr);
			Attribute parentOtherAttr = getMockAttr();
			when(otherAttr.getParent()).thenReturn(parentOtherAttr);

			testCases.add(new Object[] { attr, otherAttr, true });
		}

		{ // entity equals
			Attribute attr = getMockAttr("entityType");
			Attribute otherAttr = getMockAttr("entityType");
			EntityType entityType = when(mock(EntityType.class).getId()).thenReturn("entityTypeId").getMock();
			when(attr.getEntity()).thenReturn(entityType);
			when(otherAttr.getEntity()).thenReturn(entityType);

			testCases.add(new Object[] { attr, otherAttr, true });
		}

		{ // entity not equals
			Attribute attr = getMockAttr("entityType");
			Attribute otherAttr = getMockAttr("otherEntityType");
			EntityType entityType = when(mock(EntityType.class).getId()).thenReturn("entityTypeId").getMock();
			when(attr.getEntity()).thenReturn(entityType);
			EntityType otherEntityType = when(mock(EntityType.class).getId()).thenReturn("otherEntityTypeId").getMock();
			when(otherAttr.getEntity()).thenReturn(otherEntityType);

			testCases.add(new Object[] { attr, otherAttr, false });
		}

		return testCases.iterator();
	}

	@Test(dataProvider = "testEqualsAttributeNoIdentifierCheckProvider")
	public void testEqualsAttributeNoIdentifierCheck(Attribute attr, Attribute otherAttr, boolean shouldEqual)
	{
		assertEquals(EntityUtils.equals(attr, otherAttr, false), shouldEqual);
	}

	@DataProvider(name = "testEqualsAttributeNoIdentifierCheckProvider")
	public Iterator<Object[]> testEqualsAttributeNoIdentifierCheckProvider()
	{
		List<Object[]> testCases = newArrayList();

		{
			// identifiers differ
			Attribute attr = getMockAttr();
			when(attr.getIdentifier()).thenReturn("attrId");
			Attribute otherAttr = getMockAttr();
			when(otherAttr.getIdentifier()).thenReturn("otherAttrId");
			testCases.add(new Object[] { attr, otherAttr, true });
		}

		{   // parent attribute identifiers differ
			Attribute attr = getMockAttr();
			Attribute parentAttr = getMockAttr();
			when(parentAttr.getIdentifier()).thenReturn("parentAttrId");
			when(attr.getParent()).thenReturn(parentAttr);

			Attribute otherAttr = getMockAttr();
			Attribute otherParentAttr = getMockAttr();
			when(otherParentAttr.getIdentifier()).thenReturn("otherParentAttrId");
			when(otherAttr.getParent()).thenReturn(otherParentAttr);

			testCases.add(new Object[] { attr, otherAttr, true });
		}

		{   // entity identifier differs
			Attribute attr = getMockAttr();
			EntityType entityType = when(mock(EntityType.class).getId()).thenReturn("entityTypeId").getMock();
			when(attr.getEntity()).thenReturn(entityType);

			Attribute otherAttr = getMockAttr();
			EntityType otherEntityType = when(mock(EntityType.class).getId()).thenReturn("otherEntityTypeId").getMock();
			when(otherAttr.getEntity()).thenReturn(otherEntityType);

			testCases.add(new Object[] { attr, otherAttr, true });
		}

		{
			// identifiers and label differ
			Attribute attr = getMockAttr();
			when(attr.getIdentifier()).thenReturn("attrId");
			when(attr.getLabel()).thenReturn("label");
			Attribute otherAttr = getMockAttr();
			when(otherAttr.getIdentifier()).thenReturn("otherAttrId");
			when(otherAttr.getLabel()).thenReturn("otherLabel");
			testCases.add(new Object[] { attr, otherAttr, false });
		}

		return testCases.iterator();
	}

	// TODO COMPOUND
	@DataProvider(name = "testIsNullValueProvider")
	public Iterator<Object[]> testIsNullValueProvider()
	{
		String attrName = "attr";
		List<Object[]> dataList = new ArrayList<>();
		{
			Entity entity = getIsNullValueMockEntity();
			when(entity.getBoolean(attrName)).thenReturn(true);
			dataList.add(new Object[] { entity, getIsNullValueMockAttribute(attrName, BOOL), false });
		}
		{
			Entity entity = getIsNullValueMockEntity();
			when(entity.getBoolean(attrName)).thenReturn(null);
			dataList.add(new Object[] { entity, getIsNullValueMockAttribute(attrName, BOOL), true });
		}
		EnumSet.of(CATEGORICAL, FILE, XREF).forEach(attributeType ->
		{
			{
				Entity entity = getIsNullValueMockEntity();
				when(entity.getEntity(attrName)).thenReturn(mock(Entity.class));
				dataList.add(new Object[] { entity, getIsNullValueMockAttribute(attrName, attributeType), false });
			}
			{
				Entity entity = getIsNullValueMockEntity();
				when(entity.getEntity(attrName)).thenReturn(null);
				dataList.add(new Object[] { entity, getIsNullValueMockAttribute(attrName, attributeType), true });
			}
		});
		EnumSet.of(CATEGORICAL_MREF, MREF, ONE_TO_MANY).forEach(attributeType ->
		{
			{
				Entity entity = getIsNullValueMockEntity();
				when(entity.getEntities(attrName)).thenReturn(singletonList(mock(Entity.class)));
				dataList.add(new Object[] { entity, getIsNullValueMockAttribute(attrName, attributeType), false });
			}
			{
				Entity entity = getIsNullValueMockEntity();
				when(entity.getEntities(attrName)).thenReturn(emptyList());
				dataList.add(new Object[] { entity, getIsNullValueMockAttribute(attrName, attributeType), true });
			}
		});
		{
			Entity entity = getIsNullValueMockEntity();
			when(entity.getLocalDate(attrName)).thenReturn(LocalDate.now());
			dataList.add(new Object[] { entity, getIsNullValueMockAttribute(attrName, DATE), false });
		}
		{
			Entity entity = getIsNullValueMockEntity();
			when(entity.getLocalDate(attrName)).thenReturn(null);
			dataList.add(new Object[] { entity, getIsNullValueMockAttribute(attrName, DATE), true });
		}
		{
			Entity entity = getIsNullValueMockEntity();
			when(entity.getInstant(attrName)).thenReturn(Instant.now());
			dataList.add(new Object[] { entity, getIsNullValueMockAttribute(attrName, DATE_TIME), false });
		}
		{
			Entity entity = getIsNullValueMockEntity();
			when(entity.getInstant(attrName)).thenReturn(null);
			dataList.add(new Object[] { entity, getIsNullValueMockAttribute(attrName, DATE_TIME), true });
		}
		{
			Entity entity = getIsNullValueMockEntity();
			when(entity.getDouble(attrName)).thenReturn(1.23);
			dataList.add(new Object[] { entity, getIsNullValueMockAttribute(attrName, DECIMAL), false });
		}
		{
			Entity entity = getIsNullValueMockEntity();
			when(entity.getDouble(attrName)).thenReturn(null);
			dataList.add(new Object[] { entity, getIsNullValueMockAttribute(attrName, DECIMAL), true });
		}
		EnumSet.of(ENUM, EMAIL, HTML, HYPERLINK, SCRIPT, STRING, TEXT).forEach(attributeType ->
		{
			{
				Entity entity = getIsNullValueMockEntity();
				when(entity.getString(attrName)).thenReturn("str");
				dataList.add(new Object[] { entity, getIsNullValueMockAttribute(attrName, attributeType), false });
			}
			{
				Entity entity = getIsNullValueMockEntity();
				when(entity.getDouble(attrName)).thenReturn(null);
				dataList.add(new Object[] { entity, getIsNullValueMockAttribute(attrName, attributeType), true });
			}
		});
		{
			Entity entity = getIsNullValueMockEntity();
			when(entity.getInt(attrName)).thenReturn(123);
			dataList.add(new Object[] { entity, getIsNullValueMockAttribute(attrName, INT), false });
		}
		{
			Entity entity = getIsNullValueMockEntity();
			when(entity.getInt(attrName)).thenReturn(null);
			dataList.add(new Object[] { entity, getIsNullValueMockAttribute(attrName, INT), true });
		}
		{
			Entity entity = getIsNullValueMockEntity();
			when(entity.getLong(attrName)).thenReturn(123L);
			dataList.add(new Object[] { entity, getIsNullValueMockAttribute(attrName, LONG), false });
		}
		{
			Entity entity = getIsNullValueMockEntity();
			when(entity.getLong(attrName)).thenReturn(null);
			dataList.add(new Object[] { entity, getIsNullValueMockAttribute(attrName, LONG), true });
		}
		return dataList.iterator();
	}

	@Test(dataProvider = "testIsNullValueProvider")
	public void isNullValue(Entity entity, Attribute attribute, boolean expectedIsNullValue)
	{
		assertEquals(EntityUtils.isNullValue(entity, attribute), expectedIsNullValue);
	}

	@Test(expectedExceptions = RuntimeException.class)
	public void isNullValueCompoundAttribute()
	{
		EntityUtils.isNullValue(getIsNullValueMockEntity(), getIsNullValueMockAttribute("attribute", COMPOUND));
	}

	private Entity getIsNullValueMockEntity()
	{
		Entity entity = mock(Entity.class);
		when(entity.toString()).thenReturn("entity");
		return entity;
	}

	private Attribute getIsNullValueMockAttribute(String attributeName, AttributeType attributeType)
	{
		Attribute attribute = mock(Attribute.class);
		when(attribute.getName()).thenReturn(attributeName);
		when(attribute.getDataType()).thenReturn(attributeType);
		when(attribute.toString()).thenReturn(attributeType.toString());
		return attribute;
	}

	private Attribute getMockAttr(String toString)
	{
		Attribute attr = getMockAttr();
		when(attr.toString()).thenReturn(toString);
		return attr;
	}

	private Attribute getMockAttr()
	{
		EntityType entityType = when(mock(EntityType.class).getId()).thenReturn("entityTypeId").getMock();
		Attribute attr = mock(Attribute.class);
		when(attr.getEntity()).thenReturn(entityType);
		when(attr.getTags()).thenReturn(emptyList());
		return attr;
	}
}
