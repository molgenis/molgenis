package org.molgenis.app.manager.service.impl;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.io.FileUtils.deleteDirectory;

@Service
public class AppManagerServiceImpl implements AppManagerService
{
	private static final String ZIP_INDEX_FILE = "index.html";
	private static final String ZIP_CONFIG_FILE = "config.json";

	private static final String APP_PLUGIN_ROOT = "app/";

	private final AppFactory appFactory;
	private final DataService dataService;
	private final FileStore fileStore;
	private final Gson gson;
	private final PluginFactory pluginFactory;

	public AppManagerServiceImpl(AppFactory appFactory, DataService dataService, FileStore fileStore, Gson gson,
			PluginFactory pluginFactory)
	{
		this.appFactory = requireNonNull(appFactory);
		this.dataService = requireNonNull(dataService);
		this.fileStore = requireNonNull(fileStore);
		this.gson = requireNonNull(gson);
		this.pluginFactory = requireNonNull(pluginFactory);
	}

	@Override
	public List<AppResponse> getApps()
	{
		return dataService.findAll(AppMetadata.APP, App.class).map(AppResponse::create).collect(toList());
	}

	@Override
	public AppResponse getAppByUri(String uri)
	{
		Query<App> query = QueryImpl.EQ(AppMetadata.URI, uri);
		App app = dataService.findOne(AppMetadata.APP, query, App.class);
		if(app == null) {
			throw new AppForURIDoesNotExistException(uri);
		}
		return AppResponse.create(app);
	}

	@Override
	@Transactional
	public void activateApp(String id)
	{
		// Set app to active
		App app = getAppById(id);
		app.setActive(true);
		dataService.update(AppMetadata.APP, app);

		// Add plugin to plugin table to enable permissions and menu management
		String pluginId = generatePluginId(app);
		Plugin plugin = pluginFactory.create(pluginId);
		plugin.setLabel(app.getLabel());
		plugin.setDescription(app.getDescription());
		dataService.add(PluginMetadata.PLUGIN, plugin);
	}

	@Override
	@Transactional
	public void deactivateApp(String id)
	{
		App app = getAppById(id);
		app.setActive(false);
		dataService.update(AppMetadata.APP, app);

		String pluginId = generatePluginId(app);
		dataService.deleteById(PluginMetadata.PLUGIN, pluginId);

		// TODO remove from menu JSON?
	}

	@Override
	@Transactional
	public void deleteApp(String id) throws IOException
	{
		App app = getAppById(id);
		deleteDirectory(new File(app.getResourceFolder()));
		dataService.deleteById(AppMetadata.APP, id);
	}

	@Override
	@Transactional
	public void uploadApp(InputStream zipData, String zipFileName, String formFieldName) throws IOException, ZipException
	{
		String appArchiveName = "zip_file_" + zipFileName;
		ZipFile appArchive = new ZipFile(fileStore.store(zipData, appArchiveName));

		if (!appArchive.isValidZipFile())
		{
			fileStore.delete(appArchiveName);
			throw new InvalidAppArchiveException(formFieldName);
		}

		String appDirectoryName = fileStore.getStorageDir() + File.separator + zipFileName;
		appArchive.extractAll(appDirectoryName);
		fileStore.delete(appArchiveName);

		List<String> missingRequiredFilesList = buildMissingRequiredFiles(appDirectoryName);
		if (!missingRequiredFilesList.isEmpty())
		{
			fileStore.deleteDirectory(appDirectoryName);
			throw new AppArchiveMissingFilesException(missingRequiredFilesList);
		}


		File indexFile = new File(appDirectoryName + File.separator + ZIP_INDEX_FILE);
		File configFile = new File(appDirectoryName + File.separator + ZIP_CONFIG_FILE);
		if (!isConfigContentValidJson(configFile))
		{
			fileStore.deleteDirectory(appDirectoryName);
			throw new InvalidAppConfigException();
		}

		AppConfig appConfig = gson.fromJson(utf8Encodedfiletostring(configFile), AppConfig.class);
		List<String> missingAppConfigParams = buildMissingConfigParams(appConfig);
		if (!missingAppConfigParams.isEmpty())
		{
			fileStore.deleteDirectory(appDirectoryName);
			throw new AppConfigMissingParametersException(missingAppConfigParams);
		}

		// If provided config does not include runtimeOptions, set an empty map
		Map<String, Object> runtimeOptions = appConfig.getRuntimeOptions();
		if (runtimeOptions == null)
		{
			runtimeOptions = Maps.newHashMap();
		}

		App newApp = appFactory.create();
		newApp.setLabel(appConfig.getLabel());
		newApp.setDescription(appConfig.getDescription());
		newApp.setAppVersion(appConfig.getVersion());
		newApp.setApiDependency(appConfig.getApiDependency());
		newApp.setTemplateContent(utf8Encodedfiletostring(indexFile));
		newApp.setActive(false);
		newApp.setIncludeMenuAndFooter(appConfig.getIncludeMenuAndFooter());
		newApp.setResourceFolder(appDirectoryName);
		newApp.setAppConfig(gson.toJson(runtimeOptions));
		newApp.setUri(appConfig.getUri());

		dataService.add(AppMetadata.APP, newApp);
	}

	private String generatePluginId(App app)
	{
		String pluginId = APP_PLUGIN_ROOT + app.getUri();
		if (!pluginId.endsWith("/"))
		{
			pluginId = pluginId + "/";
		}
		return pluginId;
	}

	private App getAppById(String id)
	{
		App app = dataService.findOneById(AppMetadata.APP, id, App.class);
		if (app == null)
		{
			throw new AppForIDDoesNotExistException(id);
		}
		return app;
	}

	private boolean isConfigContentValidJson(File configFile)
	{
		String fileContents = utf8Encodedfiletostring(configFile);
		try
		{
			gson.fromJson(fileContents, AppConfig.class);
		}
		catch (JsonSyntaxException e)
		{
			return false;
		}
		return true;
	}

	private String utf8Encodedfiletostring(File file)
	{
		try (FileInputStream fileInputStream = new FileInputStream(file))
		{
			return IOUtils.toString(fileInputStream, UTF_8);
		}
		catch (IOException e)
		{
			throw new InvalidAppConfigException();
		}
	}

	private List<String> buildMissingRequiredFiles(String appDirectoryName)
	{
		List<String> missingFromArchive = newArrayList();

		File indexFile = new File(appDirectoryName + File.separator + ZIP_INDEX_FILE);
		if (!indexFile.exists())
		{
			missingFromArchive.add(ZIP_INDEX_FILE);
		}

		File configFile = new File(appDirectoryName + File.separator + ZIP_CONFIG_FILE);
		if (!configFile.exists())
		{
			missingFromArchive.add(ZIP_CONFIG_FILE);
		}

		return missingFromArchive;
	}

	private List<String> buildMissingConfigParams(AppConfig appConfig)
	{
		List<String> missingConfigParameters = newArrayList();

		if (appConfig.getLabel() == null)
		{
			missingConfigParameters.add("label");
		}

		if (appConfig.getDescription() == null)
		{
			missingConfigParameters.add("description");
		}

		if (appConfig.getIncludeMenuAndFooter() == null)
		{
			missingConfigParameters.add("includeMenuAndFooter");
		}

		if (appConfig.getUri() == null)
		{
			missingConfigParameters.add("uri");
		}

		if (appConfig.getVersion() == null)
		{
			missingConfigParameters.add("version");
		}

		return missingConfigParameters;
	}
}
