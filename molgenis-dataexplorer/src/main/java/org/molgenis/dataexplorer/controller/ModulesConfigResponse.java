package org.molgenis.dataexplorer.controller;

import java.util.ArrayList;
import java.util.List;

public class ModulesConfigResponse
{
	private final List<ModuleConfig> modules;

	public ModulesConfigResponse()
	{
		modules = new ArrayList<>();
	}

	public void add(ModuleConfig moduleConfig)
	{
		modules.add(moduleConfig);
	}
}
