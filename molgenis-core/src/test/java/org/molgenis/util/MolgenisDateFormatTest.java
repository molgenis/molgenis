package org.molgenis.util;

import junit.framework.Assert;
import org.testng.annotations.Test;

import java.text.ParseException;
import java.util.Date;

public class MolgenisDateFormatTest
{
	@Test
	public void formatDate() throws ParseException
	{
		Date date = new Date(1480582682188l);
		Date actual = MolgenisDateFormat.formatDate(date);
		Assert.assertFalse(date.equals(actual));
		Assert.assertEquals(1480546800000l, actual.getTime());
	}

	@Test
	public void formatDateTime() throws ParseException
	{
		Date date = new Date(1480582682188l);
		Date actual = MolgenisDateFormat.formatDateTime(date);
		Assert.assertFalse(date.equals(actual));
		Assert.assertEquals(1480582682000l, actual.getTime());
	}
}
