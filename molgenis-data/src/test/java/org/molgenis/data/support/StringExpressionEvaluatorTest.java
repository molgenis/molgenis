package org.molgenis.data.support;

public class StringExpressionEvaluatorTest
{
	//	private Entity entity;
	//	private EntityMetaData emd;
	//
	//	@BeforeTest
	//	public void createEntity()
	//	{
	//		emd = new EntityMetaData("Source");
	//		emd.addAttribute(new AttributeMetaData("Identifier").setDataType(new IntField()), ROLE_ID);
	//		emd.addAttribute(new AttributeMetaData("Int").setDataType(new IntField()));
	//		emd.addAttribute(new AttributeMetaData("String").setDataType(new StringField()));
	//		emd.addAttribute(new AttributeMetaData("NonNumericString").setDataType(new StringField()));
	//		emd.addAttribute(new AttributeMetaData("Long").setDataType(new LongField()));
	//		entity = new MapEntity(emd);
	//		entity.set("Int", 1);
	//		entity.set("String", "12");
	//		entity.set("Long", 10L);
	//		entity.set("NonNumericString", "Hello World!");
	//	}
	//
	//	@Test
	//	public void testStringEvaluatorConstructorChecksIfAttributeHasExpression()
	//	{
	//		AttributeMetaData amd = new AttributeMetaData("#CHROM").setDataType(new StringField());
	//		try
	//		{
	//			new StringExpressionEvaluator(amd, emd);
	//			fail("Expected NPE");
	//		}
	//		catch (NullPointerException expected)
	//		{
	//			assertEquals(expected.getMessage(), "Attribute has no expression.");
	//		}
	//
	//	}
	//
	//	@Test
	//	public void testStringEvaluatorConstructorChecksIfExpressionIsMap()
	//	{
	//		AttributeMetaData amd = new AttributeMetaData("#CHROM").setDataType(new StringField())
	//				.setExpression("{}");
	//		try
	//		{
	//			new StringExpressionEvaluator(amd, emd);
	//			fail("expected illegal state exception");
	//		}
	//		catch (JsonSyntaxException expected)
	//		{
	//		}
	//
	//	}
	//
	//	@Test
	//	public void testStringEvaluatorConstructorChecksIfAttributeMentionsExistingAttribute()
	//	{
	//		AttributeMetaData amd = new AttributeMetaData("#CHROM").setDataType(new StringField())
	//				.setExpression("bogus");
	//		try
	//		{
	//			new StringExpressionEvaluator(amd, emd);
	//			fail("expected illegal argument exception");
	//		}
	//		catch (IllegalArgumentException expected)
	//		{
	//			assertEquals(expected.getMessage(),
	//					"Expression for attribute '#CHROM' references non-existant attribute 'bogus'.");
	//		}
	//
	//	}
	//
	//	@Test
	//	public void testStringEvaluatorLookupAttributeAndConvertFromIntToString()
	//	{
	//		AttributeMetaData amd = new AttributeMetaData("#CHROM").setDataType(new StringField())
	//				.setExpression("Int");
	//		assertEquals(new StringExpressionEvaluator(amd, emd).evaluate(entity), "1");
	//	}
	//
	//	@Test
	//	public void testStringEvaluatorLookupAttributeAndConvertFromIntToLong()
	//	{
	//		AttributeMetaData amd = new AttributeMetaData("#POS").setDataType(new LongField()).setExpression("Int");
	//		assertEquals(new StringExpressionEvaluator(amd, emd).evaluate(entity), 1L);
	//	}
	//
	//	@Test
	//	public void testStringEvaluatorLookupAttributeAndConvertFromLongToInt()
	//	{
	//		AttributeMetaData amd = new AttributeMetaData("#POS").setDataType(new IntField()).setExpression("Long");
	//		assertEquals(new StringExpressionEvaluator(amd, emd).evaluate(entity), 10);
	//	}
	//
	//	@Test
	//	public void testStringEvaluatorLookupAttributeAndConvertFromStringToLong()
	//	{
	//		AttributeMetaData amd = new AttributeMetaData("#POS").setDataType(new LongField())
	//				.setExpression("String");
	//		assertEquals(new StringExpressionEvaluator(amd, emd).evaluate(entity), 12L);
	//	}
	//
	//	@Test
	//	public void testStringEvaluatorLookupAttributeAndConvertFromNonNumericStringToLongFails()
	//	{
	//
	//		AttributeMetaData amd = new AttributeMetaData("#POS").setDataType(new LongField())
	//				.setExpression("NonNumericString");
	//		try
	//		{
	//			assertEquals(new StringExpressionEvaluator(amd, emd).evaluate(entity), 12L);
	//			fail("Expected ConversionFailedException.");
	//		}
	//		catch (ConversionFailedException expected)
	//		{
	//			assertEquals(expected.getMessage(),
	//					"Failed to convert from type java.lang.String to type java.lang.Long for value 'Hello World!'; nested exception is java.lang.NumberFormatException: For input string: \"HelloWorld!\"");
	//		}
	//	}
}
