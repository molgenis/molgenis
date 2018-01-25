package org.molgenis.core.framework.db;

public interface WebAppDatabasePopulatorService
{
	void populateDatabase();

	boolean isDatabasePopulated();
}
