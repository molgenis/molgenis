package org.molgenis.security.twofactor.auth;

import org.molgenis.security.twofactor.service.RecoveryService;
import org.molgenis.security.twofactor.service.RecoveryServiceImpl;
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

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.*;

@ContextConfiguration(classes = { RecoveryAuthenticationProviderTest.Config.class })
@TestExecutionListeners(listeners = WithSecurityContextTestExecutionListener.class)
public class RecoveryAuthenticationProviderTest extends AbstractTestNGSpringContextTests
{

	private final static String USERNAME = "admin";
	private final static String ROLE_SU = "SU";

	@Autowired
	private RecoveryAuthenticationProvider recoveryAuthenticationProvider;
	@Autowired
	private RecoveryService recoveryService;

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testAuthenticateInvalidToken()
	{
		recoveryAuthenticationProvider.authenticate(new UsernamePasswordAuthenticationToken("dsda545ds4dsa456", ""));
	}

	@Test
	@WithMockUser(value = USERNAME, roles = ROLE_SU)
	public void testAuthentication()
	{
		RecoveryAuthenticationToken authToken = new RecoveryAuthenticationToken("dsda545ds4dsa456");
		assertFalse(authToken.isAuthenticated());

		doNothing().when(recoveryService).useRecoveryCode(authToken.getRecoveryCode());

		Authentication auth = recoveryAuthenticationProvider.authenticate(authToken);
		assertNotNull(auth);
		assertTrue(auth.isAuthenticated());
		assertEquals(auth.getName(), "admin");
	}

	@Configuration
	static class Config
	{
		@Bean
		public RecoveryAuthenticationProvider recoveryAuthenticationProvider()
		{
			return new RecoveryAuthenticationProviderImpl(recoveryService());
		}

		@Bean
		public RecoveryService recoveryService()
		{
			return mock(RecoveryServiceImpl.class);
		}
	}

}
