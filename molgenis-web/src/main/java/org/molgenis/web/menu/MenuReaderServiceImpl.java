package org.molgenis.web.menu;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static org.molgenis.data.plugin.model.PluginPermission.VIEW_PLUGIN;

import com.google.gson.Gson;
import java.util.Collection;
import java.util.Optional;
import javax.annotation.Nullable;
import org.molgenis.data.plugin.model.PluginIdentity;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.molgenis.settings.AppSettings;
import org.molgenis.web.menu.model.Menu;
import org.molgenis.web.menu.model.MenuItem;
import org.molgenis.web.menu.model.MenuNode;

public class MenuReaderServiceImpl implements MenuReaderService {
  private final AppSettings appSettings;
  private final Gson gson;
  private final UserPermissionEvaluator userPermissionEvaluator;

  public MenuReaderServiceImpl(
      AppSettings appSettings, Gson gson, UserPermissionEvaluator userPermissionEvaluator) {
    this.appSettings = requireNonNull(appSettings);
    this.gson = requireNonNull(gson);
    this.userPermissionEvaluator = requireNonNull(userPermissionEvaluator);
  }

  private boolean userHasViewPermission(MenuNode it) {
    if (it instanceof MenuItem) {
      return userPermissionEvaluator.hasPermission(new PluginIdentity(it.getId()), VIEW_PLUGIN);
    } else {
      return !((Menu) it).getItems().isEmpty();
    }
  }

  @Override
  public Optional<Menu> getMenu() {
    return Optional.ofNullable(appSettings.getMenu())
        .map(menuJson -> gson.fromJson(menuJson, Menu.class))
        .flatMap(menu -> menu.filter(this::userHasViewPermission))
        .map(Menu.class::cast);
  }

  @Override
  @Nullable
  public String findMenuItemPath(String menuItemId) {
    return getMenu()
        .flatMap(it -> it.getPath(menuItemId))
        .map(Collection::stream)
        .map(stream -> stream.collect(joining("/", "/menu/", "")))
        .orElse(null);
  }
}
