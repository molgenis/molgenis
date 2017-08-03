package org.molgenis.security.twofactor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import static java.util.Objects.requireNonNull;

/**
 * AuthenticationProvider that uses the TwoFactorAuthenticationService and expects a TwoFactorAuthenticationToken
 * <p>
 * Checks if 2 factor authentication is configured for user.
 * <p>
 * If it is then check only with verificationKey
 */
public class TwoFactorAuthenticationProviderImpl implements TwoFactorAuthenticationProvider
{
	private final TwoFactorAuthenticationService twoFactorAuthenticationService;
	private final OTPService otpService;
	private RecoveryService recoveryService;

	public TwoFactorAuthenticationProviderImpl(TwoFactorAuthenticationService twoFactorAuthenticationService,
			OTPService otpService, RecoveryService recoveryService)
	{
		this.twoFactorAuthenticationService = requireNonNull(twoFactorAuthenticationService);
		this.otpService = requireNonNull(otpService);
		this.recoveryService = requireNonNull(recoveryService);
	}

	@Override
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

				//TODO combine configuration methods into one
				twoFactorAuthenticationService.setSecretKey(authToken.getSecretKey());
				recoveryService.generateRecoveryCodes();
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
