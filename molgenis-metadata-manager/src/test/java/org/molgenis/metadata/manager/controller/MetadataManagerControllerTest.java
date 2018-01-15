package org.molgenis.metadata.manager.controller;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.molgenis.core.ui.menu.Menu;
import org.molgenis.core.ui.menu.MenuReaderService;
import org.molgenis.core.ui.util.GsonConfig;
import org.molgenis.core.util.GsonHttpMessageConverter;
import org.molgenis.data.security.auth.User;
import org.molgenis.metadata.manager.model.*;
import org.molgenis.metadata.manager.service.MetadataManagerService;
import org.molgenis.security.user.UserAccountService;
import org.molgenis.settings.AppSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Locale;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebAppConfiguration
@ContextConfiguration(classes = { MetadataManagerControllerTest.Config.class, GsonConfig.class })
public class MetadataManagerControllerTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	private GsonHttpMessageConverter gsonHttpMessageConverter;

	@Autowired
	private MenuReaderService menuReaderService;

	@Autowired
	private AppSettings appSettings;

	@Autowired
	private MetadataManagerService metadataManagerService;

	@Autowired
	private UserAccountService userAccountService;

	@Mock
	private LocaleResolver localeResolver;

	private MockMvc mockMvc;

	@BeforeMethod
	public void beforeMethod()
	{
		MockitoAnnotations.initMocks(this);
		FreeMarkerViewResolver freeMarkerViewResolver = new FreeMarkerViewResolver();
		freeMarkerViewResolver.setSuffix(".ftl");

		Menu menu = mock(Menu.class);
		when(menu.findMenuItemPath(MetadataManagerController.METADATA_MANAGER)).thenReturn("/test/path");
		when(menuReaderService.getMenu()).thenReturn(menu);

		when(appSettings.getLanguageCode()).thenReturn("nl");
		User user = mock(User.class);
		when(user.isSuperuser()).thenReturn(false);
		when(userAccountService.getCurrentUser()).thenReturn(user);

		MetadataManagerController metadataEditorController = new MetadataManagerController(menuReaderService,
				appSettings, metadataManagerService, userAccountService);

		mockMvc = MockMvcBuilders.standaloneSetup(metadataEditorController).setLocaleResolver(localeResolver)
								 .setMessageConverters(new FormHttpMessageConverter(), gsonHttpMessageConverter)
								 .build();
	}

	@Test
	public void testInit() throws Exception
	{
		when(localeResolver.resolveLocale(any())).thenReturn(Locale.GERMAN);
		mockMvc.perform(get("/plugin/metadata-manager"))
			   .andExpect(status().isOk())
			   .andExpect(view().name("view-metadata-manager"))
			   .andExpect(model().attribute("baseUrl", "/test/path")).andExpect(model().attribute("lng", "de"))
			   .andExpect(model().attribute("fallbackLng", "nl"));
	}

	@Test
	public void testGetEditorPackages() throws Exception
	{
		when(metadataManagerService.getEditorPackages()).thenReturn(getEditorPackageResponse());
		mockMvc.perform(get("/plugin/metadata-manager/editorPackages"))
			   .andExpect(status().isOk())
			   .andExpect(content().contentType(APPLICATION_JSON))
			   .andExpect(content().string(getEditorPackageResponseJson()));
	}

	@Test
	public void testGetEditorEntityType() throws Exception
	{
		when(metadataManagerService.getEditorEntityType("id_1")).thenReturn(getEditorEntityTypeResponse());
		mockMvc.perform(get("/plugin/metadata-manager/entityType/{id}", "id_1"))
			   .andExpect(status().isOk())
			   .andExpect(content().contentType(APPLICATION_JSON))
			   .andExpect(content().string(getEditorEntityTypeResponseJson()));
	}

	@Test
	public void testCreateEditorEntityType() throws Exception
	{
		EditorEntityTypeResponse editorEntityTypeResponse = getEditorEntityTypeResponse();
		when(metadataManagerService.createEditorEntityType()).thenReturn(editorEntityTypeResponse);
		mockMvc.perform(get("/plugin/metadata-manager/create/entityType"))
			   .andExpect(status().isOk())
			   .andExpect(content().contentType(APPLICATION_JSON))
			   .andExpect(content().string(getEditorEntityTypeResponseJson()));
	}

	@Test
	public void testUpsertEntityType() throws Exception
	{
		mockMvc.perform(post("/plugin/metadata-manager/entityType").contentType(APPLICATION_JSON)
																   .content(getEditorEntityTypeJson()))
			   .andExpect(status().isOk());
		verify(metadataManagerService, times(1)).upsertEntityType(getEditorEntityType());
	}

	@Test
	public void testCreateEditorAttribute() throws Exception
	{
		when(metadataManagerService.createEditorAttribute()).thenReturn(getEditorAttributeResponse());
		mockMvc.perform(get("/plugin/metadata-manager/create/attribute"))
			   .andExpect(status().isOk())
			   .andExpect(content().contentType(APPLICATION_JSON))
			   .andExpect(content().string(getEditorAttributeResponseJson()));
	}

	private List<EditorPackageIdentifier> getEditorPackageResponse()
	{
		return newArrayList(EditorPackageIdentifier.create("test", "test"));
	}

	private String getEditorPackageResponseJson()
	{
		return "[{\"id\":\"test\",\"label\":\"test\"}]";
	}

	private EditorEntityTypeResponse getEditorEntityTypeResponse()
	{
		return EditorEntityTypeResponse.create(getEditorEntityType(),
				newArrayList("en", "nl", "de", "es", "it", "pt", "fr", "xx"));
	}

	private String getEditorEntityTypeResponseJson()
	{
		return "{\"entityType\":" + getEditorEntityTypeJson()
				+ ",\"languageCodes\":[\"en\",\"nl\",\"de\",\"es\",\"it\",\"pt\",\"fr\",\"xx\"]}";
	}

	private EditorEntityType getEditorEntityType()
	{
		return EditorEntityType.create("id_1", null, ImmutableMap.of(), null, ImmutableMap.of(), false, "backend", null,
				null, ImmutableList.of(), ImmutableList.of(), ImmutableList.of(),
				EditorAttributeIdentifier.create("id", "label"), EditorAttributeIdentifier.create("id", "label"),
				ImmutableList.of());
	}

	private String getEditorEntityTypeJson()
	{
		return "{\"id\":\"id_1\",\"labelI18n\":{},\"descriptionI18n\":{},\"abstract0\":false,\"backend\":\"backend\",\"attributes\":[],\"referringAttributes\":[],\"tags\":[],\"idAttribute\":{\"id\":\"id\",\"label\":\"label\"},\"labelAttribute\":{\"id\":\"id\",\"label\":\"label\"},\"lookupAttributes\":[]}";
	}

	private EditorAttributeResponse getEditorAttributeResponse()
	{
		EditorAttribute editorAttribute = EditorAttribute.create("1", null, null, null, null, null, null, null, false,
				false, false, null, ImmutableMap.of(), null, ImmutableMap.of(), false, ImmutableList.of(), null, null,
				false, false, ImmutableList.of(), null, null, null, null, 1);

		return EditorAttributeResponse.create(editorAttribute,
				newArrayList("en", "nl", "de", "es", "it", "pt", "fr", "xx"));
	}

	private String getEditorAttributeResponseJson()
	{
		return "{\"attribute\":{\"id\":\"1\",\"nullable\":false,\"auto\":false,\"visible\":false,\"labelI18n\":{},\"descriptionI18n\":{},\"aggregatable\":false,\"enumOptions\":[],\"readonly\":false,\"unique\":false,\"tags\":[],\"sequenceNumber\":1},\"languageCodes\":[\"en\",\"nl\",\"de\",\"es\",\"it\",\"pt\",\"fr\",\"xx\"]}";
	}

	@Configuration
	public static class Config
	{
		@Bean
		public MenuReaderService menuReaderService()
		{
			return mock(MenuReaderService.class);
		}

		@Bean
		public AppSettings appSettings()
		{
			return mock(AppSettings.class);
		}

		@Bean
		public MetadataManagerService metadataManagerService()
		{
			return mock(MetadataManagerService.class);
		}

		@Bean
		public UserAccountService userAccountService()
		{
			return mock(UserAccountService.class);
		}
	}
}