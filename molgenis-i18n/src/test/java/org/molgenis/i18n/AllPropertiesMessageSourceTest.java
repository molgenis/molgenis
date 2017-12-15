package org.molgenis.i18n;

import com.google.common.collect.ImmutableSetMultimap;
import org.molgenis.i18n.properties.AllPropertiesMessageSource;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Locale;

import static java.util.Locale.ENGLISH;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

public class AllPropertiesMessageSourceTest
{
	private AllPropertiesMessageSource propertiesMessageSource;
	private Locale DUTCH = new Locale("nl");

	@BeforeMethod
	public void setUp() throws Exception
	{
		propertiesMessageSource = new AllPropertiesMessageSource();
		propertiesMessageSource.addMolgenisNamespaces("test");
	}

	@AfterMethod
	public void tearDown() throws Exception
	{
	}

	@Test
	public void testGetAllMessageIds() throws Exception
	{
		assertEquals(propertiesMessageSource.getAllMessageIds(),
				ImmutableSetMultimap.of("test", "EN_ONLY", "test", "EN_PLUS_NL", "test", "BIOBANK_UTF8", "test",
						"NL_ONLY"));
	}

	@Test
	public void testGetMessageSpecifiedInBundle() throws Exception
	{
		assertEquals(propertiesMessageSource.resolveCodeWithoutArguments("NL_ONLY", DUTCH), "alleen Nederlands");
	}

	@Test
	public void testGetMessageSpecifiedInOtherBundle() throws Exception
	{
		assertNull(propertiesMessageSource.resolveCodeWithoutArguments("EN_ONLY", new Locale("nl")));
	}

	@Test
	public void testGetMessageNotSpecified() throws Exception
	{
		assertNull(propertiesMessageSource.resolveCodeWithoutArguments("MISSING", new Locale("nl")));
	}

	@Test
	public void testGetMessageSpecifiedInBoth() throws Exception
	{
		assertEquals(propertiesMessageSource.resolveCodeWithoutArguments("EN_PLUS_NL", ENGLISH), "English plus Dutch");
		assertEquals(propertiesMessageSource.resolveCodeWithoutArguments("EN_PLUS_NL", new Locale("nl")),
				"Engels plus Nederlands");
	}

	@Test
	public void testGetMessageUTF8()
	{
		assertEquals(propertiesMessageSource.resolveCodeWithoutArguments("BIOBANK_UTF8", ENGLISH),
				"Biøbånk\uD83D\uDC00");
	}

}