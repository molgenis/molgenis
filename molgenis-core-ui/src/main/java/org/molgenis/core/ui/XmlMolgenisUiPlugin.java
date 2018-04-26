package org.molgenis.core.ui;

import org.molgenis.core.ui.menu.MenuItemToMolgenisUiMenuItemAdapter;
import org.molgenis.data.plugin.model.PluginIdentity;
import org.molgenis.data.plugin.model.PluginPermission;
import org.molgenis.security.core.UserPermissionEvaluator;
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
	private final UserPermissionEvaluator permissionService;

	public XmlMolgenisUiPlugin(PluginType pluginType, UiMenu parentMenu, UserPermissionEvaluator permissionService)
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
		return permissionService.hasPermission(new PluginIdentity(getId()), PluginPermission.READ);
	}

	@Override
	public UiMenu getParentMenu()
	{
		return parentMenu;
	}
}
