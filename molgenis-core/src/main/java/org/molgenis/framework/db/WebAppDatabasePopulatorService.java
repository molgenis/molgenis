package org.molgenis.framework.db;

public interface WebAppDatabasePopulatorService
{
	void populateDatabase();

	boolean isDatabasePopulated();
}
