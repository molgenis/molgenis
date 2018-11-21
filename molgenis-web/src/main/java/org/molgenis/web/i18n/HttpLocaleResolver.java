package org.molgenis.web.i18n;

import static java.util.Objects.requireNonNull;

import com.google.common.base.Objects;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.molgenis.data.security.auth.User;
import org.molgenis.data.security.user.UnknownUserException;
import org.molgenis.data.security.user.UserService;
import org.molgenis.i18n.LanguageService;
import org.molgenis.security.core.utils.SecurityUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.LocaleResolver;

@Component
public class HttpLocaleResolver implements LocaleResolver {
  private final UserLocaleResolver userLocaleResolver;
  private final FallbackLocaleSupplier fallbackLocaleSupplier;
  private final UserService userService;

  HttpLocaleResolver(
      UserLocaleResolver userLocaleResolver,
      FallbackLocaleSupplier fallbackLocaleSupplier,
      UserService userService) {
    this.userLocaleResolver = requireNonNull(userLocaleResolver);
    this.fallbackLocaleSupplier = requireNonNull(fallbackLocaleSupplier);
    this.userService = requireNonNull(userService);
  }

  @Override
  public Locale resolveLocale(HttpServletRequest request) {
    String username = SecurityUtils.getCurrentUsername();

    Locale locale;
    if (username != null) {
      locale = userLocaleResolver.resolveLocale(username);
    } else {
      locale = fallbackLocaleSupplier.get();
    }

    return locale;
  }

  @Override
  public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
    String username = SecurityUtils.getCurrentUsername();
    if (username == null) {
      throw new UnsupportedOperationException("Cannot change language if not logged in");
    }

    String languageCode = locale.getLanguage();
    if (!LanguageService.hasLanguageCode(languageCode)) {
      throw new UnsupportedOperationException("Cannot set language to unsupported languageCode");
    }

    User user = userService.getUser(username);
    if (user == null) {
      throw new UnknownUserException(username);
    }

    if (!Objects.equal(user.getLanguageCode(), languageCode)) {
      user.setLanguageCode(locale.getLanguage());
      userService.update(user);
    }
  }
}
