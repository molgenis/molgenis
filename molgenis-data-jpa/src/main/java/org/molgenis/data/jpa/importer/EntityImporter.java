package org.molgenis.data.jpa.importer;

import org.molgenis.data.DataService;
import org.molgenis.data.DatabaseAction;
import org.molgenis.data.Repository;

public interface EntityImporter
{
	int importEntity(Repository repository, DataService dataService, DatabaseAction dbAction);
}
