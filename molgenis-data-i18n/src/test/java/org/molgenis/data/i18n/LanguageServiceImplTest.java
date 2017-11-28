package org.molgenis.data.i18n;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.text.FormatFactory;
import org.mockito.Mock;
import org.molgenis.auth.User;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.i18n.format.LabelFormatFactory;
import org.molgenis.data.i18n.model.Language;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.text.MessageFormat;
import java.util.Map;

import static com.google.common.collect.ImmutableMap.of;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.molgenis.auth.UserMetaData.USER;
import static org.molgenis.data.i18n.LocalizationService.NAMESPACE_ALL;
import static org.molgenis.data.i18n.model.LanguageMetadata.LANGUAGE;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.testng.Assert.assertEquals;

public class LanguageServiceImplTest extends AbstractMockitoTest
{
	private LanguageServiceImpl languageService;
	@Mock
	private LocalizationService localizationService;
	@Mock
	private DataService dataServiceMock;
	@Mock
	private AppSettings appSettingsMock;
	@Mock
	private Query<Entity> queryMock;
	@Mock
	private EntityType entityTypeMock;
	@Mock
	private User user;
	@Mock
	private EntityType nlEntityMeta;
	@Mock
	private Attribute langCodeAttr;
	@Mock
	private Attribute nlAtrr;
	@Mock
	private EntityType languageMeta;
	@Mock
	private Language language;

	@SuppressWarnings("unchecked")
	@BeforeMethod
	public void beforeMethod()
	{
		when(dataServiceMock.query(USER)).thenReturn(queryMock);
		when(queryMock.eq(any(), any())).thenReturn(queryMock);
		Map<String, FormatFactory> messageFormats = ImmutableMap.of("label", new LabelFormatFactory());
		languageService = new LanguageServiceImpl(dataServiceMock, appSettingsMock, localizationService,
				messageFormats);
	}

	@Test
	public void getCurrentUserLanguageCode()
	{
		SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("test", "test"));
		when(langCodeAttr.getDataType()).thenReturn(STRING);
		when(nlEntityMeta.getAttribute("languageCode")).thenReturn(langCodeAttr);
		DynamicEntity langEntity = new DynamicEntity(nlEntityMeta, of("languageCode", "nl"));
		when(queryMock.findOne()).thenReturn(langEntity);
		when(nlAtrr.getDataType()).thenReturn(STRING);
		when(languageMeta.getAttribute("nl")).thenReturn(nlAtrr);
		DynamicEntity nlEntity = new DynamicEntity(languageMeta, of("nl", "Nederlands"));
		when(dataServiceMock.findOneById(LANGUAGE, "nl")).thenReturn(nlEntity);
		assertEquals(languageService.getCurrentUserLanguageCode(), "nl");
	}

	@Test
	public void getCurrentUserLanguageAppSettings()
	{
		SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("test", "test"));
		when(queryMock.findOne()).thenReturn(user);
		when(appSettingsMock.getLanguageCode()).thenReturn("de");
		Language language = mock(Language.class);
		when(dataServiceMock.findOneById(LANGUAGE, "de")).thenReturn(language);
		assertEquals(languageService.getCurrentUserLanguageCode(), "de");
	}

	@Test
	public void testGetMessageFormat()
	{
		SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("test", "test"));
		when(queryMock.findOne()).thenReturn(user);
		when(appSettingsMock.getLanguageCode()).thenReturn("de");
		when(dataServiceMock.findOneById(LANGUAGE, "de")).thenReturn(language);

		when(localizationService.getMessage(NAMESPACE_ALL, "T01", "de")).thenReturn(
				"Cannot find instance of type ''{0,label}'' and id ''{1}''.");
		when(entityTypeMock.getLabel("de")).thenReturn("Entität");

		MessageFormat format = languageService.getMessageFormat("T01");

		verifyNoMoreInteractions(localizationService);
		Object[] arguments = new Object[] { entityTypeMock, "id" };
		assertEquals(format.format(arguments), "Cannot find instance of type 'Entität' and id 'id'.");
	}
}
