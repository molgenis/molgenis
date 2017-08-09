package org.molgenis.security.twofactor.service;

import org.molgenis.data.settings.AppSettings;
import org.molgenis.security.twofactor.exceptions.InvalidVerificationCodeException;
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
import static org.testng.Assert.assertEquals;

@ContextConfiguration(classes = { OTPServiceImplTest.Config.class })
@TestExecutionListeners(listeners = WithSecurityContextTestExecutionListener.class)
public class OTPServiceImplTest extends AbstractTestNGSpringContextTests
{

	private final static String USERNAME = "molgenisUser";
	private final static String ROLE_SU = "SU";

	@Configuration
	static class Config
	{
		@Bean
		public OTPService otpService()
		{
			return new OTPServiceImpl(appSettings());
		}

		@Bean
		public AppSettings appSettings()
		{
			return mock(AppSettings.class);
		}

	}

	@Autowired
	private OTPService otpService;

	@Test(expectedExceptions = InvalidVerificationCodeException.class)
	public void testTryVerificationKeyFailed()
	{
		boolean isValid = otpService.tryVerificationCode("", "secretKey");
		assertEquals(false, isValid);
	}

	@Test
	@WithMockUser(value = USERNAME, roles = ROLE_SU)
	public void testGetAuthenticatorURI()
	{
		String uri = otpService.getAuthenticatorURI("secretKey");
		assertEquals(true, !uri.isEmpty());
	}

}
