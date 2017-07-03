package org.molgenis.data.rest.v2;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class AttributeFilterConverterTest
{

	private AttributeFilterConverter attributeFilterConverter;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		attributeFilterConverter = new AttributeFilterConverter();
	}

	@Test
	public void convertSingleAttribute()
	{
		AttributeFilter attributeFilter = new AttributeFilter().add("attr0");
		assertEquals(attributeFilterConverter.convert("attr0"), attributeFilter);
	}

	@Test
	public void convertSingleAttributeEscapeComma()
	{
		AttributeFilter attributeFilter = new AttributeFilter().add("attr,0");
		assertEquals(attributeFilterConverter.convert("attr\\,0"), attributeFilter);
	}

	// @Test
	// public void convertSingleAttributeEscapeSlash()
	// {
	// AttributeFilter attributeFilter = new AttributeFilter().add("attr/0");
	// assertEquals(attributeFilterConverter.convert("attr//0"), attributeFilter);
	// }

	@Test
	public void convertSingleAttributeEscapeParenthesisLeft()
	{
		AttributeFilter attributeFilter = new AttributeFilter().add("attr(0");
		assertEquals(attributeFilterConverter.convert("attr\\(0"), attributeFilter);
	}

	@Test
	public void convertSingleAttributeEscapeParenthesisRight()
	{
		AttributeFilter attributeFilter = new AttributeFilter().add("attr)0");
		assertEquals(attributeFilterConverter.convert("attr\\)0"), attributeFilter);
	}

	// @Test
	// public void convertSingleAttributeSubSelectionSingleAttributeSlash()
	// {
	// AttributeFilter attributeFilter = new AttributeFilter().add("attr0", new AttributeFilter().add("subattr0"));
	// assertEquals(attributeFilterConverter.convert("attr0/subattr0"), attributeFilter);
	// }

	@Test
	public void convertSingleAttributeSubSelectionSingleAttributeParenthesis()
	{
		AttributeFilter attributeFilter = new AttributeFilter().add("attr0", new AttributeFilter().add("subattr0"));
		assertEquals(attributeFilterConverter.convert("attr0(subattr0)"), attributeFilter);
	}

	@Test
	public void convertSingleAttributeSubSelectionSingleAttributeParenthesisWildcard()
	{
		AttributeFilter attributeFilter = new AttributeFilter().add("attr0",
				new AttributeFilter().setIncludeAllAttrs(true));
		assertEquals(attributeFilterConverter.convert("attr0(*)"), attributeFilter);
	}

	@Test
	public void convertSingleAttributeSubSelectionMultipleAttributes()
	{
		AttributeFilter attributeFilter = new AttributeFilter().add("attr0",
				new AttributeFilter().add("subattr0").add("subattr1"));
		assertEquals(attributeFilterConverter.convert("attr0(subattr0,subattr1)"), attributeFilter);
	}

	@Test
	public void convertSingleAttributeSubSelectionMultipleAttributesWildcard1()
	{
		AttributeFilter attributeFilter = new AttributeFilter().add("attr0",
				new AttributeFilter().add("subattr0").setIncludeAllAttrs(true));
		assertEquals(attributeFilterConverter.convert("attr0(subattr0,*)"), attributeFilter);
	}

	@Test
	public void convertSingleAttributeSubSelectionMultipleAttributesWildcard2()
	{
		AttributeFilter attributeFilter = new AttributeFilter().add("attr0",
				new AttributeFilter().add("subattr1").setIncludeAllAttrs(true));
		assertEquals(attributeFilterConverter.convert("attr0(*,subattr1)"), attributeFilter);
	}

	@Test
	public void convertSingleAttributeSubSubSelection()
	{
		AttributeFilter attributeFilter = new AttributeFilter().add("attr0",
				new AttributeFilter().add("subattr0", new AttributeFilter().add("subsubattr0")));
		assertEquals(attributeFilterConverter.convert("attr0(subattr0(subsubattr0)"), attributeFilter);
	}

	@Test
	public void convertMultipleAttributes()
	{
		AttributeFilter attributeFilter = new AttributeFilter().add("attr0").add("attr1");
		assertEquals(attributeFilterConverter.convert("attr0,attr1"), attributeFilter);
	}

	// @Test
	// public void convertMultipleAttributesSubSelectionSingleAttributeSlash()
	// {
	// AttributeFilter attributeFilter = new AttributeFilter().add("attr0").add("attr1",
	// new AttributeFilter().add("subattr1"));
	// assertEquals(attributeFilterConverter.convert("attr0,attr1/subattr1"), attributeFilter);
	// }

	@Test
	public void convertMultipleAttributesSubSelectionSingleAttributeParenthesis()
	{
		AttributeFilter attributeFilter = new AttributeFilter().add("attr0")
															   .add("attr1", new AttributeFilter().add("subattr1"));
		assertEquals(attributeFilterConverter.convert("attr0,attr1(subattr1)"), attributeFilter);
	}

	@Test
	public void convertMultipleAttributesSubSelectionSingleAttributeParenthesisWildcard()
	{
		AttributeFilter attributeFilter = new AttributeFilter().add("attr0")
															   .add("attr1",
																	   new AttributeFilter().setIncludeAllAttrs(true));
		assertEquals(attributeFilterConverter.convert("attr0,attr1(*)"), attributeFilter);
	}

	@Test
	public void convertMultipleAttributesSubSelectionMultipleAttributes()
	{
		AttributeFilter attributeFilter = new AttributeFilter().add("attr0")
															   .add("attr1", new AttributeFilter().add("subattr1")
																								  .add("subattr2"));
		assertEquals(attributeFilterConverter.convert("attr0,attr1(subattr1,subattr2)"), attributeFilter);
	}

	@Test
	public void convertMultipleAttributesSubSelectionMultipleAttributesWildCard1()
	{
		AttributeFilter attributeFilter = new AttributeFilter().add("attr0")
															   .add("attr1", new AttributeFilter().add("subattr1")
																								  .setIncludeAllAttrs(
																										  true));
		assertEquals(attributeFilterConverter.convert("attr0,attr1(subattr1,*)"), attributeFilter);
	}

	@Test
	public void convertMultipleAttributesSubSelectionMultipleAttributesWildCard2()
	{
		AttributeFilter attributeFilter = new AttributeFilter().add("attr0")
															   .add("attr1", new AttributeFilter().add("subattr2")
																								  .setIncludeAllAttrs(
																										  true));
		assertEquals(attributeFilterConverter.convert("attr0,attr1(*,subattr2)"), attributeFilter);
	}
}
