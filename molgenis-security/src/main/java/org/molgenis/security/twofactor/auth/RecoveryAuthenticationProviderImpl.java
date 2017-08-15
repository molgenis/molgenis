package org.molgenis.security.twofactor.auth;

import org.molgenis.security.twofactor.service.RecoveryService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import static java.util.Objects.requireNonNull;

/**
 * AuthenticationProvider that offers the possibility to authenticate users with a recovery code. Used in conjunction
 * with Two Factor Authentication and prevents users from being locked out of their account when they lose their phone.
 */
public class RecoveryAuthenticationProviderImpl implements RecoveryAuthenticationProvider
{
	private final RecoveryService recoveryService;

	public RecoveryAuthenticationProviderImpl(RecoveryService recoveryService)
	{
		this.recoveryService = requireNonNull(recoveryService);
	}

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException
	{
		if (!supports(authentication.getClass()))
		{
			throw new IllegalArgumentException("Only RecoveryAuthenticationToken is supported");
		}

		RecoveryAuthenticationToken authToken = (RecoveryAuthenticationToken) authentication;

		if (authToken.getRecoveryCode() != null)
		{
			recoveryService.useRecoveryCode(authToken.getRecoveryCode());
			UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext()
																		 .getAuthentication()
																		 .getPrincipal();

			authToken = new RecoveryAuthenticationToken(userDetails, userDetails.getPassword(),
					userDetails.getAuthorities(), authToken.getRecoveryCode());
		}
		else
		{
			throw new BadCredentialsException("Invalid recovery code or code already used");
		}

		return authToken;
	}

	@Override
	public boolean supports(Class<?> authentication)
	{
		return RecoveryAuthenticationToken.class.isAssignableFrom(authentication);
	}

}
