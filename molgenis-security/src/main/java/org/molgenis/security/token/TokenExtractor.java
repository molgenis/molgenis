package org.molgenis.security.token;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * Get a molgenis authentication token from a HttpServletRequest
 */
public class TokenExtractor
{
	public static final String TOKEN_HEADER = "x-molgenis-token";
	protected static final String TOKEN_PARAMETER = "molgenis-token";

	public static String getToken(HttpServletRequest request)
	{
		String token = request.getHeader(TOKEN_HEADER);
		if (StringUtils.isBlank(token))
		{
			token = request.getParameter(TOKEN_PARAMETER);
		}

		if (StringUtils.isBlank(token)) token = null;

		return token;
	}
}
