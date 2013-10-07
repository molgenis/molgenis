package org.molgenis.data;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Date;

import org.springframework.core.convert.ConversionService;
import org.testng.annotations.Test;

public class DataConfigTest
{
	@Test
	public void converionService()
	{
		ConversionService conversionService = new DataConfig().converionService();
		assertTrue(conversionService.canConvert(String.class, Date.class));
		assertTrue(conversionService.canConvert(Date.class, String.class));
		assertEquals(conversionService.convert("1", Integer.class), Integer.valueOf(1));
	}
}
