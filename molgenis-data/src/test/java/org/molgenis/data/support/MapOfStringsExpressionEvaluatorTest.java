package org.molgenis.data.support;

import com.google.gson.JsonSyntaxException;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityMetaData;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.MolgenisFieldTypes.AttributeType.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

public class MapOfStringsExpressionEvaluatorTest
{
	private Entity entity;
	private EntityMetaData emd;
	private EntityMetaData refEmd;

	@BeforeTest
	public void createEntity()
	{
		emd = when(mock(EntityMetaData.class).getName()).thenReturn("Source").getMock();
		AttributeMetaData idAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("Identifier").getMock();
		when(idAttr.getDataType()).thenReturn(INT);
		when(idAttr.getEntityMetaData()).thenReturn(mock(EntityMetaData.class));
		AttributeMetaData intAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("Int").getMock();
		when(intAttr.getDataType()).thenReturn(INT);
		when(intAttr.getEntityMetaData()).thenReturn(mock(EntityMetaData.class));
		AttributeMetaData stringAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("String").getMock();
		when(stringAttr.getDataType()).thenReturn(STRING);
		when(stringAttr.getEntityMetaData()).thenReturn(mock(EntityMetaData.class));
		AttributeMetaData nonNumericStringAttr = when(mock(AttributeMetaData.class).getName())
				.thenReturn("NonNumericString").getMock();
		when(nonNumericStringAttr.getDataType()).thenReturn(STRING);
		when(nonNumericStringAttr.getEntityMetaData()).thenReturn(mock(EntityMetaData.class));
		AttributeMetaData longAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("Long").getMock();
		when(longAttr.getDataType()).thenReturn(STRING);
		when(longAttr.getEntityMetaData()).thenReturn(mock(EntityMetaData.class));
		when(emd.getIdAttribute()).thenReturn(idAttr);
		when(emd.getAttribute("Identifier")).thenReturn(idAttr);
		when(emd.getAttribute("Int")).thenReturn(intAttr);
		when(emd.getAttribute("String")).thenReturn(stringAttr);
		when(emd.getAttribute("NonNumericString")).thenReturn(nonNumericStringAttr);
		when(emd.getAttribute("Long")).thenReturn(longAttr);

		refEmd = when(mock(EntityMetaData.class).getName()).thenReturn("RefEntity").getMock();
		AttributeMetaData refIdAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("Identifier").getMock();
		when(refIdAttr.getDataType()).thenReturn(STRING);
		when(refIdAttr.getEntityMetaData()).thenReturn(mock(EntityMetaData.class));
		AttributeMetaData chromAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("Chromosome").getMock();
		when(chromAttr.getDataType()).thenReturn(STRING);
		when(chromAttr.getEntityMetaData()).thenReturn(mock(EntityMetaData.class));
		AttributeMetaData posAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("Position").getMock();
		when(posAttr.getDataType()).thenReturn(LONG);
		when(posAttr.getEntityMetaData()).thenReturn(mock(EntityMetaData.class));
		when(refEmd.getIdAttribute()).thenReturn(refIdAttr);
		when(refEmd.getAttribute("Identifier")).thenReturn(idAttr);
		when(refEmd.getAttribute("Chromosome")).thenReturn(idAttr);
		when(refEmd.getAttribute("Position")).thenReturn(idAttr);

		entity = new DynamicEntity(emd);
		entity.set("Int", 1);
		entity.set("String", "12");
		entity.set("Long", "10");
		entity.set("NonNumericString", "Hello World!");
	}

	@Test
	public void testMapOfStringsEvaluatorConstructorChecksIfAttributeHasExpression()
	{
		AttributeMetaData amd = when(mock(AttributeMetaData.class).getName()).thenReturn("#CHROM").getMock();
		when(amd.getDataType()).thenReturn(STRING);
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
		AttributeMetaData amd = when(mock(AttributeMetaData.class).getName()).thenReturn("location").getMock();
		when(amd.getDataType()).thenReturn(XREF);
		when(amd.getExpression()).thenReturn("{'a':b}");
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
		AttributeMetaData amd = when(mock(AttributeMetaData.class).getName()).thenReturn("location").getMock();
		when(amd.getDataType()).thenReturn(XREF);
		when(amd.getRefEntity()).thenReturn(refEmd);
		when(amd.getExpression()).thenReturn("hallo");
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
		AttributeMetaData amd = when(mock(AttributeMetaData.class).getName()).thenReturn("#CHROM").getMock();
		when(amd.getDataType()).thenReturn(XREF);
		when(amd.getRefEntity()).thenReturn(refEmd);
		when(amd.getExpression()).thenReturn("{'Chromosome':{'hallo1':'bla'}}");
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
		AttributeMetaData amd = when(mock(AttributeMetaData.class).getName()).thenReturn("#CHROM").getMock();
		when(amd.getDataType()).thenReturn(STRING);
		when(amd.getRefEntity()).thenReturn(refEmd);
		when(amd.getExpression()).thenReturn("{'hallo':String}");
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
		AttributeMetaData amd = when(mock(AttributeMetaData.class).getName()).thenReturn("#CHROM").getMock();
		when(amd.getDataType()).thenReturn(STRING);
		when(amd.getRefEntity()).thenReturn(refEmd);
		when(amd.getExpression()).thenReturn("{'Chromosome':hallo}");
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
		AttributeMetaData amd = when(mock(AttributeMetaData.class).getName()).thenReturn("#CHROM").getMock();
		when(amd.getDataType()).thenReturn(XREF);
		when(amd.getRefEntity()).thenReturn(refEmd);
		when(amd.getExpression()).thenReturn("{'Chromosome':String, 'Position':Int}");
		when(amd.getEntityMetaData()).thenReturn(mock(EntityMetaData.class));
		ExpressionEvaluator evaluator = new MapOfStringsExpressionEvaluator(amd, emd);
		Entity expected = new DynamicEntity(refEmd);
		expected.set("Chromosome", "12");
		expected.set("Position", 1L);
		assertEquals(evaluator.evaluate(entity), expected);
	}
}
