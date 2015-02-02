package org.molgenis.data.support;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.fieldtypes.IntField;
import org.molgenis.fieldtypes.LongField;
import org.molgenis.fieldtypes.StringField;
import org.molgenis.fieldtypes.XrefField;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.google.gson.JsonSyntaxException;

public class MapOfStringsExpressionEvaluatorTest
{
	private Entity entity;
	private DefaultEntityMetaData emd;
	private DefaultEntityMetaData refEmd;

	@BeforeTest
	public void createEntity()
	{
		emd = new DefaultEntityMetaData("Source");
		emd.addAttributeMetaData(new DefaultAttributeMetaData("Identifier").setIdAttribute(true).setNillable(false)
				.setDataType(new IntField()));
		emd.addAttributeMetaData(new DefaultAttributeMetaData("Int").setDataType(new IntField()));
		emd.addAttributeMetaData(new DefaultAttributeMetaData("String").setDataType(new StringField()));
		emd.addAttributeMetaData(new DefaultAttributeMetaData("NonNumericString").setDataType(new StringField()));
		emd.addAttributeMetaData(new DefaultAttributeMetaData("Long").setDataType(new LongField()));

		refEmd = new DefaultEntityMetaData("RefEntity");
		refEmd.addAttributeMetaData(new DefaultAttributeMetaData("Identifier").setIdAttribute(true));
		refEmd.addAttributeMetaData(new DefaultAttributeMetaData("Chromosome"));
		refEmd.addAttributeMetaData(new DefaultAttributeMetaData("Position").setDataType(new LongField()));

		entity = new MapEntity(emd);
		entity.set("Int", 1);
		entity.set("String", "12");
		entity.set("Long", 10L);
		entity.set("NonNumericString", "Hello World!");
	}

	@Test
	public void testMapOfStringsEvaluatorConstructorChecksIfAttributeHasExpression()
	{
		AttributeMetaData amd = new DefaultAttributeMetaData("#CHROM").setDataType(new StringField());
		try
		{
			new MapOfStringsExpressionEvaluator(amd, emd);
			fail("Expected NPE");
		}
		catch (NullPointerException expected)
		{
			assertEquals(expected.getMessage(), "Attribute has no expression.");
		}
	}

	@Test
	public void testMapOfStringsEvaluatorConstructorChecksIfAttributeHasRefEntity()
	{
		AttributeMetaData amd = new DefaultAttributeMetaData("location").setDataType(new XrefField()).setExpression(
				"{'a':b}");
		try
		{
			new MapOfStringsExpressionEvaluator(amd, emd);
			fail("Expected IllegalArgumentException.");
		}
		catch (NullPointerException expected)
		{
			assertEquals(expected.getMessage(), "refEntity not specified.");
		}
	}

	@Test
	public void testMapOfStringsEvaluatorConstructorChecksIfExpressionIsMap()
	{
		AttributeMetaData amd = new DefaultAttributeMetaData("Location").setDataType(new XrefField())
				.setExpression("hallo").setRefEntity(refEmd);
		try
		{
			new MapOfStringsExpressionEvaluator(amd, emd);
			fail("Expected JSON exception");
		}
		catch (JsonSyntaxException expected)
		{

		}
	}

	@Test
	public void testMapOfStringsEvaluatorConstructorChecksThatExpressionIsMapOfStrings()
	{
		AttributeMetaData amd = new DefaultAttributeMetaData("#CHROM").setDataType(new XrefField())
				.setExpression("{'Chromosome':{'hallo1':'bla'}}").setRefEntity(refEmd);
		try
		{
			new MapOfStringsExpressionEvaluator(amd, emd);
			fail("Expected IllegalArgumentException");
		}
		catch (IllegalArgumentException expected)
		{
			assertEquals(expected.getMessage(),
					"Nested expressions not supported, expression must be Map<String,String>.");
		}
	}

	@Test
	public void testMapOfStringsEvaluatorConstructorChecksIfCalculatedAttributesAllExist()
	{
		AttributeMetaData amd = new DefaultAttributeMetaData("#CHROM").setDataType(new StringField())
				.setExpression("{'hallo':String}").setRefEntity(refEmd);
		try
		{
			new MapOfStringsExpressionEvaluator(amd, emd);
			fail("Expected illegal argument exception");
		}
		catch (IllegalArgumentException expected)
		{
			assertEquals(expected.getMessage(), "Unknown target attribute: hallo.");
		}
	}

	@Test
	public void testMapOfStringsEvaluatorConstructorChecksIfMentionedAttributesAllExist()
	{
		AttributeMetaData amd = new DefaultAttributeMetaData("#CHROM").setDataType(new StringField())
				.setExpression("{'Chromosome':hallo}").setRefEntity(refEmd);
		try
		{
			new MapOfStringsExpressionEvaluator(amd, emd);
			fail("Expected illegal argument exception");
		}
		catch (IllegalArgumentException expected)
		{
			assertEquals(expected.getMessage(),
					"Expression for attribute 'Chromosome' references non-existant attribute 'hallo'.");
		}
	}

	@Test
	public void testEvaluate()
	{
		AttributeMetaData amd = new DefaultAttributeMetaData("#CHROM").setDataType(new XrefField())
				.setExpression("{'Chromosome':String, 'Position':Int}").setRefEntity(refEmd);
		ExpressionEvaluator evaluator = new MapOfStringsExpressionEvaluator(amd, emd);
		Entity expected = new MapEntity(refEmd);
		expected.set("Chromosome", "12");
		expected.set("Position", 1L);
		assertEquals(evaluator.evaluate(entity), expected);
	}
}
