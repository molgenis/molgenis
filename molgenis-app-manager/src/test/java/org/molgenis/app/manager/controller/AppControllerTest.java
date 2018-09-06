package org.molgenis.app.manager.controller;

import static java.util.Locale.ENGLISH;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.molgenis.data.plugin.model.PluginPermission.VIEW_PLUGIN;
import static org.molgenis.web.bootstrap.PluginPopulator.APP_PREFIX;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.google.common.io.Resources;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import org.molgenis.app.manager.meta.App;
import org.molgenis.app.manager.model.AppResponse;
import org.molgenis.app.manager.service.AppManagerService;
import org.molgenis.core.ui.menu.Menu;
import org.molgenis.core.ui.menu.MenuReaderService;
import org.molgenis.data.DataService;
import org.molgenis.data.file.FileStore;
import org.molgenis.data.plugin.model.PluginIdentity;
import org.molgenis.i18n.MessageSourceHolder;
import org.molgenis.i18n.format.MessageFormatFactory;
import org.molgenis.i18n.test.exception.TestAllPropertiesMessageSource;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.molgenis.settings.AppSettings;
import org.molgenis.web.ErrorMessageResponse;
import org.molgenis.web.exception.FallbackExceptionHandler;
import org.molgenis.web.exception.GlobalControllerExceptionHandler;
import org.molgenis.web.exception.SpringExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@WebAppConfiguration
@ContextConfiguration(classes = AppControllerTest.Config.class)
public class AppControllerTest extends AbstractTestNGSpringContextTests {
  private MockMvc mockMvc;

  @Autowired private AppController appController;

  @Autowired private FallbackExceptionHandler fallbackExceptionHandler;

  @Autowired private SpringExceptionHandler springExceptionHandler;

  @Autowired private GlobalControllerExceptionHandler globalControllerExceptionHandler;

  @Autowired private AppManagerService appManagerService;

  @Autowired private AppSettings appSettings;

  @Autowired private LocaleResolver localeResolver;

  @Autowired private MenuReaderService menuReaderService;

  @Autowired private UserPermissionEvaluator userPermissionEvaluator;

  private AppResponse appResponse;

  @Autowired private FileStore fileStore;

  @BeforeClass
  public void beforeClass() {
    TestAllPropertiesMessageSource messageSource =
        new TestAllPropertiesMessageSource(new MessageFormatFactory());
    messageSource.addMolgenisNamespaces("app-manager", "data-plugin");
    MessageSourceHolder.setMessageSource(messageSource);
  }

  @AfterClass
  public void afterClass() {
    MessageSourceHolder.setMessageSource(null);
  }

  @BeforeMethod
  public void beforeMethod() throws URISyntaxException {
    initMocks(this);

    Menu menu = mock(Menu.class);
    String appName = "app1";
    when(menu.findMenuItemPath(APP_PREFIX + appName)).thenReturn("/test/path");
    when(menuReaderService.getMenu()).thenReturn(menu);
    when(appSettings.getLanguageCode()).thenReturn("en");
    when(localeResolver.resolveLocale(any())).thenReturn(ENGLISH);

    App app = mock(App.class);
    when(app.getId()).thenReturn("id");
    when(app.getName()).thenReturn(appName);
    when(app.getLabel()).thenReturn("label");
    when(app.getDescription()).thenReturn("description");
    when(app.isActive()).thenReturn(true);
    when(app.getAppVersion()).thenReturn("v1.0.0");
    when(app.includeMenuAndFooter()).thenReturn(true);
    when(app.getTemplateContent()).thenReturn("<h1>Test</h1>");
    when(app.getAppConfig()).thenReturn("{'config': 'test'}");
    when(app.getResourceFolder()).thenReturn("fake-app");
    URL resourceUrl = Resources.getResource(AppControllerTest.class, "/index.html");
    File testJs = new File(new URI(resourceUrl.toString()).getPath());

    when(fileStore.getFile("fake-app/js/test.js")).thenReturn(testJs);

    appResponse = AppResponse.create(app);
    when(appManagerService.getAppByName(appName)).thenReturn(appResponse);

    mockMvc =
        MockMvcBuilders.standaloneSetup(appController)
            .setControllerAdvice(
                globalControllerExceptionHandler, fallbackExceptionHandler, springExceptionHandler)
            .setLocaleResolver(localeResolver)
            .build();
  }

