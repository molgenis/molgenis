package org.molgenis.security.twofactor;

import org.molgenis.auth.User;
import org.molgenis.auth.UserMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.data.support.QueryImpl;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.StringUtils;

import static java.util.Objects.requireNonNull;
import static org.molgenis.auth.UserMetaData.USER;
import static org.molgenis.data.populate.IdGenerator.Strategy.SECURE_RANDOM;
import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;

public class TwoFactorAuthenticationServiceImpl implements TwoFactorAuthenticationService
{
	private AppSettings appSettings;
	private OTPService otpService;
	private DataService dataService;
	private IdGenerator idGenerator;

	public TwoFactorAuthenticationServiceImpl(AppSettings appSettings, OTPService otpService, DataService dataService,
			IdGenerator idGenerator)
	{
		this.appSettings = requireNonNull(appSettings);
		this.otpService = requireNonNull(otpService);
		this.dataService = requireNonNull(dataService);
		this.idGenerator = requireNonNull(idGenerator);
	}

	@Override
	public boolean isVerificationCodeValidForUser(String verificationCode)
			throws BadCredentialsException, UsernameNotFoundException
	{
		boolean isValid = false;

		if (appSettings.getTwoFactorAuthentication().equals(TwoFactorAuthenticationSetting.ENABLED.toString())
				|| appSettings.getTwoFactorAuthentication().equals(TwoFactorAuthenticationSetting.ENFORCED.toString()))
		{
			User user = getUser();

			if (otpService.tryVerificationCode(verificationCode, user.getSecretKey()))
			{
				isValid = true;
			}

		}
		return isValid;
	}

	@Override
	public void setSecretKey(String secret) throws UsernameNotFoundException
	{
		User user = getUser();
		user.setSecretKey(secret);
		runAsSystem(() -> dataService.update(USER, user));
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
		User user = getUser();

		if (StringUtils.hasText(user.getSecretKey()))
		{
			isConfigured = true;
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

	//FIXME use userservice
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
