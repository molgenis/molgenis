package org.molgenis.security;

import org.molgenis.data.DataService;

public interface MolgenisSecurityWebAppDatabasePopulatorService
{
	void populateDatabase(DataService dataService, String homeControllerId, String userAccountControllerId);
}