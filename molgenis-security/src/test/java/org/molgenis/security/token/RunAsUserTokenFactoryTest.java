package org.molgenis.security.token;

import org.molgenis.data.MolgenisDataAccessException;
import org.springframework.security.core.userdetails.UserDetails;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RunAsUserTokenFactoryTest
{

	@Test(expectedExceptions = MolgenisDataAccessException.class)
	public void testTokenForDisabledUser()
	{
		UserDetails userDetails = mock(UserDetails.class);
		when(userDetails.isEnabled()).thenReturn(false);
//		TokenFactory.getRestAuthenticationToken(userDetails, "test");
	}
}
