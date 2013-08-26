package org.molgenis.ui;

import java.util.ArrayList;
import java.util.List;

import org.molgenis.framework.server.MolgenisPermissionService;

public class XmlMolgenisUiMenu implements MolgenisUiMenu
{
	private final MolgenisPermissionService molgenisPermissionService;
	private final MenuType menuType;

	public XmlMolgenisUiMenu(MolgenisPermissionService molgenisPermissionService, MenuType menuType)
	{
		if (molgenisPermissionService == null) throw new IllegalArgumentException("molgenis permission service is null");
		if (menuType == null) throw new IllegalArgumentException("menu type is null");
		this.molgenisPermissionService = molgenisPermissionService;
		this.menuType = menuType;
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
		for (Object menuItem : menuType.getFormOrMenuOrPlugin())
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
		for (Object menuItem : menuType.getFormOrMenuOrPlugin())
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
		for (Object menuItem : menuType.getFormOrMenuOrPlugin())
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

	private MolgenisUiMenuItem toMenuItem(Object menuItem)
	{
		if (menuItem instanceof FormType)
		{
			return new XmlMolgenisUiForm(molgenisPermissionService, (FormType) menuItem);
		}
		else if (menuItem instanceof MenuType)
		{
			return new XmlMolgenisUiMenu(molgenisPermissionService, (MenuType) menuItem);
		}
		else if (menuItem instanceof PluginType)
		{
			return new XmlMolgenisUiPlugin(molgenisPermissionService, (PluginType) menuItem);
		}
		else throw new RuntimeException("unknown menu item type");
	}
}
