package org.molgenis.fieldtypes;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.math.BigDecimal;

import org.testng.annotations.Test;

public class DecimalFieldTest
{
	@Test
	public void convert_fromString()
	{
		Object obj = new DecimalField().convert("1");
		assertTrue(obj instanceof Double);
		assertEquals(((Double) obj).doubleValue(), 1.0, 1E-6);
	}

	@Test
	public void convert_fromDouble()
	{
		Object obj = new DecimalField().convert(1.0);
		assertTrue(obj instanceof Double);
		assertEquals(((Double) obj).doubleValue(), 1.0, 1E-6);
	}

	@Test
	public void convert_fromInteger()
	{
		Object obj = new DecimalField().convert(Integer.valueOf(1));
		assertTrue(obj instanceof Double);
		assertEquals(((Double) obj).doubleValue(), 1.0, 1E-6);
	}

	@Test
	public void convert_fromBigDecimal()
	{
		Object obj = new DecimalField().convert(BigDecimal.valueOf(2.9));
		assertTrue(obj instanceof Double);
		assertEquals(((Double) obj).doubleValue(), 2.9, 1E-6);
	}
}
