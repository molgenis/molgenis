package org.molgenis.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.security.web.access.WebInvocationPrivilegeEvaluator;

import com.google.common.collect.Lists;

public class XmlMolgenisUiMenu implements MolgenisUiMenu
{
	private final WebInvocationPrivilegeEvaluator webInvocationPrivilegeEvaluator;
	private final MenuType menuType;
	private final MolgenisUiMenu parentMenu;

	public XmlMolgenisUiMenu(WebInvocationPrivilegeEvaluator webInvocationPrivilegeEvaluator, MenuType menuType)
	{
		this(webInvocationPrivilegeEvaluator, menuType, null);
	}

	public XmlMolgenisUiMenu(WebInvocationPrivilegeEvaluator webInvocationPrivilegeEvaluator, MenuType menuType,
			MolgenisUiMenu parentMenu)
	{
		if (webInvocationPrivilegeEvaluator == null) throw new IllegalArgumentException(
				"molgenis permission service is null");
		if (menuType == null) throw new IllegalArgumentException("menu type is null");
		this.webInvocationPrivilegeEvaluator = webInvocationPrivilegeEvaluator;
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
	public MolgenisUiMenuItemType getType()
	{
		return MolgenisUiMenuItemType.MENU;
	}

	@Override
	public boolean isAuthorized()
	{
		for (Object menuItem : menuType.getMenuOrPlugin())
		{
			MolgenisUiMenuItem pluginMenuItem = toMenuItem(menuItem);
			if (pluginMenuItem.isAuthorized()) return true;
		}
		return false;
	}

	@Override
	public List<MolgenisUiMenuItem> getItems()
	{
		List<MolgenisUiMenuItem> pluginMenuItems = new ArrayList<MolgenisUiMenuItem>();
		for (Object menuItem : menuType.getMenuOrPlugin())
		{
			MolgenisUiMenuItem pluginMenuItem = toMenuItem(menuItem);
			if (pluginMenuItem.isAuthorized()) pluginMenuItems.add(pluginMenuItem);
		}
		return pluginMenuItems;
	}

	@Override
	public MolgenisUiMenuItem getActiveItem()
	{
		MolgenisUiMenuItem activeMenuItem = null;
		for (Object menuItem : menuType.getMenuOrPlugin())
		{
			MolgenisUiMenuItem pluginMenuItem = toMenuItem(menuItem);
			if (pluginMenuItem.getType() != MolgenisUiMenuItemType.MENU && pluginMenuItem.isAuthorized())
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
		if (parentMenu == null) return Collections.<MolgenisUiMenu> singletonList(this);
		List<MolgenisUiMenu> breadcrumb = new ArrayList<MolgenisUiMenu>();
		for (MolgenisUiMenu menu = this; menu != null; menu = menu.getParentMenu())
			breadcrumb.add(menu);
		return Lists.reverse(breadcrumb);
	}

	private MolgenisUiMenuItem toMenuItem(Object menuItem)
	{
		if (menuItem instanceof MenuType)
		{
			return new XmlMolgenisUiMenu(webInvocationPrivilegeEvaluator, (MenuType) menuItem, this);
		}
		else if (menuItem instanceof PluginType)
		{
			return new XmlMolgenisUiPlugin(webInvocationPrivilegeEvaluator, (PluginType) menuItem, this);
		}
		else throw new RuntimeException("unknown menu item type");
	}
}
