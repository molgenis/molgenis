package org.molgenis.ui;

import com.google.common.collect.Lists;
import org.molgenis.security.core.PermissionService;
import org.molgenis.web.UiMenu;
import org.molgenis.web.UiMenuItem;
import org.molgenis.web.UiMenuItemType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @deprecated use {@link org.molgenis.ui.menu.MenuItemToMolgenisUiMenuAdapter} instead
 */
@Deprecated
public class XmlMolgenisUiMenu implements UiMenu
{
	private final MenuType menuType;
	private final UiMenu parentMenu;
	private final PermissionService permissionService;

	public XmlMolgenisUiMenu(MenuType menuType, PermissionService permissionService)
	{
		this(menuType, null, permissionService);
	}

	public XmlMolgenisUiMenu(MenuType menuType, UiMenu parentMenu, PermissionService permissionService)
	{
		if (menuType == null) throw new IllegalArgumentException("menu type is null");
		if (permissionService == null) throw new IllegalArgumentException("MolgenisPermissionService is null");
		this.menuType = menuType;
		this.parentMenu = parentMenu;
		this.permissionService = permissionService;
	}

	@Override
	public String getId()
	{
		return menuType.getName();
	}

	@Override
	public String getName()
	{
		String label = menuType.getLabel();
		return label != null ? label : getId();
	}

	@Override
	public String getUrl()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public UiMenuItemType getType()
	{
		return UiMenuItemType.MENU;
	}

	@Override
	public boolean isAuthorized()
	{
		for (Object menuItem : menuType.getMenuOrPlugin())
		{
			UiMenuItem pluginMenuItem = toMenuItem(menuItem);
			if (pluginMenuItem.isAuthorized()) return true;
		}
		return false;
	}

	@Override
	public List<UiMenuItem> getItems()
	{
		List<UiMenuItem> pluginMenuItems = new ArrayList<>();
		for (Object menuItem : menuType.getMenuOrPlugin())
		{
			UiMenuItem pluginMenuItem = toMenuItem(menuItem);
			if (pluginMenuItem.isAuthorized()) pluginMenuItems.add(pluginMenuItem);
		}
		return pluginMenuItems;
	}

	@Override
	public boolean containsItem(String itemId)
	{
		for (UiMenuItem molgenisUiMenuItem : getItems())
		{
			if (molgenisUiMenuItem.getId().equals(itemId)) return true;
		}
		return false;
	}

	@Override
	public UiMenuItem getActiveItem()
	{
		UiMenuItem activeMenuItem = null;
		for (Object menuItem : menuType.getMenuOrPlugin())
		{
			UiMenuItem pluginMenuItem = toMenuItem(menuItem);
			if (pluginMenuItem.getType() != UiMenuItemType.MENU && pluginMenuItem.isAuthorized())
			{
				activeMenuItem = pluginMenuItem;
				break;
			}
		}

		return activeMenuItem;
	}

	@Override
	public UiMenu getParentMenu()
	{
		return parentMenu;
	}

	@Override
	public List<UiMenu> getBreadcrumb()
	{
		if (parentMenu == null) return Collections.singletonList(this);
		List<UiMenu> breadcrumb = new ArrayList<>();
		for (UiMenu menu = this; menu != null; menu = menu.getParentMenu())
			breadcrumb.add(menu);
		return Lists.reverse(breadcrumb);
	}

	private UiMenuItem toMenuItem(Object menuItem)
	{
		if (menuItem instanceof MenuType)
		{
			return new XmlMolgenisUiMenu((MenuType) menuItem, this, permissionService);
		}
		else if (menuItem instanceof PluginType)
		{
			return new XmlMolgenisUiPlugin((PluginType) menuItem, this, permissionService);
		}
		else throw new RuntimeException("unknown menu item type");
	}
}
