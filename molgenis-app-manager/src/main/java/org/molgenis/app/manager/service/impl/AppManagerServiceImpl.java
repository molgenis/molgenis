package org.molgenis.app.manager.service.impl;

import org.molgenis.app.manager.meta.App;
import org.molgenis.app.manager.meta.AppMetadata;
import org.molgenis.app.manager.model.AppRequest;
import org.molgenis.app.manager.model.AppResponse;
import org.molgenis.app.manager.service.AppManagerService;
import org.molgenis.data.DataService;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.plugin.model.Plugin;
import org.molgenis.data.plugin.model.PluginMetadata;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@Service
public class AppManagerServiceImpl implements AppManagerService
{
	private final DataService dataService;

	public AppManagerServiceImpl(DataService dataService)
	{
		this.dataService = requireNonNull(dataService);
	}

	@Override
	public List<AppResponse> getApps()
	{
		return dataService.findAll(AppMetadata.APP, App.class)
						  .map(this::appToAppResponseMapper)
						  .collect(Collectors.toList());
	}

	@Override
	public AppResponse getAppById(String id)
	{
		App app = findAppById(id);
		return appToAppResponseMapper(app);
	}

	@Override
	public void activateApp(String id)
	{
		App app = findAppById(id);

		// Set app to active and unpack resources
		app.setActive(true);
		// TODO unpack resources
		//noinspection StringConcatenationMissingWhitespace
//		ZipFile zipFile = new ZipFile(fileStoreFile);
//		zipFile.extractAll(
//				fileStore.getStorageDir() + separatorChar + FILE_STORE_PLUGIN_APPS_PATH + separatorChar
//						+ app.getId() + separatorChar);
		// TODO fill in templateContent
		dataService.update(AppMetadata.APP, app);

		// Add plugin to plugin table to enable permissions
		Plugin plugin = new Plugin("app/" + id, dataService.getEntityType(PluginMetadata.PLUGIN));
		plugin.setLabel(app.getLabel());
		plugin.setDescription(app.getDescription());

		dataService.add(PluginMetadata.PLUGIN, plugin);
	}

	public void deactivateApp(String id)
	{
		App app = findAppById(id);

		// Set app to active and unpack resources
		app.setActive(false);
		// TODO cleanup resources
		dataService.update(AppMetadata.APP, app);

		// Remove plugin from plugin table
		dataService.deleteById(PluginMetadata.PLUGIN, "app/" + id);

		// TODO remove permissions?
		// TODO remove from menu JSON?
	}

	@Override
	public void createApp(AppRequest appRequest)
	{
		// Do create stuff
	}

	@Override
	public void editApp(AppRequest appRequest)
	{
		App app = findAppById(appRequest.getId());
		// Do edit stuff
	}

	@Override
	public void deleteApp(String id)
	{
		dataService.deleteById(AppMetadata.APP, id);
	}

	private App findAppById(String id)
	{
		App app = dataService.findOneById(AppMetadata.APP, id, App.class);
		if (app == null)
		{
			throw new MolgenisDataException("App with id [" + id + "] does not exist.");
		}
		return app;
	}

	private AppResponse appToAppResponseMapper(App app)
	{
		return AppResponse.create(app.getId(), app.getLabel(), app.getDescription(), app.isActive());
	}
}
