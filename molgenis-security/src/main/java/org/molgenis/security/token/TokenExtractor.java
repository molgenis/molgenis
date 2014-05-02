package org.molgenis.security.token;

import javax.servlet.http.HttpServletRequest;

/**
 * Get a molgenis authentication token from a HttpServletRequest
 */
public class TokenExtractor
{
	protected static final String TOKEN_HEADER = "x-molgenis-token";

	public static String getToken(HttpServletRequest request)
	{
		return request.getHeader(TOKEN_HEADER);
	}

}
