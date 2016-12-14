package org.molgenis.util;

import junit.framework.Assert;
import org.testng.annotations.Test;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class MolgenisDateFormatTest
{
	@Test
	public void getDateFormat() throws ParseException
	{
		Date date = MolgenisDateFormat.getDateFormat().parse("2016-12-16");
		LocalDate actual = date.toInstant().atZone(ZoneId.of("UTC")).toLocalDate();
		Assert.assertEquals(LocalDate.of(2016, 12, 16), actual);
	}
}
