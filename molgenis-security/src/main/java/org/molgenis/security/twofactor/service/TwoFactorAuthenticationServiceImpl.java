package org.molgenis.security.twofactor.service;

import org.molgenis.data.DataService;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.security.auth.User;
import org.molgenis.data.security.user.UserService;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.security.twofactor.exceptions.InvalidVerificationCodeException;
import org.molgenis.security.twofactor.exceptions.TooManyLoginAttemptsException;
import org.molgenis.security.twofactor.model.UserSecret;
import org.molgenis.security.twofactor.model.UserSecretFactory;
import org.molgenis.security.twofactor.model.UserSecretMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.stream.Stream;

import static java.text.MessageFormat.format;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.populate.IdGenerator.Strategy.SECURE_RANDOM;
import static org.molgenis.security.core.runas.RunAsSystemAspect.runAsSystem;
import static org.molgenis.security.twofactor.model.UserSecretMetaData.USER_ID;
import static org.molgenis.security.twofactor.model.UserSecretMetaData.USER_SECRET;

@Service
public class TwoFactorAuthenticationServiceImpl implements TwoFactorAuthenticationService
{
	private static final Logger LOG = LoggerFactory.getLogger(TwoFactorAuthenticationService.class);

	private static final int MAX_FAILED_LOGIN_ATTEMPTS = 3;
	private static final int FAILED_LOGIN_ATTEMPT_ITERATION = 1;
	private static final int BLOCKED_USER_INTERVAL = 30;

	private final OtpService otpService;
	private final DataService dataService;
	private final UserService userService;
	private final IdGenerator idGenerator;
	private final UserSecretFactory userSecretFactory;

	public TwoFactorAuthenticationServiceImpl(OtpService otpService, DataService dataService, UserService userService,
			IdGenerator idGenerator, UserSecretFactory userSecretFactory)
	{
		this.otpService = requireNonNull(otpService);
		this.dataService = requireNonNull(dataService);
		this.userService = requireNonNull(userService);
		this.idGenerator = requireNonNull(idGenerator);
		this.userSecretFactory = requireNonNull(userSecretFactory);
	}

	@Override
	public boolean isVerificationCodeValidForUser(String verificationCode)
	{
		boolean isValid = false;

		UserSecret userSecret = getSecret();
		if (!userIsBlocked())
		{
			try
			{
				if (otpService.tryVerificationCode(verificationCode, userSecret.getSecret()))
				{
					isValid = true;
					updateFailedLoginAttempts(0);
				}
			}
			catch (InvalidVerificationCodeException err)
			{
				updateFailedLoginAttempts(userSecret.getFailedLoginAttempts() + FAILED_LOGIN_ATTEMPT_ITERATION);
				if (!userIsBlocked())
				{
					throw err;
				}

			}
		}
		return isValid;
	}

	@Override
	public boolean userIsBlocked()
	{
		UserSecret userSecret = getSecret();
		if (userSecret.getFailedLoginAttempts() >= MAX_FAILED_LOGIN_ATTEMPTS)
		{
			if (userSecret.getLastFailedAuthentication() != null && (Instant.now().toEpochMilli()
					< userSecret.getLastFailedAuthentication()
								.plus(Duration.ofSeconds(BLOCKED_USER_INTERVAL))
								.toEpochMilli()))
			{
				throw new TooManyLoginAttemptsException(
						format("You entered the wrong verification code {0} times, please wait for {1} seconds before you try again",
								MAX_FAILED_LOGIN_ATTEMPTS, BLOCKED_USER_INTERVAL));
			}
		}
		return false;
	}

	@Override
	public void saveSecretForUser(String secret)
	{

		if (secret == null)
		{
			throw new InternalAuthenticationServiceException("No secretKey found");
		}
		else
		{
			User user = getUser();
			UserSecret userSecret = userSecretFactory.create();
			userSecret.setUserId(user.getId());
			userSecret.setSecret(secret);
			runAsSystem(() -> dataService.add(USER_SECRET, userSecret));
		}
	}

	@Override
	public void resetSecretForUser()
	{
		User user = getUser();
		Stream<UserSecret> userSecrets = runAsSystem(
				() -> dataService.query(USER_SECRET, UserSecret.class).eq(USER_ID, user.getId()).findAll());
		//noinspection RedundantCast
		runAsSystem((Runnable) () -> dataService.delete(USER_SECRET, userSecrets));
	}

	@Override
	public void enableForUser()
	{
		User user = getUser();
		user.setTwoFactorAuthentication(true);
		userService.update(user);
	}

	@Override
	public void disableForUser()
	{
		User user = getUser();
		user.setTwoFactorAuthentication(false);
		userService.update(user);
		UserSecret userSecret = getSecret();
		runAsSystem(() -> dataService.delete(USER_SECRET, userSecret));
	}

	@Override
	public String generateSecretKey()
	{
		return idGenerator.generateId(SECURE_RANDOM);
	}

	@Override
	public boolean isConfiguredForUser()
	{
		boolean isConfigured = false;
		try
		{
			UserSecret secret = getSecret();
			if (StringUtils.hasText(secret.getSecret()))
			{
				isConfigured = true;
			}
		}
		catch (InternalAuthenticationServiceException err)
		{
			LOG.warn(err.getMessage());
		}

		return isConfigured;
	}

	/**
	 * Check if user has 3 or more failed login attempts
	 * -> then determine if the user is within the 30 seconds of the last failed login attempt
	 * -> if the user is not outside the timeframe than the failed login attempts are set to 1 because it is a failed login attempt
	 * When the user has less than 3 failed login attempts
	 * -> the last failed login attempt is logged
	 *
	 * @param numberOfAttempts number of failed login attempts
	 */
	private void updateFailedLoginAttempts(int numberOfAttempts)
	{
		UserSecret userSecret = getSecret();
		userSecret.setFailedLoginAttempts(numberOfAttempts);
		if (userSecret.getFailedLoginAttempts() >= MAX_FAILED_LOGIN_ATTEMPTS)
		{
			if (!(userSecret.getLastFailedAuthentication() != null && (Instant.now().toEpochMilli()
					< userSecret.getLastFailedAuthentication()
								.plus(Duration.ofSeconds(BLOCKED_USER_INTERVAL))
								.toEpochMilli())))
			{
				userSecret.setFailedLoginAttempts(FAILED_LOGIN_ATTEMPT_ITERATION);
			}
		}
		else
		{
			userSecret.setLastFailedAuthentication(Instant.now());
		}
		runAsSystem(() -> dataService.update(USER_SECRET, userSecret));
	}

	private UserSecret getSecret()
	{
		User user = getUser();
		UserSecret secret = runAsSystem(() -> dataService.query(USER_SECRET, UserSecret.class)
														 .eq(UserSecretMetaData.USER_ID, user.getId())
														 .findOne());

		if (secret != null)
		{
			return secret;
		}
		else
		{
			throw new InternalAuthenticationServiceException(
					format("Secret not found, user: [{0}] is not configured for two factor authentication",
							user.getUsername()));
		}

	}

	private User getUser()
	{
		User user = userService.getUser(SecurityUtils.getCurrentUsername());

		if (user != null)
		{
			return user;
		}
		else
		{
			throw new UsernameNotFoundException(format("Can not find user: [{0}]", SecurityUtils.getCurrentUsername()));
		}
	}
}
