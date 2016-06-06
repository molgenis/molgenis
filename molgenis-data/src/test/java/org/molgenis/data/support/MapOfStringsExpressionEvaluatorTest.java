package org.molgenis.data.support;

import static org.molgenis.MolgenisFieldTypes.INT;
import static org.molgenis.MolgenisFieldTypes.LONG;
import static org.molgenis.MolgenisFieldTypes.STRING;
import static org.molgenis.MolgenisFieldTypes.XREF;
import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_ID;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.AttributeMetaData;
import org.molgenis.data.meta.EntityMetaData;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.google.gson.JsonSyntaxException;

public class MapOfStringsExpressionEvaluatorTest
{
	private Entity entity;
	private EntityMetaData emd;
	private EntityMetaData refEmd;

	@BeforeTest
	public void createEntity()
	{
		emd = new EntityMetaData("Source");
		emd.addAttribute(new AttributeMetaData("Identifier").setDataType(INT), ROLE_ID);
		emd.addAttribute(new AttributeMetaData("Int").setDataType(INT));
		emd.addAttribute(new AttributeMetaData("String").setDataType(STRING));
		emd.addAttribute(new AttributeMetaData("NonNumericString").setDataType(STRING));
		emd.addAttribute(new AttributeMetaData("Long").setDataType(STRING));

		refEmd = new EntityMetaData("RefEntity");
		refEmd.addAttribute(new AttributeMetaData("Identifier"), ROLE_ID);
		refEmd.addAttribute(new AttributeMetaData("Chromosome"));
		refEmd.addAttribute(new AttributeMetaData("Position").setDataType(LONG));

		entity = new MapEntity(emd);
		entity.set("Int", 1);
		entity.set("String", "12");
		entity.set("Long", 10L);
		entity.set("NonNumericString", "Hello World!");
	}

	@Test
	public void testMapOfStringsEvaluatorConstructorChecksIfAttributeHasExpression()
	{
		AttributeMetaData amd = new AttributeMetaData("#CHROM").setDataType(STRING);
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
		AttributeMetaData amd = new AttributeMetaData("location").setDataType(XREF).setExpression("{'a':b}");
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
		AttributeMetaData amd = new AttributeMetaData("Location").setDataType(XREF).setExpression("hallo")
				.setRefEntity(refEmd);
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
		AttributeMetaData amd = new AttributeMetaData("#CHROM").setDataType(XREF)
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
		AttributeMetaData amd = new AttributeMetaData("#CHROM").setDataType(STRING)
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
		AttributeMetaData amd = new AttributeMetaData("#CHROM").setDataType(STRING)
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
		AttributeMetaData amd = new AttributeMetaData("#CHROM").setDataType(XREF)
				.setExpression("{'Chromosome':String, 'Position':Int}").setRefEntity(refEmd);
		ExpressionEvaluator evaluator = new MapOfStringsExpressionEvaluator(amd, emd);
		Entity expected = new MapEntity(refEmd);
		expected.set("Chromosome", "12");
		expected.set("Position", 1L);
		assertEquals(evaluator.evaluate(entity), expected);
	}
}
