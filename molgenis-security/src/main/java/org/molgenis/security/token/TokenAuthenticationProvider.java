package org.molgenis.security.token;

import org.molgenis.security.core.runas.RunAsSystem;
import org.molgenis.security.core.token.TokenService;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * AuthenticationProvider that uses the TokenService and expects a RestAuthenticationToken
 */
public class TokenAuthenticationProvider implements AuthenticationProvider
{
	private final TokenService tokenService;

	public TokenAuthenticationProvider(TokenService tokenService)
	{
		this.tokenService = tokenService;
	}

	@Override
	@RunAsSystem
	public Authentication authenticate(Authentication authentication)
	{
		if (!supports(authentication.getClass()))
			throw new IllegalArgumentException("Only RestAuthenticationToken is supported");

		RestAuthenticationToken authToken = (RestAuthenticationToken) authentication;

		if (authToken.getToken() != null)
		{
			UserDetails userDetails = tokenService.findUserByToken(authToken.getToken());// Throws UnknownTokenException
			// if token is invalid
			authToken = new RestAuthenticationToken(userDetails, userDetails.getPassword(),
					userDetails.getAuthorities(), authToken.getToken());
		}

		return authToken;
	}

	@Override
	public boolean supports(Class<?> authentication)
	{
		return RestAuthenticationToken.class.isAssignableFrom(authentication);
	}

}
