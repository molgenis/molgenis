package org.molgenis.data.meta.model;

import org.molgenis.data.Entity;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Iterator;
import java.util.LinkedHashSet;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newLinkedHashSet;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.MolgenisFieldTypes.AttributeType.COMPOUND;
import static org.molgenis.MolgenisFieldTypes.AttributeType.STRING;
import static org.molgenis.data.meta.model.EntityMetaDataMetaData.ATTRIBUTES;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

public class EntityMetaDataTest
{
	private EntityMetaData entityMetaData;
	private AttributeMetaData randomAttribute;
	private AttributeMetaData compoundAttribute;
	private AttributeMetaData attributePart;
	private Entity entity;

	private EntityMetaData nestedEntityMetaData;
	private AttributeMetaData nestedCompoundParent;
	private AttributeMetaData nestedCompoundPart;
	private AttributeMetaData nestedAttributePart;
	private Entity nestedEntity;

	@BeforeMethod
	public void beforeMethod()
	{
		// Listup for single level compound
		randomAttribute = when(mock(AttributeMetaData.class).getDataType()).thenReturn(STRING).getMock();
		when(randomAttribute.getName()).thenReturn("randomAttribute");

		attributePart = when(mock(AttributeMetaData.class).getDataType()).thenReturn(STRING).getMock();
		when(attributePart.getName()).thenReturn("attributePart");
		Iterable<AttributeMetaData> attributeParts = newArrayList(attributePart);

		compoundAttribute = when(mock(AttributeMetaData.class).getDataType()).thenReturn(COMPOUND).getMock();
		when(compoundAttribute.getName()).thenReturn("compoundAttribute");
		when(compoundAttribute.getAttributeParts()).thenReturn(attributeParts);

		Iterable<AttributeMetaData> mockedAttributes = newArrayList(compoundAttribute, randomAttribute);

		entity = when(mock(Entity.class).getEntities(ATTRIBUTES, AttributeMetaData.class)).thenReturn(mockedAttributes)
				.getMock();
		entityMetaData = new EntityMetaData(entity);

		// Listup for nested compound test
		nestedAttributePart = when(mock(AttributeMetaData.class).getDataType()).thenReturn(STRING).getMock();
		Iterable<AttributeMetaData> nestedCompoundAttributeParts = newArrayList(nestedAttributePart);

		nestedCompoundPart = when(mock(AttributeMetaData.class).getDataType()).thenReturn(COMPOUND).getMock();
		when(nestedCompoundPart.getAttributeParts()).thenReturn(nestedCompoundAttributeParts);
		Iterable<AttributeMetaData> nestedAttributeParts = newArrayList(nestedCompoundPart, attributePart);

		nestedCompoundParent = when(mock(AttributeMetaData.class).getDataType()).thenReturn(COMPOUND).getMock();
		when(nestedCompoundParent.getAttributeParts()).thenReturn(nestedAttributeParts);

		Iterable<AttributeMetaData> mockedNestedAttributes = newArrayList(nestedCompoundParent, randomAttribute);

		nestedEntity = when(mock(Entity.class).getEntities(ATTRIBUTES, AttributeMetaData.class))
				.thenReturn(mockedNestedAttributes).getMock();
		nestedEntityMetaData = new EntityMetaData(nestedEntity);
	}

	@Test
	public void getCompoundOrderedAttributesCorrectOrderTest()
	{
		LinkedHashSet<AttributeMetaData> expectedAttributes = newLinkedHashSet();
		expectedAttributes.add(attributePart);
		expectedAttributes.add(compoundAttribute);
		expectedAttributes.add(randomAttribute);

		LinkedHashSet<AttributeMetaData> actualAttributes = entityMetaData.getCompoundOrderedAttributes();
		assertEquals(actualAttributes, expectedAttributes);
	}

	@Test
	public void getCompoundOrderedAttributesIncorrectOrderTest()
	{
		LinkedHashSet<AttributeMetaData> expectedAttributes = newLinkedHashSet();
		expectedAttributes.add(compoundAttribute);
		expectedAttributes.add(randomAttribute);
		expectedAttributes.add(attributePart);

		LinkedHashSet<AttributeMetaData> actualAttributes = entityMetaData.getCompoundOrderedAttributes();

		Iterator<AttributeMetaData> expectedAttributesIterator = expectedAttributes.iterator();
		Iterator<AttributeMetaData> actualAttributesIterator = actualAttributes.iterator();
		while (expectedAttributesIterator.hasNext() && actualAttributesIterator.hasNext())
		{
			assertNotEquals(actualAttributesIterator.next(), expectedAttributesIterator.next());
		}
	}

	@Test
	public void getCompoundOrderedAttributesWithNestedCompoundsTest()
	{
		LinkedHashSet<AttributeMetaData> expectedAttributes = newLinkedHashSet();
		expectedAttributes.add(nestedAttributePart);
		expectedAttributes.add(nestedCompoundPart);
		expectedAttributes.add(attributePart);
		expectedAttributes.add(nestedCompoundParent);
		expectedAttributes.add(randomAttribute);

		LinkedHashSet<AttributeMetaData> actualAttributes = nestedEntityMetaData.getCompoundOrderedAttributes();
		assertEquals(actualAttributes, expectedAttributes);
	}
}
