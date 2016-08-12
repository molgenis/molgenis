package org.molgenis.security.user;

import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;

/**
 * Checks user details during the authentication process
 */
public class MolgenisUserDetailsChecker implements UserDetailsChecker
{
	protected final MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();

	@Override
	public void check(UserDetails userDetails)
	{
		if (!userDetails.isEnabled())
		{
			throw new DisabledException(
					messages.getMessage("AccountStatusUserDetailsChecker.disabled", "User is not active") + ' '
							+ userDetails.toString());
		}
	}
}
