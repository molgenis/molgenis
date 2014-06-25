package org.molgenis.data.importer;

import java.io.IOException;
import java.util.Map;

import org.molgenis.data.DatabaseAction;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.framework.db.EntitiesValidationReport;
import org.molgenis.framework.db.EntityImportReport;

public interface EmxImporterService
{
	EntityImportReport doImport(RepositoryCollection repositories, DatabaseAction databaseAction) throws IOException;

	Map<String, DefaultEntityMetaData> getEntityMetaData(RepositoryCollection source);

	EntitiesValidationReport validateImport(RepositoryCollection source);
}
