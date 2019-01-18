package org.molgenis.app.manager.service.impl;

import static com.google.common.collect.Lists.newArrayList;
import static java.io.File.separator;
import static org.apache.commons.codec.CharEncoding.UTF_8;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.molgenis.app.manager.service.impl.AppManagerServiceImpl.APPS_DIR;
import static org.molgenis.web.bootstrap.PluginPopulator.APP_PREFIX;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import com.google.common.io.Files;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.mockito.Mock;
import org.molgenis.app.manager.controller.AppControllerTest;
import org.molgenis.app.manager.exception.AppAlreadyExistsException;
import org.molgenis.app.manager.exception.AppArchiveMissingFilesException;
import org.molgenis.app.manager.exception.AppConfigMissingParametersException;
import org.molgenis.app.manager.exception.AppForIDDoesNotExistException;
import org.molgenis.app.manager.exception.AppForURIDoesNotExistException;
import org.molgenis.app.manager.exception.InvalidAppArchiveException;
import org.molgenis.app.manager.exception.InvalidAppConfigException;
import org.molgenis.app.manager.meta.App;
import org.molgenis.app.manager.meta.AppFactory;
import org.molgenis.app.manager.meta.AppMetadata;
import org.molgenis.app.manager.model.AppConfig;
import org.molgenis.app.manager.model.AppResponse;
import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.data.file.FileStore;
import org.molgenis.data.plugin.model.Plugin;
import org.molgenis.data.plugin.model.PluginFactory;
import org.molgenis.data.plugin.model.PluginMetadata;
import org.molgenis.i18n.MessageSourceHolder;
import org.molgenis.i18n.format.MessageFormatFactory;
import org.molgenis.i18n.test.exception.TestAllPropertiesMessageSource;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class AppManagerServiceImplTest {
  @Mock private AppFactory appFactory;
  @Mock private DataService dataService;
  @Mock private FileStore fileStore;
  @Mock private PluginFactory pluginFactory;
  private AppManagerServiceImpl appManagerServiceImpl;

  private App app;

  private File tempDir;

  @Mock private File indexFile;
  @Mock private File configFile;

  @BeforeClass
  public void beforeClass() {
    TestAllPropertiesMessageSource messageSource =
        new TestAllPropertiesMessageSource(new MessageFormatFactory());
    messageSource.addMolgenisNamespaces("app-manager");
    MessageSourceHolder.setMessageSource(messageSource);
  }

  @AfterClass
  public void afterClass() {
    MessageSourceHolder.setMessageSource(null);
  }

  @BeforeMethod
  public void beforeMethod() {
    initMocks(this);

    tempDir = Files.createTempDir();

    app = mock(App.class);
    when(app.getId()).thenReturn("id");
    when(app.getName()).thenReturn("app1");
    when(app.getLabel()).thenReturn("label");
    when(app.getDescription()).thenReturn("description");
    when(app.isActive()).thenReturn(true);
    when(app.getAppVersion()).thenReturn("v1.0.0");
    when(app.includeMenuAndFooter()).thenReturn(true);
    when(app.getTemplateContent()).thenReturn("<h1>Test</h1>");
    when(app.getAppConfig()).thenReturn("{'config': 'test'}");
    when(app.getResourceFolder()).thenReturn("folder");

    File appDir = mock(File.class);
    when(fileStore.getFileUnchecked("folder")).thenReturn(appDir);

    Gson gson = new Gson();
    appManagerServiceImpl =
        new AppManagerServiceImpl(appFactory, dataService, fileStore, gson, pluginFactory);
  }

  @AfterMethod
  public void afterMethod() throws IOException {
    FileUtils.deleteDirectory(tempDir);
  }

  @AfterClass(alwaysRun = true)
  public void cleanup() throws IOException {
    FileUtils.deleteDirectory(Paths.get("dir").toFile());
    FileUtils.deleteDirectory(Paths.get("null").toFile());
  }

  @Test
  public void testGetApps() {
    AppResponse appResponse = AppResponse.create(app);

    when(dataService.findAll(AppMetadata.APP, App.class)).thenReturn(newArrayList(app).stream());
    List<AppResponse> actual = appManagerServiceImpl.getApps();
    List<AppResponse> expected = newArrayList(appResponse);

    assertEquals(actual, expected);
  }

  @Test
  public void testGetAppByName() {
    String id = "id";
    String label = "label";
    String description = "description";
    String name = "name";
    String templateContent = "templateContent";
    String resourceFolder = "resourceFolder";
    String appVersion = "appVersion";
    App app = mock(App.class);
    when(app.getId()).thenReturn(id);
    when(app.getLabel()).thenReturn(label);
    when(app.getDescription()).thenReturn(description);
    when(app.getName()).thenReturn(name);
    when(app.getTemplateContent()).thenReturn(templateContent);
    when(app.getResourceFolder()).thenReturn(resourceFolder);
    when(app.getAppVersion()).thenReturn(appVersion);

    @SuppressWarnings("unchecked")
    Query<App> query = mock(Query.class, RETURNS_SELF);
    when(dataService.query("sys_App", App.class)).thenReturn(query);
    when(query.eq("name", name).findOne()).thenReturn(app);

    AppResponse expectedAppResponse = AppResponse.create(app);
    assertEquals(appManagerServiceImpl.getAppByName(name), expectedAppResponse);
  }

  @Test(expectedExceptions = AppForURIDoesNotExistException.class)
  public void testGetAppByNameUnknownAppName() {
    String name = "unknownName";
    @SuppressWarnings("unchecked")
    Query<App> query = mock(Query.class, RETURNS_SELF);
    when(dataService.query("sys_App", App.class)).thenReturn(query);
    when(query.eq("name", name).findOne()).thenReturn(null);
    appManagerServiceImpl.getAppByName(name);
  }

  @Test
  public void testActivateApp() {
    when(dataService.findOneById(AppMetadata.APP, "test", App.class)).thenReturn(app);
    app.setActive(true);

    Plugin plugin = mock(Plugin.class);
    when(pluginFactory.create(APP_PREFIX + "app1")).thenReturn(plugin);
    plugin.setLabel("label");
    plugin.setDescription("description");

    appManagerServiceImpl.activateApp(app);

    verify(dataService).add("sys_Plugin", plugin);
  }

  @Test
  public void testDeactivateApp() {
    when(dataService.findOneById(AppMetadata.APP, "test", App.class)).thenReturn(app);
    app.setActive(false);

    appManagerServiceImpl.deactivateApp(app);
    verify(dataService).deleteById(PluginMetadata.PLUGIN, APP_PREFIX + "app1");
  }

  @Test
  public void testDeleteApp() {
    when(dataService.findOneById(AppMetadata.APP, "test", App.class)).thenReturn(app);

    appManagerServiceImpl.deleteApp("test");

    verify(dataService).deleteById(PluginMetadata.PLUGIN, APP_PREFIX + "app1");
  }

  @Test
  public void testAppIdDoesNotExist() {
    when(dataService.findOneById(AppMetadata.APP, "test", App.class)).thenReturn(null);
    try {
      appManagerServiceImpl.deleteApp("test");
      fail();
    } catch (AppForIDDoesNotExistException actual) {
      assertEquals(actual.getId(), "test");
    }
  }

  @Test
  public void testUploadApp() throws IOException {
    InputStream zipData = AppManagerServiceImplTest.class.getResourceAsStream("/valid-app.zip");
    String fileName = "valid-app.zip";

    String tmpDirName = "apps_tmp" + File.separator + "extracted_valid-app.zip";
    doReturn(tempDir).when(fileStore).getFileUnchecked(tmpDirName);
    doReturn(indexFile)
        .when(fileStore)
        .getFileUnchecked(tmpDirName + File.separator + "index.html");
    when(indexFile.exists()).thenReturn(true);
    doReturn(configFile)
        .when(fileStore)
        .getFileUnchecked(tmpDirName + File.separator + "config.json");
    when(configFile.exists()).thenReturn(true);

    assertEquals(appManagerServiceImpl.uploadApp(zipData, fileName, "app"), tmpDirName);

    verify(fileStore).createDirectory(tmpDirName);
  }

  @Test(expectedExceptions = InvalidAppArchiveException.class)
  public void testUploadAppInvalidZip() throws IOException {
    InputStream zipData = AppManagerServiceImplTest.class.getResourceAsStream("/flip.zip");
    String fileName = "flip.zip";

    String tmpDirName = "apps_tmp" + File.separator + "extracted_flip.zip";
    doReturn(tempDir).when(fileStore).getFileUnchecked(tmpDirName);

    appManagerServiceImpl.uploadApp(zipData, fileName, "app");
  }

  @Test(
      expectedExceptions = AppArchiveMissingFilesException.class,
      expectedExceptionsMessageRegExp = "missingFromArchive:\\[index.html\\]")
  public void testUploadAppMissingRequiredIndexFile() throws IOException {
    InputStream zipData = AppManagerServiceImplTest.class.getResourceAsStream("/valid-app.zip");
    String fileName = "app.zip";

    String tmpDirName = "apps_tmp" + File.separator + "extracted_app.zip";
    doReturn(tempDir).when(fileStore).getFileUnchecked(tmpDirName);
    doReturn(indexFile)
        .when(fileStore)
        .getFileUnchecked(tmpDirName + File.separator + "index.html");
    doReturn(configFile)
        .when(fileStore)
        .getFileUnchecked(tmpDirName + File.separator + "config.json");
    when(configFile.exists()).thenReturn(true);

    assertEquals(appManagerServiceImpl.uploadApp(zipData, fileName, "app"), tmpDirName);

    verify(fileStore).createDirectory(tmpDirName);
  }

  @Test(
      expectedExceptions = AppArchiveMissingFilesException.class,
      expectedExceptionsMessageRegExp = "missingFromArchive:\\[config.json\\]")
  public void testUploadAppMissingRequiredConfigFile() throws IOException {
    InputStream zipData = AppManagerServiceImplTest.class.getResourceAsStream("/valid-app.zip");
    String fileName = "app.zip";

    String tmpDirName = "apps_tmp" + File.separator + "extracted_app.zip";
    doReturn(tempDir).when(fileStore).getFileUnchecked(tmpDirName);
    doReturn(indexFile)
        .when(fileStore)
        .getFileUnchecked(tmpDirName + File.separator + "index.html");
    when(indexFile.exists()).thenReturn(true);
    doReturn(configFile)
        .when(fileStore)
        .getFileUnchecked(tmpDirName + File.separator + "config.json");

    assertEquals(appManagerServiceImpl.uploadApp(zipData, fileName, "app"), tmpDirName);

    verify(fileStore).createDirectory(tmpDirName);
  }

  @Test
  public void testCheckAndObtainConfig() throws IOException {
    String tempDir = "temp";
    String appUri = "example2";
    InputStream configFile =
        AppManagerServiceImplTest.class.getResource("/config.json").openStream();
    String configContent = IOUtils.toString(configFile, UTF_8);
    File file = mock(File.class);
    when(fileStore.getFileUnchecked(APPS_DIR + separator + appUri)).thenReturn(file);
    when(fileStore.getFileUnchecked(APPS_DIR + separator + appUri).exists()).thenReturn(false);

    appManagerServiceImpl.checkAndObtainConfig(tempDir, configContent);

    verify(fileStore).move(tempDir, APPS_DIR + separator + appUri);
  }

  @Test(expectedExceptions = InvalidAppConfigException.class)
  public void testCheckAndObtainConfigInvalidJsonConfigFile() throws IOException {
    String appUri = "";
    File appDir = mock(File.class);
    when(fileStore.getFileUnchecked(APPS_DIR + separator + appUri)).thenReturn(appDir);
    when(fileStore.getFileUnchecked(APPS_DIR + separator + appUri).exists()).thenReturn(false);
    appManagerServiceImpl.checkAndObtainConfig("tempDir", "");
  }

  @Test(
      expectedExceptions = AppConfigMissingParametersException.class,
      expectedExceptionsMessageRegExp =
          "missingConfigParameters:\\[label, description, includeMenuAndFooter, name, version\\]")
  public void testCheckAndObtainConfigMissingRequiredConfigParameters() throws IOException {
    InputStream is =
        AppManagerServiceImplTest.class.getResourceAsStream("/config-missing-keys.json");
    appManagerServiceImpl.checkAndObtainConfig("tempDir", IOUtils.toString(is, UTF_8));
  }

  @Test(
      expectedExceptions = AppAlreadyExistsException.class,
      expectedExceptionsMessageRegExp = "example2")
  public void testCheckAndObtainConfigAppAlreadyExists() throws IOException {
    InputStream is = AppManagerServiceImplTest.class.getResourceAsStream("/config.json");
    File appDir = mock(File.class);
    when(fileStore.getFileUnchecked(APPS_DIR + separator + "example2")).thenReturn(appDir);
    when(fileStore.getFileUnchecked(APPS_DIR + separator + "example2").exists()).thenReturn(true);
    appManagerServiceImpl.checkAndObtainConfig(
        APPS_DIR + separator + "tempDir", IOUtils.toString(is, UTF_8));
  }

  @Test
  public void testExtractFileContent() throws URISyntaxException {
    URL resourceUrl = Resources.getResource(AppControllerTest.class, "/index.html");
    File testIndexHtml = new File(new URI(resourceUrl.toString()).getPath());
    when(fileStore.getFileUnchecked("testDir" + separator + "test")).thenReturn(testIndexHtml);
    appManagerServiceImpl.extractFileContent("testDir", "test");
  }

  @Test
  public void testConfigureApp() {
    when(appFactory.create()).thenReturn(app);

    AppConfig appConfig = mock(AppConfig.class);
    when(appConfig.getLabel()).thenReturn("test-app");
    when(appConfig.getDescription()).thenReturn("Test app description");
    when(appConfig.getIncludeMenuAndFooter()).thenReturn(true);
    when(appConfig.getVersion()).thenReturn("1.0");
    when(appConfig.getName()).thenReturn("app1");
    when(appConfig.getApiDependency()).thenReturn("v2.0");

    appManagerServiceImpl.configureApp(appConfig, "<h1>Test</h1>");

    verify(dataService).add(AppMetadata.APP, app);
  }
}
