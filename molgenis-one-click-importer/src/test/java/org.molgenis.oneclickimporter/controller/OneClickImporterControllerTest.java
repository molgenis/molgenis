package org.molgenis.oneclickimporter.controller;

import com.google.common.io.Resources;
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

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

		OneClickImporterController oneClickImporterController = new OneClickImporterController(menuReaderService,
				languageService, appSettings);

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
			   .andExpect(model().attribute("baseUrl", "/test-path"))
			   .andExpect(model().attribute("lng", "nl"))
			   .andExpect(model().attribute("fallbackLng", "en"));
	}

	@Test
	public void testFileImport() throws Exception
	{
		URL resourceUrl = Resources.getResource(OneClickImporterControllerTest.class, "/simple-valid.xlsx");
		File file = new File(new URI(resourceUrl.toString()).getPath());

		Path path = Paths.get(file.getAbsolutePath());
		byte[] data = Files.readAllBytes(path);

		MockMultipartFile multipartFile = new MockMultipartFile("file", file.getName(),
				"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", data);

		mockMvc.perform(fileUpload(OneClickImporterController.URI + "/upload").file(multipartFile))
			   .andExpect(status().isOk());
	}

}
