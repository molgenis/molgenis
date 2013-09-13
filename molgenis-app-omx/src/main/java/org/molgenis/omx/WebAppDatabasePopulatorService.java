package org.molgenis.omx;

import org.molgenis.framework.db.DatabaseException;

public interface WebAppDatabasePopulatorService
{
	void populateDatabase() throws DatabaseException;

	boolean isDatabasePopulated() throws DatabaseException;
}
