package org.molgenis.security.twofactor;

import org.jboss.aerogear.security.otp.Totp;
import org.molgenis.auth.User;
import org.molgenis.auth.UserMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.data.support.QueryImpl;
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

import static org.molgenis.auth.UserMetaData.USER;
import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;

/**
 * @author sido
 * @since 26-7-2017
 */
@Service
public class TwoFactorAuthenticationServiceImpl implements TwoFactorAuthenticationService
{

	private AppSettings appSettings;
	private DataService dataService;

	public TwoFactorAuthenticationServiceImpl(AppSettings appSettings, DataService dataService) {
		this.appSettings = appSettings;
		this.dataService = dataService;
	}

	@Override
	public boolean isVerificationCodeValid(String verificationCode) throws BadCredentialsException, UsernameNotFoundException {
		boolean isValid = false;
		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		if (appSettings.is2fa()) {
			User user = runAsSystem(() -> dataService.findOne(USER, new QueryImpl<User>().eq(UserMetaData.USERNAME, userDetails.getUsername()), User.class));
			if (StringUtils.hasText(user.getSecret2fa())) {
				if (verificationCode == null)
				{
					throw new BadCredentialsException("2 factor authentication code is mandatory");
				}
				final Totp totp = new Totp(user.getSecret2fa());
				if (!isValidLong(verificationCode) || !totp.verify(verificationCode))
				{
					throw new BadCredentialsException("Invalid 2 factor authentication code");
				}
			} else {
				throw new BadCredentialsException("2 factor authentication secret key is not available");
			}
		}
		return isValid;
	}

	private boolean isValidLong(String code) {
		try {
			Long.parseLong(code);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	@Override
	public void set2FaSecret(String secret) throws UsernameNotFoundException {
		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		User user = runAsSystem(() -> dataService.findOne(USER, new QueryImpl<User>().eq(UserMetaData.USERNAME, userDetails.getUsername()), User.class));
		if(user != null)
		{
			user.setSecret2fa(secret);
		}
		else
		{
			throw new UsernameNotFoundException("Can't find user: [" + userDetails.getUsername() + "]");
		}
	}

	@Override
	public boolean is2FAEnabledForUser() throws UsernameNotFoundException {
		boolean isEnabled = false;
		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		User user = runAsSystem(() -> dataService.findOne(USER, new QueryImpl<User>().eq(UserMetaData.USERNAME, userDetails.getUsername()), User.class));
		if(user != null)
		{
			if (StringUtils.hasText(user.getSecret2fa()))
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
	public void set2FAAuthenticated() throws BadCredentialsException {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		List<GrantedAuthority> updatedAuthorities = new ArrayList<>(auth.getAuthorities());
		updatedAuthorities.add(new SimpleGrantedAuthority("ROLE_TWO_FACTOR_AUTHENTICATED"));
		Authentication newAuth = new UsernamePasswordAuthenticationToken(auth.getPrincipal(), auth.getCredentials(), updatedAuthorities);

		SecurityContextHolder.getContext().setAuthentication(newAuth);
	}

}
