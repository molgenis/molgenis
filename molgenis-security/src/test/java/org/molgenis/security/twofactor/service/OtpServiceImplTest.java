package org.molgenis.security.twofactor.service;

import org.molgenis.security.twofactor.exceptions.InvalidVerificationCodeException;
import org.molgenis.settings.AppSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;

@ContextConfiguration(classes = { OtpServiceImplTest.Config.class })
@TestExecutionListeners(listeners = WithSecurityContextTestExecutionListener.class)
public class OtpServiceImplTest extends AbstractTestNGSpringContextTests
{

	private final static String USERNAME = "molgenisUser";
	private final static String ROLE_SU = "SU";

	@Autowired
	private OtpService otpService;
	@Autowired
	private AppSettings appSettings;

	@Test(expectedExceptions = InvalidVerificationCodeException.class)
	public void testTryVerificationKeyFailed()
	{
		boolean isValid = otpService.tryVerificationCode("", "secretKey");
		assertFalse(isValid);
	}

	@Test
	@WithMockUser(value = USERNAME, roles = ROLE_SU)
	public void testGetAuthenticatorURI()
	{
		when(appSettings.getTitle()).thenReturn("MOLGENIS");
		String uri = otpService.getAuthenticatorURI("secretKey");
		assertFalse(uri.isEmpty());
	}

	@Configuration
	static class Config
	{
		@Bean
		public OtpService otpService()
		{
			return new OtpServiceImpl(appSettings());
		}

		@Bean
		public AppSettings appSettings()
		{
			return mock(AppSettings.class);
		}

	}

}
