package org.molgenis.data.convert;

import static org.testng.Assert.assertEquals;

import java.util.Date;

import org.molgenis.util.MolgenisDateFormat;
import org.testng.annotations.Test;

public class StringToDateConverterTest
{
	@Test
	public void convert()
	{
		Date d = new Date();
		String s = MolgenisDateFormat.getDateFormat().format(d);
		assertEquals(new StringToDateConverter().convert(s).getTime(), d.getTime(), 1000);
	}
	
	@Test(expectedExceptions = IllegalArgumentException.class)
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
