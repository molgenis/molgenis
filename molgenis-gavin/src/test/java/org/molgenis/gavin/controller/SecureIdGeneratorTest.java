package org.molgenis.gavin.controller;

import org.testng.Assert;
import org.testng.annotations.Test;

public class SecureIdGeneratorTest
{
	private final SecureIdGenerator secureIdGenerator = new SecureIdGenerator();

	@Test
	public void testGenerateId()
	{
		Assert.assertNotNull(secureIdGenerator.generateId());
	}
}
