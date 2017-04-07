package org.molgenis.data.i18n;

import org.mockito.Mock;
import org.molgenis.data.settings.AppSettings;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Locale;

import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.Assert.*;

public class LocalizationMessageSourceTest
{
	@Mock
	private LocalizationService localizationService;
	@Mock
	private AppSettings appSettings;

	private LocalizationMessageSource localizationMessageSource;

	@BeforeMethod
	public void setUp() throws Exception
	{
		initMocks(this);
		when(localizationService.getMessage("my-namespace", "TEST_MESSAGE_DE", "de")).thenReturn("Deutsche Nachricht");
		when(localizationService.getMessage("my-namespace", "TEST_MESSAGE_NL", "nl")).thenReturn("Nederlands bericht");
		when(localizationService.getMessage("my-namespace", "TEST_MESSAGE_EN", "en")).thenReturn("English message");
		this.localizationMessageSource = new LocalizationMessageSource(localizationService, "my-namespace",
				appSettings);
	}

	@AfterMethod
	public void tearDown() throws Exception
	{
	}

	@Test
	public void testGetDefaultMessage() throws Exception
	{
		assertEquals(localizationMessageSource.getDefaultMessage("CODE"), "#CODE#");
	}

	@Test
	public void testResolveCodeCreatesMessageFormat() throws Exception
	{
	}

	@Test
	public void testResolveCodeWithoutArgumentsMessageFoundForCode() throws Exception
	{
		assertEquals(localizationMessageSource.resolveCodeWithoutArguments("TEST_MESSAGE_DE", Locale.GERMAN),
				"Deutsche Nachricht");
	}

	@Test
	public void testResolveCodeWithoutArgumentsMessageFoundForAppDefaultLanguage() throws Exception
	{
		when(appSettings.getLanguageCode()).thenReturn("nl");
		assertEquals(localizationMessageSource.resolveCodeWithoutArguments("TEST_MESSAGE_NL", new Locale("nl")),
				"Nederlands bericht");
	}

	@Test
	public void testResolveCodeWithoutArgumentsMessageFoundForDefaultLanguage() throws Exception
	{
		assertEquals(localizationMessageSource.resolveCodeWithoutArguments("TEST_MESSAGE_EN", new Locale("en")),
				"English message");
	}

	@Test
	public void testResolveCodeWithoutArgumentsMessageNotFound() throws Exception
	{
		assertNull(localizationMessageSource.resolveCodeWithoutArguments("MISSING", new Locale("en")));
	}

}