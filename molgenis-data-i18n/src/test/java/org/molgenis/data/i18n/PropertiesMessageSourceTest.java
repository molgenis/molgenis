package org.molgenis.data.i18n;

import com.google.common.collect.Sets;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class PropertiesMessageSourceTest
{
	private PropertiesMessageSource propertiesMessageSource;

	@BeforeMethod
	public void setUp() throws Exception
	{
		propertiesMessageSource = new PropertiesMessageSource("test");
	}

	@AfterMethod
	public void tearDown() throws Exception
	{
	}

	@Test
	public void testGetNamespace() throws Exception
	{
		assertEquals(propertiesMessageSource.getNamespace(), "test");
	}

	@Test
	public void testGetMessageIDs() throws Exception
	{
		assertEquals(propertiesMessageSource.getMessageIDs(),
				Sets.newHashSet("EN_ONLY", "EN_PLUS_NL", "NL_ONLY", "BIOBANK_UTF8"));
	}

	@Test
	public void testGetMessageSpecifiedInBundle() throws Exception
	{
		assertEquals(propertiesMessageSource.getMessage("nl", "NL_ONLY"), "alleen Nederlands");
	}

	@Test
	public void testGetMessageSpecifiedInOtherBundle() throws Exception
	{
		assertNull(propertiesMessageSource.getMessage("nl", "EN_ONLY"));
	}

	@Test
	public void testGetMessageNotSpecified() throws Exception
	{
		assertNull(propertiesMessageSource.getMessage("nl", "MISSING"));
	}

	@Test
	public void testGetMessageSpecifiedInBoth() throws Exception
	{
		assertEquals(propertiesMessageSource.getMessage("en", "EN_PLUS_NL"), "English plus Dutch");
		assertEquals(propertiesMessageSource.getMessage("nl", "EN_PLUS_NL"), "Engels plus Nederlands");
	}

	@Test
	public void testGetMessageUTF8()
	{
		assertEquals(propertiesMessageSource.getMessage("en", "BIOBANK_UTF8"), "Biøbånk\uD83D\uDC00");
	}

}