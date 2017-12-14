package org.molgenis.i18n;

import org.mockito.Mock;
import org.molgenis.i18n.format.MessageFormatFactory;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Locale;
import java.util.function.Supplier;

import static java.util.Locale.*;
import static org.mockito.Mockito.*;
import static org.molgenis.i18n.LanguageService.DEFAULT_LOCALE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

public class LocalizationMessageSourceTest extends AbstractMockitoTest
{
	@Mock
	private MessageResolution messageRepository;
	@Mock
	private Labeled labeled;
	@Mock
	private Supplier<Locale> fallbackLocaleSupplier;

	private LocalizationMessageSource messageSource;
	private MessageFormatFactory messageFormatFactory = new MessageFormatFactory();

	@BeforeMethod
	public void setUp()
	{
		messageSource = new LocalizationMessageSource(messageFormatFactory, messageRepository, fallbackLocaleSupplier);
	}

	@Test
	public void testGetDefaultMessage()
	{
		assertEquals(messageSource.getDefaultMessage("CODE"), "#CODE#");
	}

	@Test
	public void testResolveCodeWithoutArgumentsMessageFoundForCode()
	{
		when(messageRepository.resolveCodeWithoutArguments("TEST_MESSAGE_DE", GERMAN)).thenReturn("Deutsche Nachricht");
		assertEquals(messageSource.resolveCodeWithoutArguments("TEST_MESSAGE_DE", GERMAN),
				"Deutsche Nachricht");
	}

	@Test
	public void testResolveCodeWithoutArgumentsMessageFoundForAppDefaultLanguage()
	{
		doReturn(new Locale("nl")).when(fallbackLocaleSupplier).get();
		doReturn(null).when(messageRepository).resolveCodeWithoutArguments("TEST_MESSAGE_NL", GERMAN);
		doReturn("Nederlands bericht").when(messageRepository)
									  .resolveCodeWithoutArguments("TEST_MESSAGE_NL", new Locale("nl"));
		assertEquals(messageSource.resolveCodeWithoutArguments("TEST_MESSAGE_NL", GERMAN),
				"Nederlands bericht");
	}

	@Test
	public void testResolveCodeWithoutArgumentsMessageFoundForDefaultLanguage()
	{
		when(messageRepository.resolveCodeWithoutArguments("TEST_MESSAGE_EN", ENGLISH)).thenReturn("English message");

		assertEquals(messageSource.resolveCodeWithoutArguments("TEST_MESSAGE_EN", ENGLISH),
				"English message");
	}

	@Test
	public void testResolveCodeWithoutArgumentsMessageNotFound()
	{
		assertNull(messageSource.resolveCodeWithoutArguments("MISSING", new Locale("en")));
	}

	@Test
	public void testResolveCode()
	{
		doReturn("The Label").when(labeled).getLabel("en");
		when(messageRepository.resolveCodeWithoutArguments("TEST_MESSAGE_EN", ENGLISH)).thenReturn(
				"label: ''{0, label}''");
		assertEquals(messageSource.resolveCode("TEST_MESSAGE_EN", ENGLISH).format(new Object[] { labeled }),
				"label: 'The Label'");
	}

	@Test
	public void testResolveCodeFallbackIsIndependentOfArgumentFallback()
	{
		doReturn(new Locale("nl")).when(fallbackLocaleSupplier).get();
		// The message lookup is done with fallback but independently of the lookup by the formatter which in MOLGENIS
		// has its own standard fallback mechanism.
		doReturn(null).when(messageRepository).resolveCodeWithoutArguments("TEST_MESSAGE_EN", new Locale("ko"));
		doReturn("The Label (ko)").when(labeled).getLabel("ko");
		doReturn("Het label: ''{0, label}''").when(messageRepository)
											 .resolveCodeWithoutArguments("TEST_MESSAGE_EN", new Locale("nl"));
		assertEquals(messageSource.resolveCode("TEST_MESSAGE_EN", KOREAN).format(new Object[] { labeled }),
				"Het label: 'The Label (ko)'");
	}

	@Test
	public void testResolveCodeWithoutArgumentsSupplierFails()
	{
		doThrow(new RuntimeException()).when(fallbackLocaleSupplier).get();
		doReturn("test").when(messageRepository).resolveCodeWithoutArguments("TEST", DEFAULT_LOCALE);

		assertEquals(messageSource.resolveCodeWithoutArguments("TEST", null), "test");

	}
}