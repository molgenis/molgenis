package org.molgenis.data.plugin;

public interface PluginRegistry extends Iterable<Plugin>
{
	void registerPlugin(Plugin molgenisPlugin);

	void registerPluginFactory(PluginFactory molgenisPluginFactory);

	Plugin getPlugin(String id);
}