package org.molgenis.security.twofactor;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.security.twofactor.TwoFactorAuthenticationController.ATTRIBUTE_2FA_SECRET_KEY;
import static org.testng.Assert.assertEquals;

import org.mockito.Mockito;
import org.molgenis.security.login.MolgenisLoginController;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@ContextConfiguration(classes = {TwoFactorAuthenticationControllerTest.Config.class})
@TestExecutionListeners(listeners = WithSecurityContextTestExecutionListener.class)
public class TwoFactorAuthenticationControllerTest extends AbstractTestNGSpringContextTests {
  private static final String USERNAME = "molgenisUser";
  private static final String ROLE_SU = "SU";

  @Autowired private AppSettings appSettings;
  @Autowired private TwoFactorAuthenticationService twoFactorAuthenticationService;
  @Autowired private TwoFactorAuthenticationController twoFactorAuthenticationController;
  @Autowired private TwoFactorAuthenticationProvider twoFactorAuthenticationProvider;
  @Autowired private OtpService otpService;

  @BeforeMethod
  public void setUp() {
    Mockito.reset(twoFactorAuthenticationService, otpService, twoFactorAuthenticationProvider);
  }

  @Test
  @WithMockUser(value = USERNAME, roles = ROLE_SU)
  public void activationExceptionTest() {
    when(twoFactorAuthenticationService.generateSecretKey()).thenReturn("secretKey");
    when(appSettings.getTitle()).thenReturn("MOLGENIS");
    Model model = new ExtendedModelMap();
    String viewTemplate = twoFactorAuthenticationController.activation(model);
    assertEquals(viewTemplate, "view-2fa-activation-modal");
  }

  @Test
  public void configuredExceptionTest() {
    String viewTemplate = twoFactorAuthenticationController.configured();
    assertEquals(viewTemplate, "view-2fa-configured-modal");
  }

  @Test
  @WithMockUser(value = USERNAME, roles = ROLE_SU)
  public void authenticateExceptionTest() {
    String secretKey = "secretKey";
    String verificationCode = "123456";
    when(appSettings.getTitle()).thenReturn("MOLGENIS");
    TwoFactorAuthenticationToken authToken =
        new TwoFactorAuthenticationToken(verificationCode, null);
    when(twoFactorAuthenticationProvider.authenticate(authToken))
        .thenThrow(new InvalidVerificationCodeException("Invalid verification code entered"));

    Model model = new ExtendedModelMap();

    String viewTemplate =
        twoFactorAuthenticationController.authenticate(model, verificationCode, secretKey);
    assertEquals(model.asMap().get(ATTRIBUTE_2FA_SECRET_KEY), secretKey);
    assertEquals(
        "Invalid verification code entered",
        model.asMap().get(MolgenisLoginController.ERROR_MESSAGE_ATTRIBUTE));
    assertEquals(viewTemplate, "view-2fa-activation-modal");
  }

  @Test
  public void validateVerificationCodeAndAuthenticateExceptionTest() {
    String verificationCode = "123456";
    Model model = new ExtendedModelMap();
    TwoFactorAuthenticationToken authToken =
        new TwoFactorAuthenticationToken(verificationCode, null);
    when(twoFactorAuthenticationProvider.authenticate(authToken))
        .thenThrow(new BadCredentialsException("test"));
    String viewTemplate = twoFactorAuthenticationController.validate(model, verificationCode);
    assertEquals(viewTemplate, "view-2fa-configured-modal");
  }

  @Test
  @WithMockUser(value = USERNAME, roles = ROLE_SU)
  public void testRecoverAccount() {
    String recoveryCodeId = "123456";
    Model model = new ExtendedModelMap();
    String viewTemplate = twoFactorAuthenticationController.recoverAccount(model, recoveryCodeId);
    assertEquals(viewTemplate, "redirect:/");
  }

  @Test
  @WithMockUser(value = USERNAME, roles = ROLE_SU)
  public void activationTest() {
    Model model = new ExtendedModelMap();
    when(twoFactorAuthenticationService.generateSecretKey()).thenReturn("secret");
    when(appSettings.getTitle()).thenReturn("TEST MOLGENIS");
    String viewTemplate = twoFactorAuthenticationController.activation(model);
    assertEquals(viewTemplate, "view-2fa-activation-modal");
  }

  @Test
  @WithMockUser(value = USERNAME, roles = ROLE_SU)
  public void authenticateTest() {
    String secretKey = "secretKey";
    String verificationCode = "123456";
    when(appSettings.getTitle()).thenReturn("MOLGENIS");
    Model model = new ExtendedModelMap();
    TwoFactorAuthenticationToken authToken =
        new TwoFactorAuthenticationToken(verificationCode, secretKey);
    Authentication authentication = mock(Authentication.class);
    when(otpService.tryVerificationCode(verificationCode, secretKey)).thenReturn(true);
    when(twoFactorAuthenticationProvider.authenticate(authToken)).thenReturn(authentication);

    String viewTemplate =
        twoFactorAuthenticationController.authenticate(model, verificationCode, secretKey);

    assertEquals(viewTemplate, "redirect:/menu/main/useraccount?showCodes=true#security");
    verify(twoFactorAuthenticationService).enableForUser();
  }

  @Configuration
  public static class Config {
    @Bean
    public TwoFactorAuthenticationService twoFactorAuthenticationService() {
      return mock(TwoFactorAuthenticationServiceImpl.class);
    }

    @Bean
    public OtpService otpService() {
      return mock(OtpService.class);
    }

    @Bean
    public RecoveryService recoveryService() {
      return mock(RecoveryService.class);
    }

    @Bean
    public TwoFactorAuthenticationProvider twoFactorAuthenticationProvider() {
      return mock(TwoFactorAuthenticationProvider.class);
    }

    @Bean
    public RecoveryAuthenticationProvider recoveryAuthenticationProvider() {
      return new RecoveryAuthenticationProviderImpl(recoveryService());
    }

    @Bean
    public AppSettings appSettings() {
      return mock(AppSettings.class);
    }

    @Bean
    public TwoFactorAuthenticationController twoFactorAuthenticationController() {
      return new TwoFactorAuthenticationController(
          twoFactorAuthenticationProvider(),
          twoFactorAuthenticationService(),
          recoveryAuthenticationProvider(),
          otpService());
    }
  }
}
