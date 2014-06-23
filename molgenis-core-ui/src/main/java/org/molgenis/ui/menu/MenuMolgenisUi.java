package org.molgenis.ui.menu;

import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.security.core.MolgenisPermissionService;
import org.molgenis.ui.MolgenisUi;
import org.molgenis.ui.MolgenisUiMenu;
import org.springframework.beans.factory.annotation.Autowired;

public class MenuMolgenisUi implements MolgenisUi
{
	private static final String KEY_HREF_LOGO = "app.href.logo";
	private static final String KEY_HREF_CSS = "app.href.css";
	private static final String KEY_TITLE = "app.href.logo";

	private final MolgenisSettings molgenisSettings;
	private final MenuReaderService menuReaderService;
	private final MolgenisPermissionService molgenisPermissionService;

	@Autowired
	public MenuMolgenisUi(MolgenisSettings molgenisSettings, MenuReaderService menuReaderService,
			MolgenisPermissionService molgenisPermissionService)
	{
		if (molgenisSettings == null) throw new IllegalArgumentException("molgenisSettings is null");
		this.molgenisSettings = molgenisSettings;
		this.menuReaderService = menuReaderService;
		this.molgenisPermissionService = molgenisPermissionService;
	}

	@Override
	public String getTitle()
	{
		return molgenisSettings.getProperty(KEY_TITLE);
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
		return new MenuItemToMolgenisUiMenuAdapter(menu, molgenisPermissionService);
	}

	@Override
	public MolgenisUiMenu getMenu(String menuId)
	{
		Menu topMenu = menuReaderService.getMenu();
		MenuItem menu = getMenuRec(topMenu, menuId);
		return new MenuItemToMolgenisUiMenuAdapter(menu, molgenisPermissionService);
	}

	private MenuItem getMenuRec(MenuItem menu, String menuId)
	{
		if (menuId.equals(menu.getId())) return menu;
		for (MenuItem menuItem : menu.getItems())
		{
			if (menuItem.getType() == MenuItemType.MENU)
			{
				MenuItem submenu = getMenuRec(menuItem, menuId);
				if (submenu != null) return submenu;
			}
		}
		return null;
	}
}
