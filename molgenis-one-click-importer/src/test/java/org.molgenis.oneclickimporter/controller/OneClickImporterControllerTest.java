package org.molgenis.oneclickimporter.controller;

import com.google.common.io.Resources;
import org.apache.poi.ss.usermodel.Sheet;
import org.mockito.Mock;
import org.molgenis.data.i18n.LanguageService;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.oneclickimporter.service.ExcelService;
import org.molgenis.oneclickimporter.service.OneClickImporterService;
import org.molgenis.ui.menu.Menu;
import org.molgenis.ui.menu.MenuReaderService;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebAppConfiguration
public class OneClickImporterControllerTest
{
	private static final String CONTENT_TYPE_EXCEL = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
	private MockMvc mockMvc;

	@Mock
	private MenuReaderService menuReaderService;

	@Mock
	private LanguageService languageService;

	@Mock
	private AppSettings appSettings;

	@Mock
	private ExcelService excelService;

	@Mock
	private OneClickImporterService oneClickImporterService;

	@BeforeMethod
	public void before()
	{
		initMocks(this);

		OneClickImporterController oneClickImporterController = new OneClickImporterController(menuReaderService,
				languageService, appSettings, excelService, oneClickImporterService);

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
	public void testXLSXFileImport() throws Exception
	{
		MockMultipartFile multipartFile = getTestMultipartFile("/simple-valid.xlsx", CONTENT_TYPE_EXCEL);

		Sheet sheet = mock(Sheet.class);
		when(excelService.buildExcelSheetFromFile(any(File.class))).thenReturn(sheet);

		mockMvc.perform(fileUpload(OneClickImporterController.URI + "/upload").file(multipartFile))
			   .andExpect(status().isOk());

		verify(oneClickImporterService).buildDataCollection("simple-valid", sheet);
	}

	@Test
	public void testXLSFileImport() throws Exception
	{
		MockMultipartFile multipartFile = getTestMultipartFile("/simple-valid.xls",
				"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

		Sheet sheet = mock(Sheet.class);
		when(excelService.buildExcelSheetFromFile(any(File.class))).thenReturn(sheet);

		mockMvc.perform(fileUpload(OneClickImporterController.URI + "/upload").file(multipartFile))
			   .andExpect(status().isOk());

		verify(oneClickImporterService, times(1)).buildDataCollection("simple-valid", sheet);
	}

	@Test
	public void testUnsupportedFileTypeImport() throws Exception
	{
		MockMultipartFile multipartFile = getTestMultipartFile("/unsupported-file-type.nft", "some-unknown-type");

		Sheet sheet = mock(Sheet.class);
		when(excelService.buildExcelSheetFromFile(any(File.class))).thenReturn(sheet);

		mockMvc.perform(fileUpload(OneClickImporterController.URI + "/upload").file(multipartFile))
			   .andExpect(status().isBadRequest());

		verifyZeroInteractions(oneClickImporterService);
	}

	private MockMultipartFile getTestMultipartFile(final String path, final String contentType)
			throws URISyntaxException, IOException
	{
		URL resourceUrl = Resources.getResource(OneClickImporterControllerTest.class, path);
		File file = new File(new URI(resourceUrl.toString()).getPath());

		byte[] data = Files.readAllBytes(Paths.get(file.getAbsolutePath()));

		return new MockMultipartFile("file", file.getName(), contentType, data);
	}
}
