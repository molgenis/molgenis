package org.molgenis.data.importer;

import org.molgenis.data.DatabaseAction;
import org.molgenis.data.ManageableCrudRepositoryCollection;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.framework.db.EntitiesValidationReport;
import org.molgenis.framework.db.EntityImportReport;

public abstract class AbstractRepositoryCollectionImportService implements ImportService
{
	private final ManageableCrudRepositoryCollection destination;

	public AbstractRepositoryCollectionImportService(ManageableCrudRepositoryCollection destination)
	{
		this.destination = destination;
	}

	@Override
	public EntityImportReport doImport(RepositoryCollection source, DatabaseAction databaseAction)
	{

		return null;
	}

	@Override
	public abstract EntitiesValidationReport validateImport(RepositoryCollection source);

	@Override
	public abstract boolean canImport(String fileName, RepositoryCollection source);
}
