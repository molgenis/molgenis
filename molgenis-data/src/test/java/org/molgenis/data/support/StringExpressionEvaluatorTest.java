package org.molgenis.data.support;

import com.google.gson.JsonSyntaxException;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.AttributeType.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

public class StringExpressionEvaluatorTest
{
	private Entity entity;
	private EntityType entityType;

	@BeforeTest
	public void createEntity()
	{
		entityType = when(mock(EntityType.class).getId()).thenReturn("Source").getMock();
		Attribute idAttr = when(mock(Attribute.class).getName()).thenReturn("Identifier").getMock();

		when(idAttr.getDataType()).thenReturn(INT);
		Attribute intAttr = when(mock(Attribute.class).getName()).thenReturn("Int").getMock();
		when(intAttr.getDataType()).thenReturn(INT);
		Attribute stringAttr = when(mock(Attribute.class).getName()).thenReturn("String").getMock();
		when(stringAttr.getDataType()).thenReturn(STRING);
		Attribute nonNumericStringAttr = when(mock(Attribute.class).getName()).thenReturn("NonNumericString").getMock();
		when(nonNumericStringAttr.getDataType()).thenReturn(STRING);
		Attribute longAttr = when(mock(Attribute.class).getName()).thenReturn("Long").getMock();
		when(longAttr.getDataType()).thenReturn(LONG);
		when(entityType.getIdAttribute()).thenReturn(idAttr);
		when(entityType.getId()).thenReturn("test");
		when(entityType.getAttribute("Identifier")).thenReturn(idAttr);
		when(entityType.getAttribute("Int")).thenReturn(intAttr);
		when(entityType.getAttribute("String")).thenReturn(stringAttr);
		when(entityType.getAttribute("NonNumericString")).thenReturn(nonNumericStringAttr);
		when(entityType.getAttribute("Long")).thenReturn(longAttr);

		entity = new DynamicEntity(entityType);
		entity.set("Int", 1);
		entity.set("String", "12");
		entity.set("Long", 10L);
		entity.set("NonNumericString", "Hello World!");
	}

	@Test
	public void testStringEvaluatorConstructorChecksIfAttributeHasExpression()
	{
		Attribute amd = when(mock(Attribute.class).getName()).thenReturn("#CHROM").getMock();
		when(amd.getDataType()).thenReturn(STRING);
		try
		{
			new StringExpressionEvaluator(amd, entityType);
			fail("Expected NPE");
		}
		catch (NullPointerException expected)
		{
			assertEquals(expected.getMessage(), "Attribute has no expression.");
		}

	}

	@Test
	public void testStringEvaluatorConstructorChecksIfExpressionIsMap()
	{
		Attribute amd = when(mock(Attribute.class).getName()).thenReturn("#CHROM").getMock();
		when(amd.getDataType()).thenReturn(STRING);
		when(amd.getExpression()).thenReturn("{}");
		try
		{
			new StringExpressionEvaluator(amd, entityType);
			fail("expected illegal state exception");
		}
		catch (JsonSyntaxException expected)
		{
		}

	}

	@Test
	public void testStringEvaluatorConstructorChecksIfAttributeMentionsExistingAttribute()
	{
		Attribute amd = when(mock(Attribute.class).getName()).thenReturn("#CHROM").getMock();
		when(amd.getDataType()).thenReturn(STRING);
		when(amd.getExpression()).thenReturn("bogus");
		try
		{
			new StringExpressionEvaluator(amd, entityType);
			fail("expected illegal argument exception");
		}
		catch (IllegalArgumentException expected)
		{
			assertEquals(expected.getMessage(),
					"Expression for attribute '#CHROM' references non-existant attribute 'bogus'.");
		}

	}

	@Test
	public void testStringEvaluatorLookupAttributeAndConvertFromIntToString()
	{
		Attribute amd = when(mock(Attribute.class).getName()).thenReturn("#CHROM").getMock();
		when(amd.getDataType()).thenReturn(STRING);
		when(amd.getExpression()).thenReturn("Int");
		assertEquals(new StringExpressionEvaluator(amd, entityType).evaluate(entity), "1");
	}

	@Test
	public void testStringEvaluatorLookupAttributeAndConvertFromIntToLong()
	{
		Attribute amd = when(mock(Attribute.class).getName()).thenReturn("#POS").getMock();
		when(amd.getDataType()).thenReturn(LONG);
		when(amd.getExpression()).thenReturn("Int");
		assertEquals(new StringExpressionEvaluator(amd, entityType).evaluate(entity), 1L);
	}

	@Test
	public void testStringEvaluatorLookupAttributeAndConvertFromLongToInt()
	{
		Attribute amd = when(mock(Attribute.class).getName()).thenReturn("#POS").getMock();
		when(amd.getDataType()).thenReturn(INT);
		when(amd.getExpression()).thenReturn("Long");
		assertEquals(new StringExpressionEvaluator(amd, entityType).evaluate(entity), 10);
	}

	@Test
	public void testStringEvaluatorLookupAttributeAndConvertFromStringToLong()
	{
		Attribute amd = when(mock(Attribute.class).getName()).thenReturn("#POS").getMock();
		when(amd.getDataType()).thenReturn(LONG);
		when(amd.getExpression()).thenReturn("String");
		assertEquals(new StringExpressionEvaluator(amd, entityType).evaluate(entity), 12L);
	}

	@Test(expectedExceptions = MolgenisDataException.class, expectedExceptionsMessageRegExp = "Conversion failure in entity type \\[test\\] attribute \\[id\\]; Failed to convert from type \\[java.lang.String\\] to type \\[java.lang.Long\\] for value 'Hello World!'; nested exception is java.lang.NumberFormatException: For input string: \"HelloWorld!\"")
	public void testStringEvaluatorLookupAttributeAndConvertFromNonNumericStringToLongFails()
	{
		Attribute amd = when(mock(Attribute.class).getName()).thenReturn("#POS").getMock();
		when(amd.getName()).thenReturn("id");
		when(amd.getDataType()).thenReturn(LONG);
		when(amd.getExpression()).thenReturn("NonNumericString");
		when(amd.getEntity()).thenReturn(entityType);
		new StringExpressionEvaluator(amd, entityType).evaluate(entity);
	}
}
