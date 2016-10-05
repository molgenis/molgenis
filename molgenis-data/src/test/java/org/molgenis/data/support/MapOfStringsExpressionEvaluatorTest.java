package org.molgenis.data.support;

import com.google.gson.JsonSyntaxException;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.SystemEntityMetaData;
import org.molgenis.data.meta.model.*;
import org.molgenis.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.MolgenisFieldTypes.AttributeType.*;
import static org.molgenis.data.meta.model.EntityMetaData.AttributeRole.ROLE_ID;
import static org.testng.Assert.*;

@ContextConfiguration(classes = { MapOfStringsExpressionEvaluatorTest.Config.class })
public class MapOfStringsExpressionEvaluatorTest extends AbstractTestNGSpringContextTests
{
	private Entity entity;
	private EntityMetaData emd;
	private EntityMetaData refEmd;

	@Autowired
	private EntityMetaDataFactory entityMetaDataFactory;
	@Autowired
	private AttributeMetaDataFactory attributeMetaDataFactory;

	private EntityMetaData createDynamicLocationMetaData()
	{
		return entityMetaDataFactory.create().setSimpleName("Location")
				.addAttribute(attributeMetaDataFactory.create().setName("Identifier").setDataType(STRING), ROLE_ID)
				.addAttribute(attributeMetaDataFactory.create().setName("Chromosome").setDataType(STRING))
				.addAttribute(attributeMetaDataFactory.create().setName("Position").setDataType(STRING));
	}

	private EntityMetaData createDynamicSourceMetaData()
	{
		return entityMetaDataFactory.create().setSimpleName("Source")
				.addAttribute(attributeMetaDataFactory.create().setName("Identifier").setDataType(STRING), ROLE_ID)
				.addAttribute(attributeMetaDataFactory.create().setName("Int").setDataType(INT))
				.addAttribute(attributeMetaDataFactory.create().setName("String").setDataType(STRING))
				.addAttribute(attributeMetaDataFactory.create().setName("NonNumericString").setDataType(STRING))
				.addAttribute(attributeMetaDataFactory.create().setName("Long").setDataType(LONG));
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
		when(amd.getDataType()).thenReturn(MolgenisFieldTypes.AttributeType.XREF);
		ExpressionEvaluator evaluator = new MapOfStringsExpressionEvaluator(amd, emd);
		Entity expected = new DynamicEntity(refEmd);
		expected.set("Chromosome", "12");
		expected.set("Position", "1");
		Entity actual = (Entity) evaluator.evaluate(entity);
		assertTrue(EntityUtils.equals(actual, expected));
	}

	@Autowired
	ApplicationContext applicationContext;

	@BeforeClass
	public void bootstrap()
	{
		// bootstrap meta data
		EntityTypeMetadata entityMetaMeta = applicationContext.getBean(EntityTypeMetadata.class);
		applicationContext.getBean(AttributeMetaDataMetaData.class).bootstrap(entityMetaMeta);
		Map<String, SystemEntityMetaData> systemEntityMetaMap = applicationContext
				.getBeansOfType(SystemEntityMetaData.class);
		systemEntityMetaMap.values().forEach(systemEntityMetaData -> systemEntityMetaData.bootstrap(entityMetaMeta));
	}

	@Configuration
	@ComponentScan({ "org.molgenis.data.meta.model", "org.molgenis.data.system.model", "org.molgenis.data.populate" })
	public static class Config
	{
	}
}
