package org.molgenis.security.user;

import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserDetailsCheckerTest
{
	@Test
	public void check_enabledUser()
	{
		UserDetails userDetails = when(mock(UserDetails.class).isEnabled()).thenReturn(true).getMock();
		new MolgenisUserDetailsChecker().check(userDetails);
	}

	@Test(expectedExceptions = DisabledException.class)
	public void check_disabledUser()
	{
		UserDetails userDetails = when(mock(UserDetails.class).isEnabled()).thenReturn(false).getMock();
		new MolgenisUserDetailsChecker().check(userDetails);
	}
}
