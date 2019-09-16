package org.molgenis.security.twofactor.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.security.twofactor.auth.TwoFactorAuthenticationSetting.DISABLED;
import static org.molgenis.security.twofactor.auth.TwoFactorAuthenticationSetting.ENFORCED;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.molgenis.security.settings.AuthenticationSettings;
import org.molgenis.security.twofactor.TwoFactorAuthenticationController;
import org.molgenis.security.twofactor.service.TwoFactorAuthenticationService;
import org.molgenis.security.user.UserAccountService;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.annotation.SecurityTestExecutionListeners;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
@SecurityTestExecutionListeners
class TwoFactorAuthenticationFilterTest {

  @Mock private AuthenticationSettings authenticationSettings;
  @Mock private TwoFactorAuthenticationService twoFactorAuthenticationService;
  @Mock private UserAccountService userAccountService;
  private TwoFactorAuthenticationFilter filter;

  private MockHttpServletRequest request;
  private MockHttpServletResponse response;
  private FilterChain chain;

  @BeforeEach
  void setUpBeforeMethod() {
    filter =
        new TwoFactorAuthenticationFilter(
            authenticationSettings,
            twoFactorAuthenticationService,
            new DefaultRedirectStrategy(),
            userAccountService);
    request = new MockHttpServletRequest();
    response = new MockHttpServletResponse();
    chain = mock(FilterChain.class);
  }

  @Test
  @WithMockUser(username = "user")
  void testDoFilterInternalIsConfigured() throws IOException, ServletException {
    request.setRequestURI("/login");
    when(authenticationSettings.getTwoFactorAuthentication()).thenReturn(ENFORCED);
    when(twoFactorAuthenticationService.isConfiguredForUser()).thenReturn(true);

    filter.doFilterInternal(request, response, chain);

    String initialRedirectUrl =
        TwoFactorAuthenticationController.URI
            + TwoFactorAuthenticationController.TWO_FACTOR_CONFIGURED_URI;
    assertEquals(initialRedirectUrl, response.getRedirectedUrl());
  }

  @Test
  @WithMockUser(username = "user")
  void testDoFilterInternalIsConfiguredChangePassword() throws IOException, ServletException {
    request.setRequestURI("/account/password/change");
    when(authenticationSettings.getTwoFactorAuthentication()).thenReturn(ENFORCED);

    filter.doFilterInternal(request, response, chain);

    assertNull(response.getRedirectedUrl());
  }

  @Test
  @WithMockUser(username = "user")
  void testDoFilterInternalIsNotConfigured() throws IOException, ServletException {
    request.setRequestURI("/login");
    when(authenticationSettings.getTwoFactorAuthentication()).thenReturn(ENFORCED);
    when(twoFactorAuthenticationService.isConfiguredForUser()).thenReturn(false);

    filter.doFilterInternal(request, response, chain);

    String configuredRedirectUrl =
        TwoFactorAuthenticationController.URI
            + TwoFactorAuthenticationController.TWO_FACTOR_ACTIVATION_URI;
    assertEquals(configuredRedirectUrl, response.getRedirectedUrl());
  }

  @Test
  void testDoFilterInternalNotAuthenticated() throws IOException, ServletException {
    request.setRequestURI("/login");
    when(authenticationSettings.getTwoFactorAuthentication()).thenReturn(DISABLED);

    filter.doFilterInternal(request, response, chain);
    verify(chain).doFilter(request, response);
  }

  @Test
  @WithMockUser
  void testDoFilterInternalRecoveryAuthenticated() throws IOException, ServletException {
    SecurityContext previous = SecurityContextHolder.getContext();
    try {
      SecurityContext testContext = SecurityContextHolder.createEmptyContext();
      SecurityContextHolder.setContext(testContext);
      testContext.setAuthentication(new RecoveryAuthenticationToken("recovery"));

      request.setRequestURI("/menu/main/dataexplorer");
      when(authenticationSettings.getTwoFactorAuthentication()).thenReturn(ENFORCED);

      filter.doFilterInternal(request, response, chain);
      verify(chain).doFilter(request, response);
    } finally {
      SecurityContextHolder.setContext(previous);
    }
  }
}
