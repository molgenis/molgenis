package org.molgenis.security.twofactor;

import org.jboss.aerogear.security.otp.Totp;
import org.molgenis.auth.User;
import org.molgenis.auth.UserMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.utils.SecurityUtils;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static org.molgenis.auth.UserMetaData.USER;
import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;

@Service
public class TwoFactorAuthenticationServiceImpl implements TwoFactorAuthenticationService
{
	private AppSettings appSettings;
	private DataService dataService;

	public TwoFactorAuthenticationServiceImpl(AppSettings appSettings, DataService dataService)
	{
		this.appSettings = requireNonNull(appSettings);
		this.dataService = requireNonNull(dataService);
	}

	@Override
	public boolean isVerificationCodeValid(String verificationCode)
			throws BadCredentialsException, UsernameNotFoundException
	{
		boolean isValid = true;
		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		if (appSettings.getTwoFactorAuthentication().equals(TwoFactorAuthenticationSetting.ENABLED.toString()))
		{
			User user = runAsSystem(() -> dataService.findOne(USER,
					new QueryImpl<User>().eq(UserMetaData.USERNAME, userDetails.getUsername()), User.class));
			if (StringUtils.hasText(user.getSecretKey()))
			{
				if (verificationCode == null)
				{
					throw new BadCredentialsException("2 factor authentication code is mandatory");
				}
				final Totp totp = new Totp(user.getSecretKey());
				if (!isValidLong(verificationCode) || !totp.verify(verificationCode))
				{
					throw new BadCredentialsException("Invalid 2 factor authentication code");
				}
			}
			else
			{
				throw new BadCredentialsException("2 factor authentication secret key is not available");
			}
		}
		return isValid;
	}

	private boolean isValidLong(String code)
	{
		try
		{
			Long.parseLong(code);
		}
		catch (NumberFormatException e)
		{
			return false;
		}
		return true;
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
		}
		else
		{
			throw new UsernameNotFoundException("Can't find user: [" + userDetails.getUsername() + "]");
		}
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

	@Override
	public String getUnAuthenticatedUser()
	{
		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		return userDetails.getUsername();
	}

	@Override
	public void authenticate() throws BadCredentialsException
	{
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		List<GrantedAuthority> updatedAuthorities = new ArrayList<>(auth.getAuthorities());
		updatedAuthorities.add(new SimpleGrantedAuthority(SecurityUtils.AUTHORITY_TWO_FACTOR_AUTHENTICATION));
		Authentication newAuth = new UsernamePasswordAuthenticationToken(auth.getPrincipal(), auth.getCredentials(),
				updatedAuthorities);

		SecurityContextHolder.getContext().setAuthentication(newAuth);
	}

}
