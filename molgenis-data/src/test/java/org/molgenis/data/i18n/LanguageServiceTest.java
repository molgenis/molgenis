package org.molgenis.data.i18n;

import static com.google.common.collect.ImmutableMap.of;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.MolgenisFieldTypes.STRING;
import static org.molgenis.auth.MolgenisUserMetaData.MOLGENIS_USER;
import static org.molgenis.data.i18n.LanguageMetaData.LANGUAGE;
import static org.testng.Assert.assertEquals;

import org.molgenis.auth.MolgenisUser;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.data.support.DynamicEntity;
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
		when(dataServiceMock.query(MOLGENIS_USER)).thenReturn(queryMock);
		when(queryMock.eq(any(), any())).thenReturn(queryMock);
		appSettingsMock = mock(AppSettings.class);
		languageService = new LanguageService(dataServiceMock, appSettingsMock);
	}

	@Test
	public void getCurrentUserLanguageCode()
	{
		SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("test", "test"));
		EntityMetaData nlEntityMeta = mock(EntityMetaData.class);
		AttributeMetaData langCodeAtrr = when(mock(AttributeMetaData.class).getDataType()).thenReturn(STRING).getMock();
		when(nlEntityMeta.getAttribute("languageCode")).thenReturn(langCodeAtrr);
		DynamicEntity langEntity = new DynamicEntity(nlEntityMeta, of("languageCode", "nl"));
		when(queryMock.findOne()).thenReturn(langEntity);
		AttributeMetaData nlAtrr = when(mock(AttributeMetaData.class).getDataType()).thenReturn(STRING).getMock();
		EntityMetaData languageMeta = mock(EntityMetaData.class);
		when(languageMeta.getAttribute("nl")).thenReturn(nlAtrr);
		DynamicEntity nlEntity = new DynamicEntity(languageMeta, of("nl", "Nederlands"));
		when(dataServiceMock.findOneById(LANGUAGE, "nl")).thenReturn(nlEntity);
		assertEquals(languageService.getCurrentUserLanguageCode(), "nl");
	}

	@Test
	public void getCurrentUserLanguageAppSettings()
	{
		SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("test", "test"));
		MolgenisUser molgenisUser = mock(MolgenisUser.class);
		when(molgenisUser.getLanguageCode()).thenReturn("de");
		when(queryMock.findOne()).thenReturn(molgenisUser);
		when(appSettingsMock.getLanguageCode()).thenReturn("de");
		Language language = mock(Language.class);
		when(dataServiceMock.findOneById(LANGUAGE, "de")).thenReturn(language);
		assertEquals(languageService.getCurrentUserLanguageCode(), "de");
	}
}
