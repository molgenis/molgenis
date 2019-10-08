package org.molgenis.security.twofactor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.security.login.MolgenisLoginController.ERROR_MESSAGE_ATTRIBUTE;
import static org.molgenis.security.twofactor.TwoFactorAuthenticationController.ATTRIBUTE_2FA_SECRET_KEY;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.security.twofactor.auth.RecoveryAuthenticationProvider;
import org.molgenis.security.twofactor.auth.RecoveryAuthenticationProviderImpl;
import org.molgenis.security.twofactor.auth.TwoFactorAuthenticationProvider;
import org.molgenis.security.twofactor.auth.TwoFactorAuthenticationToken;
import org.molgenis.security.twofactor.exceptions.InvalidVerificationCodeException;
import org.molgenis.security.twofactor.service.OtpService;
import org.molgenis.security.twofactor.service.RecoveryService;
import org.molgenis.security.twofactor.service.TwoFactorAuthenticationService;
import org.molgenis.security.twofactor.service.TwoFactorAuthenticationServiceImpl;
import org.molgenis.settings.AppSettings;
import org.molgenis.test.AbstractMockitoSpringContextTests;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.annotation.SecurityTestExecutionListeners;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

@SecurityTestExecutionListeners
class TwoFactorAuthenticationControllerTest extends AbstractMockitoSpringContextTests {
  private static final String USERNAME = "molgenisUser";
  private static final String ROLE_SU = "SU";

  @Mock private TwoFactorAuthenticationService twoFactorAuthenticationService;
  @Mock private TwoFactorAuthenticationProvider twoFactorAuthenticationProvider;
  @Mock private OtpService otpService;
  private TwoFactorAuthenticationController twoFactorAuthenticationController;

  @BeforeEach
  void setUpBeforeEach() {
    twoFactorAuthenticationController =
        new TwoFactorAuthenticationController(
            twoFactorAuthenticationProvider,
            twoFactorAuthenticationService,
            new RecoveryAuthenticationProviderImpl(mock(RecoveryService.class)),
            otpService);
  }

  @Test
  @WithMockUser(value = USERNAME, roles = ROLE_SU)
  void activationExceptionTest() {
    when(twoFactorAuthenticationService.generateSecretKey()).thenReturn("secretKey");
    Model model = new ExtendedModelMap();
    String viewTemplate = twoFactorAuthenticationController.activation(model);
    assertEquals("view-2fa-activation-modal", viewTemplate);
  }

  @Test
  void configuredExceptionTest() {
    String viewTemplate = twoFactorAuthenticationController.configured();
    assertEquals("view-2fa-configured-modal", viewTemplate);
  }

  @Test
  @WithMockUser(value = USERNAME, roles = ROLE_SU)
  void authenticateExceptionTest() {
    String secretKey = "secretKey";
    String verificationCode = "123456";
    TwoFactorAuthenticationToken authToken =
        new TwoFactorAuthenticationToken(verificationCode, null);
    when(twoFactorAuthenticationProvider.authenticate(authToken))
        .thenThrow(new InvalidVerificationCodeException("Invalid verification code entered"));

    Model model = new ExtendedModelMap();

    String viewTemplate =
        twoFactorAuthenticationController.authenticate(model, verificationCode, secretKey);
    assertEquals(secretKey, model.asMap().get(ATTRIBUTE_2FA_SECRET_KEY));
    assertEquals(model.asMap().get(ERROR_MESSAGE_ATTRIBUTE), "Invalid verification code entered");
    assertEquals("view-2fa-activation-modal", viewTemplate);
  }

  @Test
  void validateVerificationCodeAndAuthenticateExceptionTest() {
    String verificationCode = "123456";
    Model model = new ExtendedModelMap();
    TwoFactorAuthenticationToken authToken =
        new TwoFactorAuthenticationToken(verificationCode, null);
    when(twoFactorAuthenticationProvider.authenticate(authToken))
        .thenThrow(new BadCredentialsException("test"));
    String viewTemplate = twoFactorAuthenticationController.validate(model, verificationCode);
    assertEquals("view-2fa-configured-modal", viewTemplate);
  }

  @Test
  @WithMockUser(value = USERNAME, roles = ROLE_SU)
  void testRecoverAccount() {
    String recoveryCodeId = "123456";
    Model model = new ExtendedModelMap();
    String viewTemplate = twoFactorAuthenticationController.recoverAccount(model, recoveryCodeId);
    assertEquals("redirect:/", viewTemplate);
  }

  @Test
  @WithMockUser(value = USERNAME, roles = ROLE_SU)
  void activationTest() {
    Model model = new ExtendedModelMap();
    when(twoFactorAuthenticationService.generateSecretKey()).thenReturn("secret");
    String viewTemplate = twoFactorAuthenticationController.activation(model);
    assertEquals("view-2fa-activation-modal", viewTemplate);
  }

  @Test
  @WithMockUser(value = USERNAME, roles = ROLE_SU)
  void authenticateTest() {
    String secretKey = "secretKey";
    String verificationCode = "123456";
    Model model = new ExtendedModelMap();
    TwoFactorAuthenticationToken authToken =
        new TwoFactorAuthenticationToken(verificationCode, secretKey);
    Authentication authentication = mock(Authentication.class);
    when(twoFactorAuthenticationProvider.authenticate(authToken)).thenReturn(authentication);

    String viewTemplate =
        twoFactorAuthenticationController.authenticate(model, verificationCode, secretKey);

    assertEquals("redirect:/menu/main/useraccount?showCodes=true#security", viewTemplate);
    verify(twoFactorAuthenticationService).enableForUser();
  }

  @Configuration
  static class Config {
    @Bean
    TwoFactorAuthenticationService twoFactorAuthenticationService() {
      return mock(TwoFactorAuthenticationServiceImpl.class);
    }

    @Bean
    OtpService otpService() {
      return mock(OtpService.class);
    }

    @Bean
    RecoveryService recoveryService() {
      return mock(RecoveryService.class);
    }

    @Bean
    TwoFactorAuthenticationProvider twoFactorAuthenticationProvider() {
      return mock(TwoFactorAuthenticationProvider.class);
    }

    @Bean
    RecoveryAuthenticationProvider recoveryAuthenticationProvider() {
      return new RecoveryAuthenticationProviderImpl(recoveryService());
    }

    @Bean
    AppSettings appSettings() {
      return mock(AppSettings.class);
    }

    @Bean
    TwoFactorAuthenticationController twoFactorAuthenticationController() {
      return new TwoFactorAuthenticationController(
          twoFactorAuthenticationProvider(),
          twoFactorAuthenticationService(),
          recoveryAuthenticationProvider(),
          otpService());
    }
  }
}
