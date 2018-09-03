package org.molgenis.app.manager.service.impl;

import static com.google.common.collect.Lists.newArrayList;
import static java.io.File.separator;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.io.FileUtils.deleteDirectory;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.molgenis.app.manager.exception.*;
import org.molgenis.app.manager.meta.App;
import org.molgenis.app.manager.meta.AppFactory;
import org.molgenis.app.manager.meta.AppMetadata;
import org.molgenis.app.manager.model.AppConfig;
import org.molgenis.app.manager.model.AppResponse;
import org.molgenis.app.manager.service.AppManagerService;
import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.data.file.FileStore;
import org.molgenis.data.plugin.model.Plugin;
import org.molgenis.data.plugin.model.PluginFactory;
import org.molgenis.data.plugin.model.PluginMetadata;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.util.file.UnzipException;
import org.molgenis.util.file.ZipFileUtil;
import org.molgenis.web.bootstrap.PluginPopulator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppManagerServiceImpl implements AppManagerService {
  public static final String APPS_DIR = "apps";
  public static final String ZIP_INDEX_FILE = "index.html";
  public static final String ZIP_CONFIG_FILE = "config.json";
  public static final String APP_PLUGIN_ROOT = "app/";

  private static final String APPS_TMP_DIR = "apps_tmp";

  private final AppFactory appFactory;
  private final DataService dataService;
  private final FileStore fileStore;
  private final Gson gson;
  private final PluginFactory pluginFactory;

  public AppManagerServiceImpl(
      AppFactory appFactory,
      DataService dataService,
      FileStore fileStore,
      Gson gson,
      PluginFactory pluginFactory) {
    this.appFactory = requireNonNull(appFactory);
    this.dataService = requireNonNull(dataService);
    this.fileStore = requireNonNull(fileStore);
    this.gson = requireNonNull(gson);
    this.pluginFactory = requireNonNull(pluginFactory);

    fileStore.createDirectory(APPS_DIR);
    fileStore.createDirectory(APPS_TMP_DIR);
  }

  @Override
  public List<AppResponse> getApps() {
    return dataService
        .findAll(AppMetadata.APP, App.class)
        .map(AppResponse::create)
        .collect(toList());
  }

  @Override
  public AppResponse getAppByName(String appName) {
    Query<App> query = QueryImpl.EQ(AppMetadata.NAME, appName);
    App app = dataService.findOne(AppMetadata.APP, query, App.class);
    if (app == null) {
      throw new AppForURIDoesNotExistException(appName);
    }
    return AppResponse.create(app);
  }

  @Override
  @Transactional
  public void activateApp(App app) {
    // Add plugin to plugin table to enable permissions and menu management
    String pluginId = generatePluginId(app);
    Plugin plugin = pluginFactory.create(pluginId);
    plugin.setLabel(app.getLabel());
    plugin.setPath(APP_PLUGIN_ROOT + app.getName());
    plugin.setDescription(app.getDescription());
    dataService.add(PluginMetadata.PLUGIN, plugin);
  }

  @Override
  @Transactional
  public void deactivateApp(App app) {
    String pluginId = generatePluginId(app);
    dataService.deleteById(PluginMetadata.PLUGIN, pluginId);

    // TODO remove from menu JSON?
  }

  @Override
  @Transactional
  public void deleteApp(String id) {
    App app = getAppById(id);
    if (app.isActive()) {
      deactivateApp(app);
    }
    try {
      deleteDirectory(fileStore.getFile(app.getResourceFolder()));
    } catch (IOException err) {
      throw new CouldNotDeleteAppException(id);
    }
  }

  @Override
  public String uploadApp(InputStream zipData, String zipFileName, String formFieldName)
      throws IOException {
    String tempFilesDir = "extracted_" + zipFileName;
    String tempAppDirectoryName = APPS_TMP_DIR + separator + tempFilesDir;

    fileStore.createDirectory(tempAppDirectoryName);

    try {
      ZipFileUtil.unzip(zipData, fileStore.getFile(tempAppDirectoryName));
    } catch (UnzipException unzipException) {
      fileStore.delete(tempAppDirectoryName);
      throw new InvalidAppArchiveException(formFieldName, unzipException);
    }

    List<String> missingRequiredFilesList = buildMissingRequiredFiles(tempAppDirectoryName);
    if (!missingRequiredFilesList.isEmpty()) {
      fileStore.deleteDirectory(APPS_TMP_DIR);
      throw new AppArchiveMissingFilesException(missingRequiredFilesList);
    }

    return tempAppDirectoryName;
  }

  @Override
  public AppConfig checkAndObtainConfig(String tempDir, String configContent) throws IOException {
    if (configContent.isEmpty() || !isConfigContentValidJson(configContent)) {
      fileStore.deleteDirectory(APPS_TMP_DIR);
      throw new InvalidAppConfigException();
    }

    AppConfig appConfig = gson.fromJson(configContent, AppConfig.class);
    List<String> missingAppConfigParams = buildMissingConfigParams(appConfig);
    if (!missingAppConfigParams.isEmpty()) {
      fileStore.deleteDirectory(APPS_TMP_DIR);
      throw new AppConfigMissingParametersException(missingAppConfigParams);
    }

    if (appConfig.getName().contains("/")) {
      fileStore.deleteDirectory(APPS_TMP_DIR);
      throw new IllegalAppNameException(appConfig.getName());
    }

    if (fileStore.getFile(APPS_DIR + separator + appConfig.getName()).exists()) {
      fileStore.deleteDirectory(APPS_TMP_DIR);
      throw new AppAlreadyExistsException(appConfig.getName());
    }

    fileStore.move(tempDir, APPS_DIR + separator + appConfig.getName());
    fileStore.deleteDirectory(APPS_TMP_DIR);

    return appConfig;
  }

  @Override
  @Transactional
  public void configureApp(AppConfig appConfig, String htmlTemplate) {
    String appDirName = APPS_DIR + separator + appConfig.getName();

    // If provided config does not include runtimeOptions, set an empty map
    Map<String, Object> runtimeOptions = appConfig.getRuntimeOptions();
    if (runtimeOptions == null) {
      runtimeOptions = Maps.newHashMap();
    }

    App newApp = appFactory.create();
    newApp.setLabel(appConfig.getLabel());
    newApp.setDescription(appConfig.getDescription());
    newApp.setAppVersion(appConfig.getVersion());
    newApp.setApiDependency(appConfig.getApiDependency());
    newApp.setTemplateContent(htmlTemplate);
    newApp.setActive(false);
    newApp.setIncludeMenuAndFooter(appConfig.getIncludeMenuAndFooter());
    newApp.setResourceFolder(appDirName);
    newApp.setAppConfig(gson.toJson(runtimeOptions));
    newApp.setName(appConfig.getName());

    dataService.add(AppMetadata.APP, newApp);
  }

  @Override
  public String extractFileContent(String appDir, String fileName) {
    File indexFile = fileStore.getFile(appDir + separator + fileName);
    return utf8Encodedfiletostring(indexFile);
  }

  private String generatePluginId(App app) {
    return PluginPopulator.APP_PREFIX + app.getName();
  }

  private App getAppById(String id) {
    App app = dataService.findOneById(AppMetadata.APP, id, App.class);
    if (app == null) {
      throw new AppForIDDoesNotExistException(id);
    }
    return app;
  }

  private boolean isConfigContentValidJson(String configContent) {
    try {
      gson.fromJson(configContent, AppConfig.class);
    } catch (JsonSyntaxException e) {
      return false;
    }
    return true;
  }

  private String utf8Encodedfiletostring(File file) {
    try (FileInputStream fileInputStream = new FileInputStream(file)) {
      return IOUtils.toString(fileInputStream, UTF_8);
    } catch (IOException e) {
      throw new InvalidAppConfigException();
    }
  }

  private List<String> buildMissingRequiredFiles(String appDirectoryName) {
    List<String> missingFromArchive = newArrayList();

    File indexFile = fileStore.getFile(appDirectoryName + separator + ZIP_INDEX_FILE);
    if (!indexFile.exists()) {
      missingFromArchive.add(ZIP_INDEX_FILE);
    }

    File configFile = fileStore.getFile(appDirectoryName + separator + ZIP_CONFIG_FILE);
    if (!configFile.exists()) {
      missingFromArchive.add(ZIP_CONFIG_FILE);
    }

    return missingFromArchive;
  }

  private List<String> buildMissingConfigParams(AppConfig appConfig) {
    List<String> missingConfigParameters = newArrayList();

    if (appConfig.getLabel() == null) {
      missingConfigParameters.add("label");
    }

    if (appConfig.getDescription() == null) {
      missingConfigParameters.add("description");
    }

    if (appConfig.getIncludeMenuAndFooter() == null) {
      missingConfigParameters.add("includeMenuAndFooter");
    }

    if (appConfig.getName() == null) {
      missingConfigParameters.add("name");
    }

    if (appConfig.getVersion() == null) {
      missingConfigParameters.add("version");
    }

    return missingConfigParameters;
  }
}
