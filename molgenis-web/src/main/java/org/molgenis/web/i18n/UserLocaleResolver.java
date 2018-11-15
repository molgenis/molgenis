package org.molgenis.web.i18n;

import java.util.Locale;

public interface UserLocaleResolver {
  /**
   * Resolve the current locale via the given username.
   *
   * @return locale for the user, never <tt>null</tt>
   */
  Locale resolveLocale(String username);
}
