package org.molgenis.data.meta.model;

import org.molgenis.data.Entity;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
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

	@BeforeMethod
	public void beforeMethod()
	{
		randomAttribute = when(mock(AttributeMetaData.class).getDataType()).thenReturn(STRING).getMock();
		attributePart = when(mock(AttributeMetaData.class).getDataType()).thenReturn(STRING).getMock();
		Iterable<AttributeMetaData> attributeParts = newArrayList(attributePart);

		compoundAttribute = when(mock(AttributeMetaData.class).getDataType()).thenReturn(COMPOUND).getMock();
		when(compoundAttribute.getAttributeParts()).thenReturn(attributeParts);

		Iterable<AttributeMetaData> mockedAttributes = newArrayList(compoundAttribute, randomAttribute);

		entity = when(mock(Entity.class).getEntities(ATTRIBUTES, AttributeMetaData.class)).thenReturn(mockedAttributes)
				.getMock();
		entityMetaData = new EntityMetaData(entity);
	}

	@Test
	public void getCompoundOrderedAttributesCorrectOrderTest()
	{
		List<AttributeMetaData> expectedAttributes = newArrayList(attributePart, compoundAttribute, randomAttribute);

		List<AttributeMetaData> actualAttributes = entityMetaData.getCompoundOrderedAttributes();
		assertEquals(actualAttributes, expectedAttributes);
	}

	@Test
	public void getCompoundOrderedAttributesIncorrectOrderTest()
	{
		List<AttributeMetaData> expectedAttributes = newArrayList(compoundAttribute, attributePart, randomAttribute);

		List<AttributeMetaData> actualAttributes = entityMetaData.getCompoundOrderedAttributes();
		assertNotEquals(actualAttributes, expectedAttributes);
	}
}
