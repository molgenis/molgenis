package org.molgenis.util.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Locale;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.test.AbstractMockitoTest;
import org.molgenis.util.i18n.AllPropertiesMessageSource;
import org.molgenis.util.i18n.MessageSourceHolder;
import org.molgenis.util.i18n.TestAllPropertiesMessageSource;
import org.molgenis.util.i18n.format.MessageFormatFactory;
import org.springframework.context.i18n.LocaleContextHolder;

public abstract class ExceptionMessageTest extends AbstractMockitoTest {
  private MessageFormatFactory messageFormatFactory = new MessageFormatFactory();
  protected AllPropertiesMessageSource messageSource;

  @BeforeEach
  public void exceptionMessageTestBeforeMethod() {
    messageSource = new TestAllPropertiesMessageSource(messageFormatFactory);
    MessageSourceHolder.setMessageSource(messageSource);
  }

  /**
   * Parameterized test case. Overrides must be annotated with {@link Test} and set the name of the
   * {@link MethodSource}.
   *
   * @param language message language
   * @param message expected exception message
   */
  @SuppressWarnings("unused")
  protected abstract void testGetLocalizedMessage(String language, String message);

  /** Asserts that localized exception messages match. */
  protected static void assertExceptionMessageEquals(Exception e, String language, String message) {
    LocaleContextHolder.setLocale(new Locale(language));
    try {
      assertEquals(e.getLocalizedMessage(), message);
    } finally {
      LocaleContextHolder.setLocale(null);
    }
  }

  @AfterEach
  public void exceptionMessageTestAfterMethod() {
    MessageSourceHolder.setMessageSource(null);
  }
}
