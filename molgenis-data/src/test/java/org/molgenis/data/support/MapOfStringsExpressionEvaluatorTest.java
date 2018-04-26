package org.molgenis.data.support;

import com.google.gson.JsonSyntaxException;
import org.mockito.quality.Strictness;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.AttributeType.*;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.testng.Assert.*;

@ContextConfiguration(classes = { MapOfStringsExpressionEvaluatorTest.Config.class })
public class MapOfStringsExpressionEvaluatorTest extends AbstractMolgenisSpringTest
{
	private Entity entity;
	private EntityType emd;
	private EntityType refEmd;

	@Autowired
	private EntityTypeFactory entityTypeFactory;
	@Autowired
	private AttributeFactory attributeFactory;

	public MapOfStringsExpressionEvaluatorTest()
	{
		super(Strictness.WARN);
	}

	private EntityType createDynamicLocationMetaData()
	{
		return entityTypeFactory.create("Location")
								.addAttribute(attributeFactory.create().setName("Identifier").setDataType(STRING),
										ROLE_ID)
								.addAttribute(attributeFactory.create().setName("Chromosome").setDataType(STRING))
								.addAttribute(attributeFactory.create().setName("Position").setDataType(STRING));
	}

	private EntityType createDynamicSourceMetaData()
	{
		return entityTypeFactory.create("Source")
								.addAttribute(attributeFactory.create().setName("Identifier").setDataType(STRING),
										ROLE_ID)
								.addAttribute(attributeFactory.create().setName("Int").setDataType(INT))
								.addAttribute(attributeFactory.create().setName("String").setDataType(STRING))
								.addAttribute(attributeFactory.create().setName("NonNumericString").setDataType(STRING))
								.addAttribute(attributeFactory.create().setName("Long").setDataType(LONG));
	}

	@BeforeMethod
	public void createEntity()
	{
		emd = createDynamicSourceMetaData();
		refEmd = createDynamicLocationMetaData();

		entity = new DynamicEntity(emd);
		entity.set("Int", 1);
		entity.set("String", "12");
		entity.set("Long", 10L);
		entity.set("NonNumericString", "Hello World!");
	}

	@Test
	public void testMapOfStringsEvaluatorConstructorChecksIfAttributeHasExpression()
	{
		Attribute amd = when(mock(Attribute.class).getName()).thenReturn("#CHROM").getMock();
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
		Attribute amd = when(mock(Attribute.class).getName()).thenReturn("location").getMock();
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
		Attribute amd = when(mock(Attribute.class).getName()).thenReturn("location").getMock();
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
		Attribute amd = when(mock(Attribute.class).getName()).thenReturn("#CHROM").getMock();
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
		Attribute amd = when(mock(Attribute.class).getName()).thenReturn("#CHROM").getMock();
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
		Attribute amd = when(mock(Attribute.class).getName()).thenReturn("#CHROM").getMock();
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
		Attribute amd = when(mock(Attribute.class).getName()).thenReturn("#CHROM").getMock();
		when(amd.getDataType()).thenReturn(XREF);
		when(amd.getRefEntity()).thenReturn(refEmd);
		when(amd.getExpression()).thenReturn("{'Chromosome':String, 'Position':Int}");
		when(amd.getEntityType()).thenReturn(mock(EntityType.class));
		when(amd.getDataType()).thenReturn(XREF);
		ExpressionEvaluator evaluator = new MapOfStringsExpressionEvaluator(amd, emd);
		Entity expected = new DynamicEntity(refEmd);
		expected.set("Chromosome", "12");
		expected.set("Position", "1");
		Entity actual = (Entity) evaluator.evaluate(entity);
		assertTrue(EntityUtils.equals(actual, expected));
	}
}
