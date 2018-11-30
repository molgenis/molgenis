package org.molgenis.data.plugin.model;

import static com.google.common.collect.Iterators.partition;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

import com.google.gson.Gson;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import org.molgenis.data.AbstractRepositoryDecorator;
import org.molgenis.data.Repository;
import org.molgenis.settings.AppSettings;

public class PluginRepositoryDecorator extends AbstractRepositoryDecorator<Plugin> {
  private static final int BATCH_SIZE = 1000;

  private final AppSettings appSettings;
  private final Gson gson;

  PluginRepositoryDecorator(Repository<Plugin> delegateRepository, AppSettings appSettings) {
    super(delegateRepository);
    this.appSettings = requireNonNull(appSettings);
    this.gson = new Gson();
  }

  @Override
  public void delete(Plugin plugin) {
    super.delete(plugin);
    deleteMenuEntries(plugin);
  }

  @Override
  public void deleteById(Object id) {
    super.deleteById(id);
    deleteMenuEntriesById(id);
  }

  @Override
  public void deleteAll() {
    forEachBatched(this::deleteBatch, BATCH_SIZE);
  }

  @Override
  public void delete(Stream<Plugin> pluginStream) {
    partition(pluginStream.iterator(), BATCH_SIZE).forEachRemaining(this::deleteBatch);
  }

  @Override
  public void deleteAll(Stream<Object> ids) {
    partition(ids.iterator(), BATCH_SIZE).forEachRemaining(this::deleteBatchById);
  }

  private void deleteBatchById(List<Object> ids) {
    delegate().deleteAll(ids.stream());
    deleteMenuEntriesById(ids);
  }

  private void deleteBatch(List<Plugin> plugins) {
    delegate().delete(plugins.stream());
    deleteMenuEntries(plugins);
  }

  private void deleteMenuEntries(Map<String, Object> menu, String id) {
    @SuppressWarnings("unchecked")
    List<Map<String, Object>> items = (List<Map<String, Object>>) menu.get("items");
    for (Iterator<Map<String, Object>> it = items.iterator(); it.hasNext(); ) {
      Map<String, Object> item = it.next();
      if (Objects.equals(item.get("type"), "menu")) {
        deleteMenuEntries(item, id);
      } else if (Objects.equals(item.get("type"), "plugin") && Objects.equals(item.get("id"), id)) {
        it.remove();
      }
    }
  }

  private void deleteMenuEntriesById(List<Object> ids) {
    Map<String, Object> menu = getMenu();
    ids.forEach(id -> deleteMenuEntries(menu, id.toString()));
    updateMenu(menu);
  }

  private void deleteMenuEntriesById(Object id) {
    deleteMenuEntriesById(singletonList(id));
  }

  private void deleteMenuEntries(List<Plugin> plugins) {
    deleteMenuEntriesById(plugins.stream().map(Plugin::getId).collect(toList()));
  }

  private void deleteMenuEntries(Plugin plugin) {
    deleteMenuEntriesById(plugin.getId());
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> getMenu() {
    String menuJson = appSettings.getMenu();
    return (Map<String, Object>) gson.fromJson(menuJson, Map.class);
  }

  private void updateMenu(Map<String, Object> menu) {
    String menuJson = gson.toJson(menu);
    appSettings.setMenu(menuJson);
  }
}
