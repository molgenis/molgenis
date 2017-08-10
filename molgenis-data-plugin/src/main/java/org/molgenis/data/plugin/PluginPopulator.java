package org.molgenis.data.plugin;

import org.molgenis.data.DataService;
import org.molgenis.data.plugin.model.Plugin;
import org.molgenis.data.plugin.model.PluginFactory;
import org.molgenis.data.plugin.model.PluginMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toMap;
import static org.molgenis.data.plugin.model.PluginMetadata.PLUGIN;

@Component
public class PluginPopulator
{
	private final DataService dataService;
	private final PluginRegistry pluginRegistry;
	private final PluginFactory pluginFactory;

	@Autowired
	public PluginPopulator(DataService dataService, PluginRegistry pluginRegistry, PluginFactory pluginFactory)
	{
		this.dataService = requireNonNull(dataService);
		this.pluginRegistry = requireNonNull(pluginRegistry);
		this.pluginFactory = requireNonNull(pluginFactory);
	}

	public void populate()
	{
		Map<String, Plugin> existingPluginMap = dataService.findAll(PLUGIN, org.molgenis.data.plugin.model.Plugin.class)
														   .collect(toMap(Plugin::getId, Function.identity()));
		List<Plugin> newOrChangedPlugins = new ArrayList<>();
		List<Plugin> deletedPlugins = new ArrayList<>();

		pluginRegistry.getPlugins().forEach(molgenisPlugin ->
		{
			Plugin plugin = toPlugin(molgenisPlugin);
			newOrChangedPlugins.add(plugin);
		});
		existingPluginMap.forEach((pluginId, plugin) ->
		{
			if (pluginRegistry.getPlugin(pluginId) == null)
			{
				deletedPlugins.add(plugin);
			}
		});

		if (!newOrChangedPlugins.isEmpty())
		{
			dataService.getRepository(PluginMetadata.PLUGIN, Plugin.class).upsertBatch(newOrChangedPlugins);
		}
		if (!deletedPlugins.isEmpty())
		{
			dataService.delete(PluginMetadata.PLUGIN, deletedPlugins.stream());
		}
	}

	private Plugin toPlugin(org.molgenis.data.plugin.Plugin molgenisPlugin)
	{
		Plugin plugin = pluginFactory.create(molgenisPlugin.getId());
		plugin.setLabel(molgenisPlugin.getName());
		plugin.setUri(molgenisPlugin.getUrl());
		plugin.setFullUri(molgenisPlugin.getFullUri());
		return plugin;
	}
}