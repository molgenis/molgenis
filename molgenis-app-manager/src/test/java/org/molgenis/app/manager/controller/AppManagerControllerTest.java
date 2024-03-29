package org.molgenis.app.manager.controller;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.molgenis.app.manager.service.impl.AppManagerServiceImpl.ZIP_CONFIG_FILE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.google.gson.Gson;
import java.io.InputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.app.manager.meta.App;
import org.molgenis.app.manager.meta.AppMetadata;
import org.molgenis.app.manager.model.AppConfig;
import org.molgenis.app.manager.model.AppResponse;
import org.molgenis.app.manager.service.AppManagerService;
import org.molgenis.data.DataService;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableWebMvc
class AppManagerControllerTest {
  private MockMvc mockMvc;

  @Mock private AppManagerService appManagerService;

  @Mock private DataService dataService;

  private AppResponse appResponse;

  @BeforeEach
  void beforeMethod() {
    initMocks(this);

    App app = mock(App.class);
    when(app.getId()).thenReturn("id");
    when(app.getName()).thenReturn("app1");
    when(app.getLabel()).thenReturn("label");
    when(app.getDescription()).thenReturn("description");
    when(app.isActive()).thenReturn(true);
    when(app.getAppVersion()).thenReturn("v1.0.0");
    when(app.includeMenuAndFooter()).thenReturn(true);
    when(app.getTemplateContent()).thenReturn("<h1>Test</h1>");
    when(app.getAppConfig()).thenReturn("{'config': 'test'}");
    when(app.getResourceFolder()).thenReturn("resource-folder");
    appResponse = AppResponse.create(app);

    AppManagerController controller = new AppManagerController(appManagerService, dataService);
    mockMvc =
        MockMvcBuilders.standaloneSetup(controller)
            .setMessageConverters(new GsonHttpMessageConverter(new Gson()))
            .build();
  }

  @Test
  void testInit() throws Exception {
    mockMvc
        .perform(get(AppManagerController.URI))
        .andExpect(status().is(200))
        .andExpect(view().name("view-app-manager"));
  }

  @Test
  void testGetApps() throws Exception {
    when(appManagerService.getApps()).thenReturn(newArrayList(appResponse));
    mockMvc
        .perform(get(AppManagerController.URI + "/apps"))
        .andExpect(status().is(200))
        .andExpect(
            content()
                .string(
                    "[{\"id\":\"id\",\"name\":\"app1\",\"label\":\"label\",\"description\":\"description\",\"isActive\":true,\"includeMenuAndFooter\":true,\"templateContent\":\"\\u003ch1\\u003eTest\\u003c/h1\\u003e\",\"version\":\"v1.0.0\",\"resourceFolder\":\"resource-folder\",\"appConfig\":\"{\\u0027config\\u0027: \\u0027test\\u0027}\"}]"));
  }

  @Test
  void testActivateApp() throws Exception {
    App app = mock(App.class);
    when(dataService.findOneById(AppMetadata.APP, "id", App.class)).thenReturn(app);
    mockMvc.perform(post(AppManagerController.URI + "/activate/id")).andExpect(status().is(200));
    verify(app).setActive(true);
    verify(dataService).update(AppMetadata.APP, app);
  }

  @Test
  void testDeactivateApp() throws Exception {
    App app = mock(App.class);
    when(dataService.findOneById(AppMetadata.APP, "id", App.class)).thenReturn(app);
    mockMvc.perform(post(AppManagerController.URI + "/deactivate/id")).andExpect(status().is(200));
    verify(app).setActive(false);
    verify(dataService).update(AppMetadata.APP, app);
  }

  @Test
  void testDeleteApp() throws Exception {
    mockMvc.perform(delete(AppManagerController.URI + "/delete/id")).andExpect(status().is(200));
    verify(dataService).deleteById(AppMetadata.APP, "id");
  }

  @Test
  void testUploadApp() throws Exception {
    AppConfig appConfig = mock(AppConfig.class);
    when(appConfig.getName()).thenReturn("");

    when(appManagerService.uploadApp(any(InputStream.class), eq(""), eq("file")))
        .thenReturn("temp_dir");
    when(appManagerService.extractFileContent("temp_dir", ZIP_CONFIG_FILE))
        .thenReturn("config: 'test_config'");
    when(appManagerService.checkAndObtainConfig("temp_dir", "config: 'test_config'"))
        .thenReturn(appConfig);
    String testFile = AppManagerControllerTest.class.getResource("/test-app.zip").getFile();

    mockMvc
        .perform(multipart(AppManagerController.URI + "/upload").file("file", testFile.getBytes()))
        .andExpect(status().is(200));
  }

  @Test
  void testUpdateApp() throws Exception {
    App app = mock(App.class);
    when(app.getName()).thenReturn("app1");

    AppConfig appConfig = mock(AppConfig.class);
    when(appConfig.getName()).thenReturn("app1");

    when(dataService.findOneById(AppMetadata.APP, "id", App.class)).thenReturn(app);
    // originalFileName is not mockable, so therefor ""
    when(appManagerService.uploadApp(any(InputStream.class), eq(""), eq("app1")))
        .thenReturn("temp_dir");
    when(appManagerService.extractFileContent("temp_dir", ZIP_CONFIG_FILE))
        .thenReturn("config: 'test_config'");
    when(appManagerService.updateApp("id", "temp_dir", "config: 'test_config'"))
        .thenReturn(appConfig);

    String testFile = AppManagerControllerTest.class.getResource("/test-app.zip").getFile();

    mockMvc
        .perform(
            multipart(AppManagerController.URI + "/update/id").file("file", testFile.getBytes()))
        .andExpect(status().is(200));
  }
}
