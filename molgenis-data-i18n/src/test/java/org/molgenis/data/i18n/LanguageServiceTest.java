package org.molgenis.data.i18n;

import org.molgenis.data.DataService;
import org.molgenis.data.i18n.model.Language;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.security.core.model.User;
import org.molgenis.security.core.service.UserAccountService;
import org.springframework.security.test.context.support.WithMockUser;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.i18n.model.LanguageMetadata.LANGUAGE;
import static org.testng.Assert.assertEquals;

public class LanguageServiceTest
{
	private LanguageService languageService;
	private LocalizationService localizationService;
	private DataService dataServiceMock;
	private AppSettings appSettings;
	private UserAccountService userAccountService;
	private User user = mock(User.class);

	@BeforeMethod
	public void beforeMethod()
	{
		dataServiceMock = mock(DataService.class);
		localizationService = mock(LocalizationService.class);
		appSettings = mock(AppSettings.class);
		userAccountService = mock(UserAccountService.class);
		when(dataServiceMock.findOneById(LANGUAGE, "nl")).thenReturn(mock(Language.class));
		languageService = new LanguageService(dataServiceMock, appSettings, localizationService, userAccountService);
	}

	@Test
	@WithMockUser
	public void getCurrentUserLanguageCodePrefersUserLanguageCode()
	{
		when(user.getLanguageCode()).thenReturn(Optional.of("nl"));
		when(userAccountService.getCurrentUserIfPresent()).thenReturn(Optional.of(user));
		assertEquals(languageService.getCurrentUserLanguageCode(), "nl");
	}

	@Test
	@WithMockUser
	public void getCurrentUserLanguageCodeUserCodeUnknown()
	{
		when(user.getLanguageCode()).thenReturn(Optional.of("??"));
		when(userAccountService.getCurrentUserIfPresent()).thenReturn(Optional.of(user));
		when(appSettings.getLanguageCode()).thenReturn("nl");
		assertEquals(languageService.getCurrentUserLanguageCode(), "nl");
	}

	@Test
	@WithMockUser
	public void getCurrentUserLanguageCodeUserCodeNotSpecified()
	{
		when(user.getLanguageCode()).thenReturn(Optional.empty());
		when(userAccountService.getCurrentUserIfPresent()).thenReturn(Optional.of(user));
		when(appSettings.getLanguageCode()).thenReturn("nl");
		assertEquals(languageService.getCurrentUserLanguageCode(), "nl");
	}

	@Test
	@WithMockUser
	public void getCurrentUserLanguageCodeNoUserLanguageCode()
	{
		when(userAccountService.getCurrentUserIfPresent()).thenReturn(Optional.of(user));
		when(appSettings.getLanguageCode()).thenReturn("nl");
		assertEquals(languageService.getCurrentUserLanguageCode(), "nl");
	}

	@Test
	public void getCurrentUserLanguageNoCurrentUserAppSettings()
	{
		when(userAccountService.getCurrentUserIfPresent()).thenReturn(Optional.empty());
		when(appSettings.getLanguageCode()).thenReturn("nl");
		assertEquals(languageService.getCurrentUserLanguageCode(), "nl");
	}

	@Test
	public void getCurrentUserLanguageNoCurrentUserAppSettingsLanguageCodeUnknownFallbackToDefaultLanguage()
	{
		when(userAccountService.getCurrentUserIfPresent()).thenReturn(Optional.empty());
		when(appSettings.getLanguageCode()).thenReturn("??");
		assertEquals(languageService.getCurrentUserLanguageCode(), "en");
	}
}
