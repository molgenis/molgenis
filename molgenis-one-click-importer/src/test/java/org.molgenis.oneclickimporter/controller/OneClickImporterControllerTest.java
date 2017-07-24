package org.molgenis.oneclickimporter.controller;

import com.google.common.io.Resources;
import org.mockito.Mock;
import org.molgenis.data.i18n.LanguageService;
import org.molgenis.data.jobs.JobExecutor;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.file.FileStore;
import org.molgenis.oneclickimporter.job.OneClickImportJobExecution;
import org.molgenis.oneclickimporter.job.OneClickImportJobExecutionFactory;
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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
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
	private OneClickImporterService oneClickImporterService;

	@Mock
	private EntityService entityService;

	@Mock
	private FileStore fileStore;

	@Mock
	private OneClickImportJobExecutionFactory oneClickImportJobExecutionFactory;

	@Mock
	private JobExecutor jobExecutor;

	@BeforeMethod
	public void before()
	{
		initMocks();

		OneClickImporterController oneClickImporterController = new OneClickImporterController(menuReaderService,
				languageService, appSettings, fileStore, oneClickImportJobExecutionFactory, jobExecutor);

		Menu menu = mock(Menu.class);
		when(menu.findMenuItemPath(OneClickImporterController.ONE_CLICK_IMPORTER)).thenReturn("/test-path");
		when(menuReaderService.getMenu()).thenReturn(menu);
		when(languageService.getCurrentUserLanguageCode()).thenReturn("nl");
		when(appSettings.getLanguageCode()).thenReturn("en");

		OneClickImportJobExecution jobExecution = mock(OneClickImportJobExecution.class);
		when(oneClickImportJobExecutionFactory.create()).thenReturn(jobExecution);

		EntityType oneClickImportJobExecutionEntityType = mock(EntityType.class);
		when(jobExecution.getEntityType()).thenReturn(oneClickImportJobExecutionEntityType);
		when(jobExecution.getIdValue()).thenReturn("id_1");
		when(oneClickImportJobExecutionEntityType.getId()).thenReturn("jobExecutionId");

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
	public void testUpload() throws Exception
	{
		MockMultipartFile multipartFile = getTestMultipartFile("/simple-valid.xlsx", CONTENT_TYPE_EXCEL);

		mockMvc.perform(
				fileUpload(OneClickImporterController.URI + "/upload").file(multipartFile))
			   .andExpect(status().isOk())
			   .andExpect(content().string("\"/api/v2/jobExecutionId/id_1\""));
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
