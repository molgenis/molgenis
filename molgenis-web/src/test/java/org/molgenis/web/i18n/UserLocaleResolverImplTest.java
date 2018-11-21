package org.molgenis.web.i18n;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.Locale;
import org.mockito.Mock;
import org.molgenis.data.security.auth.User;
import org.molgenis.data.security.user.UnknownUserException;
import org.molgenis.data.security.user.UserService;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class UserLocaleResolverImplTest extends AbstractMockitoTest {
  @Mock private UserService userService;
  @Mock private FallbackLocaleSupplier fallbackLocaleSupplier;
  private UserLocaleResolver userLocaleResolver;

  @BeforeMethod
  public void setUpBeforeMethod() {
    userLocaleResolver = new UserLocaleResolverImpl(userService, fallbackLocaleSupplier);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void testUserLocaleResolver() {
    new UserLocaleResolverImpl(null, null);
  }

  @Test
  public void testResolveLocale() {
    String username = "MyUsername";
    String languageCode = "nl";
    User user = when(mock(User.class).getLanguageCode()).thenReturn(languageCode).getMock();
    when(userService.getUser(username)).thenReturn(user);
    assertEquals(userLocaleResolver.resolveLocale(username), Locale.forLanguageTag(languageCode));
  }

  @Test
  public void testResolveLocaleUserWithoutLanguagecode() {
    String username = "MyUsername";
    String languageCode = "nl";
    User user = mock(User.class);
    when(userService.getUser(username)).thenReturn(user);
    when(fallbackLocaleSupplier.get()).thenReturn(Locale.forLanguageTag(languageCode));
    assertEquals(userLocaleResolver.resolveLocale(username), Locale.forLanguageTag(languageCode));
  }

  @Test(expectedExceptions = UnknownUserException.class)
  public void testResolveLocaleUnknownUser() {
    String username = "MyUsername";
    userLocaleResolver.resolveLocale(username);
  }
}
