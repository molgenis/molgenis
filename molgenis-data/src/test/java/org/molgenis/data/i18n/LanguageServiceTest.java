package org.molgenis.data.i18n;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.settings.AppSettings;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class LanguageServiceTest
{
	private LanguageService languageService;
	private DataService dataServiceMock;
	private AppSettings appSettingsMock;
	private Query<Entity> queryMock;

	@BeforeMethod
	public void beforeMethod()
	{
		dataServiceMock = mock(DataService.class);
		queryMock = mock(Query.class);
		when(dataServiceMock.query("MolgenisUser")).thenReturn(queryMock);
		when(queryMock.eq(any(), any())).thenReturn(queryMock);
		appSettingsMock = mock(AppSettings.class);
		languageService = new LanguageService(dataServiceMock, appSettingsMock);
	}

	@Test
	public void getCurrentUserLanguageCode()
	{
		SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("test", "test"));
		//		when(queryMock.findOne()).thenReturn(new MapEntity("languageCode", "nl")); // FIXME replace with DynamicEntity
		//		when(dataServiceMock.findOneById(LANGUAGE, "nl")).thenReturn(new MapEntity("nl", "Nederlands")); // FIXME replace with DynamicEntity
		assertEquals(languageService.getCurrentUserLanguageCode(), "nl");
	}

	@Test
	public void getCurrentUserLanguageAppSettings()
	{
		SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("test", "test"));
		//		when(queryMock.findOne()).thenReturn(new MapEntity()); // FIXME replace with DynamicEntity
		when(appSettingsMock.getLanguageCode()).thenReturn("de");
		//		when(dataServiceMock.findOneById(LANGUAGE, "de")).thenReturn(new MapEntity("nl", "Nederlands")); // FIXME replace with DynamicEntity
		assertEquals(languageService.getCurrentUserLanguageCode(), "de");
	}
}
