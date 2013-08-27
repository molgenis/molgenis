package org.molgenis.ui;

import org.molgenis.framework.server.MolgenisPermissionService;
import org.molgenis.framework.server.MolgenisPermissionService.Permission;

public class XmlMolgenisUiPlugin implements MolgenisUiMenuItem
{
	private final MolgenisPermissionService molgenisPermissionService;
	private final PluginType pluginType;
	private final MolgenisUiMenu parentMenu;

	public XmlMolgenisUiPlugin(MolgenisPermissionService molgenisPermissionService, PluginType pluginType,
			MolgenisUiMenu parentMenu)
	{
		if (molgenisPermissionService == null) throw new IllegalArgumentException("molgenis permission service is null");
		if (pluginType == null) throw new IllegalArgumentException("plugin type is null");
		if (parentMenu == null) throw new IllegalArgumentException("parent menu is null");
		this.molgenisPermissionService = molgenisPermissionService;
		this.pluginType = pluginType;
		this.parentMenu = parentMenu;
	}

	@Override
	public String getId()
	{
		return pluginType.getId();
	}

	@Override
	public String getName()
	{
		String label = pluginType.getLabel();
		return label != null ? label : getId();
	}

	@Override
	public MolgenisUiMenuItemType getType()
	{
		return MolgenisUiMenuItemType.PLUGIN;
	}

	@Override
	public boolean isAuthorized()
	{
		return molgenisPermissionService.hasPermissionOnPlugin(pluginType.getName(), Permission.READ);
	}

	@Override
	public MolgenisUiMenu getParentMenu()
	{
		return parentMenu;
	}
}
