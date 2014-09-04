package org.molgenis.data.jpa.importer;

import java.io.File;
import java.io.IOException;

import org.molgenis.data.DatabaseAction;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.framework.db.EntityImportReport;

/**
 * Import entities into the database
 */
public interface EntitiesImporter
{
	/**
	 * Import entities from a {@link java.io.File}
	 * 
	 * @param file
	 * @param dbAction
	 * @return
	 * @throws IOException
	 */
	public EntityImportReport importEntities(File file, DatabaseAction dbAction) throws IOException;

	/**
	 * Import entities from a {@link org.molgenis.data.Repository}
	 * 
	 * @param tupleReader
	 * @param entityName
	 * @param dbAction
	 * @return
	 * @throws IOException
	 */
	public EntityImportReport importEntities(RepositoryCollection sourceRepositories, DatabaseAction dbAction)
			throws IOException;

}
