package org.molgenis.app.manager.service;

import static com.google.common.collect.Lists.newArrayList;
import static java.io.File.separator;
import static org.apache.commons.codec.CharEncoding.UTF_8;
import static org.mockito.Mockito.*;
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
import org.molgenis.app.manager.exception.*;
import org.molgenis.app.manager.meta.App;
import org.molgenis.app.manager.meta.AppFactory;
import org.molgenis.app.manager.meta.AppMetadata;
import org.molgenis.app.manager.model.AppConfig;
import org.molgenis.app.manager.model.AppResponse;
import org.molgenis.app.manager.service.impl.AppManagerServiceImpl;
import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.data.file.FileStore;
import org.molgenis.data.plugin.model.Plugin;
import org.molgenis.data.plugin.model.PluginFactory;
import org.molgenis.data.plugin.model.PluginMetadata;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.i18n.MessageSourceHolder;
import org.molgenis.i18n.format.MessageFormatFactory;
import org.molgenis.i18n.test.exception.TestAllPropertiesMessageSource;
import org.testng.annotations.*;

public class AppManagerServiceTest {
  private AppManagerService appManagerService;

  @Mock private AppFactory appFactory;

  @Mock private DataService dataService;

  @Mock private FileStore fileStore;

  @Mock private PluginFactory pluginFactory;

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
    when(fileStore.getFile("folder")).thenReturn(appDir);

    Gson gson = new Gson();
    appManagerService =
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
    List<AppResponse> actual = appManagerService.getApps();
    List<AppResponse> expected = newArrayList(appResponse);

