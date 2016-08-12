package org.molgenis.data.convert;

import org.molgenis.util.MolgenisDateFormat;
import org.testng.annotations.Test;

import java.util.Date;

import static org.testng.Assert.assertEquals;

public class StringToDateConverterTest
{
	@Test
	public void convert()
	{
		Date d = new Date();
		String s = MolgenisDateFormat.getDateTimeFormat().format(d);
		// close within 24h*60min*60sec*1000ms (as we round of to a day)
		assertEquals(new StringToDateConverter().convert(s).getTime(), d.getTime(), 24 * 60 * 60 * 1000);
	}

	@Test
	// (expectedExceptions = IllegalArgumentException.class)
	public void convertDateWrongFormat()
	{
		StringToDateConverter stringToDateConverter = new StringToDateConverter();
		stringToDateConverter.convert("2014-12-31");
	}

	@Test
	public void convertDate()
	{
		StringToDateConverter stringToDateConverter = new StringToDateConverter();
		stringToDateConverter.convert("2014-12-31T00:00:00+0100");
	}
}
