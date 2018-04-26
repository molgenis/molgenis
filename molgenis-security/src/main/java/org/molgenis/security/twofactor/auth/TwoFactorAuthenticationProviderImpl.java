package org.molgenis.security.twofactor.auth;

import org.molgenis.security.twofactor.service.OtpService;
import org.molgenis.security.twofactor.service.RecoveryService;
import org.molgenis.security.twofactor.service.TwoFactorAuthenticationService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
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
	private final OtpService otpService;
	private final RecoveryService recoveryService;

	public TwoFactorAuthenticationProviderImpl(TwoFactorAuthenticationService twoFactorAuthenticationService,
			OtpService otpService, RecoveryService recoveryService)
	{
		this.twoFactorAuthenticationService = requireNonNull(twoFactorAuthenticationService);
		this.otpService = requireNonNull(otpService);
		this.recoveryService = requireNonNull(recoveryService);
	}

	@Override
	public Authentication authenticate(Authentication authentication)
	{
		if (!supports(authentication.getClass()))
		{
			throw new IllegalArgumentException("Only TwoFactorAuthenticationToken is supported");
		}

		TwoFactorAuthenticationToken authToken = (TwoFactorAuthenticationToken) authentication;

		if (!twoFactorAuthenticationService.isConfiguredForUser())
		{
			if (authToken.getSecretKey() != null)
			{
				if (otpService.tryVerificationCode(authToken.getVerificationCode(), authToken.getSecretKey()))
				{
					activateTwoFactorAuthentication(authToken);
					UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext()
																				 .getAuthentication()
																				 .getPrincipal();

					authToken = new TwoFactorAuthenticationToken(userDetails, userDetails.getPassword(),
							userDetails.getAuthorities(), authToken.getVerificationCode(), authToken.getSecretKey());
				}
			}
			else
			{
				throw new BadCredentialsException("Invalid secret generated");
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
			else
			{
				throw new BadCredentialsException("Invalid verification code entered");
			}
		}

		return authToken;
	}

	private void activateTwoFactorAuthentication(TwoFactorAuthenticationToken authToken)
	{
		twoFactorAuthenticationService.enableForUser();
		twoFactorAuthenticationService.saveSecretForUser(authToken.getSecretKey());
		recoveryService.generateRecoveryCodes();
	}

	@Override
	public boolean supports(Class<?> authentication)
	{
		return TwoFactorAuthenticationToken.class.isAssignableFrom(authentication);
	}

}
