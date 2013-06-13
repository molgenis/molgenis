package org.molgenis.omx.utils;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

public class I18nToolsTest
{
	@Test
	public void getString_i18n()
	{
		assertEquals(I18nTools.get("{\"en\":\"car\", \"nl\":\"auto\"}"), "car");
	}

	@Test
	public void getString_plain()
	{
		assertEquals(I18nTools.get("plain"), "plain");
	}

	@Test
	public void getString_plainBraces()
	{
		assertEquals(I18nTools.get("{plain}"), "{plain}");
	}

	@Test
	public void getStringString_i18n()
	{
		assertEquals(I18nTools.get("{\"en\":\"car\", \"nl\":\"auto\"}", "en"), "car");
		assertEquals(I18nTools.get("{\"en\":\"car\", \"nl\":\"auto\"}", "nl"), "auto");
	}

	@Test
	public void getStringString_plain()
	{
		assertEquals(I18nTools.get("plain", "en"), "plain");
	}

	@Test
	public void getStringString_plainBraces()
	{
		assertEquals(I18nTools.get("{plain}", "en"), "{plain}");
	}
}
