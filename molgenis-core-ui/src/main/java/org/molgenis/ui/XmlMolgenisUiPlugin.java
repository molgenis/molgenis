package org.molgenis.ui;

import org.molgenis.framework.server.MolgenisPermissionService;

public class XmlMolgenisUiPlugin implements MolgenisUiMenuItem
{
	private final MolgenisPermissionService molgenisPermissionService;
	private final PluginType pluginType;

	public XmlMolgenisUiPlugin(MolgenisPermissionService molgenisPermissionService, PluginType pluginType)
	{
		if (molgenisPermissionService == null) throw new IllegalArgumentException("molgenis permission service is null");
		if (pluginType == null) throw new IllegalArgumentException("plugin type is null");
		this.molgenisPermissionService = molgenisPermissionService;
		this.pluginType = pluginType;
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
		return molgenisPermissionService.hasReadPermissionOnPlugin(pluginType.getType());
	}
}
