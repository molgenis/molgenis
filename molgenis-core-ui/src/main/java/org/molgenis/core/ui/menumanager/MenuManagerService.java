package org.molgenis.core.ui.menumanager;

import org.molgenis.core.ui.menu.Menu;
import org.molgenis.core.ui.menu.MenuReaderService;
import org.molgenis.data.plugin.model.Plugin;

public interface MenuManagerService extends MenuReaderService
{
	Iterable<Plugin> getPlugins();

	void saveMenu(Menu molgenisMenu);
}