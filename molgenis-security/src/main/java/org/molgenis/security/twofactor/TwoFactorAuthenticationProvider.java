package org.molgenis.security.twofactor;

import org.molgenis.security.core.runas.RunAsSystem;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import static java.util.Objects.requireNonNull;

/**
 * AuthenticationProvider that uses the TwoFactorAuthenticationSerivce and expects a TwoFactorAuthenticationToken
 * <p>
 * Checks if 2 factor authentication is configured for user.
 * <p>
 * If it is then check only with verificationKey
 */
public class TwoFactorAuthenticationProvider implements AuthenticationProvider
{
	private final TwoFactorAuthenticationService twoFactorAuthenticationService;
	private OTPService otpService;

	public TwoFactorAuthenticationProvider(TwoFactorAuthenticationService twoFactorAuthenticationService,
			OTPService otpService)
	{
		this.twoFactorAuthenticationService = requireNonNull(twoFactorAuthenticationService);
		this.otpService = requireNonNull(otpService);
	}

	@Override
	@RunAsSystem
	public Authentication authenticate(Authentication authentication) throws AuthenticationException
	{
		if (!supports(authentication.getClass()))
			throw new IllegalArgumentException("Only RestAuthenticationToken is supported");

		TwoFactorAuthenticationToken authToken = (TwoFactorAuthenticationToken) authentication;

		if (!twoFactorAuthenticationService.isConfiguredForUser())
		{
			if (authToken.getSecretKey() != null)
			{
				otpService.tryVerificationCode(authToken.getVerificationCode(), authToken.getSecretKey());
				twoFactorAuthenticationService.setSecretKey(authToken.getSecretKey());
				UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext()
																			 .getAuthentication()
																			 .getPrincipal();
				// if token is invalid
				authToken = new TwoFactorAuthenticationToken(userDetails, userDetails.getPassword(),
						userDetails.getAuthorities(), authToken.getVerificationCode(), authToken.getSecretKey());
			}
		}
		else
		{
			if (authToken.getVerificationCode() != null)
			{
				if (twoFactorAuthenticationService.isVerificationCodeValidForUser(authToken.getVerificationCode()))
				{
					UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext()
																				 .getAuthentication()
																				 .getPrincipal();
					// if token is invalid
					authToken = new TwoFactorAuthenticationToken(userDetails, userDetails.getPassword(),
							userDetails.getAuthorities(), authToken.getVerificationCode(), null);
				}

			}
		}

		return authToken;
	}

	@Override
	public boolean supports(Class<?> authentication)
	{
		return TwoFactorAuthenticationToken.class.isAssignableFrom(authentication);
	}

}
