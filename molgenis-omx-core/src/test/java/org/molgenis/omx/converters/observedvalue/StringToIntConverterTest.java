package org.molgenis.omx.converters.observedvalue;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class StringToIntConverterTest
{
	private StringToIntConverter converter;

	@BeforeMethod
	public void beforeMethod()
	{
		converter = new StringToIntConverter();
	}

	@Test
	public void fromString()
	{
		assertEquals(converter.fromString("2", null, null), new Integer(2));
	}

	@Test
	public void fromStringNullValue()
	{
		assertNull(converter.fromString(null, null, null));
	}

	@Test(expectedExceptions = NumberFormatException.class)
	public void fromStringIllegalValue()
	{
		converter.fromString("2.9", null, null);
	}
}
