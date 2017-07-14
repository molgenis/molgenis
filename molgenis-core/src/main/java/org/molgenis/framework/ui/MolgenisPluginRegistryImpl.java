package org.molgenis.framework.ui;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class MolgenisPluginRegistryImpl implements MolgenisPluginRegistry
{
	private final List<MolgenisPluginFactory> pluginFactories;

	public MolgenisPluginRegistryImpl()
	{
		pluginFactories = new ArrayList<MolgenisPluginFactory>();
	}

	@Override
	public void registerPlugin(final MolgenisPlugin molgenisPlugin)
	{
		if (molgenisPlugin == null)
		{
			throw new IllegalArgumentException(MolgenisPlugin.class.getSimpleName() + " cannot be null");
		}
		pluginFactories.add(new MolgenisPluginFactory()
		{
			@Override
			public Iterator<MolgenisPlugin> iterator()
			{
				return Collections.singleton(molgenisPlugin).iterator();
			}
		});
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
		return Iterables.find(this, new Predicate<MolgenisPlugin>()
		{
			@Override
			public boolean apply(MolgenisPlugin molgenisPlugin)
			{
				return molgenisPlugin.getId().equals(id);
			}
		}, null);
	}

	@Override
	public Iterable<MolgenisPlugin> getPlugins()
	{
		return this;
	}
}
