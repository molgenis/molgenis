package org.molgenis.data.security;

import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Iterator;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.AttributeType.*;
import static org.testng.Assert.assertEquals;

public class EntityIdentityUtilsTest
{
	@Test
	public void testToTypeEntityType()
	{
		EntityType entityType = mock(EntityType.class);
		when(entityType.getId()).thenReturn("MyEntityTypeId");
		assertEquals(EntityIdentityUtils.toType(entityType), "entity-MyEntityTypeId");
	}

	@Test
	public void testToTypeString()
	{
		assertEquals(EntityIdentityUtils.toType("MyEntityTypeId"), "entity-MyEntityTypeId");
	}

	@DataProvider(name = "testInitProvider")
	public static Iterator<Object[]> testToIdTypeProvider()
	{
		return asList(new Object[] { EMAIL, String.class }, new Object[] { HYPERLINK, String.class },
				new Object[] { STRING, String.class }, new Object[] { INT, Integer.class },
				new Object[] { LONG, Long.class }).iterator();
	}

	@Test(dataProvider = "testToIdTypeProvider")
	public void testToIdType(AttributeType attributeType, Class<?> expectedIdType)
	{
		EntityType entityType = mock(EntityType.class);
		Attribute idAttribute = when(mock(Attribute.class).getDataType()).thenReturn(attributeType).getMock();
		when(entityType.getIdAttribute()).thenReturn(idAttribute);
		assertEquals(EntityIdentityUtils.toIdType(entityType), expectedIdType);
	}
}