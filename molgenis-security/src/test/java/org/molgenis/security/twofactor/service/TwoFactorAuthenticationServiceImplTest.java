package org.molgenis.security.twofactor.service;

import org.molgenis.data.DataService;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.populate.IdGeneratorImpl;
import org.molgenis.data.security.auth.User;
import org.molgenis.data.security.user.UserService;
import org.molgenis.data.security.user.UserServiceImpl;
import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.security.twofactor.exceptions.TooManyLoginAttemptsException;
import org.molgenis.security.twofactor.model.UserSecret;
import org.molgenis.security.twofactor.model.UserSecretFactory;
import org.molgenis.security.twofactor.model.UserSecretMetaData;
import org.molgenis.settings.AppSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.Instant;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

@ContextConfiguration(classes = { TwoFactorAuthenticationServiceImplTest.Config.class })
@TestExecutionListeners(listeners = WithSecurityContextTestExecutionListener.class)
public class TwoFactorAuthenticationServiceImplTest extends AbstractTestNGSpringContextTests
{
	private final static String USERNAME = "molgenisUser";
	private final static String ROLE_SU = "SU";
	@Autowired
	private DataService dataService;
	@Autowired
	private UserService userService;
	@Autowired
	private UserSecretFactory userSecretFactory;
	@Autowired
	private TwoFactorAuthenticationService twoFactorAuthenticationService;
	private User molgenisUser = mock(User.class);
	private UserSecret userSecret = mock(UserSecret.class);

	@WithMockUser(value = USERNAME, roles = ROLE_SU)
	@BeforeMethod
	@SuppressWarnings("unchecked")
	public void setUpBeforeMethod()
	{
		when(molgenisUser.getUsername()).thenReturn(USERNAME);
		when(molgenisUser.getId()).thenReturn("1324");
		when(userService.getUser(USERNAME)).thenReturn(molgenisUser);
		when(dataService.query(UserSecretMetaData.USER_SECRET, UserSecret.class)
						.eq(UserSecretMetaData.USER_ID, molgenisUser.getId())
						.findOne()).thenReturn(userSecret);
	}

	@Test
	public void generateSecretKeyTest()
	{
		String key = twoFactorAuthenticationService.generateSecretKey();
		assertTrue(key.matches("^[a-z0-9]+$"));
	}

	@Test
	public void generateWrongSecretKeyTest()
	{
		String key = twoFactorAuthenticationService.generateSecretKey();
		assertTrue(!key.matches("^[A-Z0-9]+$"));
	}

	@Test
	@WithMockUser(value = USERNAME, roles = ROLE_SU)
	public void setSecretKeyTest()
	{
		String secretKey = "secretKey";
		when(userSecretFactory.create()).thenReturn(userSecret);
		twoFactorAuthenticationService.saveSecretForUser(secretKey);
	}

	@Test
	@WithMockUser(value = USERNAME, roles = ROLE_SU)
	public void isConfiguredForUserTest()
	{
		when(userSecret.getSecret()).thenReturn("secretKey");
		boolean isConfigured = twoFactorAuthenticationService.isConfiguredForUser();
		assertEquals(true, isConfigured);
	}

	@Test(expectedExceptions = TooManyLoginAttemptsException.class)
	@WithMockUser(value = USERNAME, roles = ROLE_SU)
	public void testUserIsBlocked()
	{
		when(userSecret.getLastFailedAuthentication()).thenReturn(Instant.now());
		when(userSecret.getFailedLoginAttempts()).thenReturn(3);
		boolean isBlocked = twoFactorAuthenticationService.userIsBlocked();
		assertEquals(true, isBlocked);
	}

	@Test
	@WithMockUser(value = USERNAME, roles = ROLE_SU)
	public void testDisableForUser()
	{
		when(userService.getUser(molgenisUser.getUsername())).thenReturn(molgenisUser);
		when(dataService.query(UserSecretMetaData.USER_SECRET, UserSecret.class)
						.eq(UserSecretMetaData.USER_ID, molgenisUser.getId())
						.findOne()).thenReturn(userSecret);
		twoFactorAuthenticationService.disableForUser();
	}

	@Configuration
	static class Config
	{
		@Bean
		public TwoFactorAuthenticationService twoFactorAuthenticationService()
		{
			return new TwoFactorAuthenticationServiceImpl(otpService(), dataService(), userService(), idGenerator(),
					userSecretFactory());
		}

		@Bean
		public OtpService otpService()
		{
			return new OtpServiceImpl(appSettings());
		}

		@Bean
		public DataService dataService()
		{
			return mock(DataServiceImpl.class, RETURNS_DEEP_STUBS);
		}

		@Bean
		public UserService userService()
		{
			return mock(UserServiceImpl.class);
		}

		@Bean
		public IdGenerator idGenerator()
		{
			return new IdGeneratorImpl();
		}

		@Bean
		public AppSettings appSettings()
		{
			return mock(AppSettings.class);
		}

		@Bean
		public UserSecretFactory userSecretFactory()
		{
			return mock(UserSecretFactory.class);
		}

	}
}
