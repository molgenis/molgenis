package org.molgenis.data.plugin;

import com.google.common.collect.Iterables;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class PluginRegistryImpl implements PluginRegistry
{
	private final List<PluginFactory> pluginFactories;

	public PluginRegistryImpl()
	{
		pluginFactories = new ArrayList<>();
	}

	@Override
	public void registerPlugin(final Plugin molgenisPlugin)
	{
		if (molgenisPlugin == null)
		{
			throw new IllegalArgumentException(Plugin.class.getSimpleName() + " cannot be null");
		}
		pluginFactories.add(() -> Collections.singleton(molgenisPlugin).iterator());
	}

	@Override
	public void registerPluginFactory(PluginFactory molgenisPluginFactory)
	{
		if (molgenisPluginFactory == null)
		{
			throw new IllegalArgumentException(PluginFactory.class.getSimpleName() + " cannot be null");
		}
		pluginFactories.add(molgenisPluginFactory);
	}

	@Override
	public Iterator<Plugin> iterator()
	{
		return Iterables.concat(pluginFactories).iterator();
	}

	@Override
	public Plugin getPlugin(final String id)
	{
		return Iterables.find(this, molgenisPlugin -> molgenisPlugin.getId().equals(id), null);
	}
}
