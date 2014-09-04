package org.molgenis.data.importer;

import org.molgenis.data.DatabaseAction;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.framework.db.EntitiesValidationReport;
import org.molgenis.framework.db.EntityImportReport;
import org.springframework.core.Ordered;

public interface ImportService extends Ordered
{
	EntityImportReport doImport(RepositoryCollection source, DatabaseAction databaseAction);

	EntitiesValidationReport validateImport(RepositoryCollection source);

	boolean canImport(String fileName, RepositoryCollection source);
}
