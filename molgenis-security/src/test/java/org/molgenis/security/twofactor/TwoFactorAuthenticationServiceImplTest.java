package org.molgenis.security.twofactor;

import org.junit.Ignore;
import org.molgenis.auth.UserMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.data.support.QueryImpl;
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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.auth.UserMetaData.USER;
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
			return new TwoFactorAuthenticationServiceImpl(appSettings(), otpService(), dataService(), idGenerator());
		}

		@Bean
		public OTPService otpService()
		{
			return mock(OTPServiceImpl.class);
		}

		@Bean
		public AppSettings appSettings()
		{
			return mock(AppSettings.class);
		}

		@Bean
		public DataService dataService()
		{
			return mock(DataServiceImpl.class);
		}

		@Bean
		public IdGenerator idGenerator()
		{
			return mock(IdGenerator.class);
		}

		@Bean
		RecoveryCodeFactory recoveryCodeFactory()
		{
			return mock(RecoveryCodeFactory.class);
		}

		@Bean
		public org.molgenis.auth.User user()
		{
			return mock(org.molgenis.auth.User.class);
		}
	}

	@Autowired
	private org.molgenis.auth.User molgenisUser;

	@Autowired
	private DataService dataService;

	@Autowired
	private AppSettings appSettings;

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
		when(dataService.findOne(USER,
				new QueryImpl<org.molgenis.auth.User>().eq(UserMetaData.USERNAME, userDetails.getUsername()),
				org.molgenis.auth.User.class)).thenReturn(molgenisUser);
	}

	@Test
	public void generateSecretKeyTest()
	{
		String key = twoFactorAuthenticationService.generateSecretKey();
		assertTrue(key.matches("^[A-Z0-9]+$"));
	}

	@Test
	public void generateWrongSecretKeyTest()
	{
		String key = twoFactorAuthenticationService.generateSecretKey();
		assertTrue(!key.matches("^[a-z0-9]+$"));
	}

	@Test
	public void setSecretKeyTest()
	{
		String secretKey = "secretKey";
		twoFactorAuthenticationService.setSecretKey(secretKey);
	}

	@Test
	public void isConfiguredForUserTest()
	{
		when(molgenisUser.getSecretKey()).thenReturn("secretKey");
		boolean isConfigured = twoFactorAuthenticationService.isConfiguredForUser();
		assertEquals(true, isConfigured);
	}

	/**
	 * TODO(SH): needs to be implmentend in {@link UserMetaData}
	 */
	@Test
	@Ignore
	public void isEnabledForUserTest()
	{
		when(molgenisUser.getSecretKey()).thenReturn("secretKey");
		boolean isEnabled = twoFactorAuthenticationService.isEnabledForUser();
		assertEquals(true, isEnabled);
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
		when(molgenisUser.getSecretKey()).thenReturn("secretKey");
		boolean isValid = twoFactorAuthenticationService.isVerificationCodeValidForUser(verificationCode);
		assertEquals(false, isValid);
	}

}
