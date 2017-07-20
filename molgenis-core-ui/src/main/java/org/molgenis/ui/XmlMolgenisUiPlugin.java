package org.molgenis.ui;

/**
 * @deprecated use {@link org.molgenis.ui.menu.MenuItemToMolgenisUiMenuItemAdapter} instead
 */
@Deprecated
public class XmlMolgenisUiPlugin implements MolgenisUiMenuItem
{
	private final PluginType pluginType;
	private final MolgenisUiMenu parentMenu;

	public XmlMolgenisUiPlugin(PluginType pluginType, MolgenisUiMenu parentMenu)
	{
		if (pluginType == null) throw new IllegalArgumentException("plugin type is null");
		if (parentMenu == null) throw new IllegalArgumentException("parent menu is null");
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
	public MolgenisUiMenu getParentMenu()
	{
		return parentMenu;
	}
}
