package org.molgenis.dataexplorer.controller;

import org.molgenis.data.meta.model.EntityType;

import java.util.List;

public interface DataExplorerService
{
	List<Module> getModules(EntityType entityType);
}
