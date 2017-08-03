package org.molgenis.security.twofactor;

import org.molgenis.auth.User;
import org.molgenis.auth.UserMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.twofactor.exceptions.InvalidVerificationCodeException;
import org.molgenis.security.twofactor.exceptions.TooManyLoginAttemptsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

import static com.google.api.client.util.Lists.newArrayList;
import static java.text.MessageFormat.format;
import static java.util.Objects.requireNonNull;
import static org.molgenis.auth.UserMetaData.USER;
import static org.molgenis.data.populate.IdGenerator.Strategy.SECURE_RANDOM;
import static org.molgenis.data.populate.IdGenerator.Strategy.SHORT_SECURE_RANDOM;
import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;
import static org.molgenis.security.twofactor.RecoveryCodeMetadata.*;

@Service
public class TwoFactorAuthenticationServiceImpl implements TwoFactorAuthenticationService
{
	private static final Logger LOG = LoggerFactory.getLogger(TwoFactorAuthenticationService.class);

	private static final int RECOVERY_CODE_COUNT = 10;

	private OTPService otpService;
	private DataService dataService;
	private IdGenerator idGenerator;
	private RecoveryCodeFactory recoveryCodeFactory;
	private UserSecretFactory userSecretFactory;

	public TwoFactorAuthenticationServiceImpl(OTPService otpService, DataService dataService, IdGenerator idGenerator,
			RecoveryCodeFactory recoveryCodeFactory, UserSecretFactory userSecretFactory)
	{
		this.otpService = requireNonNull(otpService);
		this.dataService = requireNonNull(dataService);
		this.idGenerator = requireNonNull(idGenerator);
		this.recoveryCodeFactory = requireNonNull(recoveryCodeFactory);
		this.userSecretFactory = requireNonNull(userSecretFactory);
	}

	@Override
	public boolean isVerificationCodeValidForUser(String verificationCode)
			throws InvalidVerificationCodeException, TooManyLoginAttemptsException
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
				updateFailedLoginAttempts(userSecret.getFailedLoginAttempts() + 1);
				throw err;
			}
		}
		return isValid;
	}

	@Override
	public boolean userIsBlocked() throws TooManyLoginAttemptsException
	{
		UserSecret userSecret = getSecret();
		if (userSecret.getFailedLoginAttempts() > 2)
		{
			if (userSecret.getLastFailedAuthentication() != null && (Instant.now().toEpochMilli()
					< userSecret.getLastFailedAuthentication().plus(Duration.ofSeconds(30)).toEpochMilli()))
			{
				throw new TooManyLoginAttemptsException(
						"You entered the wrong verification code 3 times, please wait 30 seconds before you try again");
			}
		}
		return false;
	}

	@Override
	public void setSecretKey(String secret) throws InternalAuthenticationServiceException
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
			runAsSystem(() -> dataService.add(UserSecretMetaData.USERSECRET, userSecret));
		}

	}

	@Override
	public String generateSecretKey()
	{
		return idGenerator.generateId(SECURE_RANDOM);
	}

	@Override
	public boolean isConfiguredForUser() throws InternalAuthenticationServiceException
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

	@Override
	public boolean isEnabledForUser() throws InternalAuthenticationServiceException
	{
		boolean isEnabled = false;
		User user = getUser();
		isEnabled = true;
		return isEnabled;
	}

	@Override
	@Transactional
	public void generateNewRecoveryCodes()
	{
		String userId = getUser().getId();
		deleteOldRecoveryCodes(userId);
		dataService.add(RECOVERY_CODE, generateRecoveryCodes(userId));
	}

	@Override
	@Transactional
	public void useRecoveryCode(String recoveryCode)
	{
		String userId = getUser().getId();
		RecoveryCode existingCode = runAsSystem(() -> dataService.findOne(RECOVERY_CODE,
				new QueryImpl<RecoveryCode>().eq(USER_ID, userId).and().eq(CODE, recoveryCode), RecoveryCode.class));

		if (existingCode != null)
		{
			runAsSystem(() -> dataService.delete(RECOVERY_CODE, existingCode));
			updateFailedLoginAttempts(0);
		}
		else
		{
			throw new MolgenisDataException("Invalid recovery code");
		}
	}

	private void deleteOldRecoveryCodes(String userId)
	{
		runAsSystem(() ->
		{
			Stream<RecoveryCode> recoveryCodes = dataService.findAll(RECOVERY_CODE,
					new QueryImpl<RecoveryCode>().eq(USER_ID, userId), RecoveryCode.class);
			dataService.delete(RECOVERY_CODE, recoveryCodes);
		});
	}

	private Stream<RecoveryCode> generateRecoveryCodes(String userId)
	{
		List<RecoveryCode> recoveryCodes = newArrayList();
		for (int i = 0; i < RECOVERY_CODE_COUNT; i++)
		{
			RecoveryCode recoveryCode = recoveryCodeFactory.create();
			recoveryCode.setUserId(userId);
			recoveryCode.setCode(idGenerator.generateId(SHORT_SECURE_RANDOM));
			recoveryCodes.add(recoveryCode);
		}
		return recoveryCodes.stream();
	}

	private UserSecret getSecret() throws InternalAuthenticationServiceException
	{
		User user = getUser();
		UserSecret secret = runAsSystem(() -> dataService.findOne(UserSecretMetaData.USERSECRET,
				new QueryImpl<UserSecret>().eq(UserSecretMetaData.USER_ID, user.getId()), UserSecret.class));

		if (secret != null)
		{
			return secret;
		}
		else
		{
			throw new InternalAuthenticationServiceException(
					format("Secret not found, user: [ {0} ] is not configured for 2 factor authentication",
							user.getUsername()));
		}

	}

	private void updateFailedLoginAttempts(int numberOfAttempts)
	{
		UserSecret userSecret = getSecret();
		userSecret.setFailedLoginAttempts(numberOfAttempts);
		if (numberOfAttempts >= 3)
		{
			userSecret.setLastFailedAuthentication(Instant.now());
		}
		runAsSystem(() -> dataService.update(UserSecretMetaData.USERSECRET, userSecret));
	}

	private User getUser()
	{
		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		User user = runAsSystem(() -> dataService.findOne(USER,
				new QueryImpl<User>().eq(UserMetaData.USERNAME, userDetails.getUsername()), User.class));

		if (user != null)
		{
			return user;
		}
		else
		{
			throw new UsernameNotFoundException(format("Can''t find user: [ {0} ]", userDetails.getUsername()));
		}
	}
}
