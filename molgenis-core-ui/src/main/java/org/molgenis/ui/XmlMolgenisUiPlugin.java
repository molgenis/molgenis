package org.molgenis.ui;

import org.molgenis.framework.ui.MolgenisPlugin;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.WebInvocationPrivilegeEvaluator;

public class XmlMolgenisUiPlugin implements MolgenisUiMenuItem
{
	private final WebInvocationPrivilegeEvaluator webInvocationPrivilegeEvaluator;
	private final PluginType pluginType;
	private final MolgenisUiMenu parentMenu;

	public XmlMolgenisUiPlugin(WebInvocationPrivilegeEvaluator webInvocationPrivilegeEvaluator, PluginType pluginType,
			MolgenisUiMenu parentMenu)
	{
		if (webInvocationPrivilegeEvaluator == null) throw new IllegalArgumentException(
				"WebInvocationPrivilegeEvaluator is null");
		if (pluginType == null) throw new IllegalArgumentException("plugin type is null");
		if (parentMenu == null) throw new IllegalArgumentException("parent menu is null");
		this.webInvocationPrivilegeEvaluator = webInvocationPrivilegeEvaluator;
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
		return webInvocationPrivilegeEvaluator.isAllowed(MolgenisPlugin.PLUGIN_URI_PREFIX + pluginType.getId(),
				SecurityContextHolder.getContext().getAuthentication());
	}

	@Override
	public MolgenisUiMenu getParentMenu()
	{
		return parentMenu;
	}
}