    assertEquals(actual, expected);
  }

  @Test
  public void testGetAppByUri() {
    Query<App> query = QueryImpl.EQ(AppMetadata.NAME, "test");
    when(dataService.findOne(AppMetadata.APP, query, App.class)).thenReturn(app);
    AppResponse actual = appManagerService.getAppByName("test");
    AppResponse expected = AppResponse.create(app);

    assertEquals(actual, expected);
  }

  @Test
  public void testActivateApp() {
    when(dataService.findOneById(AppMetadata.APP, "test", App.class)).thenReturn(app);
    app.setActive(true);

    Plugin plugin = mock(Plugin.class);
    when(pluginFactory.create(APP_PREFIX + "app1")).thenReturn(plugin);
    plugin.setLabel("label");
    plugin.setDescription("description");

    appManagerService.activateApp(app);

    verify(dataService).add("sys_Plugin", plugin);
  }

  @Test
  public void testDeactivateApp() {
    when(dataService.findOneById(AppMetadata.APP, "test", App.class)).thenReturn(app);
    app.setActive(false);

    appManagerService.deactivateApp(app);
    verify(dataService).deleteById(PluginMetadata.PLUGIN, APP_PREFIX + "app1");
  }

  @Test
  public void testDeleteApp() {
    when(dataService.findOneById(AppMetadata.APP, "test", App.class)).thenReturn(app);

    appManagerService.deleteApp("test");

    verify(dataService).deleteById(PluginMetadata.PLUGIN, APP_PREFIX + "app1");
  }

  @Test
  public void testAppUriDoesNotExist() {
    Query<App> query = QueryImpl.EQ(AppMetadata.NAME, "test");
    when(dataService.findOne(AppMetadata.APP, query, App.class)).thenReturn(null);
    try {
      appManagerService.getAppByName("test");
      fail();
    } catch (AppForURIDoesNotExistException actual) {
      assertEquals(actual.getUri(), "test");
    }
  }

  @Test
  public void testAppIdDoesNotExist() {
    when(dataService.findOneById(AppMetadata.APP, "test", App.class)).thenReturn(null);
    try {
      appManagerService.deleteApp("test");
      fail();
    } catch (AppForIDDoesNotExistException actual) {
      assertEquals(actual.getId(), "test");
    }
  }

  @Test
  public void testUploadApp() throws IOException {
    InputStream zipData = AppManagerServiceTest.class.getResourceAsStream("/valid-app.zip");
    String fileName = "valid-app.zip";

    String tmpDirName = "apps_tmp" + File.separator + "extracted_valid-app.zip";
    doReturn(tempDir).when(fileStore).getFile(tmpDirName);
    doReturn(indexFile).when(fileStore).getFile(tmpDirName + File.separator + "index.html");
    when(indexFile.exists()).thenReturn(true);
    doReturn(configFile).when(fileStore).getFile(tmpDirName + File.separator + "config.json");
    when(configFile.exists()).thenReturn(true);

    assertEquals(appManagerService.uploadApp(zipData, fileName, "app"), tmpDirName);

    verify(fileStore).createDirectory(tmpDirName);
  }

  @Test(expectedExceptions = InvalidAppArchiveException.class)
  public void testUploadAppInvalidZip() throws IOException {
    InputStream zipData = AppManagerServiceTest.class.getResourceAsStream("/flip.zip");
    String fileName = "flip.zip";

    String tmpDirName = "apps_tmp" + File.separator + "extracted_flip.zip";
    doReturn(tempDir).when(fileStore).getFile(tmpDirName);

    appManagerService.uploadApp(zipData, fileName, "app");
  }

  @Test(
      expectedExceptions = AppArchiveMissingFilesException.class,
      expectedExceptionsMessageRegExp = "missingFromArchive:\\[index.html\\]")
  public void testUploadAppMissingRequiredIndexFile() throws IOException {
    InputStream zipData = AppManagerServiceTest.class.getResourceAsStream("/valid-app.zip");
    String fileName = "app.zip";

    String tmpDirName = "apps_tmp" + File.separator + "extracted_app.zip";
    doReturn(tempDir).when(fileStore).getFile(tmpDirName);
    doReturn(indexFile).when(fileStore).getFile(tmpDirName + File.separator + "index.html");
    doReturn(configFile).when(fileStore).getFile(tmpDirName + File.separator + "config.json");
    when(configFile.exists()).thenReturn(true);

    assertEquals(appManagerService.uploadApp(zipData, fileName, "app"), tmpDirName);

    verify(fileStore).createDirectory(tmpDirName);
  }

  @Test(
      expectedExceptions = AppArchiveMissingFilesException.class,
      expectedExceptionsMessageRegExp = "missingFromArchive:\\[config.json\\]")
  public void testUploadAppMissingRequiredConfigFile() throws IOException {
    InputStream zipData = AppManagerServiceTest.class.getResourceAsStream("/valid-app.zip");
    String fileName = "app.zip";

    String tmpDirName = "apps_tmp" + File.separator + "extracted_app.zip";
    doReturn(tempDir).when(fileStore).getFile(tmpDirName);
    doReturn(indexFile).when(fileStore).getFile(tmpDirName + File.separator + "index.html");
    when(indexFile.exists()).thenReturn(true);
    doReturn(configFile).when(fileStore).getFile(tmpDirName + File.separator + "config.json");

    assertEquals(appManagerService.uploadApp(zipData, fileName, "app"), tmpDirName);

    verify(fileStore).createDirectory(tmpDirName);
  }

  @Test
  public void testCheckAndObtainConfig() throws IOException {
    String tempDir = "temp";
    String appUri = "example2";
    InputStream configFile = AppManagerServiceTest.class.getResource("/config.json").openStream();
    String configContent = IOUtils.toString(configFile, UTF_8);
    File file = mock(File.class);
    when(fileStore.getFile(APPS_DIR + separator + appUri)).thenReturn(file);
    when(fileStore.getFile(APPS_DIR + separator + appUri).exists()).thenReturn(false);

    appManagerService.checkAndObtainConfig(tempDir, configContent);

    verify(fileStore).move(tempDir, APPS_DIR + separator + appUri);
  }

  @Test(expectedExceptions = InvalidAppConfigException.class)
  public void testCheckAndObtainConfigInvalidJsonConfigFile() throws IOException {
    String appUri = "";
    File appDir = mock(File.class);
    when(fileStore.getFile(APPS_DIR + separator + appUri)).thenReturn(appDir);
    when(fileStore.getFile(APPS_DIR + separator + appUri).exists()).thenReturn(false);
    appManagerService.checkAndObtainConfig("tempDir", "");
  }

  @Test(
      expectedExceptions = AppConfigMissingParametersException.class,
      expectedExceptionsMessageRegExp =
          "missingConfigParameters:\\[label, description, includeMenuAndFooter, name, version\\]")
  public void testCheckAndObtainConfigMissingRequiredConfigParameters() throws IOException {
    InputStream is = AppManagerServiceTest.class.getResourceAsStream("/config-missing-keys.json");
    appManagerService.checkAndObtainConfig("tempDir", IOUtils.toString(is, UTF_8));
  }

  @Test(
      expectedExceptions = AppAlreadyExistsException.class,
      expectedExceptionsMessageRegExp = "example2")
  public void testCheckAndObtainConfigAppAlreadyExists() throws IOException {
    InputStream is = AppManagerServiceTest.class.getResourceAsStream("/config.json");
    File appDir = mock(File.class);
    when(fileStore.getFile(APPS_DIR + separator + "example2")).thenReturn(appDir);
    when(fileStore.getFile(APPS_DIR + separator + "example2").exists()).thenReturn(true);
    appManagerService.checkAndObtainConfig(
        APPS_DIR + separator + "tempDir", IOUtils.toString(is, UTF_8));
  }

  @Test
  public void testExtractFileContent() throws URISyntaxException {
    URL resourceUrl = Resources.getResource(AppControllerTest.class, "/index.html");
    File testIndexHtml = new File(new URI(resourceUrl.toString()).getPath());
    when(fileStore.getFile("testDir" + separator + "test")).thenReturn(testIndexHtml);
    appManagerService.extractFileContent("testDir", "test");
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

    appManagerService.configureApp(appConfig, "<h1>Test</h1>");

    verify(dataService).add(AppMetadata.APP, app);
  }
}
