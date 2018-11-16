package org.molgenis.web.i18n;

import static java.util.Objects.requireNonNull;

import java.util.Locale;
import org.molgenis.data.security.auth.User;
import org.molgenis.data.security.user.UnknownUserException;
import org.molgenis.data.security.user.UserService;
import org.springframework.stereotype.Component;

@Component
public class UserLocaleResolverImpl implements UserLocaleResolver {
  private final UserService userService;
  private final FallbackLocaleSupplier fallbackLocaleSupplier;

  UserLocaleResolverImpl(UserService userService, FallbackLocaleSupplier fallbackLocaleSupplier) {
    this.userService = requireNonNull(userService);
    this.fallbackLocaleSupplier = requireNonNull(fallbackLocaleSupplier);
  }

  @Override
  public Locale resolveLocale(String username) {
    User user = userService.getUser(username);
    if (user == null) {
      throw new UnknownUserException(username);
    }

    String languageCode = user.getLanguageCode();
    Locale locale;
    if (languageCode != null) {
      locale = Locale.forLanguageTag(languageCode);
    } else {
      locale = fallbackLocaleSupplier.get();
    }
    return locale;
  }
}
