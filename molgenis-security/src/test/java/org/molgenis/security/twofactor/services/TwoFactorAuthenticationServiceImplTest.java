package org.molgenis.security.twofactor.services;

import org.junit.Ignore;
import org.molgenis.data.DataService;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.populate.IdGeneratorImpl;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.twofactor.*;
import org.molgenis.security.twofactor.exceptions.TooManyLoginAttemptsException;
import org.molgenis.security.user.UserService;
import org.molgenis.security.user.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

import java.time.Instant;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;
import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

@ContextConfiguration(classes = { TwoFactorAuthenticationServiceImplTest.Config.class, })
public class TwoFactorAuthenticationServiceImplTest extends AbstractTestNGSpringContextTests
{
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
			return new OTPServiceImpl();
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

		@Bean
		public org.molgenis.auth.User user()
		{
			return mock(org.molgenis.auth.User.class);
		}

		@Bean
		public UserSecret userSecret()
		{
			return mock(UserSecret.class);
		}
	}

	@Autowired
	private org.molgenis.auth.User molgenisUser;

	@Autowired
	private UserSecret userSecret;

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

	@BeforeClass
	public void setUpBeforeClass()
	{
		UserDetails userDetails = new User("molgenisUser", "", Lists.newArrayList());
		Authentication authentication = mock(Authentication.class);
		when(authentication.getPrincipal()).thenReturn(userDetails);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(userDetails);
		when(molgenisUser.getUsername()).thenReturn("molgenisUser");
		when(userService.getUser(userDetails.getUsername())).thenReturn(molgenisUser);
		when(runAsSystem(() -> dataService.findOne(UserSecretMetaData.USERSECRET,
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
	public void setSecretKeyTest()
	{
		String secretKey = "secretKey";
		when(userSecretFactory.create()).thenReturn(userSecret);
		twoFactorAuthenticationService.setSecretKey(secretKey);
	}

	@Test
	public void isConfiguredForUserTest()
	{
		when(userSecret.getSecret()).thenReturn("secretKey");
		boolean isConfigured = twoFactorAuthenticationService.isConfiguredForUser();
		assertEquals(true, isConfigured);
	}

	/**
	 * FIXME(SH): how do we implement this test properly
	 */
	@Test
	@Ignore
	public void isVerificationCodeValidForUserTest()
	{
		String verificationCode = "123467";
		when(appSettings.getTwoFactorAuthentication()).thenReturn(TwoFactorAuthenticationSetting.ENABLED.toString());
		when(userSecret.getSecret()).thenReturn("secretKey");
		boolean isValid = twoFactorAuthenticationService.isVerificationCodeValidForUser(verificationCode);
		assertEquals(false, isValid);
	}

	@Test(expectedExceptions = TooManyLoginAttemptsException.class)
	public void testUserIsBlocked()
	{
		when(userSecret.getLastFailedAuthentication()).thenReturn(Instant.now());
		when(userSecret.getFailedLoginAttempts()).thenReturn(3);
		boolean isBlocked = twoFactorAuthenticationService.userIsBlocked();
		assertEquals(true, isBlocked);
	}

}
