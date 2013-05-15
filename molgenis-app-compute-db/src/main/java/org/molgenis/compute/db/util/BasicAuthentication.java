package org.molgenis.compute.db.util;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;

/**
 * Retrieve and parse a basic authentication http header
 * 
 * @author erwin
 * 
 */
public abstract class BasicAuthentication
{
	/**
	 * Gets the username and password from the Authorization http header.
	 * Returns null if the header is not present.
	 * 
	 * @param request
	 * @return
	 */
	public static Result getUsernamePassword(HttpServletRequest request)
	{
		String authorization = request.getHeader("Authorization");
		if (authorization != null)
		{
			if (authorization.startsWith("Basic "))
			{
				authorization = authorization.substring(6);
				if (Base64.isBase64(authorization))
				{
					String decoded = new String(Base64.decodeBase64(authorization));
					int index = decoded.indexOf(':');
					String username = decoded.substring(0, index);
					String password = decoded.substring(index + 1);

					return new Result(username, password);
				}
			}
		}

		return null;
	}

	public static class Result
	{
		private final String username;
		private final String password;

		private Result(String username, String password)
		{
			this.username = username;
			this.password = password;
		}

		public String getUsername()
		{
			return username;
		}

		public String getPassword()
		{
			return password;
		}

	}
}
