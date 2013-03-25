package org.molgenis.omx.converters.observedvalue;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

public class StringToBooleanConverterTest
{
	@Test
	public void fromString()
	{
		StringToBooleanConverter converter = new StringToBooleanConverter();
		assertNull(converter.fromString(null, null, null));
		assertTrue(converter.fromString("true", null, null));
		assertTrue(converter.fromString("True", null, null));
		assertTrue(converter.fromString("Yes", null, null));
		assertTrue(converter.fromString("y", null, null));
		assertTrue(converter.fromString("J", null, null));
		assertTrue(converter.fromString("j", null, null));
		assertTrue(converter.fromString("Ja", null, null));
		assertTrue(converter.fromString("1", null, null));

		assertFalse(converter.fromString("false", null, null));
		assertFalse(converter.fromString("0", null, null));
		assertFalse(converter.fromString("xxx", null, null));
		assertFalse(converter.fromString("", null, null));
	}
}
