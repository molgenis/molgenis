package org.molgenis.data;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

public class DataConverterTest
{
	@Test
	public void convert()
	{
		assertEquals(DataConverter.convert("test", String.class), "test");
		assertEquals(DataConverter.convert(5L, Number.class).longValue(), 5L);
	}
}
