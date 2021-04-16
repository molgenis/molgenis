package org.molgenis.web.i18n;

import static java.util.Locale.GERMAN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.data.security.auth.User;
import org.molgenis.data.security.user.UnknownUserException;
import org.molgenis.data.security.user.UserService;
import org.molgenis.security.core.WithMockSystemUser;
import org.molgenis.test.AbstractMockitoSpringContextTests;
import org.springframework.security.test.context.annotation.SecurityTestExecutionListeners;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = {HttpLocaleResolverTest.Config.class})
@SecurityTestExecutionListeners
class HttpLocaleResolverTest extends AbstractMockitoSpringContextTests {
  @Mock private UserLocaleResolver userLocaleResolver;
  @Mock private FallbackLocaleSupplier fallbackLocaleSupplier;
  @Mock private UserService userService;
  private HttpLocaleResolver httpLocaleResolver;

  @BeforeEach
  void setUpBeforeMethod() {
    httpLocaleResolver =
        new HttpLocaleResolver(userLocaleResolver, fallbackLocaleSupplier, userService);
  }

  @WithMockUser
  @Test
  void testResolveLocaleAuthenticated() {
    HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
    String username = "user";
    when(userLocaleResolver.resolveLocale(username)).thenReturn(Locale.GERMAN);
    assertEquals(GERMAN, httpLocaleResolver.resolveLocale(httpServletRequest));
  }

  @WithMockSystemUser(originalUsername = "user")
  @Test
  void testResolveLocaleAuthenticatedElevated() {
    HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
    String username = "user";
    when(userLocaleResolver.resolveLocale(username)).thenReturn(Locale.GERMAN);
    assertEquals(GERMAN, httpLocaleResolver.resolveLocale(httpServletRequest));
  }

  @Test
  void testResolveLocaleNotAuthenticated() {
    HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
    when(fallbackLocaleSupplier.get()).thenReturn(Locale.GERMAN);
    assertEquals(GERMAN, httpLocaleResolver.resolveLocale(httpServletRequest));
  }

  @WithMockUser
  @Test
  void testSetLocaleAuthenticated() {
    HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
    HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
    Locale locale = GERMAN;

    String username = "user";
    User user = mock(User.class);
    when(userService.getUser(username)).thenReturn(user);
    httpLocaleResolver.setLocale(httpServletRequest, httpServletResponse, locale);

    verify(user).setLanguageCode(locale.getLanguage());
    verify(userService).update(user);
  }

  @WithMockSystemUser(originalUsername = "user")
  @Test
  void testSetLocaleAuthenticatedElevated() {
    HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
    HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
    Locale locale = GERMAN;

    String username = "user";
    User user = mock(User.class);
    when(userService.getUser(username)).thenReturn(user);
    httpLocaleResolver.setLocale(httpServletRequest, httpServletResponse, locale);

    verify(user).setLanguageCode(locale.getLanguage());
    verify(userService).update(user);
  }

  @WithMockUser
  @Test
  void testSetLocaleAuthenticatedUnchangedLanguage() {
    HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
    HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
    Locale locale = GERMAN;

    String username = "user";
    User user = when(mock(User.class).getLanguageCode()).thenReturn(locale.getLanguage()).getMock();
    when(userService.getUser(username)).thenReturn(user);
    httpLocaleResolver.setLocale(httpServletRequest, httpServletResponse, locale);

    verifyNoMoreInteractions(userService);
  }

  @WithMockUser
  @Test
  void testSetLocaleAuthenticatedUnknownUser() {
    HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
    HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
    assertThrows(
        UnknownUserException.class,
        () -> httpLocaleResolver.setLocale(httpServletRequest, httpServletResponse, Locale.GERMAN));
  }

  @Test
  void testSetLocaleNotAuthenticated() {
    HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
    HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
    assertThrows(
        UnsupportedOperationException.class,
        () -> httpLocaleResolver.setLocale(httpServletRequest, httpServletResponse, Locale.GERMAN));
  }

  @WithMockUser
  @Test
  void testSetLocaleUnusedLanguage() {
    HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
    HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
    assertThrows(
        UnsupportedOperationException.class,
        () ->
            httpLocaleResolver.setLocale(httpServletRequest, httpServletResponse, Locale.CHINESE));
  }

  static class Config {}
}
