package org.molgenis.ui;

import org.molgenis.security.core.MolgenisPermissionService;
import org.molgenis.security.core.Permission;

/**
 * @deprecated use {@link org.molgenis.ui.menu.MenuItemToMolgenisUiMenuItemAdapter} instead
 */
@Deprecated
public class XmlMolgenisUiPlugin implements MolgenisUiMenuItem
{
	private final PluginType pluginType;
	private final MolgenisUiMenu parentMenu;
	private final MolgenisPermissionService molgenisPermissionService;

	public XmlMolgenisUiPlugin(PluginType pluginType, MolgenisUiMenu parentMenu,
			MolgenisPermissionService molgenisPermissionService)
	{
		if (pluginType == null) throw new IllegalArgumentException("plugin type is null");
		if (parentMenu == null) throw new IllegalArgumentException("parent menu is null");
		if (molgenisPermissionService == null) throw new IllegalArgumentException("MolgenisPermissionService is null");
		this.pluginType = pluginType;
		this.parentMenu = parentMenu;
		this.molgenisPermissionService = molgenisPermissionService;
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
		return molgenisPermissionService.hasPermissionOnPlugin(getId(), Permission.READ);
	}

	@Override
	public MolgenisUiMenu getParentMenu()
	{
		return parentMenu;
	}
}
