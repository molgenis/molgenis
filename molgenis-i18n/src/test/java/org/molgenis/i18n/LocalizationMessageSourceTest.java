package org.molgenis.i18n;

import static java.util.Locale.ENGLISH;
import static java.util.Locale.GERMAN;
import static java.util.Locale.KOREAN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.molgenis.util.i18n.LanguageService.DEFAULT_LOCALE;

import java.util.Locale;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.test.AbstractMockitoTest;
import org.molgenis.util.i18n.Labeled;
import org.molgenis.util.i18n.format.MessageFormatFactory;

class LocalizationMessageSourceTest extends AbstractMockitoTest {
  @Mock private MessageResolution messageRepository;
  @Mock private Labeled labeled;
  @Mock private Supplier<Locale> fallbackLocaleSupplier;

  private LocalizationMessageSource messageSource;
  private MessageFormatFactory messageFormatFactory = new MessageFormatFactory();

  @BeforeEach
  void setUp() {
    messageSource =
        new LocalizationMessageSource(
            messageFormatFactory, messageRepository, fallbackLocaleSupplier);
  }

  @Test
  void testGetDefaultMessage() {
    assertEquals("#CODE#", messageSource.getDefaultMessage("CODE"));
  }

  @Test
  void testResolveCodeWithoutArgumentsMessageFoundForCode() {
    when(messageRepository.resolveCodeWithoutArguments("TEST_MESSAGE_DE", GERMAN))
        .thenReturn("Deutsche Nachricht");
    assertEquals(
        "Deutsche Nachricht", messageSource.resolveCodeWithoutArguments("TEST_MESSAGE_DE", GERMAN));
  }

  @Test
  void testResolveCodeWithoutArgumentsMessageFoundForAppDefaultLanguage() {
    doReturn(new Locale("nl")).when(fallbackLocaleSupplier).get();
    doReturn(null).when(messageRepository).resolveCodeWithoutArguments("TEST_MESSAGE_NL", GERMAN);
    doReturn("Nederlands bericht")
        .when(messageRepository)
        .resolveCodeWithoutArguments("TEST_MESSAGE_NL", new Locale("nl"));
    assertEquals(
        "Nederlands bericht", messageSource.resolveCodeWithoutArguments("TEST_MESSAGE_NL", GERMAN));
  }

  @Test
  void testResolveCodeWithoutArgumentsMessageFoundForDefaultLanguage() {
    when(messageRepository.resolveCodeWithoutArguments("TEST_MESSAGE_EN", ENGLISH))
        .thenReturn("English message");

    assertEquals(
        "English message", messageSource.resolveCodeWithoutArguments("TEST_MESSAGE_EN", ENGLISH));
  }

  @Test
  void testResolveCodeWithoutArgumentsMessageNotFound() {
    assertNull(messageSource.resolveCodeWithoutArguments("MISSING", new Locale("en")));
  }

  @Test
  void testResolveCode() {
    doReturn("The Label").when(labeled).getLabel("en");
    when(messageRepository.resolveCodeWithoutArguments("TEST_MESSAGE_EN", ENGLISH))
        .thenReturn("label: ''{0, label}''");
    assertEquals(
        "label: 'The Label'",
        messageSource.resolveCode("TEST_MESSAGE_EN", ENGLISH).format(new Object[] {labeled}));
  }

  @Test
  void testResolveCodeFallbackIsIndependentOfArgumentFallback() {
    doReturn(new Locale("nl")).when(fallbackLocaleSupplier).get();
    // The message lookup is done with fallback but independently of the lookup by the formatter
    // which in MOLGENIS
    // has its own standard fallback mechanism.
    doReturn(null)
        .when(messageRepository)
        .resolveCodeWithoutArguments("TEST_MESSAGE_EN", new Locale("ko"));
    doReturn("The Label (ko)").when(labeled).getLabel("ko");
    doReturn("Het label: ''{0, label}''")
        .when(messageRepository)
        .resolveCodeWithoutArguments("TEST_MESSAGE_EN", new Locale("nl"));
    assertEquals(
        "Het label: 'The Label (ko)'",
        messageSource.resolveCode("TEST_MESSAGE_EN", KOREAN).format(new Object[] {labeled}));
  }

  @Test
  void testResolveCodeWithoutArgumentsSupplierFails() {
    doThrow(new RuntimeException()).when(fallbackLocaleSupplier).get();
    doReturn("test").when(messageRepository).resolveCodeWithoutArguments("TEST", DEFAULT_LOCALE);

    assertEquals("test", messageSource.resolveCodeWithoutArguments("TEST", null));
  }
}
