package org.molgenis.security.twofactor.service;

import org.molgenis.data.DataService;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.model.User;
import org.molgenis.security.core.service.UserAccountService;
import org.molgenis.security.core.service.UserService;
import org.molgenis.security.twofactor.exceptions.InvalidVerificationCodeException;
import org.molgenis.security.twofactor.exceptions.TooManyLoginAttemptsException;
import org.molgenis.security.twofactor.model.UserSecret;
import org.molgenis.security.twofactor.model.UserSecretFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.stream.Stream;

import static java.text.MessageFormat.format;
import static java.time.Instant.now;
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
	private static final int BLOCKED_USER_INTERVAL = 30;

	private final OtpService otpService;
	private final DataService dataService;
	private final UserAccountService userAccountService;
	private final UserService userService;
	private final IdGenerator idGenerator;
	private final UserSecretFactory userSecretFactory;

	public TwoFactorAuthenticationServiceImpl(OtpService otpService, DataService dataService,
			UserAccountService userAccountService, UserService userService, IdGenerator idGenerator,
			UserSecretFactory userSecretFactory)
	{
		this.otpService = requireNonNull(otpService);
		this.dataService = requireNonNull(dataService);
		this.userAccountService = requireNonNull(userAccountService);
		this.userService = requireNonNull(userService);
		this.idGenerator = requireNonNull(idGenerator);
		this.userSecretFactory = requireNonNull(userSecretFactory);
	}

	@Override
	public boolean isVerificationCodeValidForUser(String verificationCode)
	{
		boolean isValid = false;

		UserSecret userSecret = getSecretForUser();
		if (!userIsBlocked())
		{
			try
			{
				if (otpService.tryVerificationCode(verificationCode, userSecret.getSecret()))
				{
					isValid = true;
					registerValidCode(userSecret);
				}
			}
			catch (InvalidVerificationCodeException err)
			{
				registerInvalidCode(userSecret);
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
		if (Optional.of(getSecretForUser())
					.filter(secret -> secret.getFailedLoginAttempts() >= MAX_FAILED_LOGIN_ATTEMPTS)
					.filter(secret -> secret.hasRecentFailedLoginAttempt(BLOCKED_USER_INTERVAL))
					.isPresent())
		{
			throw new TooManyLoginAttemptsException(
					format("You entered the wrong verification code {0} times, please wait for {1} seconds before you try again",
							MAX_FAILED_LOGIN_ATTEMPTS, BLOCKED_USER_INTERVAL));
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
			User user = userAccountService.getCurrentUser();
			UserSecret userSecret = userSecretFactory.create();
			userSecret.setUserId(user.getId());
			userSecret.setSecret(secret);
			runAsSystem(() -> dataService.add(USER_SECRET, userSecret));
		}
	}

	@Override
	public void resetSecretForUser()
	{
		User user = userAccountService.getCurrentUser();
		Stream<UserSecret> userSecrets = runAsSystem(
				() -> dataService.findAll(USER_SECRET, new QueryImpl<UserSecret>().eq(USER_ID, user.getId()),
						UserSecret.class));
		//noinspection RedundantCast
		runAsSystem((Runnable) () -> dataService.delete(USER_SECRET, userSecrets));
	}

	@Override
	public void enableForUser()
	{
		User user = userAccountService.getCurrentUser().toBuilder().twoFactorAuthentication(true).build();
		userService.update(user);
	}

	@Override
	public void disableForUser()
	{
		User user = userAccountService.getCurrentUser().toBuilder().twoFactorAuthentication(false).build();
		userService.update(user);
		UserSecret userSecret = getSecretForUser();
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
		try
		{
			return StringUtils.hasText(getSecretForUser().getSecret());
		}
		catch (InternalAuthenticationServiceException err)
		{
			LOG.warn(err.getMessage(), err);
		}
		return false;
	}

	/**
	 * Registers that the user has entered a valid code.
	 */
	private void registerValidCode(UserSecret userSecret)
	{
		userSecret.setFailedLoginAttempts(0);
		update(userSecret);
	}

	/**
	 * Registers that the user has entered an invalid code.
	 */
	private void registerInvalidCode(UserSecret userSecret)
	{
		if (userSecret.hasRecentFailedLoginAttempt(BLOCKED_USER_INTERVAL))
		{
			userSecret.incrementFailedLoginAttempts();
		}
		else
		{
			userSecret.setFailedLoginAttempts(1);
		}
		userSecret.setLastFailedAuthentication(now());
		update(userSecret);
	}

	private void update(UserSecret userSecret)
	{
		runAsSystem(() -> dataService.update(USER_SECRET, userSecret));
	}

	private UserSecret getSecretForUser()
	{
		User user = userAccountService.getCurrentUser();
		return getSecret(user).orElseThrow(() -> new InternalAuthenticationServiceException(
				format("Secret not found, user: [{0}] is not configured for two factor authentication",
						user.getUsername())));
	}

	private Optional<UserSecret> getSecret(User user)
	{
		return runAsSystem(() -> Optional.ofNullable(
				dataService.findOne(USER_SECRET, new QueryImpl<UserSecret>().eq(USER_ID, user.getId()),
						UserSecret.class)));
	}

}
