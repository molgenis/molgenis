package org.molgenis.framework.ui;

public interface MolgenisPluginRegistry extends Iterable<MolgenisPlugin>
{
	void registerPlugin(MolgenisPlugin molgenisPlugin);

	void registerPluginFactory(MolgenisPluginFactory molgenisPluginFactory);

	MolgenisPlugin getPlugin(String id);
}