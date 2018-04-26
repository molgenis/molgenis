package org.molgenis.security.token;

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
