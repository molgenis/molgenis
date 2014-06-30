package org.molgenis.ui;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.molgenis.framework.ui.MolgenisPlugin;
import org.molgenis.framework.ui.MolgenisPluginFactory;
import org.molgenis.framework.ui.MolgenisPluginRegistry;
import org.springframework.beans.factory.annotation.Autowired;

public class MolgenisUiPluginRegistry implements MolgenisPluginRegistry
{
	private final MolgenisUi molgenisUi;

	@Autowired
	public MolgenisUiPluginRegistry(MolgenisUi molgenisUi)
	{
		if (molgenisUi == null) throw new IllegalArgumentException("Molgenis UI is null");
		this.molgenisUi = molgenisUi;
	}

	// @Override
	// public Collection<MolgenisPlugin> getPlugins()
	// {
	// Map<String, MolgenisPlugin> plugins = new HashMap<String, MolgenisPlugin>();
	// getPluginsRec(molgenisUi.getMenu(), plugins);
	// return plugins.values();
	// }

	@Override
	public MolgenisPlugin getPlugin(String id)
	{
		Map<String, MolgenisPlugin> plugins = new HashMap<String, MolgenisPlugin>();
		getPluginsRec(molgenisUi.getMenu(), plugins);
		return plugins.get(id);
	}

	private void getPluginsRec(MolgenisUiMenuItem menuItem, Map<String, MolgenisPlugin> pluginMap)
	{
		switch (menuItem.getType())
		{
			case MENU:
				for (MolgenisUiMenuItem subMenuItem : ((MolgenisUiMenu) menuItem).getItems())
					getPluginsRec(subMenuItem, pluginMap);
				break;
			case PLUGIN:
				String fullUri = "/" + menuItem.getUrl();

				MolgenisUiMenu menu = menuItem.getParentMenu();
				while (menu != null)
				{
					fullUri = "/" + menu.getId() + fullUri;
					menu = menu.getParentMenu();
				}
				fullUri = "/menu" + fullUri;

				pluginMap.put(menuItem.getId(),
						new MolgenisPlugin(menuItem.getId(), menuItem.getName(), menuItem.getUrl(), fullUri));
				break;
			default:
				throw new RuntimeException("Unknown menu item type [" + menuItem.getType() + "]");
		}
	}

	@Override
	public void registerPlugin(MolgenisPlugin molgenisPlugin)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void registerPluginFactory(MolgenisPluginFactory molgenisPluginFactory)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public Iterator<MolgenisPlugin> iterator()
	{
		// TODO Auto-generated method stub
		return null;
	}
}
