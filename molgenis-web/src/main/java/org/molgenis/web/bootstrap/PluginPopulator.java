package org.molgenis.web.bootstrap;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toMap;
import static org.molgenis.data.plugin.model.PluginMetadata.PLUGIN;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.molgenis.data.DataService;
import org.molgenis.data.plugin.model.Plugin;
import org.molgenis.data.plugin.model.PluginFactory;
import org.molgenis.web.PluginController;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class PluginPopulator {
  private final DataService dataService;
  private final PluginFactory pluginFactory;

  public static final String APP_PREFIX = "app-";

  PluginPopulator(DataService dataService, PluginFactory pluginFactory) {
    this.dataService = requireNonNull(dataService);
    this.pluginFactory = requireNonNull(pluginFactory);
  }

  public void populate(ApplicationContext ctx) {
    Map<String, Plugin> newOrChangedPluginMap = getPlugins(ctx);
    List<Plugin> deletedPlugins = new ArrayList<>();

    Map<String, Plugin> existingPluginMap =
        dataService
            .findAll(PLUGIN, Plugin.class)
            .collect(toMap(Plugin::getId, Function.identity()));

    existingPluginMap.forEach(
        (pluginId, plugin) -> {
          if (newOrChangedPluginMap.get(pluginId) == null && !pluginId.startsWith(APP_PREFIX)) {
            deletedPlugins.add(plugin);
          }
        });

    if (!newOrChangedPluginMap.isEmpty()) {
      dataService
          .getRepository(PLUGIN, Plugin.class)
          .upsertBatch(newArrayList(newOrChangedPluginMap.values()));
    }
    if (!deletedPlugins.isEmpty()) {
      dataService.delete(PLUGIN, deletedPlugins.stream());
    }
  }

  private Map<String, Plugin> getPlugins(ApplicationContext ctx) {
    Map<String, PluginController> pluginControllerMap = ctx.getBeansOfType(PluginController.class);
    return pluginControllerMap
        .values()
        .stream()
        .map(this::createPlugin)
        .collect(toMap(Plugin::getId, Function.identity()));
  }

  private Plugin createPlugin(PluginController pluginController) {
    String pluginControllerId = pluginController.getId();
    return pluginFactory
        .create(pluginControllerId)
        .setLabel(pluginControllerId)
        .setPath(pluginControllerId);
  }
}
