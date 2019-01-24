package org.molgenis.i18n;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;

/** Returns messages based on the {@link LocaleContextHolder} */
public interface ContextMessageSource {
  /**
   * Returns the message in the locale of the current user.
   *
   * @param code the code to lookup up, such as 'calculator.noRateSet'
   * @return the resolved message, never <tt>null</tt>
   * @throws NoSuchMessageException if the message wasn't found
   */
  String getMessage(String code);

  /**
   * Returns the message in the locale of the current user.
   *
   * @param code the code to lookup up, such as 'calculator.noRateSet'
   * @param args an array of arguments that will be filled in for params within the message (params
   *     look like "{0}", "{1,date}", "{2,time}" within a message), * or {@code null} if none.
   * @return the resolved message, never <tt>null</tt>
   * @throws NoSuchMessageException if the message wasn't found
   */
  String getMessage(String code, @Nullable @CheckForNull Object[] args);
}
