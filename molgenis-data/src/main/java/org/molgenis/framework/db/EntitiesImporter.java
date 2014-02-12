package org.molgenis.framework.db;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.molgenis.data.DatabaseAction;
import org.molgenis.data.Repository;

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
	public EntityImportReport importEntities(List<Repository> sourceRepositories, DatabaseAction dbAction)
			throws IOException;

}
