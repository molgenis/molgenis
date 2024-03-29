package org.molgenis.oneclickimporter.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.google.common.io.Resources;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.molgenis.data.file.FileStore;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.security.auth.User;
import org.molgenis.jobs.JobExecutor;
import org.molgenis.oneclickimporter.job.OneClickImportJobExecution;
import org.molgenis.oneclickimporter.job.OneClickImportJobExecutionFactory;
import org.molgenis.security.user.UserAccountService;
import org.molgenis.settings.AppSettings;
import org.molgenis.test.AbstractMockitoSpringContextTests;
import org.molgenis.web.converter.GsonWebConfig;
import org.molgenis.web.menu.MenuReaderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.LocaleResolver;

@MockitoSettings(strictness = Strictness.LENIENT)
@WebAppConfiguration
@ContextConfiguration(classes = GsonWebConfig.class)
class OneClickImporterControllerTest extends AbstractMockitoSpringContextTests {
  private static final String CONTENT_TYPE_EXCEL =
      "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
  private MockMvc mockMvc;

  @Autowired private GsonHttpMessageConverter gsonHttpMessageConverter;

  @Mock private MenuReaderService menuReaderService;

  @Mock private LocaleResolver localeResolver;

  @Mock private AppSettings appSettings;

  @Mock private UserAccountService userAccountService;

  @Mock private FileStore fileStore;

  @Mock private OneClickImportJobExecutionFactory oneClickImportJobExecutionFactory;

  @Mock private JobExecutor jobExecutor;

  @BeforeEach
  void before() {
    OneClickImporterController oneClickImporterController =
        new OneClickImporterController(
            menuReaderService, fileStore, oneClickImportJobExecutionFactory, jobExecutor);

    when(menuReaderService.findMenuItemPath(OneClickImporterController.ONE_CLICK_IMPORTER))
        .thenReturn("/test-path");
    when(localeResolver.resolveLocale(any())).thenReturn(new Locale("nl"));
    when(appSettings.getLanguageCode()).thenReturn("en");
    User user = mock(User.class);
    when(user.isSuperuser()).thenReturn(false);
    when(userAccountService.getCurrentUser()).thenReturn(user);

    OneClickImportJobExecution jobExecution = mock(OneClickImportJobExecution.class);
    when(oneClickImportJobExecutionFactory.create()).thenReturn(jobExecution);

    EntityType oneClickImportJobExecutionEntityType = mock(EntityType.class);
    when(jobExecution.getEntityType()).thenReturn(oneClickImportJobExecutionEntityType);
    when(jobExecution.getIdValue()).thenReturn("id_1");
    when(oneClickImportJobExecutionEntityType.getId()).thenReturn("jobExecutionId");

    StringHttpMessageConverter stringConverter = new StringHttpMessageConverter();
    stringConverter.setWriteAcceptCharset(false);

    mockMvc =
        MockMvcBuilders.standaloneSetup(oneClickImporterController)
            .setLocaleResolver(localeResolver)
            .setMessageConverters(gsonHttpMessageConverter, stringConverter)
            .build();
  }

  /** Test that a get call to the plugin returns the correct view */
  @Test
  void testInit() throws Exception {
    mockMvc
        .perform(get(OneClickImporterController.URI))
        .andExpect(status().isOk())
        .andExpect(view().name("view-one-click-importer"))
        .andExpect(model().attribute("baseUrl", "/test-path"));
  }

  @Test
  void testUpload() throws Exception {
    MockMultipartFile multipartFile =
        getTestMultipartFile("/simple-valid.xlsx", CONTENT_TYPE_EXCEL);

    mockMvc
        .perform(
            fileUpload(OneClickImporterController.URI + "/upload")
                .file(multipartFile)
                .accept(MediaType.TEXT_HTML))
        .andExpect(status().isOk())
        .andExpect(content().string("/api/v2/jobExecutionId/id_1"));
  }

  private MockMultipartFile getTestMultipartFile(final String path, final String contentType)
      throws URISyntaxException, IOException {
    URL resourceUrl = Resources.getResource(OneClickImporterControllerTest.class, path);
    File file = new File(new URI(resourceUrl.toString()).getPath());

    byte[] data = Files.readAllBytes(Paths.get(file.getAbsolutePath()));

    return new MockMultipartFile("file", file.getName(), contentType, data);
  }
}
