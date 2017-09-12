package org.molgenis.security.twofactor.auth;

import org.molgenis.security.twofactor.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

@ContextConfiguration(classes = { TwoFactorAuthenticationProviderTest.Config.class })
@TestExecutionListeners(listeners = WithSecurityContextTestExecutionListener.class)
public class TwoFactorAuthenticationProviderTest extends AbstractTestNGSpringContextTests
{

	private final static String USERNAME = "admin";
	private final static String ROLE_SU = "SU";
	@Autowired
	private OtpService otpService;
	@Autowired
	private TwoFactorAuthenticationProvider twoFactorAuthenticationProvider;
	@Autowired
	private TwoFactorAuthenticationService twoFactorAuthenticationService;

	@Test
	@WithMockUser(value = USERNAME, roles = ROLE_SU)
	public void testAuthentication2faIsConfigured()
	{
		TwoFactorAuthenticationToken authToken = new TwoFactorAuthenticationToken("123456", null);
		assertFalse(authToken.isAuthenticated());

		when(twoFactorAuthenticationService.isConfiguredForUser()).thenReturn(true);
		when(twoFactorAuthenticationService.isVerificationCodeValidForUser(authToken.getVerificationCode())).thenReturn(
				true);

		Authentication auth = twoFactorAuthenticationProvider.authenticate(authToken);
		assertNotNull(auth);
		assertTrue(auth.isAuthenticated());
		assertEquals(auth.getName(), "admin");
	}

	@Test
	@WithMockUser(value = USERNAME, roles = ROLE_SU)
	public void testAuthentication2fIsNotConfigured()
	{
		TwoFactorAuthenticationToken authToken = new TwoFactorAuthenticationToken("123456", "dsda545ds4dsa456");
		assertFalse(authToken.isAuthenticated());

		when(twoFactorAuthenticationService.isConfiguredForUser()).thenReturn(false);
		when(otpService.tryVerificationCode(authToken.getVerificationCode(), authToken.getSecretKey())).thenReturn(
				true);
		when(twoFactorAuthenticationService.isVerificationCodeValidForUser(authToken.getVerificationCode())).thenReturn(
				true);

		Authentication auth = twoFactorAuthenticationProvider.authenticate(authToken);
		assertNotNull(auth);
		assertTrue(auth.isAuthenticated());
		assertEquals(auth.getName(), "admin");
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testAuthenticateInvalidToken()
	{
		twoFactorAuthenticationProvider.authenticate(
				new UsernamePasswordAuthenticationToken("123456", "dsda545ds4dsa456"));
	}

	@Configuration
	static class Config
	{
		@Bean
		public TwoFactorAuthenticationProvider twoFactorAuthenticationProvider()
		{
			return new TwoFactorAuthenticationProviderImpl(twoFactorAuthenticationService(), otpService(),
					recoveryService());
		}

		@Bean
		public TwoFactorAuthenticationService twoFactorAuthenticationService()
		{
			return mock(TwoFactorAuthenticationServiceImpl.class);
		}

		@Bean
		public RecoveryService recoveryService()
		{
			return mock(RecoveryServiceImpl.class);
		}

		@Bean
		public OtpService otpService()
		{
			return mock(OtpServiceImpl.class);
		}
	}
}
