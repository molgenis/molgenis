package org.molgenis.data.rest;

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

}
