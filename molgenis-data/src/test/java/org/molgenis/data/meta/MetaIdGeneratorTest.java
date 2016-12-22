package org.molgenis.data.meta;

import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Iterator;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

public class MetaIdGeneratorTest
{
	@DataProvider(name = "generateIdEntityTypeIntProvider")
	public static Iterator<Object[]> generateIdEntityTypeIntProvider()
	{
		return newArrayList(new Object[] { 10, "t#e7c7af28" }, new Object[] { 30, "this_is_a_very_long_s#e7c7af28" },
				new Object[] { 50, "this_is_a_very_long_simple_name#e7c7af28" }).iterator();
	}

	@Test(dataProvider = "generateIdEntityTypeIntProvider")
	public void testGenerateIdEntityTypeInt(int maxLength, String expectedId)
	{
		EntityType entityType = mock(EntityType.class);
		when(entityType.getId()).thenReturn("0123456789-0123456789-0123456789");
		when(entityType.getSimpleName()).thenReturn("this_is_a_very_long_simple_name");
		assertEquals(MetaIdGenerator.generateId(entityType, maxLength), expectedId);
	}

	@Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Max byte length must be >= 10")
	public void testGenerateIdEntityTypeIntInvalidLength()
	{
		EntityType entityType = mock(EntityType.class);
		when(entityType.getId()).thenReturn("0123456789-0123456789-0123456789");
		when(entityType.getSimpleName()).thenReturn("this_is_a_very_long_simple_name");
		MetaIdGenerator.generateId(entityType, 5);
	}

	@DataProvider(name = "generateIdAttributeIntProvider")
	public static Iterator<Object[]> generateIdAttributeIntProvider()
	{
		return newArrayList(new Object[] { "attr", 32, "attr" }, new Object[] { "ättr", 32, "ttr#69363cb7" },
				new Object[] { "at-tr", 32, "attr#69363cb7" },
				new Object[] { "this_is_a_very_long_attribute_name", 32, "this_is_a_very_long_att#69363cb7" })
				.iterator();
	}

	@Test(dataProvider = "generateIdAttributeIntProvider")
	public void testGenerateIdAttributeInt(String attrName, int maxLength, String expectedId)
	{
		Attribute attr = mock(Attribute.class);
		when(attr.getIdentifier()).thenReturn("9876543210-9876543210-9876543210");
		when(attr.getName()).thenReturn(attrName);
		assertEquals(MetaIdGenerator.generateId(attr, maxLength), expectedId);
	}

	@Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Max byte length must be >= 10")
	public void testGenerateIdAttributeIntInvalidLength()
	{
		Attribute attr = mock(Attribute.class);
		when(attr.getIdentifier()).thenReturn("9876543210-9876543210-9876543210");
		when(attr.getName()).thenReturn("this_is_a_very_long_attribute_name");
		MetaIdGenerator.generateId(attr, 5);
	}
}