  @Test
  public void testServeApp() throws Exception {
    PluginIdentity pluginIdentity = new PluginIdentity(APP_PREFIX + "app1");
    when(userPermissionEvaluator.hasPermission(pluginIdentity, VIEW_PLUGIN)).thenReturn(true);
    mockMvc
        .perform(get(AppController.URI + "/app1/"))
        .andExpect(status().isOk())
        .andExpect(model().attribute("app", appResponse))
        .andExpect(model().attribute("baseUrl", "/test/path"))
        .andExpect(view().name("view-app"));
  }

  @Test
  public void testServeAppNoPermissions() throws Exception {
    PluginIdentity pluginIdentity = new PluginIdentity(APP_PREFIX + "app1");
    when(userPermissionEvaluator.hasPermission(pluginIdentity, VIEW_PLUGIN)).thenReturn(false);
    mockMvc.perform(get(AppController.URI + "/app1/")).andExpect(status().isUnauthorized());
  }

  @Test
  public void testServeAppRedirectToApp() throws Exception {
    PluginIdentity pluginIdentity = new PluginIdentity(APP_PREFIX + "app1");
    when(userPermissionEvaluator.hasPermission(pluginIdentity, VIEW_PLUGIN)).thenReturn(true);
    mockMvc.perform(get(AppController.URI + "/app1")).andExpect(status().is3xxRedirection());
  }

  @Test
  public void testServeAppInactiveApp() throws Exception {
    PluginIdentity pluginIdentity = new PluginIdentity(APP_PREFIX + "app1");
    when(userPermissionEvaluator.hasPermission(pluginIdentity, VIEW_PLUGIN)).thenReturn(true);

    App app = mock(App.class);
    when(app.getId()).thenReturn("id");
    when(app.getName()).thenReturn("app1");
    when(app.getLabel()).thenReturn("label");
    when(app.getDescription()).thenReturn("description");
    when(app.getAppVersion()).thenReturn("v1.0.0");
    when(app.includeMenuAndFooter()).thenReturn(true);
    when(app.getTemplateContent()).thenReturn("<h1>Test</h1>");
    when(app.getAppConfig()).thenReturn("{'config': 'test'}");
    when(app.getResourceFolder()).thenReturn("foo/bar");

    when(app.isActive()).thenReturn(false);

    AppResponse appResponse = AppResponse.create(app);
    when(appManagerService.getAppByName("app1")).thenReturn(appResponse);

    mockMvc
        .perform(get(AppController.URI + "/app1/"))
        .andExpect(status().is4xxClientError())
        .andExpect(
            model()
                .attribute(
                    "errorMessageResponse",
                    ErrorMessageResponse.create(
                        "Access denied for inactive app at location /app/app1", "AM07")))
        .andExpect(view().name("view-exception"));
  }

  @Test
  public void testServeResource() throws Exception {
    PluginIdentity pluginIdentity = new PluginIdentity("app/app1/");
    when(userPermissionEvaluator.hasPermission(pluginIdentity, VIEW_PLUGIN)).thenReturn(true);
    mockMvc
        .perform(get(AppController.URI + "/app1/js/test.js"))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse();
  }

  @Configuration
  @EnableWebMvc
  public static class Config {
    @Bean
    public DataService dataService() {
      return mock(DataService.class);
    }

    @Bean
    public FileStore fileStore() {
      return mock(FileStore.class);
    }

    @Bean
    public AppController appController() {
      return new AppController(
          appManagerService(),
          userPermissionEvaluator(),
          appSettings(),
          menuReaderService(),
          fileStore());
    }

    @Bean
    public GlobalControllerExceptionHandler globalControllerExceptionHandler() {
      return new GlobalControllerExceptionHandler();
    }

    @Bean
    public AppManagerService appManagerService() {
      return mock(AppManagerService.class);
    }

    @Bean
    public AppSettings appSettings() {
      return mock(AppSettings.class);
    }

    @Bean
    public LocaleResolver localeResolver() {
      return mock(LocaleResolver.class);
    }

    @Bean
    public MenuReaderService menuReaderService() {
      return mock(MenuReaderService.class);
    }

    @Bean
    public UserPermissionEvaluator userPermissionEvaluator() {
      return mock(UserPermissionEvaluator.class);
    }

    @Bean
    public FallbackExceptionHandler fallbackExceptionHandler() {
      return new FallbackExceptionHandler();
    }

    @Bean
    public SpringExceptionHandler springExceptionHandler() {
      return new SpringExceptionHandler();
    }
  }
}
