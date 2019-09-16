package org.molgenis.security.twofactor.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.molgenis.security.twofactor.service.OtpService;
import org.molgenis.security.twofactor.service.OtpServiceImpl;
import org.molgenis.security.twofactor.service.RecoveryService;
import org.molgenis.security.twofactor.service.RecoveryServiceImpl;
import org.molgenis.security.twofactor.service.TwoFactorAuthenticationService;
import org.molgenis.security.twofactor.service.TwoFactorAuthenticationServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.annotation.SecurityTestExecutionListeners;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
@SecurityTestExecutionListeners
class TwoFactorAuthenticationProviderTest {

  private static final String USERNAME = "admin";
  private static final String ROLE_SU = "SU";

  @Mock private OtpService otpService;
  @Mock private RecoveryService recoveryService;
  @Mock private TwoFactorAuthenticationService twoFactorAuthenticationService;
  private TwoFactorAuthenticationProviderImpl twoFactorAuthenticationProviderImpl;

  @BeforeEach
  void setUpBeforeEach() {
    twoFactorAuthenticationProviderImpl =
        new TwoFactorAuthenticationProviderImpl(
            twoFactorAuthenticationService, otpService, recoveryService);
  }

  @Test
  @WithMockUser(value = USERNAME, roles = ROLE_SU)
  void testAuthentication2faIsConfigured() {
    TwoFactorAuthenticationToken authToken = new TwoFactorAuthenticationToken("123456", null);
    assertFalse(authToken.isAuthenticated());

    when(twoFactorAuthenticationService.isConfiguredForUser()).thenReturn(true);
    when(twoFactorAuthenticationService.isVerificationCodeValidForUser(
            authToken.getVerificationCode()))
        .thenReturn(true);

    Authentication auth = twoFactorAuthenticationProviderImpl.authenticate(authToken);
    assertNotNull(auth);
    assertTrue(auth.isAuthenticated());
    assertEquals("admin", auth.getName());
  }

  @Test
  @WithMockUser(value = USERNAME, roles = ROLE_SU)
  void testAuthentication2fIsNotConfigured() {
    TwoFactorAuthenticationToken authToken =
        new TwoFactorAuthenticationToken("123456", "dsda545ds4dsa456");
    assertFalse(authToken.isAuthenticated());

    when(twoFactorAuthenticationService.isConfiguredForUser()).thenReturn(false);
    when(otpService.tryVerificationCode(authToken.getVerificationCode(), authToken.getSecretKey()))
        .thenReturn(true);

    Authentication auth = twoFactorAuthenticationProviderImpl.authenticate(authToken);
    assertNotNull(auth);
    assertTrue(auth.isAuthenticated());
    assertEquals("admin", auth.getName());
  }

  @Test
  void testAuthenticateInvalidToken() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            twoFactorAuthenticationProviderImpl.authenticate(
                new UsernamePasswordAuthenticationToken("123456", "dsda545ds4dsa456")));
  }

  @Configuration
  static class Config {
    @Bean
    TwoFactorAuthenticationProvider twoFactorAuthenticationProvider() {
      return new TwoFactorAuthenticationProviderImpl(
          twoFactorAuthenticationService(), otpService(), recoveryService());
    }

    @Bean
    TwoFactorAuthenticationService twoFactorAuthenticationService() {
      return mock(TwoFactorAuthenticationServiceImpl.class);
    }

    @Bean
    RecoveryService recoveryService() {
      return mock(RecoveryServiceImpl.class);
    }

    @Bean
    OtpService otpService() {
      return mock(OtpServiceImpl.class);
    }
  }
}
