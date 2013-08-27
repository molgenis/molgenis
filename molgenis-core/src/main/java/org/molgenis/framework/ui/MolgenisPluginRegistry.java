package org.molgenis.framework.ui;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MolgenisPluginRegistry
{
	private final static MolgenisPluginRegistry INSTANCE = new MolgenisPluginRegistry();

	private MolgenisPluginRegistry()
	{
		molgenisPluginMap = new ConcurrentHashMap<String, Class<? extends MolgenisPlugin>>();
	}

	public static MolgenisPluginRegistry getInstance()
	{
		return INSTANCE;
	}

	private final Map<String, Class<? extends MolgenisPlugin>> molgenisPluginMap;

	public void register(MolgenisPlugin molgenisPlugin)
	{
		molgenisPluginMap.put(molgenisPlugin.getId(), molgenisPlugin.getClass());
	}

	public Iterable<Class<? extends MolgenisPlugin>> getPluginClasses()
	{
		return molgenisPluginMap.values();
	}
}
