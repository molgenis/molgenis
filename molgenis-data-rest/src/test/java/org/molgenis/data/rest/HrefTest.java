package org.molgenis.data.rest;

import java.util.Arrays;

import org.testng.Assert;
import org.testng.annotations.Test;

public class HrefTest
{
	@Test
	public static void testAttribute()
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
