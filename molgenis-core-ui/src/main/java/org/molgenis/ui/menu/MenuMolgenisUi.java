package org.molgenis.ui.menu;

import static com.google.common.base.Preconditions.checkNotNull;

import org.molgenis.ui.MolgenisUi;
import org.molgenis.ui.MolgenisUiMenu;
import org.springframework.beans.factory.annotation.Autowired;

public class MenuMolgenisUi implements MolgenisUi
{
	private final MenuReaderService menuReaderService;

	@Autowired
	public MenuMolgenisUi(MenuReaderService menuReaderService)
	{
		this.menuReaderService = checkNotNull(menuReaderService);
	}

	@Override
	public MolgenisUiMenu getMenu()
	{
		Menu menu = menuReaderService.getMenu();
		return new MenuItemToMolgenisUiMenuAdapter(menu, menu);
	}

	@Override
	public MolgenisUiMenu getMenu(String menuId)
	{
		Menu rootMenu = menuReaderService.getMenu();
		MenuItem menu = findMenu(rootMenu, menuId);
		return menu != null ? new MenuItemToMolgenisUiMenuAdapter(menu, rootMenu) : null;
	}

	private MenuItem findMenu(MenuItem menu, String menuId)
	{
		if (menuId.equals(menu.getId())) return menu;
		for (MenuItem menuItem : menu.getItems())
		{
			if (menuItem.getType() == MenuItemType.MENU)
			{
				MenuItem submenu = findMenu(menuItem, menuId);
				if (submenu != null)
				{
					return submenu;
				}
			}
		}
		return null;
	}
}
