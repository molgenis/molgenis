package org.molgenis.data.support;

import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.fieldtypes.IntField;
import org.molgenis.fieldtypes.LongField;
import org.molgenis.fieldtypes.StringField;
import org.springframework.core.convert.ConversionFailedException;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.google.gson.JsonSyntaxException;

public class StringExpressionEvaluatorTest
{
	private Entity entity;
	private DefaultEntityMetaData emd;

	@BeforeTest
	public void createEntity()
	{
		emd = new DefaultEntityMetaData("Source");
		emd.addAttributeMetaData(new DefaultAttributeMetaData("Identifier").setDataType(new IntField()), ROLE_ID);
		emd.addAttributeMetaData(new DefaultAttributeMetaData("Int").setDataType(new IntField()));
		emd.addAttributeMetaData(new DefaultAttributeMetaData("String").setDataType(new StringField()));
		emd.addAttributeMetaData(new DefaultAttributeMetaData("NonNumericString").setDataType(new StringField()));
		emd.addAttributeMetaData(new DefaultAttributeMetaData("Long").setDataType(new LongField()));
		entity = new MapEntity(emd);
		entity.set("Int", 1);
		entity.set("String", "12");
		entity.set("Long", 10L);
		entity.set("NonNumericString", "Hello World!");
	}

	@Test
	public void testStringEvaluatorConstructorChecksIfAttributeHasExpression()
	{
		AttributeMetaData amd = new DefaultAttributeMetaData("#CHROM").setDataType(new StringField());
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
		AttributeMetaData amd = new DefaultAttributeMetaData("#CHROM").setDataType(new StringField())
				.setExpression("{}");
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
		AttributeMetaData amd = new DefaultAttributeMetaData("#CHROM").setDataType(new StringField())
				.setExpression("bogus");
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
		AttributeMetaData amd = new DefaultAttributeMetaData("#CHROM").setDataType(new StringField())
				.setExpression("Int");
		assertEquals(new StringExpressionEvaluator(amd, emd).evaluate(entity), "1");
	}

	@Test
	public void testStringEvaluatorLookupAttributeAndConvertFromIntToLong()
	{
		AttributeMetaData amd = new DefaultAttributeMetaData("#POS").setDataType(new LongField()).setExpression("Int");
		assertEquals(new StringExpressionEvaluator(amd, emd).evaluate(entity), 1L);
	}

	@Test
	public void testStringEvaluatorLookupAttributeAndConvertFromLongToInt()
	{
		AttributeMetaData amd = new DefaultAttributeMetaData("#POS").setDataType(new IntField()).setExpression("Long");
		assertEquals(new StringExpressionEvaluator(amd, emd).evaluate(entity), 10);
	}

	@Test
	public void testStringEvaluatorLookupAttributeAndConvertFromStringToLong()
	{
		AttributeMetaData amd = new DefaultAttributeMetaData("#POS").setDataType(new LongField())
				.setExpression("String");
		assertEquals(new StringExpressionEvaluator(amd, emd).evaluate(entity), 12L);
	}

	@Test
	public void testStringEvaluatorLookupAttributeAndConvertFromNonNumericStringToLongFails()
	{

		AttributeMetaData amd = new DefaultAttributeMetaData("#POS").setDataType(new LongField())
				.setExpression("NonNumericString");
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
