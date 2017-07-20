package org.molgenis.ui;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @deprecated use {@link org.molgenis.ui.menu.MenuItemToMolgenisUiMenuAdapter} instead
 */
@Deprecated
public class XmlMolgenisUiMenu implements MolgenisUiMenu
{
	private final MenuType menuType;
	private final MolgenisUiMenu parentMenu;

	public XmlMolgenisUiMenu(MenuType menuType)
	{
		this(menuType, null);
	}

	public XmlMolgenisUiMenu(MenuType menuType, MolgenisUiMenu parentMenu)
	{
		if (menuType == null) throw new IllegalArgumentException("menu type is null");
		this.menuType = menuType;
		this.parentMenu = parentMenu;
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
	public MolgenisUiMenuItemType getType()
	{
		return MolgenisUiMenuItemType.MENU;
	}

	@Override
	public List<MolgenisUiMenuItem> getItems()
	{
		List<MolgenisUiMenuItem> pluginMenuItems = new ArrayList<>();
		for (Object menuItem : menuType.getMenuOrPlugin())
		{
			MolgenisUiMenuItem pluginMenuItem = toMenuItem(menuItem);
			pluginMenuItems.add(pluginMenuItem);
		}
		return pluginMenuItems;
	}

	@Override
	public boolean containsItem(String itemId)
	{
		for (MolgenisUiMenuItem molgenisUiMenuItem : getItems())
		{
			if (molgenisUiMenuItem.getId().equals(itemId)) return true;
		}
		return false;
	}

	@Override
	public MolgenisUiMenuItem getActiveItem()
	{
		MolgenisUiMenuItem activeMenuItem = null;
		for (Object menuItem : menuType.getMenuOrPlugin())
		{
			MolgenisUiMenuItem pluginMenuItem = toMenuItem(menuItem);
			if (pluginMenuItem.getType() != MolgenisUiMenuItemType.MENU)
			{
				activeMenuItem = pluginMenuItem;
				break;
			}
		}

		return activeMenuItem;
	}

	@Override
	public MolgenisUiMenu getParentMenu()
	{
		return parentMenu;
	}

	@Override
	public List<MolgenisUiMenu> getBreadcrumb()
	{
		if (parentMenu == null) return Collections.singletonList(this);
		List<MolgenisUiMenu> breadcrumb = new ArrayList<>();
		for (MolgenisUiMenu menu = this; menu != null; menu = menu.getParentMenu())
			breadcrumb.add(menu);
		return Lists.reverse(breadcrumb);
	}

	private MolgenisUiMenuItem toMenuItem(Object menuItem)
	{
		if (menuItem instanceof MenuType)
		{
			return new XmlMolgenisUiMenu((MenuType) menuItem, this);
		}
		else if (menuItem instanceof PluginType)
		{
			return new XmlMolgenisUiPlugin((PluginType) menuItem, this);
		}
		else throw new RuntimeException("unknown menu item type");
	}
}
