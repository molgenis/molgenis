package org.molgenis.data.importer;

import java.util.List;

import org.molgenis.security.core.runas.RunAsSystem;
import org.molgenis.ui.menu.Menu;
import org.molgenis.ui.menu.MenuItem;
import org.molgenis.ui.menu.MenuItemType;
import org.molgenis.ui.menumanager.MenuManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ImportPostProcessingService
{
	private final MenuManagerService menuManagerService;

	@Autowired
	public ImportPostProcessingService(MenuManagerService menuManagerService)
	{
		this.menuManagerService = menuManagerService;
	}

	@RunAsSystem
	public void addMenuItems(List<String> entities)
	{
		Menu menu = menuManagerService.getMenu();
		MenuItem entitiesMenu = menu.findMenuItem("entities");

		if (entitiesMenu != null)
		{
			for (String entityName : entities)
			{
				entitiesMenu.getItems().add(new MenuItem(MenuItemType.PLUGIN, "form." + entityName, entityName));
			}
			menuManagerService.saveMenu(menu);
		}
	}

}
