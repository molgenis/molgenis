package org.molgenis.security.core.service.impl;

import org.molgenis.security.core.service.impl.TokenGenerator;
import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;

public class TokenGeneratorTest
{

	@Test
	public void generateToken()
	{
		TokenGenerator tg = new TokenGenerator();
		String token = tg.generateToken();
		assertNotNull(token);
		assertFalse(token.contains("-"));
		assertFalse(tg.generateToken().equals(token));
	}
}
