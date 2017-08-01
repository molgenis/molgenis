package org.molgenis.security.twofactor;

import org.apache.commons.codec.binary.Base32;
import org.molgenis.auth.User;
import org.molgenis.auth.UserMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.data.support.QueryImpl;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;

import static java.util.Objects.requireNonNull;
import static org.molgenis.auth.UserMetaData.USER;
import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;

public class TwoFactorAuthenticationServiceImpl implements TwoFactorAuthenticationService
{
	private AppSettings appSettings;
	private OTPService otpService;
	private DataService dataService;

	public TwoFactorAuthenticationServiceImpl(AppSettings appSettings, OTPService otpService, DataService dataService)
	{
		this.appSettings = requireNonNull(appSettings);
		this.otpService = requireNonNull(otpService);
		this.dataService = requireNonNull(dataService);
	}

	@Override
	public boolean isVerificationCodeValidForUser(String verificationCode)
			throws BadCredentialsException, UsernameNotFoundException
	{
		boolean isValid = false;
		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		if (appSettings.getTwoFactorAuthentication().equals(TwoFactorAuthenticationSetting.ENABLED.toString())
				|| appSettings.getTwoFactorAuthentication().equals(TwoFactorAuthenticationSetting.ENFORCED.toString()))
		{
			User user = runAsSystem(() -> dataService.findOne(USER,
					new QueryImpl<User>().eq(UserMetaData.USERNAME, userDetails.getUsername()), User.class));

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
		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		User user = runAsSystem(() -> dataService.findOne(USER,
				new QueryImpl<User>().eq(UserMetaData.USERNAME, userDetails.getUsername()), User.class));
		if (user != null)
		{
			user.setSecretKey(secret);
			runAsSystem(() -> dataService.update(USER, user));
		}
		else
		{
			throw new UsernameNotFoundException("Can't find user: [" + userDetails.getUsername() + "]");
		}
	}

	@Override
	public String generateSecretKey()
	{
		SecureRandom random = new SecureRandom();
		byte[] bytes = new byte[20];
		random.nextBytes(bytes);
		Base32 base32 = new Base32();
		return base32.encodeToString(bytes);
	}

	@Override
	public boolean isConfiguredForUser() throws UsernameNotFoundException
	{
		boolean isConfigured = false;
		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		User user = runAsSystem(() -> dataService.findOne(USER,
				new QueryImpl<User>().eq(UserMetaData.USERNAME, userDetails.getUsername()), User.class));
		if (user != null)
		{
			if (StringUtils.hasText(user.getSecretKey()))
			{
				isConfigured = true;
			}
		}
		else
		{
			throw new UsernameNotFoundException("Can't find user: [" + userDetails.getUsername() + "]");
		}
		return isConfigured;
	}

	@Override
	public boolean isEnabledForUser()
	{
		boolean isEnabled = false;
		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		User user = runAsSystem(() -> dataService.findOne(USER,
				new QueryImpl<User>().eq(UserMetaData.USERNAME, userDetails.getUsername()), User.class));
		if (user != null)
		{
			{
				isEnabled = true;
			}
		}
		else
		{
			throw new UsernameNotFoundException("Can't find user: [" + userDetails.getUsername() + "]");
		}
		return isEnabled;
	}

}
