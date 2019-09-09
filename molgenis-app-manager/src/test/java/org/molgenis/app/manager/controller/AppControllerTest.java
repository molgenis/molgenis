package org.molgenis.app.manager.controller;

import static java.util.Locale.ENGLISH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.plugin.model.PluginPermission.VIEW_PLUGIN;
import static org.molgenis.web.bootstrap.PluginPopulator.APP_PREFIX;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.google.common.io.Resources;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.molgenis.app.manager.exception.AppIsInactiveException;
import org.molgenis.app.manager.meta.App;
import org.molgenis.app.manager.model.AppResponse;
import org.molgenis.app.manager.service.AppManagerService;
import org.molgenis.data.DataService;
import org.molgenis.data.file.FileStore;
import org.molgenis.data.plugin.model.PluginIdentity;
import org.molgenis.data.plugin.model.PluginPermissionDeniedException;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.molgenis.settings.AppSettings;
import org.molgenis.test.AbstractMockitoSpringContextTests;
import org.molgenis.util.i18n.MessageSourceHolder;
import org.molgenis.util.i18n.TestAllPropertiesMessageSource;
import org.molgenis.util.i18n.format.MessageFormatFactory;
import org.molgenis.web.menu.MenuReaderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.util.NestedServletException;

@WebAppConfiguration
@ContextConfiguration(classes = AppControllerTest.Config.class)
public class AppControllerTest extends AbstractMockitoSpringContextTests {
  private MockMvc mockMvc;

  @Autowired private AppController appController;

  @Autowired private AppManagerService appManagerService;

  @Autowired private AppSettings appSettings;

  @Autowired private LocaleResolver localeResolver;

  @Autowired private MenuReaderService menuReaderService;

  @Autowired private UserPermissionEvaluator userPermissionEvaluator;

  private AppResponse appResponse;

  @Autowired private FileStore fileStore;

  @BeforeAll
  static void beforeClass() {
    TestAllPropertiesMessageSource messageSource =
        new TestAllPropertiesMessageSource(new MessageFormatFactory());
    messageSource.addMolgenisNamespaces("app-manager", "data-plugin");
    MessageSourceHolder.setMessageSource(messageSource);
  }

  @AfterAll
  static void afterClass() {
    MessageSourceHolder.setMessageSource(null);
  }

  @BeforeEach
  void beforeMethod() throws URISyntaxException {
    String appName = "app1";
    when(menuReaderService.findMenuItemPath(APP_PREFIX + appName)).thenReturn("/test/path");
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

    when(fileStore.getFileUnchecked("fake-app/js/test.js")).thenReturn(testJs);

    appResponse = AppResponse.create(app);
    when(appManagerService.getAppByName(appName)).thenReturn(appResponse);

    mockMvc =
        MockMvcBuilders.standaloneSetup(appController).setLocaleResolver(localeResolver).build();
  }

  @Test
  void testServeApp() throws Exception {
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
  void testServeAppNoPermissions() throws Throwable {
    PluginIdentity pluginIdentity = new PluginIdentity(APP_PREFIX + "app1");
    when(userPermissionEvaluator.hasPermission(pluginIdentity, VIEW_PLUGIN)).thenReturn(false);
    try {
      mockMvc.perform(get(AppController.URI + "/app1/"));
    } catch (NestedServletException e) {
      Exception exception =
          assertThrows(
              PluginPermissionDeniedException.class,
              () -> {
                throw e.getCause();
              });
      assertThat(exception.getMessage())
          .containsPattern("pluginPermission: VIEW_PLUGIN, pluginId:app1");
    }
  }

  @Test
  void testServeAppRedirectToApp() throws Exception {
    PluginIdentity pluginIdentity = new PluginIdentity(APP_PREFIX + "app1");
    when(userPermissionEvaluator.hasPermission(pluginIdentity, VIEW_PLUGIN)).thenReturn(true);
    mockMvc.perform(get(AppController.URI + "/app1")).andExpect(status().is3xxRedirection());
  }

  @Test
  void testServeAppInactiveApp() throws Throwable {
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

    try {
      mockMvc.perform(get(AppController.URI + "/app1/"));
    } catch (NestedServletException e) {
      Exception exception =
          assertThrows(
              AppIsInactiveException.class,
              () -> {
                throw e.getCause();
              });
      assertThat(exception.getMessage()).containsPattern("appName:app1");
    }
  }

  @Test
  void testServeResource() throws Exception {
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
  static class Config {
    @Bean
    DataService dataService() {
      return mock(DataService.class);
    }

    @Bean
    FileStore fileStore() {
      return mock(FileStore.class);
    }

    @Bean
    AppController appController() {
      return new AppController(
          appManagerService(),
          userPermissionEvaluator(),
          appSettings(),
          menuReaderService(),
          fileStore());
    }

    @Bean
    AppManagerService appManagerService() {
      return mock(AppManagerService.class);
    }

    @Bean
    AppSettings appSettings() {
      return mock(AppSettings.class);
    }

    @Bean
    LocaleResolver localeResolver() {
      return mock(LocaleResolver.class);
    }

    @Bean
    MenuReaderService menuReaderService() {
      return mock(MenuReaderService.class);
    }

    @Bean
    UserPermissionEvaluator userPermissionEvaluator() {
      return mock(UserPermissionEvaluator.class);
    }
  }
}
