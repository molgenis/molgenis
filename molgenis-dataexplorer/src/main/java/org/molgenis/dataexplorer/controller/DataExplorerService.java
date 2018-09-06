package org.molgenis.dataexplorer.controller;

import java.util.List;
import org.molgenis.data.meta.model.EntityType;

public interface DataExplorerService {
  List<Module> getModules(EntityType entityType);
}
