package org.molgenis.web.i18n;

import static java.util.Locale.forLanguageTag;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.data.security.auth.User;
import org.molgenis.data.security.user.UnknownUserException;
import org.molgenis.data.security.user.UserService;
import org.molgenis.test.AbstractMockitoTest;

class UserLocaleResolverImplTest extends AbstractMockitoTest {
  @Mock private UserService userService;
  @Mock private FallbackLocaleSupplier fallbackLocaleSupplier;
  private UserLocaleResolver userLocaleResolver;

  @BeforeEach
  void setUpBeforeMethod() {
    userLocaleResolver = new UserLocaleResolverImpl(userService, fallbackLocaleSupplier);
  }

  @Test
  void testUserLocaleResolver() {
    assertThrows(NullPointerException.class, () -> new UserLocaleResolverImpl(null, null));
  }

  @Test
  void testResolveLocale() {
    String username = "MyUsername";
    String languageCode = "nl";
    User user = when(mock(User.class).getLanguageCode()).thenReturn(languageCode).getMock();
    when(userService.getUser(username)).thenReturn(user);
    assertEquals(forLanguageTag(languageCode), userLocaleResolver.resolveLocale(username));
  }

  @Test
  void testResolveLocaleUserWithoutLanguagecode() {
    String username = "MyUsername";
    String languageCode = "nl";
    User user = mock(User.class);
    when(userService.getUser(username)).thenReturn(user);
    when(fallbackLocaleSupplier.get()).thenReturn(Locale.forLanguageTag(languageCode));
    assertEquals(forLanguageTag(languageCode), userLocaleResolver.resolveLocale(username));
  }

  @Test
  void testResolveLocaleUnknownUser() {
    String username = "MyUsername";
    assertThrows(UnknownUserException.class, () -> userLocaleResolver.resolveLocale(username));
  }
}
