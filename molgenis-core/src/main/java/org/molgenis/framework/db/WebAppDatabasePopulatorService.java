package org.molgenis.framework.db;

import org.molgenis.framework.db.DatabaseException;

public interface WebAppDatabasePopulatorService
{
	void populateDatabase() throws DatabaseException;

	boolean isDatabasePopulated() throws DatabaseException;
}
