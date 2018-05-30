package org.molgenis.app.manager.service;

import com.google.gson.Gson;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.mockito.Mock;
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
import org.molgenis.data.support.QueryImpl;
import org.molgenis.i18n.MessageSourceHolder;
import org.molgenis.i18n.format.MessageFormatFactory;
import org.molgenis.i18n.test.exception.TestAllPropertiesMessageSource;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.io.File.separator;
import static org.apache.commons.codec.CharEncoding.UTF_8;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.molgenis.app.manager.service.impl.AppManagerServiceImpl.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

public class AppManagerServiceTest
{
	private static final String APP_META_NAME = "sys_App";
	private static final String APP_META_URI = "uri";
	private static final String PLUGIN_META_NAME = "sys_Plugin";

	private AppManagerService appManagerService;

	@Mock
	private AppFactory appFactory;

	@Mock
	private DataService dataService;

	@Mock
	private FileStore fileStore;

	@Mock
	private PluginFactory pluginFactory;

	private Gson gson;

	private App app;

	@BeforeClass
	public void beforeClass()
	{
		TestAllPropertiesMessageSource messageSource = new TestAllPropertiesMessageSource(new MessageFormatFactory());
		messageSource.addMolgenisNamespaces("app-manager");
		MessageSourceHolder.setMessageSource(messageSource);
	}

	@AfterClass
	public void afterClass()
	{
		MessageSourceHolder.setMessageSource(null);
	}

	@BeforeMethod
	public void beforeMethod()
	{
		initMocks(this);

		app = mock(App.class);
		when(app.getId()).thenReturn("id");
		when(app.getUri()).thenReturn("uri");
		when(app.getLabel()).thenReturn("label");
		when(app.getDescription()).thenReturn("description");
		when(app.isActive()).thenReturn(true);
		when(app.getAppVersion()).thenReturn("v1.0.0");
		when(app.includeMenuAndFooter()).thenReturn(true);
		when(app.getTemplateContent()).thenReturn("<h1>Test</h1>");
		when(app.getAppConfig()).thenReturn("{'config': 'test'}");
		when(app.getResourceFolder()).thenReturn("folder");

		gson = new Gson();
		appManagerService = new AppManagerServiceImpl(appFactory, dataService, fileStore, gson, pluginFactory);
	}

	@AfterClass(alwaysRun = true)
	public void cleanup() throws IOException
	{
		FileUtils.deleteDirectory(Paths.get("dir").toFile());
		FileUtils.deleteDirectory(Paths.get("null").toFile());
	}

	@Test
	public void testGetApps()
	{
		AppResponse appResponse = AppResponse.create(app);

		when(dataService.findAll(APP_META_NAME, App.class)).thenReturn(newArrayList(app).stream());
		List<AppResponse> actual = appManagerService.getApps();
		List<AppResponse> expected = newArrayList(appResponse);

		assertEquals(actual, expected);
	}

	@Test
	public void testGetAppByUri()
	{
		Query<App> query = QueryImpl.EQ(APP_META_URI, "test");
		when(dataService.findOne(APP_META_NAME, query, App.class)).thenReturn(app);
		AppResponse actual = appManagerService.getAppByUri("test");
		AppResponse expected = AppResponse.create(app);

		assertEquals(actual, expected);
	}

	@Test
	public void testActivateApp()
	{
		when(dataService.findOneById(APP_META_NAME, "test", App.class)).thenReturn(app);
		app.setActive(true);

		Plugin plugin = mock(Plugin.class);
		when(pluginFactory.create("app/uri/")).thenReturn(plugin);
		plugin.setLabel("label");
		plugin.setDescription("description");

		appManagerService.activateApp("test");
		verify(dataService).update(APP_META_NAME, app);
		verify(dataService).add("sys_Plugin", plugin);
	}

	@Test
	public void testDeactivateApp()
	{
		when(dataService.findOneById(APP_META_NAME, "test", App.class)).thenReturn(app);
		app.setActive(false);

		appManagerService.deactivateApp("test");
		verify(dataService).update(APP_META_NAME, app);
		verify(dataService).deleteById(PLUGIN_META_NAME, "app/uri/");
	}

	@Test
	public void testDeleteApp() throws IOException
	{
		when(dataService.findOneById(APP_META_NAME, "test", App.class)).thenReturn(app);

		appManagerService.deleteApp("test");
		verify(dataService).deleteById(APP_META_NAME, "test");
	}

	@Test
	public void testAppUriDoesNotExist()
	{
		Query<App> query = QueryImpl.EQ(APP_META_URI, "test");
		when(dataService.findOne(APP_META_NAME, query, App.class)).thenReturn(null);
		try
		{
			appManagerService.getAppByUri("test");
			fail();
		}
		catch (AppForURIDoesNotExistException actual)
		{
			assertEquals(actual.getUri(), "test");
		}
	}

	@Test
	public void testAppIdDoesNotExist()
	{
		when(dataService.findOneById(APP_META_NAME, "test", App.class)).thenReturn(null);
		try
		{
			appManagerService.activateApp("test");
			fail();
		}
		catch (AppForIDDoesNotExistException actual)
		{
			assertEquals(actual.getId(), "test");
		}
	}

	@Test
	public void testUploadApp() throws URISyntaxException, IOException, ZipException
	{
		URL url = AppManagerServiceTest.class.getResource("/valid-app.zip");
		File zipFile = new File(new URI(url.toString()).getPath());
		FileInputStream zipData = new FileInputStream(zipFile);
		String fileName = zipFile.getName();
		when(fileStore.store(zipData, APPS_TMP_DIR + separator + ZIP_FILE_PREFIX + fileName)).thenReturn(zipFile);
		String storageDir = "dir";
		when(fileStore.getStorageDir()).thenReturn(storageDir);
		appManagerService.uploadApp(zipData, fileName, "app");
	}

