package org.molgenis.data.convert;

import static org.testng.Assert.assertEquals;

import java.util.Date;

import org.molgenis.util.MolgenisDateFormat;
import org.testng.annotations.Test;

public class DateToStringConverterTest
{

	@Test
	public void convert()
	{
		Date d = new Date();
		String s = MolgenisDateFormat.getDateFormat().format(d);
		assertEquals(new DateToStringConverter().convert(d), s);
	}
}
