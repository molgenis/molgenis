package org.molgenis.web.i18n;

import static java.util.Locale.GERMAN;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.mockito.Mock;
import org.molgenis.data.security.auth.User;
import org.molgenis.data.security.user.UnknownUserException;
import org.molgenis.data.security.user.UserService;
import org.molgenis.test.AbstractMockitoTestNGSpringContextTests;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@ContextConfiguration(classes = {HttpLocaleResolverTest.Config.class})
@TestExecutionListeners(listeners = WithSecurityContextTestExecutionListener.class)
public class HttpLocaleResolverTest extends AbstractMockitoTestNGSpringContextTests {
  @Mock private UserLocaleResolver userLocaleResolver;
  @Mock private FallbackLocaleSupplier fallbackLocaleSupplier;
  @Mock private UserService userService;
  private HttpLocaleResolver httpLocaleResolver;

  @BeforeMethod
  public void setUpBeforeMethod() {
    httpLocaleResolver =
        new HttpLocaleResolver(userLocaleResolver, fallbackLocaleSupplier, userService);
  }

  @WithMockUser
  @Test
  public void testResolveLocaleAuthenticated() {
    HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
    String username = "user";
    when(userLocaleResolver.resolveLocale(username)).thenReturn(Locale.GERMAN);
    assertEquals(httpLocaleResolver.resolveLocale(httpServletRequest), Locale.GERMAN);
  }

  @Test
  public void testResolveLocaleNotAuthenticated() {
    HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
    when(fallbackLocaleSupplier.get()).thenReturn(Locale.GERMAN);
    assertEquals(httpLocaleResolver.resolveLocale(httpServletRequest), Locale.GERMAN);
  }

  @WithMockUser
  @Test
  public void testSetLocaleAuthenticated() {
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
  public void testSetLocaleAuthenticatedUnchangedLanguage() {
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
  @Test(expectedExceptions = UnknownUserException.class)
  public void testSetLocaleAuthenticatedUnknownUser() {
    HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
    HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
    httpLocaleResolver.setLocale(httpServletRequest, httpServletResponse, Locale.GERMAN);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testSetLocaleNotAuthenticated() {
    HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
    HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
    httpLocaleResolver.setLocale(httpServletRequest, httpServletResponse, Locale.GERMAN);
  }

  @WithMockUser
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testSetLocaleUnusedLanguage() {
    HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
    HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
    httpLocaleResolver.setLocale(httpServletRequest, httpServletResponse, Locale.CHINESE);
  }

  static class Config {}
}
