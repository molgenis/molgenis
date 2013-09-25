package org.molgenis.framework.ui;

import java.util.Collection;

public interface MolgenisPluginRegistry
{
	Collection<MolgenisPlugin> getPlugins();

	MolgenisPlugin getPlugin(String id);
}