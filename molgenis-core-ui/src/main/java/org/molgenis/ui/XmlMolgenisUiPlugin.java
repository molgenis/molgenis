package org.molgenis.ui;

import org.molgenis.security.core.PermissionService;
import org.molgenis.security.core.Permission;

/**
 * @deprecated use {@link org.molgenis.ui.menu.MenuItemToMolgenisUiMenuItemAdapter} instead
 */
@Deprecated
public class XmlMolgenisUiPlugin implements MolgenisUiMenuItem
{
	private final PluginType pluginType;
	private final MolgenisUiMenu parentMenu;
	private final PermissionService permissionService;

	public XmlMolgenisUiPlugin(PluginType pluginType, MolgenisUiMenu parentMenu,
			PermissionService permissionService)
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
	public MolgenisUiMenuItemType getType()
	{
		return MolgenisUiMenuItemType.PLUGIN;
	}

	@Override
	public boolean isAuthorized()
	{
		return permissionService.hasPermissionOnPlugin(getId(), Permission.READ);
	}

	@Override
	public MolgenisUiMenu getParentMenu()
	{
		return parentMenu;
	}
}
