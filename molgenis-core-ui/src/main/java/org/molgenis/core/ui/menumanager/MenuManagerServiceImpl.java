package org.molgenis.core.ui.menumanager;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

import com.google.gson.Gson;
import org.molgenis.data.DataService;
import org.molgenis.data.plugin.model.Plugin;
import org.molgenis.data.plugin.model.PluginMetadata;
import org.molgenis.security.core.runas.RunAsSystem;
import org.molgenis.settings.AppSettings;
import org.molgenis.web.menu.model.Menu;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class MenuManagerServiceImpl implements MenuManagerService {
  private final AppSettings appSettings;
  private final DataService dataService;
  private final Gson gson;

  public MenuManagerServiceImpl(AppSettings appSettings, DataService dataService, Gson gson) {
    this.appSettings = requireNonNull(appSettings);
    this.dataService = requireNonNull(dataService);
    this.gson = requireNonNull(gson);
  }

  @Override
  @RunAsSystem
  @PreAuthorize("hasAnyRole('ROLE_SYSTEM, ROLE_SU')")
  @Transactional(readOnly = true)
  public Iterable<Plugin> getPlugins() {
    return dataService.findAll(PluginMetadata.PLUGIN, Plugin.class).collect(toList());
  }

  @Override
  @PreAuthorize("hasAnyRole('ROLE_SYSTEM, ROLE_SU')")
  @Transactional
  public void saveMenu(Menu molgenisMenu) {
    appSettings.setMenu(gson.toJson(molgenisMenu));
  }
}
