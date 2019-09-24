package org.molgenis.i18n;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.test.AbstractMockitoTest;
import org.molgenis.util.i18n.MessageSourceHolder;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

class ContextMessageSourceImplTest extends AbstractMockitoTest {
  @Mock private MessageSource messageSource;
  private ContextMessageSourceImpl userMessageSourceImpl;

  @BeforeEach
  void setUpBeforeMethod() {
    userMessageSourceImpl = new ContextMessageSourceImpl();
  }

  @Test
  void testGetMessageCode() {
    String message = "MyMessage";
    String code = "my_code";
    Locale locale = Locale.GERMAN;

    when(messageSource.getMessage(code, null, locale)).thenReturn(message);

    LocaleContextHolder.setLocale(locale);
    MessageSourceHolder.setMessageSource(messageSource);
    assertEquals(message, userMessageSourceImpl.getMessage(code));
  }

  @Test
  void testGetMessageCodeArgs() {
    String message = "MyMessage";
    String code = "my_code";
    Object[] args = {"arg0", "arg1"};
    Locale locale = Locale.GERMAN;

    when(messageSource.getMessage(code, args, locale)).thenReturn(message);

    LocaleContextHolder.setLocale(locale);
    MessageSourceHolder.setMessageSource(messageSource);
    assertEquals(message, userMessageSourceImpl.getMessage(code, args));
  }
}
