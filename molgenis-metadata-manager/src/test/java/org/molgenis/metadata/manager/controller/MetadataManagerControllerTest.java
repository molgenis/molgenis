package org.molgenis.metadata.manager.controller;

import org.mockito.Mock;
import org.molgenis.data.i18n.LanguageService;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.ui.menu.Menu;
import org.molgenis.ui.menu.MenuReaderService;
import org.molgenis.util.GsonConfig;
import org.molgenis.util.GsonHttpMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebAppConfiguration
@ContextConfiguration(classes = { GsonConfig.class })
public class MetadataManagerControllerTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	private GsonHttpMessageConverter gsonHttpMessageConverter;

	private MockMvc mockMvc;

	@Mock
	private MenuReaderService menuReaderService;

	@Mock
	private LanguageService languageService;

	@Mock
	private AppSettings appSettings;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		initMocks(this);

		FreeMarkerViewResolver freeMarkerViewResolver = new FreeMarkerViewResolver();
		freeMarkerViewResolver.setSuffix(".ftl");

		Menu menu = mock(Menu.class);
		when(menu.findMenuItemPath(MetadataManagerController.METADATA_MANAGER)).thenReturn("/test/path");
		when(menuReaderService.getMenu()).thenReturn(menu);

		when(languageService.getCurrentUserLanguageCode()).thenReturn("en");
		when(appSettings.getLanguageCode()).thenReturn("nl");

		MetadataManagerController metadataEditorController = new MetadataManagerController(menuReaderService,
				languageService, appSettings);

		mockMvc = MockMvcBuilders.standaloneSetup(metadataEditorController)
				.setMessageConverters(new FormHttpMessageConverter(), gsonHttpMessageConverter).build();
	}

	@Test
	public void testInit() throws Exception
	{
		this.mockMvc.perform(get("/plugin/metadata-manager")).andExpect(status().isOk())
				.andExpect(view().name("view-metadata-manager")).andExpect(model().attribute("baseUrl", "/test/path"))
				.andExpect(model().attribute("lng", "en")).andExpect(model().attribute("fallbackLng", "nl"));
	}
}