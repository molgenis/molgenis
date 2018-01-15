package org.molgenis.core.ui;

import org.molgenis.core.ui.menu.MenuItemToMolgenisUiMenuItemAdapter;
import org.molgenis.security.core.Permission;
import org.molgenis.security.core.PermissionService;
import org.molgenis.web.UiMenu;
import org.molgenis.web.UiMenuItem;
import org.molgenis.web.UiMenuItemType;

/**
 * @deprecated use {@link MenuItemToMolgenisUiMenuItemAdapter} instead
 */
@Deprecated
public class XmlMolgenisUiPlugin implements UiMenuItem
{
	private final PluginType pluginType;
	private final UiMenu parentMenu;
	private final PermissionService permissionService;

	public XmlMolgenisUiPlugin(PluginType pluginType, UiMenu parentMenu, PermissionService permissionService)
	{
		if (pluginType == null) throw new IllegalArgumentException("plugin type is null");
		if (parentMenu == null) throw new IllegalArgumentException("parent menu is null");
		if (permissionService == null) throw new IllegalArgumentException("MolgenisPermissionService is null");
		this.pluginType = pluginType;
		this.parentMenu = parentMenu;
		this.permissionService = permissionService;
	}

	@Override
	public String getId()
	{
		return pluginType.getId();
	}

	@Override
	public String getName()
	{
		return pluginType.getName();
	}

	@Override
	public String getUrl()
	{
		return pluginType.getUrl();
	}

	@Override
	public UiMenuItemType getType()
	{
		return UiMenuItemType.PLUGIN;
	}

	@Override
	public boolean isAuthorized()
	{
		return permissionService.hasPermissionOnPlugin(getId(), Permission.READ);
	}

	@Override
	public UiMenu getParentMenu()
	{
		return parentMenu;
	}
}
