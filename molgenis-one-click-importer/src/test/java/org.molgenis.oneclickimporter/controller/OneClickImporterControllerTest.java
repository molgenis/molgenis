package org.molgenis.oneclickimporter.controller;

import org.mockito.Mock;
import org.molgenis.data.i18n.LanguageService;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.ui.menu.Menu;
import org.molgenis.ui.menu.MenuReaderService;
import org.springframework.context.annotation.Configuration;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@Configuration
@EnableWebMvc
public class OneClickImporterControllerTest
{
	private MockMvc mockMvc;

	@Mock
	private MenuReaderService menuReaderService;

	@Mock
	private LanguageService languageService;

	@Mock
	private AppSettings appSettings;


	@BeforeMethod
	public void before()
	{
		initMocks(this);

		OneClickImporterController oneClickImporterController = new OneClickImporterController(menuReaderService, languageService,
				appSettings);
		Menu menu = mock(Menu.class);
		when(menu.findMenuItemPath(OneClickImporterController.ONE_CLICK_IMPORTER)).thenReturn("/test-path");
		when(menuReaderService.getMenu()).thenReturn(menu);
		when(languageService.getCurrentUserLanguageCode()).thenReturn("nl");
		when(appSettings.getLanguageCode()).thenReturn("en");
		mockMvc = MockMvcBuilders.standaloneSetup(oneClickImporterController).build();
	}

	/**
	 * Test that a get call to the plugin returns the correct view
	 */
	@Test
	public void testInit() throws Exception
	{
		mockMvc.perform(get(OneClickImporterController.URI))
				.andExpect(status().isOk())
				.andExpect(view().name("view-one-click-importer"))
				.
						andExpect(model().attribute("baseUrl", "/test-path"))
				.
						andExpect(model().attribute("lng", "nl"))
				.
						andExpect(model().attribute("fallbackLng", "en"));
	}

	@Test
	public void testFileImport() throws Exception
	{
		MockMultipartFile multipartFile = new MockMultipartFile(
				"file", "test.xls", "application/vnd.ms-exceln", "Spring Framework".getBytes());
		mockMvc.perform(fileUpload(OneClickImporterController.URI + "/upload").file(multipartFile))
				.andExpect(status().isOk());
	}

}
