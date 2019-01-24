package org.molgenis.i18n;

import java.util.Locale;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
public class ContextMessageSourceImpl implements ContextMessageSource {

  @Override
  public String getMessage(String code) {
    return getMessage(code, null);
  }

  @Override
  public String getMessage(String code, @Nullable @CheckForNull Object[] args) {
    Locale locale = LocaleContextHolder.getLocale();
    MessageSource messageSource = MessageSourceHolder.getMessageSource();
    return messageSource.getMessage(code, args, locale);
  }
}
