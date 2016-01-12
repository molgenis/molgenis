package org.molgenis.integrationtest.data;

import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecuritySupport
{
	public static void login()
	{
		SecurityContextHolder.getContext().setAuthentication(
				new TestingAuthenticationToken("admin", "admin", "ROLE_SYSTEM"));
	}

	public static void logout()
	{
		SecurityContextHolder.getContext().setAuthentication(null);
	}
}
