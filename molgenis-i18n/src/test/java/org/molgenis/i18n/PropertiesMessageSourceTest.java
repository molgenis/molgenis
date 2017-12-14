package org.molgenis.i18n;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class PropertiesMessageSourceTest
{
	PropertiesMessageSource propertiesMessageSource = new PropertiesMessageSource(" naMespace\t");

	@Test
	public void testGetNamespace()
	{
		assertEquals(propertiesMessageSource.getNamespace(), "namespace");
	}
}