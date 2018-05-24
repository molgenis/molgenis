package org.molgenis.data.rest.util;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;

public class HrefTest
{
	@Test
	public static void testEntityType()
	{
		Assert.assertEquals(Href.concatMetaEntityHref("http://molgenis.org/api/v1", "org_test_TypeTest"),
				"http://molgenis.org/api/v1/org_test_TypeTest/meta");
	}

	@Test
	public static void testEntityTypeV2()
	{
		Assert.assertEquals(Href.concatMetaEntityHrefV2("http://molgenis.org/api/v2", "org_test_TypeTest"),
				"http://molgenis.org/api/v2/org_test_TypeTest");
	}

	@Test
	public static void testEntity()
	{
		Assert.assertEquals(Href.concatEntityHref("http://molgenis.org/api/v1", "org_test_TypeTest", "1"),
				"http://molgenis.org/api/v1/org_test_TypeTest/1");
	}

	@Test
	public static void testEntityUrlEncodeIdWithSpace()
	{
		Assert.assertEquals(Href.concatEntityHref("http://molgenis.org/api/v1", "org_test_TypeTest", "weird id"),
				"http://molgenis.org/api/v1/org_test_TypeTest/weird%20id");
	}

	@Test
	public static void testEntityUrlEncodeIdWithSlash()
	{
		Assert.assertEquals(Href.concatEntityHref("http://molgenis.org/api/v1", "org_test_TypeTest", "weird/id"),
				"http://molgenis.org/api/v1/org_test_TypeTest/weird%2Fid");
	}

	@Test
	public static void testAttribute()
	{
		Assert.assertEquals(Href.concatMetaAttributeHref("http://molgenis.org/api/v1", "org_test_TypeTest", "xint"),
				"http://molgenis.org/api/v1/org_test_TypeTest/meta/xint");
	}

	@Test
	public static void testAttributeEntity()
	{
		Assert.assertEquals(Href.concatAttributeHref("http://molgenis.org/api/v1", "org_test_TypeTest", "1", "xint"),
				"http://molgenis.org/api/v1/org_test_TypeTest/1/xint");
	}

	@Test
	public static void testAttributeUrlEncodeIdWithSpace()
	{
		Assert.assertEquals(
				Href.concatAttributeHref("http://molgenis.org/api/v1", "org_test_TypeTest", "weird id", "xint"),
				"http://molgenis.org/api/v1/org_test_TypeTest/weird%20id/xint");
	}

	@Test
	public static void testAttributeUrlEncodeIdWithSlash()
	{
		Assert.assertEquals(
				Href.concatAttributeHref("http://molgenis.org/api/v1", "org_test_TypeTest", "weird/id", "xint"),
				"http://molgenis.org/api/v1/org_test_TypeTest/weird%2Fid/xint");
	}

	@Test
	public static void testConcatEntityCollectionHref()
	{
		Assert.assertEquals(Href.concatEntityCollectionHref("http://molgenis.org/api/v2", "entity", "id",
				Arrays.asList("p1", "p2")), "http://molgenis.org/api/v2/entity?q=id=in=(\"p1\",\"p2\")");
	}
}
