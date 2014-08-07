package org.molgenis.ui.menu;

import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.ui.MolgenisUi;
import org.molgenis.ui.MolgenisUiMenu;
import org.springframework.beans.factory.annotation.Autowired;

public class MenuMolgenisUi implements MolgenisUi
{
	static final String DEFAULT_TITLE = "MOLGENIS";

	static final String KEY_HREF_LOGO = "app.href.logo";
	static final String KEY_HREF_CSS = "app.href.css";
	static final String KEY_TITLE = "app.name";

	private final MolgenisSettings molgenisSettings;
	private final MenuReaderService menuReaderService;

	@Autowired
	public MenuMolgenisUi(MolgenisSettings molgenisSettings, MenuReaderService menuReaderService)
	{
		if (molgenisSettings == null) throw new IllegalArgumentException("molgenisSettings is null");
		this.molgenisSettings = molgenisSettings;
		this.menuReaderService = menuReaderService;
	}

	@Override
	public String getTitle()
	{
		return molgenisSettings.getProperty(KEY_TITLE, DEFAULT_TITLE);
	}

	@Override
	public String getHrefLogo()
	{
		return molgenisSettings.getProperty(KEY_HREF_LOGO);
	}

	@Override
	public String getHrefCss()
	{
		return molgenisSettings.getProperty(KEY_HREF_CSS);
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
