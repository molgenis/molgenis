package org.molgenis.ui.menumanager;

import org.molgenis.data.plugin.Plugin;
import org.molgenis.ui.menu.Menu;
import org.molgenis.ui.menu.MenuReaderService;

public interface MenuManagerService extends MenuReaderService
{
	Iterable<Plugin> getPlugins();

	void saveMenu(Menu molgenisMenu);
}