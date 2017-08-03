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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

@ContextConfiguration(classes = { TwoFactorAuthenticationProviderTest.Config.class })
public class TwoFactorAuthenticationProviderTest extends AbstractTestNGSpringContextTests
{

	@Configuration
	static class Config
	{
		@Bean
		public TwoFactorAuthenticationProvider twoFactorAuthenticationProvider()
		{
			return new TwoFactorAuthenticationProviderImpl(twoFactorAuthenticationService(), new OTPServiceImpl());
		}

		@Bean
		public TwoFactorAuthenticationService twoFactorAuthenticationService()
		{
			return mock(TwoFactorAuthenticationServiceImpl.class);
		}
	}

	@Autowired
	private TwoFactorAuthenticationProvider twoFactorAuthenticationProvider;

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
		TwoFactorAuthenticationToken authToken = new TwoFactorAuthenticationToken("123456", "dsda545ds4dsa456");
		assertFalse(authToken.isAuthenticated());

		when(twoFactorAuthenticationService.isConfiguredForUser()).thenReturn(true);
		when(twoFactorAuthenticationService.isVerificationCodeValidForUser(authToken.getVerificationCode())).thenReturn(
				true);

		Authentication auth = twoFactorAuthenticationProvider.authenticate(authToken);
		assertNotNull(auth);
		assertTrue(auth.isAuthenticated());
		assertEquals(auth.getName(), "admin");
	}

	@Test(expectedExceptions = AuthenticationException.class)
	public void testAuthenticateInvalidToken()
	{
		twoFactorAuthenticationProvider.authenticate(new TwoFactorAuthenticationToken("123456", "dsda545ds4dsa456"));
	}
}
