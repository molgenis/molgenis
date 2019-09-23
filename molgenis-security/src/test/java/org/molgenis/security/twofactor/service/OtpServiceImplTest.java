package org.molgenis.security.twofactor.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.molgenis.security.twofactor.exceptions.InvalidVerificationCodeException;
import org.molgenis.settings.AppSettings;
import org.springframework.security.test.context.annotation.SecurityTestExecutionListeners;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
@SecurityTestExecutionListeners
class OtpServiceImplTest {

  private static final String USERNAME = "molgenisUser";
  private static final String ROLE_SU = "SU";

  private OtpServiceImpl otpServiceImpl;
  @Mock private AppSettings appSettings;

  @BeforeEach
  void setUpBeforeEach() {
    otpServiceImpl = new OtpServiceImpl(appSettings);
  }

  @Test
  void testTryVerificationKeyFailed() {
    assertThrows(
        InvalidVerificationCodeException.class,
        () -> otpServiceImpl.tryVerificationCode("", "secretKey"));
  }

  @Test
  @WithMockUser(value = USERNAME, roles = ROLE_SU)
  void testGetAuthenticatorURI() {
    when(appSettings.getTitle()).thenReturn("MOLGENIS");
    String uri = otpServiceImpl.getAuthenticatorURI("secretKey");
    assertFalse(uri.isEmpty());
  }
}
