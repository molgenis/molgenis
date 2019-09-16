package org.molgenis.security.twofactor.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.molgenis.security.twofactor.service.RecoveryService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.annotation.SecurityTestExecutionListeners;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
@SecurityTestExecutionListeners
class RecoveryAuthenticationProviderTest {

  private static final String USERNAME = "admin";
  private static final String ROLE_SU = "SU";

  @Mock private RecoveryService recoveryService;
  private RecoveryAuthenticationProvider recoveryAuthenticationProvider;

  @BeforeEach
  void setUpBeforeEach() {
    recoveryAuthenticationProvider = new RecoveryAuthenticationProviderImpl(recoveryService);
  }

  @Test
  void testAuthenticateInvalidToken() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            recoveryAuthenticationProvider.authenticate(
                new UsernamePasswordAuthenticationToken("dsda545ds4dsa456", "")));
  }

  @Test
  @WithMockUser(value = USERNAME, roles = ROLE_SU)
  void testAuthentication() {
    RecoveryAuthenticationToken authToken = new RecoveryAuthenticationToken("dsda545ds4dsa456");
    assertFalse(authToken.isAuthenticated());

    doNothing().when(recoveryService).useRecoveryCode(authToken.getRecoveryCode());

    Authentication auth = recoveryAuthenticationProvider.authenticate(authToken);
    assertNotNull(auth);
    assertTrue(auth.isAuthenticated());
    assertEquals("admin", auth.getName());
  }
}
