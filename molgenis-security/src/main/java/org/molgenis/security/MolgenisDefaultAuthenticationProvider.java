package org.molgenis.security;

import com.google.common.collect.Lists;
import org.apache.commons.codec.binary.Base32;
import org.jboss.aerogear.security.otp.Totp;
import org.molgenis.auth.User;
import org.molgenis.auth.UserMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.runas.RunAsSystem;
import org.molgenis.security.core.runas.RunAsSystemProxy;
import org.molgenis.security.google.Google2FAWebAuthenticationDetails;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;

import java.util.Objects;

import static org.molgenis.auth.UserMetaData.USER;
import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;

/**
 *
 *
 *
 * @autor sido
 */
public class MolgenisDefaultAuthenticationProvider extends DaoAuthenticationProvider
{

	private final AppSettings appSettings;
	private final DataService dataService;

	public MolgenisDefaultAuthenticationProvider(AppSettings appSettings, DataService dataService) {
		this.appSettings = Objects.requireNonNull(appSettings);
		this.dataService =  Objects.requireNonNull(dataService);
	}

	@Override
	@RunAsSystem
	protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException
	{
		super.additionalAuthenticationChecks(userDetails, authentication);

		if (appSettings.is2fa()) {
			User user = runAsSystem(() -> dataService.findOne(USER, new QueryImpl<User>().eq(UserMetaData.USERNAME, userDetails.getUsername()), User.class));
			if (StringUtils.hasText(user.getSecret2fa())) {
				String verificationCode = ((Google2FAWebAuthenticationDetails) authentication.getDetails()).getVerificationCode();
				if (verificationCode == null)
				{
					throw new BadCredentialsException ("2 factor authentication code is mandatory");
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
	public boolean supports(Class<?> authentication)
	{
		return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
	}


}
