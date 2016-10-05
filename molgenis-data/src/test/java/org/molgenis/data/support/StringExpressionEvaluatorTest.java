package org.molgenis.data.support;

import com.google.gson.JsonSyntaxException;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityType;
import org.springframework.core.convert.ConversionFailedException;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.MolgenisFieldTypes.AttributeType.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

public class StringExpressionEvaluatorTest
{
	private Entity entity;
	private EntityType emd;

	@BeforeTest
	public void createEntity()
	{
		emd = when(mock(EntityType.class).getName()).thenReturn("Source").getMock();
		AttributeMetaData idAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("Identifier").getMock();
		when(idAttr.getDataType()).thenReturn(INT);
		AttributeMetaData intAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("Int").getMock();
		when(intAttr.getDataType()).thenReturn(INT);
		AttributeMetaData stringAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("String").getMock();
		when(stringAttr.getDataType()).thenReturn(STRING);
		AttributeMetaData nonNumericStringAttr = when(mock(AttributeMetaData.class).getName())
				.thenReturn("NonNumericString").getMock();
		when(nonNumericStringAttr.getDataType()).thenReturn(STRING);
		AttributeMetaData longAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("Long").getMock();
		when(longAttr.getDataType()).thenReturn(LONG);
		when(emd.getIdAttribute()).thenReturn(idAttr);
		when(emd.getAttribute("Identifier")).thenReturn(idAttr);
		when(emd.getAttribute("Int")).thenReturn(intAttr);
		when(emd.getAttribute("String")).thenReturn(stringAttr);
		when(emd.getAttribute("NonNumericString")).thenReturn(nonNumericStringAttr);
		when(emd.getAttribute("Long")).thenReturn(longAttr);

		entity = new DynamicEntity(emd);
		entity.set("Int", 1);
		entity.set("String", "12");
		entity.set("Long", 10L);
		entity.set("NonNumericString", "Hello World!");
	}

	@Test
	public void testStringEvaluatorConstructorChecksIfAttributeHasExpression()
	{
		AttributeMetaData amd = when(mock(AttributeMetaData.class).getName()).thenReturn("#CHROM").getMock();
		when(amd.getDataType()).thenReturn(STRING);
		try
		{
			new StringExpressionEvaluator(amd, emd);
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
		AttributeMetaData amd = when(mock(AttributeMetaData.class).getName()).thenReturn("#CHROM").getMock();
		when(amd.getDataType()).thenReturn(STRING);
		when(amd.getExpression()).thenReturn("{}");
		try
		{
			new StringExpressionEvaluator(amd, emd);
			fail("expected illegal state exception");
		}
		catch (JsonSyntaxException expected)
		{
		}

	}

	@Test
	public void testStringEvaluatorConstructorChecksIfAttributeMentionsExistingAttribute()
	{
		AttributeMetaData amd = when(mock(AttributeMetaData.class).getName()).thenReturn("#CHROM").getMock();
		when(amd.getDataType()).thenReturn(STRING);
		when(amd.getExpression()).thenReturn("bogus");
		try
		{
			new StringExpressionEvaluator(amd, emd);
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
		AttributeMetaData amd = when(mock(AttributeMetaData.class).getName()).thenReturn("#CHROM").getMock();
		when(amd.getDataType()).thenReturn(STRING);
		when(amd.getExpression()).thenReturn("Int");
		assertEquals(new StringExpressionEvaluator(amd, emd).evaluate(entity), "1");
	}

	@Test
	public void testStringEvaluatorLookupAttributeAndConvertFromIntToLong()
	{
		AttributeMetaData amd = when(mock(AttributeMetaData.class).getName()).thenReturn("#POS").getMock();
		when(amd.getDataType()).thenReturn(LONG);
		when(amd.getExpression()).thenReturn("Int");
		assertEquals(new StringExpressionEvaluator(amd, emd).evaluate(entity), 1L);
	}

	@Test
	public void testStringEvaluatorLookupAttributeAndConvertFromLongToInt()
	{
		AttributeMetaData amd = when(mock(AttributeMetaData.class).getName()).thenReturn("#POS").getMock();
		when(amd.getDataType()).thenReturn(INT);
		when(amd.getExpression()).thenReturn("Long");
		assertEquals(new StringExpressionEvaluator(amd, emd).evaluate(entity), 10);
	}

	@Test
	public void testStringEvaluatorLookupAttributeAndConvertFromStringToLong()
	{
		AttributeMetaData amd = when(mock(AttributeMetaData.class).getName()).thenReturn("#POS").getMock();
		when(amd.getDataType()).thenReturn(LONG);
		when(amd.getExpression()).thenReturn("String");
		assertEquals(new StringExpressionEvaluator(amd, emd).evaluate(entity), 12L);
	}

	@Test
	public void testStringEvaluatorLookupAttributeAndConvertFromNonNumericStringToLongFails()
	{

		AttributeMetaData amd = when(mock(AttributeMetaData.class).getName()).thenReturn("#POS").getMock();
		when(amd.getDataType()).thenReturn(LONG);
		when(amd.getExpression()).thenReturn("NonNumericString");
		try
		{
			assertEquals(new StringExpressionEvaluator(amd, emd).evaluate(entity), 12L);
			fail("Expected ConversionFailedException.");
		}
		catch (ConversionFailedException expected)
		{
			assertEquals(expected.getMessage(),
					"Failed to convert from type java.lang.String to type java.lang.Long for value 'Hello World!'; nested exception is java.lang.NumberFormatException: For input string: \"HelloWorld!\"");
		}
	}
}
