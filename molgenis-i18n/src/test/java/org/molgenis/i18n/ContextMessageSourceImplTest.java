package org.molgenis.i18n;

import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;

import java.util.Locale;
import org.mockito.Mock;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ContextMessageSourceImplTest extends AbstractMockitoTest {
  @Mock private MessageSource messageSource;
  private ContextMessageSourceImpl userMessageSourceImpl;

  @BeforeMethod
  public void setUpBeforeMethod() {
    userMessageSourceImpl = new ContextMessageSourceImpl();
  }

  @Test
  public void testGetMessageCode() {
    String message = "MyMessage";
    String code = "my_code";
    Locale locale = Locale.GERMAN;

    when(messageSource.getMessage(code, null, locale)).thenReturn(message);

    LocaleContextHolder.setLocale(locale);
    MessageSourceHolder.setMessageSource(messageSource);
    assertEquals(userMessageSourceImpl.getMessage(code), message);
  }

  @Test
  public void testGetMessageCodeArgs() {
    String message = "MyMessage";
    String code = "my_code";
    Object[] args = {"arg0", "arg1"};
    Locale locale = Locale.GERMAN;

    when(messageSource.getMessage(code, args, locale)).thenReturn(message);

    LocaleContextHolder.setLocale(locale);
    MessageSourceHolder.setMessageSource(messageSource);
    assertEquals(userMessageSourceImpl.getMessage(code, args), message);
  }
}
