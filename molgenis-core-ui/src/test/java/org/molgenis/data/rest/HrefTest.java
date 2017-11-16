package org.molgenis.data.rest;

import org.molgenis.data.support.Href;
import org.testng.annotations.Test;

import java.util.Arrays;

import static org.testng.Assert.assertEquals;

public class HrefTest
{
	@Test
	public static void testEntityType()
	{
		assertEquals(Href.concatMetaEntityHref("http://molgenis.org/api/v1", "org_test_TypeTest"),
				"http://molgenis.org/api/v1/org_test_TypeTest/meta");
	}

	@Test
	public static void testEntityTypeV2()
	{
		assertEquals(Href.concatMetaEntityHrefV2("http://molgenis.org/api/v2", "org_test_TypeTest"),
				"http://molgenis.org/api/v2/org_test_TypeTest");
	}

	@Test
	public static void testEntity()
	{
		assertEquals(Href.concatEntityHref("http://molgenis.org/api/v1", "org_test_TypeTest", "1"),
				"http://molgenis.org/api/v1/org_test_TypeTest/1");
	}

	@Test
	public static void testEntityUrlEncodeIdWithSpace()
	{
		assertEquals(Href.concatEntityHref("http://molgenis.org/api/v1", "org_test_TypeTest", "weird id"),
				"http://molgenis.org/api/v1/org_test_TypeTest/weird%20id");
	}

	@Test
	public static void testEntityUrlEncodeIdWithSlash()
	{
		assertEquals(Href.concatEntityHref("http://molgenis.org/api/v1", "org_test_TypeTest", "weird/id"),
				"http://molgenis.org/api/v1/org_test_TypeTest/weird%2Fid");
	}

	@Test
	public static void testAttribute()
	{
		assertEquals(Href.concatMetaAttributeHref("http://molgenis.org/api/v1", "org_test_TypeTest", "xint"),
				"http://molgenis.org/api/v1/org_test_TypeTest/meta/xint");
	}

	@Test
	public static void testAttributeEntity()
	{
		assertEquals(Href.concatAttributeHref("http://molgenis.org/api/v1", "org_test_TypeTest", "1", "xint"),
				"http://molgenis.org/api/v1/org_test_TypeTest/1/xint");
	}

	@Test
	public static void testAttributeUrlEncodeIdWithSpace()
	{
		assertEquals(
				Href.concatAttributeHref("http://molgenis.org/api/v1", "org_test_TypeTest", "weird id", "xint"),
				"http://molgenis.org/api/v1/org_test_TypeTest/weird%20id/xint");
	}

	@Test
	public static void testAttributeUrlEncodeIdWithSlash()
	{
		assertEquals(
				Href.concatAttributeHref("http://molgenis.org/api/v1", "org_test_TypeTest", "weird/id", "xint"),
				"http://molgenis.org/api/v1/org_test_TypeTest/weird%2Fid/xint");
	}

	@Test
	public static void testConcatEntityCollectionHref()
	{
		assertEquals(Href.concatEntityCollectionHref("http://molgenis.org/api/v2", "entity", "id",
				Arrays.asList("p1", "p2")), "http://molgenis.org/api/v2/entity?q=id=in=(\"p1\",\"p2\")");
	}
}
