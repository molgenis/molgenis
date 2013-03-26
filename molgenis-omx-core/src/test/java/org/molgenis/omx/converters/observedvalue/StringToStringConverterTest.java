package org.molgenis.omx.converters.observedvalue;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class StringToStringConverterTest
{
	private StringToStringConverter converter;

	@BeforeMethod
	public void beforeMethod()
	{
		converter = new StringToStringConverter();
	}

	@Test
	public void fromString()
	{
		assertEquals(converter.fromString("2", null, null), "2");
	}

	@Test
	public void fromStringNullValue()
	{
		assertNull(converter.fromString(null, null, null));
	}
}
