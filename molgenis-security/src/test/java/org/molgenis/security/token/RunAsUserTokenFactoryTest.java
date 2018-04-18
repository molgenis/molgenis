package org.molgenis.security.token;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class RunAsUserTokenFactoryTest
{
	private RunAsUserTokenFactory runAsUserTokenFactory;
	private UserDetailsChecker userDetailsChecker;

	@BeforeMethod
	public void setupBeforeMethod()
	{
		userDetailsChecker = mock(UserDetailsChecker.class);
		runAsUserTokenFactory = new RunAsUserTokenFactory(userDetailsChecker);
	}

	@Test
	public void testTokenForDisabledUser()
	{
		UserDetails userDetails = mock(UserDetails.class);
		runAsUserTokenFactory.create("test", userDetails, null);
		verify(userDetailsChecker).check(userDetails);
	}
}
