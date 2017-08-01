package org.molgenis.security.twofactor;

import org.molgenis.auth.User;
import org.molgenis.auth.UserMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.data.support.QueryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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

public class TwoFactorAuthenticationServiceImpl implements TwoFactorAuthenticationService
{
	private static final Logger LOG = LoggerFactory.getLogger(TwoFactorAuthenticationService.class);

	private static final int RECOVERY_CODE_COUNT = 10;

	private AppSettings appSettings;
	private OTPService otpService;
	private DataService dataService;
	private IdGenerator idGenerator;
	private RecoveryCodeFactory recoveryCodeFactory;
	private UserSecretFactory userSecretFactory;

	public TwoFactorAuthenticationServiceImpl(AppSettings appSettings, OTPService otpService, DataService dataService,
			IdGenerator idGenerator, RecoveryCodeFactory recoveryCodeFactory, UserSecretFactory userSecretFactory)
	{
		this.appSettings = requireNonNull(appSettings);
		this.otpService = requireNonNull(otpService);
		this.dataService = requireNonNull(dataService);
		this.idGenerator = requireNonNull(idGenerator);
		this.recoveryCodeFactory = requireNonNull(recoveryCodeFactory);
		this.userSecretFactory = requireNonNull(userSecretFactory);
	}

	@Override
	public boolean isVerificationCodeValidForUser(String verificationCode)
			throws BadCredentialsException, UsernameNotFoundException
	{
		boolean isValid = false;

		if (appSettings.getTwoFactorAuthentication().equals(TwoFactorAuthenticationSetting.ENABLED.toString())
				|| appSettings.getTwoFactorAuthentication().equals(TwoFactorAuthenticationSetting.ENFORCED.toString()))
		{
			UserSecret userSecret = getSecret();

			if (otpService.tryVerificationCode(verificationCode, userSecret.getSecret()))
			{
				isValid = true;
				userSecret.setLastSuccessfulAuthentication(Instant.now());
				userSecret.setFailedLoginAttempts(0);
			}
			else
			{
				int failedLoginAttempts = userSecret.getFailedLoginAttempts();
				userSecret.setFailedLoginAttempts(failedLoginAttempts + 1);
			}
			runAsSystem(() -> dataService.update(UserSecretMetaData.USERSECRET, userSecret));

		}
		return isValid;
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
	public boolean isConfiguredForUser() throws UsernameNotFoundException
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
	public boolean isEnabledForUser()
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
		}
		else
		{
			throw new MolgenisDataException("Invalid recovery code!");
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
					format("Secret not found, user: [ %s ] is not configured for 2 factor authentication",
							user.getUsername()));
		}

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
			throw new UsernameNotFoundException("Can't find user: [" + userDetails.getUsername() + "]");
		}
	}
}
