package org.molgenis.app.manager.service.impl;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.molgenis.app.manager.exception.AppManagerException;
import org.molgenis.app.manager.meta.App;
import org.molgenis.app.manager.meta.AppFactory;
import org.molgenis.app.manager.meta.AppMetadata;
import org.molgenis.app.manager.model.AppConfig;
import org.molgenis.app.manager.model.AppEditRequest;
import org.molgenis.app.manager.model.AppResponse;
import org.molgenis.app.manager.service.AppManagerService;
import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.data.file.FileStore;
import org.molgenis.data.plugin.model.Plugin;
import org.molgenis.data.plugin.model.PluginMetadata;
import org.molgenis.data.support.QueryImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
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

	public AppManagerServiceImpl(AppFactory appFactory, DataService dataService, FileStore fileStore)
	{
		this.appFactory = requireNonNull(appFactory);
		this.dataService = requireNonNull(dataService);
		this.fileStore = requireNonNull(fileStore);
	}

	@Override
	public List<AppResponse> getApps()
	{
		return dataService.findAll(AppMetadata.APP, App.class).map(AppResponse::create).collect(toList());
	}

	@Override
	public AppResponse getAppByUri(String uri)
	{
		App app = findAppByUri(uri);
		if (app == null)
		{
			throw new AppManagerException("App with uri [" + uri + "] does not exist.");
		}
		return AppResponse.create(app);
	}

	@Override
	@Transactional
	public void activateApp(String id)
	{
		// Set app to active
		App app = findAppById(id);
		app.setActive(true);
		dataService.update(AppMetadata.APP, app);

		// Add plugin to plugin table to enable permissions
		// TODO check if there is already a '/' at the end app.getUri()
		// TODO use a constant for '/'
		Plugin plugin = new Plugin(APP_PLUGIN_ROOT + app.getUri() + "/",
				dataService.getEntityType(PluginMetadata.PLUGIN));
		plugin.setLabel(app.getLabel());
		plugin.setDescription(app.getDescription());
		dataService.add(PluginMetadata.PLUGIN, plugin);
	}

	@Override
	@Transactional
	public void deactivateApp(String id)
	{
		App app = findAppById(id);
		app.setActive(false);
		dataService.update(AppMetadata.APP, app);
		// TODO check if there is already a '/' at the end of app.getUri()
		// TODO use a constant for '/'
		dataService.deleteById(PluginMetadata.PLUGIN, APP_PLUGIN_ROOT + app.getUri() + "/");

		// TODO remove permissions?
		// TODO remove from menu JSON?
	}

	@Override
	@Transactional
	public void editApp(AppEditRequest appEditRequest)
	{
		App app = findAppById(appEditRequest.getId());
		app.setLabel(appEditRequest.getLabel());
		app.setDescription(appEditRequest.getDescription());
		dataService.update(AppMetadata.APP, app);
	}

	@Override
	@Transactional
	public void deleteApp(String id) throws IOException
	{
		App app = findAppById(id);
		deleteDirectory(new File(app.getResourceFolder()));
		dataService.deleteById(AppMetadata.APP, id);
	}

	@Override
	@Transactional
	public void uploadApp(MultipartFile multipartFile) throws IOException, ZipException
	{
		String appZipFileName = "zip_file_" + multipartFile.getOriginalFilename();
		ZipFile appZipFile = new ZipFile(fileStore.store(multipartFile.getInputStream(), appZipFileName));

		if (!appZipFile.isValidZipFile())
		{
			fileStore.delete(appZipFileName);
			throw new AppManagerException(multipartFile.getName() + " is not a valid zip file!");
		}

		String appDirectoryName = fileStore.getStorageDir() + File.separator + multipartFile.getOriginalFilename();
		appZipFile.extractAll(appDirectoryName);
		fileStore.delete(appZipFileName);

		List<String> missingFromZipFile = checkForMissingFilesInAppZip(appDirectoryName);
		if (missingFromZipFile.size() > 0)
		{
			fileStore.deleteDirectory(appDirectoryName);
			throw new AppManagerException("There were some missing files in your zip package " + missingFromZipFile
					+ ". Please add these and upload again.");
		}

		File indexFile = new File(appDirectoryName + File.separator + ZIP_INDEX_FILE);
		File configFile = new File(appDirectoryName + File.separator + ZIP_CONFIG_FILE);
		if (!isConfigContentValidJson(configFile))
		{
			fileStore.deleteDirectory(appDirectoryName);
			throw new AppManagerException(
					"The config file you provided has some problems. Please ensure it is a valid JSON file.");
		}

		Gson gson = new Gson();
		AppConfig appConfig = gson.fromJson(fileToString(configFile), AppConfig.class);
		App newApp = appFactory.create();

		newApp.setLabel(appConfig.getLabel());
		newApp.setDescription(appConfig.getDescription());
		newApp.setAppVersion(appConfig.getVersion());
		newApp.setApiDependency(appConfig.getApiDependency());

		// TODO What to do with index.html?
		newApp.setTemplateContent(fileToString(indexFile));
		newApp.setActive(false);

		// TODO make includeMenuAndFooter configurable?
		newApp.setIncludeMenuAndFooter(true);
		newApp.setResourceFolder(appDirectoryName);

		// If provided config does not include runtimeOptions, set an empty map
		Map<String, Object> runtimeOptions = appConfig.getRuntimeOptions();
		if (runtimeOptions == null) runtimeOptions = Maps.newHashMap();
		newApp.setAppConfig(gson.toJson(runtimeOptions));

		// Generate a URI based on supplied uri and version
		newApp.setUri(appConfig.getUri() + "/" + appConfig.getVersion());

		dataService.add(AppMetadata.APP, newApp);
	}

	private List<String> checkForMissingFilesInAppZip(String appDirectoryName)
	{
		List<String> missingFromZipFile = newArrayList();

		File indexFile = new File(appDirectoryName + File.separator + ZIP_INDEX_FILE);
		if (!indexFile.exists())
		{
			missingFromZipFile.add(ZIP_INDEX_FILE);
		}

		File configFile = new File(appDirectoryName + File.separator + ZIP_CONFIG_FILE);
		if (!configFile.exists())
		{
			missingFromZipFile.add(ZIP_CONFIG_FILE);
		}

		return missingFromZipFile;
	}

	private App findAppById(String id)
	{
		App app = dataService.findOneById(AppMetadata.APP, id, App.class);
		if (app == null)
		{
			throw new AppManagerException("App with id [" + id + "] does not exist.");
		}
		return app;
	}

	private App findAppByUri(String uri)
	{
		Query<App> query = QueryImpl.EQ(AppMetadata.URI, uri);
		return dataService.findOne(AppMetadata.APP, query, App.class);
	}

	private boolean isConfigContentValidJson(File configFile) throws IOException
	{
		String fileContents = fileToString(configFile);

		try
		{
			// TODO Check for version and URI in config, throw exception if not present
			new Gson().fromJson(fileContents, AppConfig.class);
		}
		catch (Exception e)
		{
			return false;
		}
		return true;
	}

	private String fileToString(File file) throws IOException
	{
		StringBuilder fileContents = new StringBuilder((int) file.length());

		FileReader fileReader = new FileReader(file);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		bufferedReader.lines().forEach(line -> fileContents.append(line).append(System.getProperty("line.separator")));

		return fileContents.toString();
	}
}
