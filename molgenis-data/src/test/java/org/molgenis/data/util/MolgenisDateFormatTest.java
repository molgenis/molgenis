package org.molgenis.data.util;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static java.time.ZoneId.systemDefault;

public class MolgenisDateFormatTest
{
	@DataProvider(name = "parseInstantDataProvider")
	public Object[][] parseInstantDataProvider()
	{
		return new Object[][] { { "2000-12-31T12:34:56.789+0200", "2000-12-31T10:34:56.789Z" },
				{ "2000-12-31T12:34:56.789+02:00", "2000-12-31T10:34:56.789Z" }, { "2000-12-31T12:34:56.789",
				LocalDateTime.parse("2000-12-31T12:34:56.789").atZone(systemDefault()).toInstant().toString() },
				{ "2000-12-31T12:34",
						LocalDateTime.parse("2000-12-31T12:34").atZone(systemDefault()).toInstant().toString() },
				{ "2000-12-31T12:34Z", "2000-12-31T12:34:00Z" },
				{ "2000-12-31", LocalDate.parse("2000-12-31").atStartOfDay(systemDefault()).toInstant().toString() }, };
	}

	@Test(dataProvider = "parseInstantDataProvider")
	public void testParseInstant(String text, String expected)
	{
		Assert.assertEquals(MolgenisDateFormat.parseInstant(text), Instant.parse(expected));
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
		Assert.assertEquals(MolgenisDateFormat.parseLocalDate(text), LocalDate.parse(expected));
	}
}
