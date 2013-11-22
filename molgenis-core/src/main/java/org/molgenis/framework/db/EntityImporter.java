package org.molgenis.framework.db;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.framework.db.Database.DatabaseAction;

public interface EntityImporter<E extends Entity>
{
	int importEntity(Repository<? extends Entity> repository, DataService dataService, DatabaseAction dbAction);
}
