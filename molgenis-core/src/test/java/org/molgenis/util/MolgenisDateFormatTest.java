package org.molgenis.util;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.testng.Assert.assertEquals;

public class MolgenisDateFormatTest
{
	@Test
	public void testFormatLocalDate()
	{
		assertEquals(MolgenisDateFormat.getLocalDateFormatter().format(LocalDate.parse("2014-12-31")), "2014-12-31");
	}

	@Test
	public void testFormatInstant()
	{
		assertEquals(MolgenisDateFormat.getDateTimeFormatter().format(Instant.parse("2000-12-31T12:34:56.789Z")),
				"2000-12-31T12:34:56.789Z");
	}

	@Test
	public void testGetDefaultZoneId()
	{
		assertEquals(MolgenisDateFormat.getDefaultZoneId(), ZoneId.of("Europe/Amsterdam"));
	}

	@DataProvider(name = "parseInstantDataProvider")
	public Object[][] parseInstantDataProvider()
	{
		return new Object[][] { { "2000-12-31T12:34:56.789+0200", "2000-12-31T10:34:56.789Z" },
				{ "2000-12-31T12:34:56.789+02:00", "2000-12-31T10:34:56.789Z" },
				{ "2000-12-31T12:34:56.789", "2000-12-31T11:34:56.789Z" },
				{ "2000-12-31T12:34", "2000-12-31T11:34:00Z" }, { "2000-12-31T12:34Z", "2000-12-31T12:34:00Z" },
				{ "2000-12-31", "2000-12-30T23:00:00Z" }, };
	}

	@Test(dataProvider = "parseInstantDataProvider")
	public void testParseInstant(String text, String expected)
	{
		assertEquals(MolgenisDateFormat.parseInstant(text), Instant.parse(expected));
	}

	@DataProvider(name = "parseLocalDateDataProvider")
	public Object[][] parseLocalDateDataProvider()
	{
		return new Object[][] { { "2000-12-31T00:34:56.789+0200", "2000-12-31" },
				{ "2000-12-31T00:34:56.789+02:00", "2000-12-31" }, { "2000-12-31T00:00:00+0200", "2000-12-31" },
				{ "2000-12-31T23:00:00Z", "2000-12-31" }, { "2000-12-31T00:34", "2000-12-31" },
				{ "2000-12-31Z", "2000-12-31" } };
	}

	@Test(dataProvider = "parseLocalDateDataProvider")
	public void testParseLocalDate(String text, String expected)
	{
		assertEquals(MolgenisDateFormat.parseLocalDate(text), LocalDate.parse(expected));
	}
}
