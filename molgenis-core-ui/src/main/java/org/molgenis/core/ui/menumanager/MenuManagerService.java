package org.molgenis.core.ui.menumanager;

import org.molgenis.data.plugin.model.Plugin;
import org.molgenis.web.menu.model.Menu;

public interface MenuManagerService {
  Iterable<Plugin> getPlugins();

  void saveMenu(Menu molgenisMenu);
}
