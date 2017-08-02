package org.molgenis.security.twofactor;

import org.molgenis.security.google.GoogleAuthenticatorService;
import org.molgenis.security.google.GoogleAuthenticatorServiceImpl;
import org.molgenis.security.login.MolgenisLoginController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

@ContextConfiguration(classes = { TwoFactorAuthenticationControllerTest.Config.class })
public class TwoFactorAuthenticationControllerTest extends AbstractTestNGSpringContextTests
{

	@Configuration
	public static class Config
	{
		@Bean
		public TwoFactorAuthenticationService twoFactorAuthenticationService()
		{
			return mock(TwoFactorAuthenticationServiceImpl.class);
		}

		@Bean
		public OTPService otpService()
		{
			return new OTPServiceImpl();
		}

		@Bean
		public GoogleAuthenticatorService googleAuthenticatorService()
		{
			return new GoogleAuthenticatorServiceImpl();
		}

		@Bean
		public TwoFactorAuthenticationProviderImpl twoFactorAuthenticationProvider()
		{
			return new TwoFactorAuthenticationProviderImpl(twoFactorAuthenticationService(), otpService());
		}

		@Bean
		public RecoveryAuthenticationProvider recoveryAuthenticationProvider()
		{
			return mock(RecoveryAuthenticationProvider.class);
		}

		@Bean
		public AuthenticationManager authenticationManager()
		{
			return mock(AuthenticationManager.class);
		}

		@Bean
		public TwoFactorAuthenticationController twoFactorAuthenticationController()
		{
			return new TwoFactorAuthenticationController(twoFactorAuthenticationProvider(),
					twoFactorAuthenticationService(), recoveryAuthenticationProvider(), googleAuthenticatorService());
		}
	}

	@Autowired
	private TwoFactorAuthenticationService twoFactorAuthenticationService;

	@Autowired
	private TwoFactorAuthenticationController twoFactorAuthenticationController;

	@Test
	public void initialExceptionTest() throws Exception
	{
		when(twoFactorAuthenticationService.generateSecretKey()).thenReturn("secretKey");
		Model model = new ExtendedModelMap();
		String viewTemplate = twoFactorAuthenticationController.initial(model);
		assertEquals(true, model.asMap().get(TwoFactorAuthenticationController.ATTRIBUTE_2FA_IS_INITIAL));
		assertEquals("Setup 2 factor authentication",
				model.asMap().get(TwoFactorAuthenticationController.ATTRIBUTE_HEADER_2FA_IS_INITIAL));
		assertEquals("view-login", viewTemplate);
	}

	@Test
	public void enabledExceptionTest() throws Exception
	{
		Model model = new ExtendedModelMap();
		String viewTemplate = twoFactorAuthenticationController.configured(model);
		assertEquals(true, model.asMap().get(TwoFactorAuthenticationController.ATTRIBUTE_2FA_IS_CONFIGURED));
		assertEquals("Verification code",
				model.asMap().get(TwoFactorAuthenticationController.ATTRIBUTE_HEADER_2FA_IS_CONFIGURED));
		assertEquals("view-login", viewTemplate);

	}

	@Test
	public void setSecretExceptionTest() throws Exception
	{
		String secretKey = "secretKey";
		String verificationCode = "123456";
		Model model = new ExtendedModelMap();
		String viewTemplate = twoFactorAuthenticationController.setSecret(model, verificationCode, secretKey);
		assertEquals(true, model.asMap().get(TwoFactorAuthenticationController.ATTRIBUTE_2FA_IS_INITIAL));
		assertEquals(secretKey, model.asMap().get(TwoFactorAuthenticationController.ATTRIBUTE_2FA_SECRET_KEY));
		assertEquals("Setup 2 factor authentication",
				model.asMap().get(TwoFactorAuthenticationController.ATTRIBUTE_HEADER_2FA_IS_INITIAL));
		assertEquals("No valid verification code entered!",
				model.asMap().get(MolgenisLoginController.ERROR_MESSAGE_ATTRIBUTE));
		assertEquals("view-login", viewTemplate);

	}

	@Test
	public void validateVerificationCodeAndAuthenticateExceptionTest() throws Exception
	{
		String verificationCode = "123456";
		Model model = new ExtendedModelMap();
		String viewTemplate = twoFactorAuthenticationController.validateVerificationCodeAndAuthenticate(model,
				verificationCode);
		assertEquals(true, model.asMap().get(TwoFactorAuthenticationController.ATTRIBUTE_2FA_IS_CONFIGURED));
		assertEquals("view-login", viewTemplate);

	}

}
