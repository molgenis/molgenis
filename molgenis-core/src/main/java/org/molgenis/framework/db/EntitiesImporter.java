package org.molgenis.framework.db;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.molgenis.framework.db.Database.DatabaseAction;
import org.molgenis.io.TableReader;
import org.molgenis.io.TupleReader;

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
	 * @throws DatabaseException
	 */
	public EntityImportReport importEntities(File file, DatabaseAction dbAction) throws IOException, DatabaseException;

	/**
	 * Import entities from a {@link java.io.File} list
	 * 
	 * @param files
	 * @param dbAction
	 * @return
	 * @throws IOException
	 * @throws DatabaseException
	 */
	public EntityImportReport importEntities(List<File> files, DatabaseAction dbAction) throws IOException,
			DatabaseException;

	/**
	 * Import entities from a {@link org.molgenis.io.TupleReader}
	 * 
	 * @param tupleReader
	 * @param entityName
	 * @param dbAction
	 * @return
	 * @throws IOException
	 * @throws DatabaseException
	 */
	public EntityImportReport importEntities(TupleReader tupleReader, String entityName, DatabaseAction dbAction)
			throws IOException, DatabaseException;

	/**
	 * Import entities from a {@link org.molgenis.io.TableReader}
	 * 
	 * @param tupleReader
	 * @param entityName
	 * @param dbAction
	 * @return
	 * @throws IOException
	 * @throws DatabaseException
	 */
	public EntityImportReport importEntities(TableReader tableReader, DatabaseAction dbAction) throws IOException,
			DatabaseException;

	@Deprecated
	public void setDatabase(Database db);
}
