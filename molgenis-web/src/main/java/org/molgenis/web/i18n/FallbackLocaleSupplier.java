package org.molgenis.web.i18n;

import java.util.Locale;

public interface FallbackLocaleSupplier {

  /** @return Locale, never <tt>null</tt> */
  Locale get();
}
