package org.molgenis.bootstrap.populate;

import org.molgenis.data.DataService;
import org.molgenis.framework.ui.MolgenisPlugin;
import org.molgenis.framework.ui.MolgenisPluginRegistry;
import org.molgenis.ui.Plugin;
import org.molgenis.ui.PluginFactory;
import org.molgenis.ui.PluginMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toMap;
import static org.molgenis.ui.PluginMetadata.PLUGIN;

@Component
public class PluginPopulator
{
	private final DataService dataService;
	private final MolgenisPluginRegistry pluginRegistry;
	private final PluginFactory pluginFactory;

	@Autowired
	public PluginPopulator(DataService dataService, MolgenisPluginRegistry pluginRegistry, PluginFactory pluginFactory)
	{
		this.dataService = requireNonNull(dataService);
		this.pluginRegistry = requireNonNull(pluginRegistry);
		this.pluginFactory = requireNonNull(pluginFactory);
	}

	void populate()
	{
		Map<String, Plugin> existingPluginMap = dataService.findAll(PLUGIN, Plugin.class)
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

	private Plugin toPlugin(MolgenisPlugin molgenisPlugin)
	{
		Plugin plugin = pluginFactory.create(molgenisPlugin.getId());
		plugin.setLabel(molgenisPlugin.getName());
		plugin.setUri(molgenisPlugin.getUrl());
		plugin.setFullUri(molgenisPlugin.getFullUri());
		return plugin;
	}
}
