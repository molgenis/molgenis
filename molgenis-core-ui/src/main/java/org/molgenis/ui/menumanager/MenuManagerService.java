package org.molgenis.ui.menumanager;

import org.molgenis.framework.ui.MolgenisPlugin;
import org.molgenis.ui.menu.Menu;
import org.molgenis.ui.menu.MenuReaderService;

public interface MenuManagerService extends MenuReaderService
{
	Iterable<MolgenisPlugin> getPlugins();

	void saveMenu(Menu molgenisMenu);
}