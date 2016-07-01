package org.molgenis.data.i18n;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class I18nUtilsTest
{

	@Test
	public void isI18n()
	{
		assertTrue(I18nUtils.isI18n("test-nl"));
		assertTrue(I18nUtils.isI18n("test-xxx"));
		assertFalse(I18nUtils.isI18n("test"));
		assertFalse(I18nUtils.isI18n("test-NL"));
		assertFalse(I18nUtils.isI18n("test-nlnl"));
		assertFalse(I18nUtils.isI18n("test-"));
		assertFalse(I18nUtils.isI18n("test-n1"));
		assertFalse(I18nUtils.isI18n("-nl"));
	}

	@Test
	public void getLanguageCode()
	{
		assertEquals(I18nUtils.getLanguageCode("test-nl"), "nl");
		assertEquals(I18nUtils.getLanguageCode("test-xxx"), "xxx");
		assertNull(I18nUtils.getLanguageCode("test"));
	}

}
