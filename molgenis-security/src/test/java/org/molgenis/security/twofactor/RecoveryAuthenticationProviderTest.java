package org.molgenis.security.twofactor;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

@ContextConfiguration(classes = { RecoveryAuthenticationProviderTest.Config.class })
public class RecoveryAuthenticationProviderTest extends AbstractTestNGSpringContextTests
{

	@Configuration
	static class Config
	{
		@Bean
		public RecoveryAuthenticationProvider recoveryAuthenticationProvider()
		{
			return new RecoveryAuthenticationProviderImpl(twoFactorAuthenticationService());
		}

		@Bean
		public TwoFactorAuthenticationService twoFactorAuthenticationService()
		{
			return mock(TwoFactorAuthenticationServiceImpl.class);
		}
	}

	@Autowired
	private RecoveryAuthenticationProvider recoveryAuthenticationProvider;

	@Autowired
	private TwoFactorAuthenticationService twoFactorAuthenticationService;

	@Test
	public void testAuthentication()
	{
		UserDetails userDetails = mock(UserDetails.class);
		when(userDetails.getUsername()).thenReturn("admin");
		when(userDetails.getPassword()).thenReturn("admin");

		UsernamePasswordAuthenticationToken originalToken = new UsernamePasswordAuthenticationToken(userDetails,
				"admin", Lists.newArrayList(new SimpleGrantedAuthority("admin")));
		SecurityContextHolder.getContext().setAuthentication(originalToken);

		RecoveryAuthenticationToken authToken = new RecoveryAuthenticationToken("dsda545ds4dsa456");
		assertFalse(authToken.isAuthenticated());

		doNothing().when(twoFactorAuthenticationService).useRecoveryCode(authToken.getRecoveryCode());

		Authentication auth = recoveryAuthenticationProvider.authenticate(authToken);
		assertNotNull(auth);
		assertTrue(auth.isAuthenticated());
		assertEquals(auth.getName(), "admin");
	}

	@Test(expectedExceptions = AuthenticationException.class)
	public void testAuthenticateInvalidToken()
	{
		recoveryAuthenticationProvider.authenticate(new RecoveryAuthenticationToken("dsda545ds4dsa456"));
	}
}
