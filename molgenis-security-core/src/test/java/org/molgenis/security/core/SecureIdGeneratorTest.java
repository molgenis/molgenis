package org.molgenis.security.core;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class SecureIdGeneratorTest
{
	private final SecureIdGenerator secureIdGenerator = new SecureIdGeneratorImpl();

	@Test
	public void testGenerateId()
	{
		assertNotNull(secureIdGenerator.generateId());
	}

	@Test
	public void testGeneratePassword()
	{
		final String password = secureIdGenerator.generatePassword();
		assertNotNull(password);
		assertEquals(password.length(), 8);
	}

	@Test
	public void testGenerateActivationCode()
	{
		final String activationCode = secureIdGenerator.generateActivationCode();
		assertNotNull(activationCode);
	}
}
