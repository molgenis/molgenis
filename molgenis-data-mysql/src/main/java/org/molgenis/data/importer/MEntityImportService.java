package org.molgenis.data.importer;

import java.io.IOException;

import org.molgenis.data.DatabaseAction;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.framework.db.EntityImportReport;

public interface MEntityImportService
{
    EntityImportReport doImport(RepositoryCollection repositories, DatabaseAction databaseAction) throws IOException;
}
