package org.molgenis.security.token;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * Holds the api token, passed by the client via a custom HTTP header
 */
public class RestAuthenticationToken extends UsernamePasswordAuthenticationToken
{
	private static final long serialVersionUID = 340142428848970352L;
	private final String token;

	public RestAuthenticationToken(Object principal, Object credentials,
			Collection<? extends GrantedAuthority> authorities, String token)
	{
		super(principal, credentials, authorities);
		this.token = token;
	}

	public RestAuthenticationToken(String token)
	{
		super("N/A", "N/A");
		this.token = token;
	}

	public String getToken()
	{
		return token;
	}

}
