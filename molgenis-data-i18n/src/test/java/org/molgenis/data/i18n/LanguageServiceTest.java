package org.molgenis.data.i18n;

import org.mockito.Mock;
import org.molgenis.i18n.LanguageService;
import org.molgenis.i18n.MessageSourceHolder;
import org.molgenis.i18n.properties.AllPropertiesMessageSource;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Locale;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

public class LanguageServiceTest extends AbstractMockitoTest
{
	private static final Locale DUTCH = new Locale("nl");

	@Mock
	private MessageSource mockMessageSource;

	@BeforeMethod
	public void setup()
	{
		AllPropertiesMessageSource messageSource = new AllPropertiesMessageSource();
		messageSource.addMolgenisNamespaces("test");
		MessageSourceHolder.setMessageSource(messageSource);
		LocaleContextHolder.setLocale(DUTCH);
	}

	@AfterMethod
	public void tearDown()
	{
		MessageSourceHolder.setMessageSource(null);
		LocaleContextHolder.setLocale(null);
	}

	@Test
	public void testHasLanguageCodeTrue()
	{
		assertTrue(LanguageService.hasLanguageCode("en"));
	}

	@Test
	public void testHasLanguageCodeFalse()
	{
		assertFalse(LanguageService.hasLanguageCode("ko"));
	}

	@Test
	public void testGetLanguageCodes()
	{
		LocaleContextHolder.setLocale(DUTCH);
		assertEquals(LanguageService.getBundle().getString("EN_PLUS_NL"), "Engels plus Nederlands");
	}

	@Test
	public void testGetBundle()
	{
		LocaleContextHolder.setLocale(DUTCH);
		AllPropertiesMessageSource messageSource = new AllPropertiesMessageSource();
		messageSource.addMolgenisNamespaces("test");
		MessageSourceHolder.setMessageSource(messageSource);
		assertEquals(LanguageService.getBundle().getString("EN_PLUS_NL"), "Engels plus Nederlands");
	}

	@Test
	public void testGetCurrentUserLanguageCode()
	{
		LocaleContextHolder.setLocale(Locale.KOREAN);
		assertEquals(LanguageService.getCurrentUserLanguageCode(), "ko");
		LocaleContextHolder.setLocale(null);
	}

	@Test
	public void testFormatMessageCodeExists()
	{
		LocaleContextHolder.setLocale(DUTCH);
		MessageSourceHolder.setMessageSource(mockMessageSource);
		Object[] arguments = { "arg1", "arg2" };
		when(mockMessageSource.getMessage("NL_ONLY", arguments, DUTCH)).thenReturn("message");
		assertEquals(LanguageService.formatMessage("NL_ONLY", arguments), Optional.of("message"));
	}

	@Test
	public void testFormatMessageNotFound()
	{
		assertEquals(LanguageService.formatMessage("EN_ONLY", new Object[] {}), Optional.empty());
	}
}
