package org.molgenis.security.twofactor;

import org.molgenis.data.DataService;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.populate.IdGeneratorImpl;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.twofactor.exceptions.TooManyLoginAttemptsException;
import org.molgenis.security.user.UserService;
import org.molgenis.security.user.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.Instant;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;
import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

@ContextConfiguration(classes = { TwoFactorAuthenticationServiceImplTest.Config.class })
@TestExecutionListeners(listeners = WithSecurityContextTestExecutionListener.class)
public class TwoFactorAuthenticationServiceImplTest extends AbstractTestNGSpringContextTests
{
	private final static String USERNAME = "molgenisUser";
	private final static String ROLE_SU = "SU";

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
		public OTPService otpService()
		{
			return new OTPServiceImpl(appSettings());
		}

		@Bean
		public DataService dataService()
		{
			return mock(DataServiceImpl.class);
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

	@Autowired
	private DataService dataService;

	@Autowired
	private UserService userService;

	@Autowired
	private AppSettings appSettings;

	@Autowired
	private UserSecretFactory userSecretFactory;

	@Autowired
	private TwoFactorAuthenticationService twoFactorAuthenticationService;

	private org.molgenis.auth.User molgenisUser = mock(org.molgenis.auth.User.class);
	private UserSecret userSecret = mock(UserSecret.class);

	@WithMockUser(value = USERNAME, roles = ROLE_SU)
	@WithUserDetails(USERNAME)
	@BeforeMethod
	public void setUpBeforeMethod()
	{
		when(molgenisUser.getUsername()).thenReturn(USERNAME);
		when(userService.getUser(USERNAME)).thenReturn(molgenisUser);
		when(runAsSystem(() -> dataService.findOne(UserSecretMetaData.USER_SECRET,
				new QueryImpl<UserSecret>().eq(UserSecretMetaData.USER_ID, molgenisUser.getId()),
				UserSecret.class))).thenReturn(userSecret);
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
	@WithUserDetails(USERNAME)
	public void setSecretKeyTest()
	{
		String secretKey = "secretKey";
		when(userSecretFactory.create()).thenReturn(userSecret);
		twoFactorAuthenticationService.saveSecretForUser(secretKey);
	}

	@Test
	@WithMockUser(value = USERNAME, roles = ROLE_SU)
	@WithUserDetails(USERNAME)
	public void isConfiguredForUserTest()
	{
		when(userSecret.getSecret()).thenReturn("secretKey");
		boolean isConfigured = twoFactorAuthenticationService.isConfiguredForUser();
		assertEquals(true, isConfigured);
	}

	@Test(expectedExceptions = TooManyLoginAttemptsException.class)
	@WithMockUser(value = USERNAME, roles = ROLE_SU)
	@WithUserDetails(USERNAME)
	public void testUserIsBlocked()
	{
		when(userSecret.getLastFailedAuthentication()).thenReturn(Instant.now());
		when(userSecret.getFailedLoginAttempts()).thenReturn(3);
		boolean isBlocked = twoFactorAuthenticationService.userIsBlocked();
		assertEquals(true, isBlocked);
	}

	@Test
	@WithMockUser(value = USERNAME, roles = ROLE_SU)
	@WithUserDetails(USERNAME)
	public void testDisableForUser()
	{
		when(userService.getUser(molgenisUser.getUsername())).thenReturn(molgenisUser);
		when(dataService.findOne(UserSecretMetaData.USER_SECRET,
				new QueryImpl<UserSecret>().eq(UserSecretMetaData.USER_ID, molgenisUser.getId()),
				UserSecret.class)).thenReturn(userSecret);
		twoFactorAuthenticationService.disableForUser();
	}

}
