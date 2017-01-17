package org.molgenis.framework.ui;

import com.google.common.collect.Iterables;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static java.util.Collections.singleton;
import static java.util.stream.StreamSupport.stream;

public class MolgenisPluginRegistryImpl implements MolgenisPluginRegistry
{
	private final List<MolgenisPluginFactory> pluginFactories;

	public MolgenisPluginRegistryImpl()
	{
		pluginFactories = new ArrayList<>();
	}

	@Override
	public void registerPlugin(final MolgenisPlugin molgenisPlugin)
	{
		if (molgenisPlugin == null)
		{
			throw new IllegalArgumentException(MolgenisPlugin.class.getSimpleName() + " cannot be null");
		}
		pluginFactories.add(() -> singleton(molgenisPlugin).iterator());
	}

	@Override
	public void registerPluginFactory(MolgenisPluginFactory molgenisPluginFactory)
	{
		if (molgenisPluginFactory == null)
		{
			throw new IllegalArgumentException(MolgenisPluginFactory.class.getSimpleName() + " cannot be null");
		}
		pluginFactories.add(molgenisPluginFactory);
	}

	@Override
	public Iterator<MolgenisPlugin> iterator()
	{
		return Iterables.concat(pluginFactories).iterator();
	}

	@Override
	public MolgenisPlugin getPlugin(final String id)
	{
		return stream(this.spliterator(), false).filter(molgenisPlugin -> molgenisPlugin.getId().equals(id)).findFirst()
				.orElseGet(() -> null);
	}
}