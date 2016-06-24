package org.molgenis.security.token;

import java.util.UUID;

/**
 * Generates a secure token
 */
public class TokenGenerator
{
	public String generateToken()
	{
		return UUID.randomUUID().toString().replace("-", "");
	}
}
