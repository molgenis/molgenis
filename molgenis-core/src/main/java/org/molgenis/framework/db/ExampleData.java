package org.molgenis.framework.db;

/**
 * Interface to load example data into a data model. Should be used in the
 * context of {@link Database.loadExampleData()}.
 */
public interface ExampleData
{
	/**
	 * Load the example data into the database.
	 */
	public void load(Database db) throws DatabaseException;
}
