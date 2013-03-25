package org.molgenis.omx.converters.observedvalue;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class StringToDoubleConverterTest
{
	private StringToDoubleConverter converter;

	@BeforeMethod
	public void beforeMethod()
	{
		converter = new StringToDoubleConverter();
	}

	@Test
	public void fromString()
	{
		assertEquals(converter.fromString("2", null, null), 2.0);
	}

	@Test
	public void fromStringNullValue()
	{
		assertNull(converter.fromString(null, null, null));
	}

	@Test(expectedExceptions = NumberFormatException.class)
	public void fromStringIllegalValue()
	{
		converter.fromString("2x", null, null);
	}
}
