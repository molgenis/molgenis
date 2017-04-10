package org.molgenis.data.i18n;

import org.molgenis.auth.User;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.i18n.model.Language;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.data.support.DynamicEntity;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static com.google.common.collect.ImmutableMap.of;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.auth.UserMetaData.USER;
import static org.molgenis.data.i18n.model.LanguageMetadata.LANGUAGE;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.testng.Assert.assertEquals;

public class LanguageServiceTest
{
	private LanguageService languageService;
	private LocalizationService localizationService;
	private DataService dataServiceMock;
	private AppSettings appSettingsMock;
	private Query<Entity> queryMock;

	@SuppressWarnings("unchecked")
	@BeforeMethod
	public void beforeMethod()
	{
		dataServiceMock = mock(DataService.class);
		queryMock = mock(Query.class);
		localizationService= mock(LocalizationService.class);
		when(dataServiceMock.query(USER)).thenReturn(queryMock);
		when(queryMock.eq(any(), any())).thenReturn(queryMock);
		appSettingsMock = mock(AppSettings.class);
		languageService = new LanguageService(dataServiceMock, appSettingsMock, localizationService);
	}

	@Test
	public void getCurrentUserLanguageCode()
	{
		SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("test", "test"));
		EntityType nlEntityMeta = mock(EntityType.class);
		Attribute langCodeAtrr = when(mock(Attribute.class).getDataType()).thenReturn(STRING).getMock();
		when(nlEntityMeta.getAttribute("languageCode")).thenReturn(langCodeAtrr);
		DynamicEntity langEntity = new DynamicEntity(nlEntityMeta, of("languageCode", "nl"));
		when(queryMock.findOne()).thenReturn(langEntity);
		Attribute nlAtrr = when(mock(Attribute.class).getDataType()).thenReturn(STRING).getMock();
		EntityType languageMeta = mock(EntityType.class);
		when(languageMeta.getAttribute("nl")).thenReturn(nlAtrr);
		DynamicEntity nlEntity = new DynamicEntity(languageMeta, of("nl", "Nederlands"));
		when(dataServiceMock.findOneById(LANGUAGE, "nl")).thenReturn(nlEntity);
		assertEquals(languageService.getCurrentUserLanguageCode(), "nl");
	}

	@Test
	public void getCurrentUserLanguageAppSettings()
	{
		SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("test", "test"));
		User user = mock(User.class);
		when(user.getLanguageCode()).thenReturn("de");
		when(queryMock.findOne()).thenReturn(user);
		when(appSettingsMock.getLanguageCode()).thenReturn("de");
		Language language = mock(Language.class);
		when(dataServiceMock.findOneById(LANGUAGE, "de")).thenReturn(language);
		assertEquals(languageService.getCurrentUserLanguageCode(), "de");
	}
}
