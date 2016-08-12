package org.molgenis.data.convert;

import org.molgenis.util.MolgenisDateFormat;
import org.testng.annotations.Test;

import java.util.Date;

import static org.testng.Assert.assertEquals;

public class DateToStringConverterTest
{

	@Test
	public void convert()
	{
		Date d = new Date();
		String s = MolgenisDateFormat.getDateTimeFormat().format(d);
		assertEquals(new DateToStringConverter().convert(d), s);
	}
}