	@Test
	public void testUploadAppEmptyRuntimeOptions() throws URISyntaxException, IOException, ZipException
	{
		URL url = AppManagerServiceTest.class.getResource("/valid-no-runtime-app.zip");
		File zipFile = new File(new URI(url.toString()).getPath());
		FileInputStream zipData = new FileInputStream(zipFile);
		String fileName = zipFile.getName();

		when(fileStore.store(zipData, APPS_TMP_DIR + separator + ZIP_FILE_PREFIX + fileName)).thenReturn(zipFile);
		String storageDir = "dir";
		when(fileStore.getStorageDir()).thenReturn(storageDir);

		appManagerService.uploadApp(zipData, fileName, "app");
	}

	@Test(expectedExceptions = InvalidAppArchiveException.class)
	public void testUploadAppInvalidZip() throws URISyntaxException, IOException, ZipException
	{
		URL url = AppManagerServiceTest.class.getResource("/invalid-app.txt");
		File textFile = new File(new URI(url.toString()).getPath());
		FileInputStream zipData = new FileInputStream(textFile);
		String fileName = textFile.getName();

		String archiveName = APPS_TMP_DIR + separator + ZIP_FILE_PREFIX + fileName;
		when(fileStore.store(zipData, archiveName)).thenReturn(textFile);

		appManagerService.uploadApp(zipData, fileName, "app");

	}

	@Test(expectedExceptions = AppArchiveMissingFilesException.class, expectedExceptionsMessageRegExp = "missingFromArchive:\\[index.html\\]")
	public void testUploadAppMissingRequiredIndexFile() throws URISyntaxException, IOException, ZipException
	{
		URL url = AppManagerServiceTest.class.getResource("/missing-index.zip");
		File textFile = new File(new URI(url.toString()).getPath());
		FileInputStream zipData = new FileInputStream(textFile);
		String fileName = textFile.getName();

		String archiveName = APPS_TMP_DIR + separator + ZIP_FILE_PREFIX + fileName;
		when(fileStore.store(zipData, archiveName)).thenReturn(textFile);

		appManagerService.uploadApp(zipData, fileName, "app");
	}

	@Test(expectedExceptions = AppArchiveMissingFilesException.class, expectedExceptionsMessageRegExp = "missingFromArchive:\\[config.json\\]")
	public void testUploadAppMissingRequiredConfigFile() throws URISyntaxException, IOException, ZipException
	{
		URL url = AppManagerServiceTest.class.getResource("/missing-config.zip");
		File textFile = new File(new URI(url.toString()).getPath());
		FileInputStream zipData = new FileInputStream(textFile);
		String fileName = textFile.getName();

		String archiveName = APPS_TMP_DIR + separator + ZIP_FILE_PREFIX + fileName;
		when(fileStore.store(zipData, archiveName)).thenReturn(textFile);

		appManagerService.uploadApp(zipData, fileName, "app");
	}

	@Test
	public void testCheckAndObtainConfig() throws IOException
	{
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
	public void testCheckAndObtainConfigInvalidJsonConfigFile() throws IOException
	{
		String appUri = "";
		File appDir = mock(File.class);
		when(fileStore.getFile(APPS_DIR + separator + appUri)).thenReturn(appDir);
		when(fileStore.getFile(APPS_DIR + separator + appUri).exists()).thenReturn(false);
		appManagerService.checkAndObtainConfig("tempDir", "");
	}

	@Test(expectedExceptions = AppConfigMissingParametersException.class, expectedExceptionsMessageRegExp = "missingConfigParameters:\\[label, description, includeMenuAndFooter, uri, version\\]")
	public void testCheckAndObtainConfigMissingRequiredConfigParameters() throws IOException
	{
		URL url = AppManagerServiceTest.class.getResource("/config-missing-keys.json");
		appManagerService.checkAndObtainConfig("tempDir", IOUtils.toString(url, UTF_8));
	}

	@Test(expectedExceptions = AppAlreadyExistsException.class, expectedExceptionsMessageRegExp = "example2")
	public void testCheckAndObtainConfigAppAlreadyExists() throws IOException
	{
		URL url = AppManagerServiceTest.class.getResource("/config.json");
		File appDir = mock(File.class);
		when(fileStore.getFile(APPS_DIR + separator + "example2")).thenReturn(appDir);
		when(fileStore.getFile(APPS_DIR + separator + "example2").exists()).thenReturn(true);
		appManagerService.checkAndObtainConfig(APPS_DIR + separator + "tempDir", IOUtils.toString(url, UTF_8));
	}

	@Test
	public void testConfigureApp()
	{
		when(appFactory.create()).thenReturn(app);

		AppConfig appConfig = mock(AppConfig.class);
		when(appConfig.getLabel()).thenReturn("test-app");
		when(appConfig.getDescription()).thenReturn("Test app description");
		when(appConfig.getIncludeMenuAndFooter()).thenReturn(true);
		when(appConfig.getVersion()).thenReturn("1.0");
		when(appConfig.getUri()).thenReturn("test-app-uri");
		when(appConfig.getApiDependency()).thenReturn("v2.0");

		appManagerService.configureApp(appConfig, "<h1>Test</h1>");

		verify(dataService).add(AppMetadata.APP, app);
	}

}
