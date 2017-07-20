package org.molgenis.oneclickimporter.controller;

import com.google.common.io.Resources;
import org.apache.poi.ss.usermodel.Sheet;
import org.hamcrest.core.StringEndsWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.molgenis.data.i18n.LanguageService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.file.FileStore;
import org.molgenis.oneclickimporter.model.DataCollection;
import org.molgenis.oneclickimporter.service.CsvService;
import org.molgenis.oneclickimporter.service.EntityService;
import org.molgenis.oneclickimporter.service.ExcelService;
import org.molgenis.oneclickimporter.service.OneClickImporterService;
import org.molgenis.test.AbstractMockitoTestNGSpringContextTests;
import org.molgenis.ui.menu.Menu;
import org.molgenis.ui.menu.MenuReaderService;
import org.molgenis.util.GsonConfig;
import org.molgenis.util.GsonHttpMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebAppConfiguration
@ContextConfiguration(classes = GsonConfig.class)
public class OneClickImporterControllerTest extends AbstractMockitoTestNGSpringContextTests
{
	private static final String CONTENT_TYPE_EXCEL = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
	private MockMvc mockMvc;

	@Autowired
	private GsonHttpMessageConverter gsonHttpMessageConverter;

	@Mock
	private MenuReaderService menuReaderService;

	@Mock
	private LanguageService languageService;

	@Mock
	private AppSettings appSettings;

	@Mock
	private ExcelService excelService;

	@Mock
	private CsvService csvService;

	@Mock
	private OneClickImporterService oneClickImporterService;

	@Mock
	private EntityService entityService;

	@Mock
	private FileStore fileStore;

	@BeforeMethod
	public void before()
	{
		initMocks();

		OneClickImporterController oneClickImporterController = new OneClickImporterController(menuReaderService,
				languageService, appSettings, excelService, csvService, oneClickImporterService, entityService,
				fileStore);

		Menu menu = mock(Menu.class);
		when(menu.findMenuItemPath(OneClickImporterController.ONE_CLICK_IMPORTER)).thenReturn("/test-path");
		when(menuReaderService.getMenu()).thenReturn(menu);
		when(languageService.getCurrentUserLanguageCode()).thenReturn("nl");
		when(appSettings.getLanguageCode()).thenReturn("en");

		mockMvc = MockMvcBuilders.standaloneSetup(oneClickImporterController)
								 .setMessageConverters(gsonHttpMessageConverter)
								 .build();
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
		DataCollection dataCollection = mock(DataCollection.class);
		EntityType table = mock(EntityType.class);
		String tableId = "generated_table_id";
		File file = mock(File.class);
		String fileName = "file-name";
		when(file.getName()).thenReturn(fileName);
		when(fileStore.store(any(InputStream.class), anyString())).thenReturn(file);
		when(excelService.buildExcelSheetFromFile(file)).thenReturn(sheet);
		when(oneClickImporterService.buildDataCollection("simple-valid", sheet)).thenReturn(dataCollection);
		when(entityService.createEntityType(dataCollection)).thenReturn(table);
		when(table.getId()).thenReturn(tableId);

		mockMvc.perform(fileUpload(OneClickImporterController.URI + "/upload").file(multipartFile))
			   .andExpect(status().isCreated())
			   .andExpect(header().string("Location", StringEndsWith.endsWith(tableId)))
			   .andExpect(jsonPath("$.entityId").value(tableId))
			   .andExpect(jsonPath("$.baseFileName").value(fileName));

		verify(oneClickImporterService).buildDataCollection("simple-valid", sheet);
	}

	@Test
	public void testXLSFileImport() throws Exception
	{
		MockMultipartFile multipartFile = getTestMultipartFile("/simple-valid.xls",
				"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

		Sheet sheet = mock(Sheet.class);
		DataCollection dataCollection = mock(DataCollection.class);
		EntityType table = mock(EntityType.class);
		String tableId = "generated_table_id";
		File file = mock(File.class);
		String fileName = "file-name";
		when(file.getName()).thenReturn(fileName);
		when(fileStore.store(any(InputStream.class), anyString())).thenReturn(file);
		when(excelService.buildExcelSheetFromFile(any(File.class))).thenReturn(sheet);
		when(oneClickImporterService.buildDataCollection("simple-valid", sheet)).thenReturn(dataCollection);
		when(entityService.createEntityType(dataCollection)).thenReturn(table);
		when(table.getId()).thenReturn(tableId);

		mockMvc.perform(fileUpload(OneClickImporterController.URI + "/upload").file(multipartFile))
			   .andExpect(status().isCreated())
			   .andExpect(header().string("Location", StringEndsWith.endsWith(tableId)))
			   .andExpect(jsonPath("$.entityId").value(tableId))
			   .andExpect(jsonPath("$.baseFileName").value(fileName));

		verify(oneClickImporterService, Mockito.times(1)).buildDataCollection("simple-valid", sheet);

	}

	@Test
	public void testUnsupportedFileTypeImport() throws Exception
	{
		MockMultipartFile multipartFile = getTestMultipartFile("/unsupported-file-type.nft", "some-unknown-type");

		Sheet sheet = mock(Sheet.class);
		when(excelService.buildExcelSheetFromFile(any(File.class))).thenReturn(sheet);

		mockMvc.perform(fileUpload(OneClickImporterController.URI + "/upload").file(multipartFile))
			   .andExpect(status().isBadRequest());

		Mockito.verifyZeroInteractions(oneClickImporterService);
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
