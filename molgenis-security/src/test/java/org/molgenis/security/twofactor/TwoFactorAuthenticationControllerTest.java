package org.molgenis.security.twofactor;

import org.molgenis.security.login.MolgenisLoginController;
import org.molgenis.security.twofactor.auth.RecoveryAuthenticationProvider;
import org.molgenis.security.twofactor.auth.RecoveryAuthenticationProviderImpl;
import org.molgenis.security.twofactor.auth.TwoFactorAuthenticationProviderImpl;
import org.molgenis.security.twofactor.service.*;
import org.molgenis.settings.AppSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

@ContextConfiguration(classes = { TwoFactorAuthenticationControllerTest.Config.class })
@TestExecutionListeners(listeners = WithSecurityContextTestExecutionListener.class)
public class TwoFactorAuthenticationControllerTest extends AbstractTestNGSpringContextTests
{
	private final static String USERNAME = "molgenisUser";
	private final static String ROLE_SU = "SU";

	@Autowired
	private AppSettings appSettings;
	@Autowired
	private TwoFactorAuthenticationService twoFactorAuthenticationService;
	@Autowired
	private TwoFactorAuthenticationController twoFactorAuthenticationController;

	@Test
	@WithMockUser(value = USERNAME, roles = ROLE_SU)
	public void activationExceptionTest() throws Exception
	{
		when(twoFactorAuthenticationService.generateSecretKey()).thenReturn("secretKey");
		when(appSettings.getTitle()).thenReturn("MOLGENIS");
		Model model = new ExtendedModelMap();
		String viewTemplate = twoFactorAuthenticationController.activation(model);
		assertEquals("view-2fa-activation-modal", viewTemplate);
	}

	@Test
	public void configuredExceptionTest() throws Exception
	{
		Model model = new ExtendedModelMap();
		String viewTemplate = twoFactorAuthenticationController.configured(model);
		assertEquals("view-2fa-configured-modal", viewTemplate);
	}

	@Test
	@WithMockUser(value = USERNAME, roles = ROLE_SU)
	public void authenticateExceptionTest() throws Exception
	{
		String secretKey = "secretKey";
		String verificationCode = "123456";
		when(appSettings.getTitle()).thenReturn("MOLGENIS");
		Model model = new ExtendedModelMap();
		String viewTemplate = twoFactorAuthenticationController.authenticate(model, verificationCode, secretKey);
		assertEquals(secretKey, model.asMap().get(TwoFactorAuthenticationController.ATTRIBUTE_2FA_SECRET_KEY));
		assertEquals("Invalid verification code entered",
				model.asMap().get(MolgenisLoginController.ERROR_MESSAGE_ATTRIBUTE));
		assertEquals("view-2fa-activation-modal", viewTemplate);

	}

	@Test
	public void validateVerificationCodeAndAuthenticateExceptionTest() throws Exception
	{
		String verificationCode = "123456";
		Model model = new ExtendedModelMap();
		String viewTemplate = twoFactorAuthenticationController.validate(model, verificationCode);
		assertEquals("view-2fa-configured-modal", viewTemplate);
	}

	@Test
	@WithMockUser(value = USERNAME, roles = ROLE_SU)
	public void testRecoverAccount()
	{
		String recoveryCodeId = "123456";
		Model model = new ExtendedModelMap();
		String viewTemplate = twoFactorAuthenticationController.recoverAccount(model, recoveryCodeId);
		assertEquals("redirect:/", viewTemplate);
	}

	@Configuration
	public static class Config
	{
		@Bean
		public TwoFactorAuthenticationService twoFactorAuthenticationService()
		{
			return mock(TwoFactorAuthenticationServiceImpl.class);
		}

		@Bean
		public OtpService otpService()
		{
			return new OtpServiceImpl(appSettings());
		}

		@Bean
		public RecoveryService recoveryService()
		{
			return mock(RecoveryService.class);
		}

		@Bean
		public TwoFactorAuthenticationProviderImpl twoFactorAuthenticationProvider()
		{
			return new TwoFactorAuthenticationProviderImpl(twoFactorAuthenticationService(), otpService(),
					recoveryService());
		}

		@Bean
		public RecoveryAuthenticationProvider recoveryAuthenticationProvider()
		{
			return new RecoveryAuthenticationProviderImpl(recoveryService());
		}

		@Bean
		public AppSettings appSettings()
		{
			return mock(AppSettings.class);
		}

		@Bean
		public TwoFactorAuthenticationController twoFactorAuthenticationController()
		{
			return new TwoFactorAuthenticationController(twoFactorAuthenticationProvider(),
					twoFactorAuthenticationService(), recoveryAuthenticationProvider(), otpService());
		}
	}

}
