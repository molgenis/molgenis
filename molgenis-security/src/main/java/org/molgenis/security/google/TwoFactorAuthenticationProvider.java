package org.molgenis.security.google;

import org.molgenis.auth.User;
import org.molgenis.auth.UserMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.runas.RunAsSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Objects;

import static org.molgenis.auth.UserMetaData.USER;

/**
 *
 *
 *
 * @autor sido
 */
public class TwoFactorAuthenticationProvider implements AuthenticationProvider
{

	private final DataService dataService;

	public TwoFactorAuthenticationProvider(DataService dataService) {
		this.dataService =  Objects.requireNonNull(dataService);
	}

	@Override
	@RunAsSystem
	public Authentication authenticate(Authentication authentication) throws AuthenticationException
	{
		if (!supports(authentication.getClass()))
		{
			throw new IllegalArgumentException("Only UsernamePasswordAuthenticationToken is supported");
		}

		UsernamePasswordAuthenticationToken authToken = (UsernamePasswordAuthenticationToken) authentication;

		if(authToken.getName() != null)
		{
			try
			{
				final String verificationCode = ((TwoFactorWebAuthenticationDetails) authentication.getDetails()).getVerificationCode();
				final User user = dataService.findOne(USER, new QueryImpl<User>().eq(UserMetaData.USERNAME, authentication.getName()),  User.class);

				if (user == null)
				{
					throw new UsernameNotFoundException("unknown user '" + authentication.getName() + "'");
				}

//				if(user.isUsing2fa()) {
//					final Totp totp = new Totp(user.getSecret());
//					if (!isValidLong(verificationCode) || !totp.verify(verificationCode)) {
//						throw new BadCredentialsException("Invalid verfication code");
//					}
//				}

				authToken = new UsernamePasswordAuthenticationToken(user, authentication.getCredentials(), authentication.getAuthorities());

			}
			catch (Throwable e)
			{
				throw new RuntimeException(e);
			}
		} else {
			throw new BadCredentialsException("No valid username or password is entered");
		}

		return authToken;
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
